package run.halo.app.core.extension.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.router.QueryParamBuildUtil.buildParametersFromType;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.infra.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class UserEndpoint implements CustomEndpoint {

    private static final String SELF_USER = "-";
    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final RoleService roleService;

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
            .PUT("/users/{name}/password", this::changePassword,
                builder -> builder.operationId("ChangePassword")
                    .description("Change password of user.")
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
                buildParametersFromType(builder, ListRequest.class);
            })
            .build();
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
                    currentUser.getMetadata().setAnnotations(user.getMetadata().getAnnotations());
                    var spec = currentUser.getSpec();
                    var newSpec = user.getSpec();
                    spec.setAvatar(newSpec.getAvatar());
                    spec.setBio(newSpec.getBio());
                    spec.setDisplayName(newSpec.getDisplayName());
                    spec.setTwoFactorAuthEnabled(newSpec.getTwoFactorAuthEnabled());
                    spec.setEmail(newSpec.getEmail());
                    spec.setPhone(newSpec.getPhone());
                    return currentUser;
                })
            )
            .flatMap(client::update)
            .flatMap(updatedUser -> ServerResponse.ok().bodyValue(updatedUser));
    }

    Mono<ServerResponse> changePassword(ServerRequest request) {
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

    record ChangePasswordRequest(
        @Schema(description = "New password.", required = true, minLength = 6)
        String password) {
    }

    @NonNull
    Mono<ServerResponse> me(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> {
                var name = ctx.getAuthentication().getName();
                return userService.getUser(name);
            })
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
        String name = request.pathVariable("name");
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> SELF_USER.equals(name) ? ctx.getAuthentication().getName() : name)
            .flatMapMany(userService::listRoles)
            .reduce(new LinkedHashSet<Role>(), (list, role) -> {
                list.add(role);
                return list;
            })
            .flatMap(roles -> uiPermissions(roles)
                .collectList()
                .map(uiPermissions -> new UserPermission(roles, Set.copyOf(uiPermissions)))
                .defaultIfEmpty(new UserPermission(roles, Set.of()))
            )
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    private Flux<String> uiPermissions(Set<Role> roles) {
        return Flux.fromIterable(roles)
            .map(role -> role.getMetadata().getName())
            .collectList()
            .flatMapMany(roleNames -> roleService.listDependenciesFlux(Set.copyOf(roleNames)))
            .map(role -> {
                Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(role);
                String uiPermissionStr = annotations.get(Role.UI_PERMISSIONS_ANNO);
                if (StringUtils.isBlank(uiPermissionStr)) {
                    return new HashSet<String>();
                }
                return JsonUtils.jsonToObject(uiPermissionStr,
                    new TypeReference<LinkedHashSet<String>>() {
                    });
            })
            .flatMapIterable(Function.identity());
    }

    record UserPermission(@Schema(required = true) Set<Role> roles,
                          @Schema(required = true) Set<String> uiPermissions) {
    }

    public class ListRequest extends IListRequest.QueryListRequest {

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
            return SortResolver.defaultInstance.resolve(exchange);
        }

        public Predicate<User> toPredicate() {
            Predicate<User> displayNamePredicate = user -> {
                var keyword = getKeyword();
                if (!org.springframework.util.StringUtils.hasText(keyword)) {
                    return true;
                }
                var displayName = user.getSpec().getDisplayName();
                if (!org.springframework.util.StringUtils.hasText(displayName)) {
                    return false;
                }
                return displayName.toLowerCase().contains(keyword.trim().toLowerCase());
            };
            Predicate<User> rolePredicate = user -> {
                var role = getRole();
                if (role == null) {
                    return true;
                }
                var annotations = user.getMetadata().getAnnotations();
                if (annotations == null || !annotations.containsKey(User.ROLE_NAMES_ANNO)) {
                    return false;
                } else {
                    Pattern pattern = Pattern.compile("\\[\"([^\"]*)\"\\]");
                    Matcher matcher = pattern.matcher(annotations.get(User.ROLE_NAMES_ANNO));
                    if (matcher.find()) {
                        return matcher.group(1).equals(role);
                    } else {
                        return false;
                    }
                }
            };
            return displayNamePredicate
                .and(rolePredicate)
                .and(labelAndFieldSelectorToPredicate(getLabelSelector(), getFieldSelector()));
        }

        public Comparator<User> toComparator() {
            var sort = getSort();
            var ctOrder = sort.getOrderFor("creationTimestamp");
            List<Comparator<User>> comparators = new ArrayList<>();
            if (ctOrder != null) {
                Comparator<User> comparator =
                    comparing(user -> user.getMetadata().getCreationTimestamp());
                if (ctOrder.isDescending()) {
                    comparator = comparator.reversed();
                }
                comparators.add(comparator);
            }
            comparators.add(Comparators.compareCreationTimestamp(false));
            comparators.add(Comparators.compareName(true));
            return comparators.stream()
                .reduce(Comparator::thenComparing)
                .orElse(null);
        }
    }

    record ListedUser(@Schema(requiredMode = REQUIRED) User user,
                      @Schema(requiredMode = REQUIRED) List<Role> roles) {
    }

    Mono<ServerResponse> list(ServerRequest request) {
        return Mono.just(request)
            .map(UserEndpoint.ListRequest::new)
            .flatMap(listRequest -> {
                var predicate = listRequest.toPredicate();
                var comparator = listRequest.toComparator();
                return client.list(User.class,
                    predicate,
                    comparator,
                    listRequest.getPage(),
                    listRequest.getSize());
            })
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
