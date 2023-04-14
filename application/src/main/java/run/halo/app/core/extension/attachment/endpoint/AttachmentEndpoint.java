package run.halo.app.core.extension.attachment.endpoint;

import static java.util.Comparator.comparing;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.boot.convert.ApplicationConversionService.getSharedInstance;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.router.QueryParamBuildUtil.buildParametersFromType;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.requestbody.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.SortResolver;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.IListRequest.QueryListRequest;

@Slf4j
@Component
public class AttachmentEndpoint implements CustomEndpoint {

    private final AttachmentService attachmentService;

    private final ReactiveExtensionClient client;

    public AttachmentEndpoint(AttachmentService attachmentService,
        ReactiveExtensionClient client) {
        this.attachmentService = attachmentService;
        this.client = client;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/Attachment";
        return SpringdocRouteBuilder.route()
            .POST("/attachments/upload", contentType(MediaType.MULTIPART_FORM_DATA),
                request -> request.body(BodyExtractors.toMultipartData())
                    .map(UploadRequest::new)
                    .flatMap(uploadReq -> {
                        var policyName = uploadReq.getPolicyName();
                        var groupName = uploadReq.getGroupName();
                        var filePart = uploadReq.getFile();
                        return attachmentService.upload(policyName,
                            groupName,
                            filePart.filename(),
                            filePart.content(),
                            filePart.headers().getContentType());
                    })
                    .flatMap(attachment -> ServerResponse.ok().bodyValue(attachment)),
                builder -> builder
                    .operationId("UploadAttachment")
                    .tag(tag)
                    .requestBody(Builder.requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(IUploadRequest.class))
                        ))
                    .response(responseBuilder().implementation(Attachment.class))
                    .build())
            .GET("/attachments", this::search,
                builder -> {
                    builder
                        .operationId("SearchAttachments")
                        .tag(tag)
                        .response(
                            responseBuilder().implementation(generateGenericClass(Attachment.class))
                        );
                    buildParametersFromType(builder, ISearchRequest.class);
                }
            )
            .build();
    }

    Mono<ServerResponse> search(ServerRequest request) {
        var searchRequest = new SearchRequest(request);
        return client.list(Attachment.class,
                searchRequest.toPredicate(), searchRequest.toComparator(),
                searchRequest.getPage(), searchRequest.getSize())
            .flatMap(listResult -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(listResult));
    }

    public interface ISearchRequest extends IListRequest {

        @Schema(description = "Display name of attachment")
        Optional<String> getDisplayName();

        @Schema(description = "Name of policy")
        Optional<String> getPolicy();

        @Schema(description = "Name of group")
        Optional<String> getGroup();

        @Schema(description = "Filter attachments without group. This parameter will ignore group"
            + " parameter.")
        Optional<Boolean> getUngrouped();

        @Schema(description = "Name of user who uploaded the attachment")
        Optional<String> getUploadedBy();

        @ArraySchema(uniqueItems = true,
            arraySchema = @Schema(name = "sort",
                description = "Sort property and direction of the list result. Supported fields: "
                    + "creationTimestamp, size"),
            schema = @Schema(description = "like field,asc or field,desc",
                implementation = String.class,
                example = "creationTimestamp,desc"))
        Sort getSort();

    }

    public static class SearchRequest extends QueryListRequest implements ISearchRequest {

        private final ServerWebExchange exchange;

        public SearchRequest(ServerRequest request) {
            super(request.queryParams());
            this.exchange = request.exchange();
        }

        @Override
        public Optional<String> getDisplayName() {
            return Optional.ofNullable(queryParams.getFirst("displayName"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Optional<String> getPolicy() {
            return Optional.ofNullable(queryParams.getFirst("policy"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Optional<String> getGroup() {
            return Optional.ofNullable(queryParams.getFirst("group"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Optional<Boolean> getUngrouped() {
            return Optional.ofNullable(queryParams.getFirst("ungrouped"))
                .map(ungroupedStr -> getSharedInstance().convert(ungroupedStr, Boolean.class));
        }

        @Override
        public Optional<String> getUploadedBy() {
            return Optional.ofNullable(queryParams.getFirst("uploadedBy"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Sort getSort() {
            return SortResolver.defaultInstance.resolve(exchange);
        }

        public Predicate<Attachment> toPredicate() {
            Predicate<Attachment> displayNamePred = attachment -> getDisplayName()
                .map(displayNameInParam -> {
                    String displayName = attachment.getSpec().getDisplayName();
                    return displayName.contains(displayNameInParam);
                }).orElse(true);

            Predicate<Attachment> policyPred = attachment -> getPolicy()
                .map(policy -> Objects.equals(policy, attachment.getSpec().getPolicyName()))
                .orElse(true);

            Predicate<Attachment> groupPred = attachment -> getGroup()
                .map(group -> Objects.equals(group, attachment.getSpec().getGroupName()))
                .orElse(true);

            Predicate<Attachment> ungroupedPred = attachment -> getUngrouped()
                .filter(Boolean::booleanValue)
                .map(ungrouped -> !StringUtils.hasText(attachment.getSpec().getGroupName()))
                .orElseGet(() -> groupPred.test(attachment));

            Predicate<Attachment> uploadedByPred = attachment -> getUploadedBy()
                .map(uploadedBy -> Objects.equals(uploadedBy, attachment.getSpec().getOwnerName()))
                .orElse(true);


            var selectorPred =
                labelAndFieldSelectorToPredicate(getLabelSelector(), getFieldSelector());

            return displayNamePred
                .and(policyPred)
                .and(ungroupedPred)
                .and(uploadedByPred)
                .and(selectorPred);
        }

        public Comparator<Attachment> toComparator() {
            var sort = getSort();
            List<Comparator<Attachment>> comparators = new ArrayList<>();
            var creationOrder = sort.getOrderFor("creationTimestamp");
            if (creationOrder != null) {
                Comparator<Attachment> comparator = comparing(
                    attachment -> attachment.getMetadata().getCreationTimestamp());
                if (creationOrder.isDescending()) {
                    comparator = comparator.reversed();
                }
                comparators.add(comparator);
            }

            var sizeOrder = sort.getOrderFor("size");
            if (sizeOrder != null) {
                Comparator<Attachment> comparator =
                    comparing(attachment -> attachment.getSpec().getSize());
                if (sizeOrder.isDescending()) {
                    comparator = comparator.reversed();
                }
                comparators.add(comparator);
            }

            // add default comparator
            comparators.add(Comparators.compareCreationTimestamp(false));
            comparators.add(Comparators.compareName(true));
            return comparators.stream()
                .reduce(Comparator::thenComparing)
                .orElse(null);
        }
    }

    public interface IUploadRequest {

        @Schema(required = true, description = "Attachment file")
        FilePart getFile();

        @Schema(required = true, description = "Storage policy name")
        String getPolicyName();

        @Schema(description = "The name of the group to which the attachment belongs")
        String getGroupName();

    }

    public record UploadRequest(MultiValueMap<String, Part> formData) implements IUploadRequest {

        public FilePart getFile() {
            if (formData.getFirst("file") instanceof FilePart file) {
                return file;
            }
            throw new ServerWebInputException("Invalid part of file");
        }

        public String getPolicyName() {
            if (formData.getFirst("policyName") instanceof FormFieldPart form) {
                return form.value();
            }
            throw new ServerWebInputException("Invalid part of policyName");
        }

        @Override
        public String getGroupName() {
            if (formData.getFirst("groupName") instanceof FormFieldPart form) {
                return form.value();
            }
            return null;
        }
    }
}
