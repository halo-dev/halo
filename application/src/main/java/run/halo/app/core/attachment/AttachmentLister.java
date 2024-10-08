package run.halo.app.core.attachment;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ListResult;

public interface AttachmentLister {

    Mono<ListResult<Attachment>> listBy(SearchRequest searchRequest);
}
