package run.halo.app.service;

import java.util.concurrent.Future;

public interface AttachmentHandlerCovertService {

    /**
     * Upload all attachments to the current Handler and update the attachment links in all posts,
     * If the upload is successful, the old attachment will be deleted, otherwise it will be kept.
     *
     * <p>
     * if deleteOldAttachment = true, Old attachments that have been successfully uploaded will be deleted from the specified Handler
     * if uploadAllInAttachment = true, upload all attachments, even if they are not quoted by the post, default = false.
     * if uploadAllInPost = true, download and upload all pictures in the all posts, even if they are not in the attachment library.
     *
     * @param sourceAttachmentTypeId source attachment type id (e.g. 0,1,2), default = -1 (All handlers).
     * @param deleteOldAttachment    Whether to delete old attachments, default = false.
     * @param uploadAllInAttachment  Whether to upload all attachments, default = false.
     * @param uploadAllInPost        Whether to download and upload all pictures in the all posts, default = false.
     * @return AsyncResult<String>
     */
    Future<String> covertHandlerByPosts(
            Integer sourceAttachmentTypeId,
            Boolean deleteOldAttachment,
            Boolean uploadAllInAttachment,
            Boolean uploadAllInPost);
}
