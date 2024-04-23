package run.halo.app.core.extension.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.or;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;
import static run.halo.app.security.authorization.AuthorityUtils.authoritiesToRoles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.Files;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.security.Principal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.core.extension.service.EmailVerificationService;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

@Component
@RequiredArgsConstructor
public class UserEndpoint implements CustomEndpoint {

    private static final String SELF_USER = "-";
    private static final String USER_AVATAR_GROUP_NAME = "user-avatar-group";
    private static final String DEFAULT_USER_AVATAR_ATTACHMENT_POLICY_NAME = "default-policy";
    private static final DataSize MAX_AVATAR_FILE_SIZE = DataSize.ofMegabytes(2L);
    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final RoleService roleService;
    private final AttachmentService attachmentService;
    private final EmailVerificationService emailVerificationService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/User";
        return SpringdocRouteBuilder.route()
            .GET("/users/-", this::me, builder -> builder.operationId("GetCurrentUserDetail")
                .description("Get current user detail")
                .tag(tag)
                .response(responseBuilder().implementation(DetailedUser.class)))
            .GET("/users/{name}", this::getUserByName,
                builder -> builder.operationId("GetUserDetail")
                    .description("Get user detail by name")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("User name")
                        .required(true)
                    )
                    .response(responseBuilder().implementation(DetailedUser.class)))
            .PUT("/users/-", this::updateProfile,
                builder -> builder.operationId("UpdateCurrentUser")
                    .description("Update current user profile, but password.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder().required(true).implementation(User.class))
                    .response(responseBuilder().implementation(User.class)))
            .POST("/users/{name}/permissions", this::grantPermission,
                builder -> builder.operationId("GrantPermission")
                    .description("Grant permissions to user")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description("User name")
                        .required(true))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(GrantRequest.class))
                    .response(responseBuilder().implementation(User.class)))
            .POST("/users", this::createUser,
                builder -> builder.operationId("CreateUser")
                    .description("Creates a new user.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(CreateUserRequest.class))
                    .response(responseBuilder().implementation(User.class)))
            .GET("/users/{name}/permissions", this::getUserPermission,
                builder -> builder.operationId("GetPermissions")
                    .description("Get permissions of user")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description("User name")
                        .required(true))
                    .response(responseBuilder().implementation(UserPermission.class)))
            .PUT("/users/-/password", this::changeOwnPassword,
                builder -> builder.operationId("ChangeOwnPassword")
                    .description("Change own password of user.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(ChangeOwnPasswordRequest.class))
                    .response(responseBuilder()
                        .implementation(User.class))
            )
            .PUT("/users/{name}/password", this::changeAnyonePasswordForAdmin,
                builder -> builder.operationId("ChangeAnyonePassword")
                    .description("Change anyone password of user for admin.")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name")
                        .description(
                            "Name of user. If the name is equal to '-', it will change the "
                                + "password of current user.")
                        .required(true))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(ChangePasswordRequest.class))
                    .response(responseBuilder()
                        .implementation(User.class))
            )
            .GET("users", this::list, builder -> {
                builder.operationId("ListUsers")
                    .tag(tag)
                    .description("List users")
                    .response(responseBuilder()
                        .implementation(generateGenericClass(ListedUser.class)));
                ListRequest.buildParameters(builder);
            })
            .POST("users/{name}/avatar", contentType(MediaType.MULTIPART_FORM_DATA),
                this::uploadUserAvatar,
                builder -> builder
                    .operationId("UploadUserAvatar")
                    .description("upload user avatar")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("User name")
                        .required(true)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(IAvatarUploadRequest.class))
                        ))
                    .response(responseBuilder().implementation(User.class))
            )
            .DELETE("users/{name}/avatar", this::deleteUserAvatar, builder -> builder
                .tag(tag)
                .operationId("DeleteUserAvatar")
                .description("delete user avatar")
                .parameter(parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("name")
                    .description("User name")
                    .required(true)
                )
                .response(responseBuilder().implementation(User.class))
                .build())
            .POST("users/-/send-email-verification-code",
                this::sendEmailVerificationCode,
                builder -> builder
                    .tag(tag)
                    .operationId("SendEmailVerificationCode")
                    .requestBody(requestBodyBuilder()
                        .implementation(EmailVerifyRequest.class)
                        .required(true)
                    )
                    .description("Send email verification code for user")
                    .response(responseBuilder().implementation(Void.class))
                    .build()
            )
            .POST("users/-/verify-email", this::verifyEmail,
                builder -> builder
                    .tag(tag)
                    .operationId("VerifyEmail")
                    .description("Verify email for user by code.")
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(VerifyCodeRequest.class))
                    .response(responseBuilder().implementation(Void.class))
                    .build()
            )
            .build();
    }

    private Mono<ServerResponse> verifyEmail(ServerRequest request) {
        return request.bodyToMono(VerifyCodeRequest.class)
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("Request body is required."))
            )
            .flatMap(this::doVerifyCode)
            .then(ServerResponse.ok().build());
    }

    private Mono<Void> doVerifyCode(VerifyCodeRequest verifyCodeRequest) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .flatMap(username -> verifyPasswordAndCode(username, verifyCodeRequest));
    }

    private Mono<Void> verifyPasswordAndCode(String username, VerifyCodeRequest verifyCodeRequest) {
        return userService.confirmPassword(username, verifyCodeRequest.password())
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new UnsatisfiedAttributeValueException(
                "Password is incorrect.", "problemDetail.user.password.notMatch", null)))
            .flatMap(verified -> verifyEmailCode(username, verifyCodeRequest.code()));
    }

    private Mono<Void> verifyEmailCode(String username, String code) {
        return Mono.just(username)
            .transformDeferred(verificationEmailRateLimiter(username))
            .flatMap(name -> emailVerificationService.verify(username, code))
            .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    public record EmailVerifyRequest(@Schema(requiredMode = REQUIRED) String email) {
    }

    public record VerifyCodeRequest(
        @Schema(requiredMode = REQUIRED) String password,
        @Schema(requiredMode = REQUIRED, minLength = 1) String code) {
    }

    private Mono<ServerResponse> sendEmailVerificationCode(ServerRequest request) {
        return request.bodyToMono(EmailVerifyRequest.class)
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("Request body is required."))
            )
            .doOnNext(emailRequest -> {
                if (!ValidationUtils.isValidEmail(emailRequest.email())) {
                    throw new ServerWebInputException("Invalid email address.");
                }
            })
            .flatMap(emailRequest -> {
                var email = emailRequest.email();
                return ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .map(Principal::getName)
                    .map(username -> Tuples.of(username, email));
            })
            .flatMap(tuple -> {
                var username = tuple.getT1();
                var email = tuple.getT2();
                return Mono.just(username)
                    .transformDeferred(sendEmailVerificationCodeRateLimiter(username, email))
                    .flatMap(u -> emailVerificationService.sendVerificationCode(username, email))
                    .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
            })
            .then(ServerResponse.ok().build());
    }

    <T> RateLimiterOperator<T> verificationEmailRateLimiter(String username) {
        String rateLimiterKey = "verify-email-" + username;
        var rateLimiter =
            rateLimiterRegistry.rateLimiter(rateLimiterKey, "verify-email");
        return RateLimiterOperator.of(rateLimiter);
    }

    <T> RateLimiterOperator<T> sendEmailVerificationCodeRateLimiter(String username, String email) {
        String rateLimiterKey = "send-email-verification-code-" + username + ":" + email;
        var rateLimiter =
            rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-email-verification-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    private Mono<ServerResponse> deleteUserAvatar(ServerRequest request) {
        final var nameInPath = request.pathVariable("name");
        return getUserOrSelf(nameInPath)
            .flatMap(user -> {
                MetadataUtil.nullSafeAnnotations(user)
                    .remove(User.AVATAR_ATTACHMENT_NAME_ANNO);
                user.getSpec().setAvatar(null);
                return client.update(user);
            })
            .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    private Mono<User> getUserOrSelf(String name) {
        if (!SELF_USER.equals(name)) {
            return client.get(User.class, name);
        }
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .flatMap(currentUserName -> client.get(User.class, currentUserName));
    }

    private Mono<ServerResponse> uploadUserAvatar(ServerRequest request) {
        final var username = request.pathVariable("name");
        return request.body(BodyExtractors.toMultipartData())
            .map(AvatarUploadRequest::new)
            .flatMap(this::uploadAvatar)
            .flatMap(attachment -> getUserOrSelf(username)
                .flatMap(user -> {
                    MetadataUtil.nullSafeAnnotations(user)
                        .put(User.AVATAR_ATTACHMENT_NAME_ANNO,
                            attachment.getMetadata().getName());
                    return client.update(user);
                })
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                    .filter(OptimisticLockingFailureException.class::isInstance))
            )
            .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    @Schema(types = "object")
    public interface IAvatarUploadRequest {
        @Schema(requiredMode = REQUIRED, description = "Avatar file")
        FilePart getFile();
    }

    public record AvatarUploadRequest(MultiValueMap<String, Part> formData) {
        public FilePart getFile() {
            Part file = formData.getFirst("file");
            if (file == null) {
                throw new ServerWebInputException("No file part found in the request");
            }

            if (!(file instanceof FilePart filePart)) {
                throw new ServerWebInputException("Invalid part of file");
            }

            if (!filePart.filename().endsWith(".png")) {
                throw new ServerWebInputException("Only support avatar in PNG format");
            }
            return filePart;
        }
    }

    private Mono<Attachment> uploadAvatar(AvatarUploadRequest uploadRequest) {
        return environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
            .switchIfEmpty(
                Mono.error(new IllegalStateException("User setting is not configured"))
            )
            .flatMap(userSetting -> Mono.defer(
                () -> {
                    String avatarPolicy = userSetting.getAvatarPolicy();
                    if (StringUtils.isBlank(avatarPolicy)) {
                        avatarPolicy = DEFAULT_USER_AVATAR_ATTACHMENT_POLICY_NAME;
                    }
                    FilePart filePart = uploadRequest.getFile();
                    var ext = Files.getFileExtension(filePart.filename());
                    return attachmentService.upload(avatarPolicy,
                        USER_AVATAR_GROUP_NAME,
                        UUID.randomUUID() + "." + ext,
                        maxSizeCheck(filePart.content()),
                        filePart.headers().getContentType()
                    );
                })
            );
    }

    private Flux<DataBuffer> maxSizeCheck(Flux<DataBuffer> content) {
        var lenRef = new AtomicInteger(0);
        return content.doOnNext(dataBuffer -> {
            int len = lenRef.accumulateAndGet(dataBuffer.readableByteCount(), Integer::sum);
            if (len > MAX_AVATAR_FILE_SIZE.toBytes()) {
                throw new ServerWebInputException("The avatar file needs to be smaller than "
                    + MAX_AVATAR_FILE_SIZE.toMegabytes() + " MB.");
            }
        });
    }

    private Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
            .doOnNext(createUserRequest -> {
                if (StringUtils.isBlank(createUserRequest.name())) {
                    throw new ServerWebInputException("Name is required");
                }
                if (StringUtils.isBlank(createUserRequest.email())) {
                    throw new ServerWebInputException("Email is required");
                }
            })
            .flatMap(userRequest -> {
                User newUser = CreateUserRequest.from(userRequest);
                return userService.createUser(newUser, userRequest.roles())
                    .then(Mono.defer(() -> userService.updateWithRawPassword(userRequest.name(),
                            userRequest.password()))
                        .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                            .filter(OptimisticLockingFailureException.class::isInstance)
                        )
                    );
            })
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
            );
    }

    private Mono<ServerResponse> getUserByName(ServerRequest request) {
        final var name = request.pathVariable("name");
        return userService.getUser(name)
            .flatMap(this::toDetailedUser)
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
            );
    }

    record CreateUserRequest(@Schema(requiredMode = REQUIRED) String name,
                             @Schema(requiredMode = REQUIRED) String email,
                             String displayName,
                             String avatar,
                             String phone,
                             String password,
                             String bio,
                             Map<String, String> annotations,
                             Set<String> roles) {

        /**
         * <p>Creates a new user from {@link CreateUserRequest}.</p>
         * Note: this method will not set password.
         *
         * @param userRequest user request
         * @return user from request
         */
        public static User from(CreateUserRequest userRequest) {
            var user = new User();
            user.setMetadata(new Metadata());
            user.getMetadata().setName(userRequest.name());
            user.getMetadata().setAnnotations(new HashMap<>());
            Map<String, String> annotations =
                defaultIfNull(userRequest.annotations(), Map.of());
            user.getMetadata().getAnnotations().putAll(annotations);

            var spec = new User.UserSpec();
            user.setSpec(spec);
            spec.setEmail(userRequest.email());
            spec.setDisplayName(defaultIfBlank(userRequest.displayName(), userRequest.name()));
            spec.setAvatar(userRequest.avatar());
            spec.setPhone(userRequest.phone());
            spec.setBio(userRequest.bio());
            return user;
        }
    }

    private Mono<ServerResponse> updateProfile(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .flatMap(currentUserName -> client.get(User.class, currentUserName))
            .flatMap(currentUser -> request.bodyToMono(User.class)
                .filter(user -> user.getMetadata() != null
                    && Objects.equals(user.getMetadata().getName(),
                    currentUser.getMetadata().getName())
                )
                .switchIfEmpty(
                    Mono.error(() -> new ServerWebInputException("Username didn't match.")))
                .map(user -> {
                    Map<String, String> oldAnnotations =
                        MetadataUtil.nullSafeAnnotations(currentUser);
                    Map<String, String> newAnnotations = user.getMetadata().getAnnotations();
                    if (!CollectionUtils.isEmpty(newAnnotations)) {
                        newAnnotations.put(User.LAST_AVATAR_ATTACHMENT_NAME_ANNO,
                            oldAnnotations.get(User.LAST_AVATAR_ATTACHMENT_NAME_ANNO));
                        newAnnotations.put(User.AVATAR_ATTACHMENT_NAME_ANNO,
                            oldAnnotations.get(User.AVATAR_ATTACHMENT_NAME_ANNO));
                        newAnnotations.put(User.EMAIL_TO_VERIFY,
                            oldAnnotations.get(User.EMAIL_TO_VERIFY));
                        currentUser.getMetadata().setAnnotations(newAnnotations);
                    }
                    var spec = currentUser.getSpec();
                    var newSpec = user.getSpec();
                    spec.setBio(newSpec.getBio());
                    spec.setDisplayName(newSpec.getDisplayName());
                    spec.setTwoFactorAuthEnabled(newSpec.getTwoFactorAuthEnabled());
                    spec.setPhone(newSpec.getPhone());
                    return currentUser;
                })
            )
            .flatMap(client::update)
            .flatMap(updatedUser -> ServerResponse.ok().bodyValue(updatedUser));
    }

    Mono<ServerResponse> changeAnyonePasswordForAdmin(ServerRequest request) {
        final var nameInPath = request.pathVariable("name");
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> SELF_USER.equals(nameInPath) ? ctx.getAuthentication().getName()
                : nameInPath)
            .flatMap(username -> request.bodyToMono(ChangePasswordRequest.class)
                .switchIfEmpty(Mono.defer(() ->
                    Mono.error(new ServerWebInputException("Request body is empty"))))
                .flatMap(changePasswordRequest -> {
                    var password = changePasswordRequest.password();
                    // encode password
                    return userService.updateWithRawPassword(username, password);
                }))
            .flatMap(updatedUser -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser));
    }

    Mono<ServerResponse> changeOwnPassword(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication().getName())
            .flatMap(username -> request.bodyToMono(ChangeOwnPasswordRequest.class)
                .switchIfEmpty(Mono.defer(() ->
                    Mono.error(new ServerWebInputException("Request body is empty"))))
                .flatMap(changePasswordRequest -> {
                    var rawOldPassword = changePasswordRequest.oldPassword();
                    return userService.confirmPassword(username, rawOldPassword)
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new UnsatisfiedAttributeValueException(
                            "Old password is incorrect.",
                            "problemDetail.user.oldPassword.notMatch",
                            null))
                        )
                        .thenReturn(changePasswordRequest);
                })
                .flatMap(changePasswordRequest -> {
                    var password = changePasswordRequest.password();
                    // encode password
                    return userService.updateWithRawPassword(username, password);
                }))
            .flatMap(updatedUser -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser));
    }

    record ChangeOwnPasswordRequest(
        @Schema(description = "Old password.", requiredMode = REQUIRED)
        String oldPassword,
        @Schema(description = "New password.", requiredMode = REQUIRED, minLength = 6)
        String password) {
    }

    record ChangePasswordRequest(
        @Schema(description = "New password.", requiredMode = REQUIRED, minLength = 6)
        String password) {
    }

    @NonNull
    Mono<ServerResponse> me(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(obj -> !(obj instanceof TwoFactorAuthentication))
            .map(Authentication::getName)
            .defaultIfEmpty(AnonymousUserConst.PRINCIPAL)
            .flatMap(userService::getUser)
            .flatMap(this::toDetailedUser)
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user));
    }

    private Mono<DetailedUser> toDetailedUser(User user) {
        Set<String> roleNames = roleNames(user);
        return roleService.list(roleNames)
            .collectList()
            .map(roles -> new DetailedUser(user, roles))
            .defaultIfEmpty(new DetailedUser(user, List.of()));
    }

    Set<String> roleNames(User user) {
        Assert.notNull(user, "User must not be null");
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(user);
        String roleNamesJson = annotations.get(User.ROLE_NAMES_ANNO);
        if (StringUtils.isBlank(roleNamesJson)) {
            return Set.of();
        }
        return JsonUtils.jsonToObject(roleNamesJson, new TypeReference<>() {
        });
    }

    record DetailedUser(@Schema(requiredMode = REQUIRED) User user,
                        @Schema(requiredMode = REQUIRED) List<Role> roles) {

    }

    @NonNull
    Mono<ServerResponse> grantPermission(ServerRequest request) {
        var username = request.pathVariable("name");
        return request.bodyToMono(GrantRequest.class)
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Request body is empty")))
            .flatMap(grantRequest -> userService.grantRoles(username, grantRequest.roles())
                .then(ServerResponse.ok().build()));
    }

    record GrantRequest(Set<String> roles) {
    }

    @NonNull
    private Mono<ServerResponse> getUserPermission(ServerRequest request) {
        var name = request.pathVariable("name");
        Mono<UserPermission> userPermission;
        if (SELF_USER.equals(name)) {
            userPermission = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    var roleNames = authoritiesToRoles(auth.getAuthorities());
                    var up = new UserPermission();
                    var roles = roleService.list(roleNames)
                        .collect(Collectors.toSet())
                        .doOnNext(up::setRoles)
                        .then();
                    var permissions = roleService.listPermissions(roleNames)
                        .distinct()
                        .collectList()
                        .doOnNext(up::setPermissions)
                        .doOnNext(perms -> {
                            var uiPermissions = uiPermissions(new HashSet<>(perms));
                            up.setUiPermissions(uiPermissions);
                        })
                        .then();
                    return roles.and(permissions).thenReturn(up);
                });
        } else {
            // get roles from username
            userPermission = userService.listRoles(name)
                .collect(Collectors.toSet())
                .flatMap(roles -> {
                    var up = new UserPermission();
                    var setRoles = Mono.fromRunnable(() -> up.setRoles(roles)).then();
                    var roleNames = roles.stream()
                        .map(role -> role.getMetadata().getName())
                        .collect(Collectors.toSet());
                    var setPermissions = roleService.listPermissions(roleNames)
                        .distinct()
                        .collectList()
                        .doOnNext(up::setPermissions)
                        .doOnNext(perms -> {
                            var uiPermissions = uiPermissions(new HashSet<>(perms));
                            up.setUiPermissions(uiPermissions);
                        })
                        .then();
                    return setRoles.and(setPermissions).thenReturn(up);
                });
        }

        return ServerResponse.ok().body(userPermission, UserPermission.class);
    }

    private Set<String> uiPermissions(Set<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptySet();
        }
        return roles.stream()
            .<Set<String>>map(role -> {
                var annotations = role.getMetadata().getAnnotations();
                if (annotations == null) {
                    return Set.of();
                }
                var uiPermissionsJson = annotations.get(Role.UI_PERMISSIONS_ANNO);
                if (StringUtils.isBlank(uiPermissionsJson)) {
                    return Set.of();
                }
                return JsonUtils.jsonToObject(uiPermissionsJson,
                    new TypeReference<LinkedHashSet<String>>() {
                    });
            })
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Data
    public static class UserPermission {
        @Schema(requiredMode = REQUIRED)
        private Set<Role> roles;
        @Schema(requiredMode = REQUIRED)
        private List<Role> permissions;
        @Schema(requiredMode = REQUIRED)
        private Set<String> uiPermissions;

    }

    public static class ListRequest extends IListRequest.QueryListRequest {

        private final ServerWebExchange exchange;

        public ListRequest(ServerRequest request) {
            super(request.queryParams());
            this.exchange = request.exchange();
        }

        @Schema(name = "keyword")
        public String getKeyword() {
            return queryParams.getFirst("keyword");
        }

        @Schema(name = "role")
        public String getRole() {
            return queryParams.getFirst("role");
        }

        @ArraySchema(uniqueItems = true,
            arraySchema = @Schema(name = "sort",
                description = "Sort property and direction of the list result. Supported fields: "
                    + "creationTimestamp"),
            schema = @Schema(description = "like field,asc or field,desc",
                implementation = String.class,
                example = "creationTimestamp,desc"))
        public Sort getSort() {
            var sort = SortResolver.defaultInstance.resolve(exchange);
            sort = sort.and(Sort.by("metadata.creationTimestamp", "metadata.name").descending());
            return sort;
        }

        /**
         * Converts query parameters to list options.
         */
        public ListOptions toListOptions() {
            var listOptions =
                labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());

            var fieldQuery = listOptions.getFieldSelector().query();
            if (StringUtils.isNotBlank(getKeyword())) {
                fieldQuery = and(
                    fieldQuery,
                    or(
                        contains("spec.displayName", getKeyword()),
                        equal("metadata.name", getKeyword())
                    )
                );
            }

            if (StringUtils.isNotBlank(getRole())) {
                fieldQuery = and(
                    fieldQuery,
                    equal(User.USER_RELATED_ROLES_INDEX, getRole())
                );
            }

            listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
            return listOptions;
        }

        public static void buildParameters(Builder builder) {
            IListRequest.buildParameters(builder);
            builder.parameter(sortParameter())
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("keyword")
                    .description("Keyword to search")
                    .implementation(String.class)
                    .required(false))
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("role")
                    .description("Role name")
                    .implementation(String.class)
                    .required(false));
        }

    }

    record ListedUser(@Schema(requiredMode = REQUIRED) User user,
                      @Schema(requiredMode = REQUIRED) List<Role> roles) {
    }

    Mono<ServerResponse> list(ServerRequest request) {
        return Mono.just(request)
            .map(UserEndpoint.ListRequest::new)
            .flatMap(listRequest -> client.listBy(User.class, listRequest.toListOptions(),
                PageRequestImpl.of(
                    listRequest.getPage(), listRequest.getSize(),
                    listRequest.getSort()
                )
            ))
            .flatMap(this::toListedUser)
            .flatMap(listResult -> ServerResponse.ok().bodyValue(listResult));
    }

    private Mono<ListResult<ListedUser>> toListedUser(ListResult<User> listResult) {
        return Flux.fromStream(listResult.get())
            .concatMap(user -> {
                Set<String> roleNames = roleNames(user);
                return roleService.list(roleNames)
                    .collectList()
                    .map(roles -> new ListedUser(user, roles))
                    .defaultIfEmpty(new ListedUser(user, List.of()));
            })
            .collectList()
            .map(items -> convertFrom(listResult, items))
            .defaultIfEmpty(convertFrom(listResult, List.of()));
    }

    <T> ListResult<T> convertFrom(ListResult<?> listResult, List<T> items) {
        Assert.notNull(listResult, "listResult must not be null");
        Assert.notNull(items, "items must not be null");
        return new ListResult<>(listResult.getPage(), listResult.getSize(),
            listResult.getTotal(), items);
    }
}
