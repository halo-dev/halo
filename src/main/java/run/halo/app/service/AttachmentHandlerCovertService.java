package run.halo.app.service;

import run.halo.app.model.enums.AttachmentType;

import java.util.concurrent.Future;

public interface AttachmentHandlerCovertService {

    /**
     * Covert attachments to the current Handler and update the attachment links in all posts.
     *
     * <p>
     * sourceAttachmentTypeId can be gotten by AttachmentType.getValue(),
     * In particular, -1 means to use all AttachmentTypes,
     *
     * <p>
     * if deleteOldAttachment = true, Old attachments that have been successfully uploaded will be deleted from the specified Handler
     * if uploadAllInAttachment = true, Will upload all attachments, even if they are not quoted by the post, default = false.
     * if uploadAllInPost = true, Will download and upload all pictures in the all posts, even if they are not in the attachment library.
     *
     * @param sourceAttachmentTypeId source attachment type id (e.g. 0,1,2), default = -1 (All AttachmentTypes).
     * @param deleteOldAttachment    Whether to delete old attachments, default = false.
     * @param uploadAllInAttachment  Whether to upload all attachments, default = false.
     * @param uploadAllInPost        Whether to download and upload all pictures in the all posts, default = false.
     * @return AsyncResult<String>
     */
    Future<String> covertHandlerByPosts(
            AttachmentType sourceAttachmentTypeId,
            Boolean deleteOldAttachment,
            Boolean uploadAllInAttachment,
            Boolean uploadAllInPost);
}
