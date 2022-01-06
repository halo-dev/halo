package run.halo.app.service.impl;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.BaseContent;
import run.halo.app.model.entity.BaseContent.PatchedContent;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.base.BaseContentRepository;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BaseContentPatchLogService;
import run.halo.app.service.base.BaseContentService;
import run.halo.app.utils.ReflectionUtils;

/**
 * Base content service implementation.
 *
 * @author guqing
 * @date 2022-01-07
 */
public abstract class BaseContentServiceImpl<CONTENT extends BaseContent>
    extends AbstractCrudService<CONTENT, Integer> implements BaseContentService<CONTENT> {

    private final BaseContentRepository<CONTENT> baseContentRepository;

    private final BaseContentPatchLogService contentPatchLogService;

    protected BaseContentServiceImpl(BaseContentRepository<CONTENT> baseContentRepository,
        BaseContentPatchLogService contentPatchLogService) {
        super(baseContentRepository);
        this.baseContentRepository = baseContentRepository;
        this.contentPatchLogService = contentPatchLogService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CONTENT createGenericClassInstance() {
        ParameterizedType parameterizedType =
            ReflectionUtils.getParameterizedTypeBySuperClass(BaseContentServiceImpl.class,
                this.getClass());
        if (parameterizedType == null) {
            throw new RuntimeException("Unable to get a generic parameterized type.");
        }
        Class<CONTENT> clazz = (Class<CONTENT>) parameterizedType.getActualTypeArguments()[0];
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CONTENT createOrUpdateDraftBy(Integer postId, String content,
        String originalContent) {
        Assert.notNull(postId, "The postId must not be null.");
        // 1.Judge whether there is a post content record. if there is, modify it.
        // Otherwise, create it
        Optional<CONTENT> savedContent = baseContentRepository.findById(postId);
        if (savedContent.isPresent()) {
            if (!PostStatus.DRAFT.equals(savedContent.get().getStatus())) {
                return savedContent.get();
            }
            //1).Judge whether there are unpublished draft contents in the content patch logs
            ContentPatchLog contentPatchLog =
                contentPatchLogService.createOrUpdate(postId, content, originalContent);
            CONTENT postContent = baseContentRepository.getById(postId);
            // Update header pointer
            postContent.setHeadPatchLogId(contentPatchLog.getId());
            baseContentRepository.save(postContent);
            return postContent;
        }
        ContentPatchLog contentPatchLog = new ContentPatchLog();
        // The first creation does not exist V1, so use the original content directly
        contentPatchLog.setContentDiff(content);
        contentPatchLog.setOriginalContentDiff(originalContent);
        contentPatchLog.setPostId(postId);
        contentPatchLog.setStatus(PostStatus.DRAFT);
        contentPatchLogService.createOrUpdate(contentPatchLog);

        CONTENT postContent = createGenericClassInstance();
        postContent.setPatchLogId(contentPatchLog.getId());
        postContent.setContent(content);
        postContent.setOriginalContent(originalContent);
        postContent.setId(postId);
        postContent.setStatus(PostStatus.DRAFT);
        postContent.setHeadPatchLogId(contentPatchLog.getId());
        baseContentRepository.save(postContent);

        return postContent;
    }

    @Override
    public CONTENT publishContent(Integer postId) {
        ContentPatchLog contentPatchLog = contentPatchLogService.getDraftByPostId(postId);
        if (contentPatchLog == null) {
            return baseContentRepository.getById(postId);
        }
        contentPatchLog.setStatus(PostStatus.PUBLISHED);
        contentPatchLog.setPublishTime(new Date());
        contentPatchLogService.createOrUpdate(contentPatchLog);

        final CONTENT postContent = getById(postId);
        postContent.setPatchLogId(contentPatchLog.getId());
        postContent.setStatus(PostStatus.PUBLISHED);

        PatchedContent patchedContent = contentPatchLogService.applyPatch(contentPatchLog);
        postContent.setContent(patchedContent.getContent());
        postContent.setOriginalContent(patchedContent.getOriginalContent());

        baseContentRepository.save(postContent);

        return postContent;
    }

    @Override
    @NonNull
    public CONTENT getById(@NonNull Integer postId) {
        Assert.notNull(postId, "The postId must not be null.");
        return baseContentRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("content was not found or has been deleted"));
    }
}
