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
        // First, we need to save the contentPatchLog
        ContentPatchLog contentPatchLog =
            contentPatchLogService.createOrUpdate(postId, content, originalContent);

        // then update the value of headPatchLogId field.
        Optional<CONTENT> savedContentOptional = baseContentRepository.findById(postId);
        if (savedContentOptional.isPresent()) {
            CONTENT savedContent = savedContentOptional.get();
            savedContent.setHeadPatchLogId(contentPatchLog.getId());
            baseContentRepository.save(savedContent);
            return savedContent;
        }

        // If the content record does not exist, it needs to be created
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
        contentPatchLogService.save(contentPatchLog);

        CONTENT postContent = getById(postId);
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
