package run.halo.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Content.ContentDiff;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.ContentPatchLogRepository;
import run.halo.app.repository.ContentRepository;
import run.halo.app.service.ContentPatchLogService;

/**
 * Test for content path log service implementation.
 *
 * @author guqing
 * @date 2022-02-19
 */
@ExtendWith(SpringExtension.class)
public class ContentPatchLogServiceImplTest {
    @MockBean
    private ContentPatchLogRepository contentPatchLogRepository;

    @MockBean
    private ContentRepository contentRepository;

    private ContentPatchLogService contentPatchLogService;

    @BeforeEach
    public void setUp() {
        contentPatchLogService =
            new ContentPatchLogServiceImpl(contentPatchLogRepository, contentRepository);

        Content content = new Content();
        content.setId(2);
        content.setContent("好雨知时节，当春乃发生。");
        content.setOriginalContent("<p>好雨知时节，当春乃发生。</p>\n");
        content.setPatchLogId(1);
        content.setHeadPatchLogId(2);
        content.setStatus(PostStatus.PUBLISHED);
        content.setCreateTime(new Date(1645281361));
        content.setUpdateTime(new Date(1645281361));

        ContentPatchLog contentPatchLogV1 = new ContentPatchLog();
        contentPatchLogV1.setId(1);
        contentPatchLogV1.setSourceId(0);
        contentPatchLogV1.setPostId(2);
        contentPatchLogV1.setVersion(1);
        contentPatchLogV1.setStatus(PostStatus.PUBLISHED);
        contentPatchLogV1.setCreateTime(new Date());
        contentPatchLogV1.setUpdateTime(new Date());
        contentPatchLogV1.setContentDiff("<p>望岳</p>\n<p>岱宗夫如何，齐鲁青未了。</p>\n");
        contentPatchLogV1.setOriginalContentDiff("望岳\n\n岱宗夫如何，齐鲁青未了。\n");

        ContentPatchLog contentPatchLogV2 = new ContentPatchLog();
        contentPatchLogV2.setId(2);
        contentPatchLogV2.setSourceId(1);
        contentPatchLogV2.setPostId(2);
        contentPatchLogV2.setVersion(2);
        contentPatchLogV2.setStatus(PostStatus.DRAFT);
        contentPatchLogV2.setCreateTime(new Date());
        contentPatchLogV2.setUpdateTime(new Date());
        contentPatchLogV2.setContentDiff("[{\"source\":{\"position\":2,\"lines\":[],"
            + "\"changePosition\":null},\"target\":{\"position\":2,"
            + "\"lines\":[\"<p>造化钟神秀，阴阳割昏晓。</p>\"],\"changePosition\":null},\"type\":\"INSERT\"}]");
        contentPatchLogV2.setOriginalContentDiff("[{\"source\":{\"position\":4,\"lines\":[],"
            + "\"changePosition\":null},\"target\":{\"position\":4,\"lines\":[\"造化钟神秀，阴阳割昏晓。\","
            + "\"\"],\"changePosition\":null},\"type\":\"INSERT\"}]");

        when(contentRepository.findById(1)).thenReturn(Optional.empty());
        when(contentRepository.findById(2)).thenReturn(Optional.of(content));

        when(contentPatchLogRepository.findById(1)).thenReturn(Optional.of(contentPatchLogV1));
        when(contentPatchLogRepository.getById(1)).thenReturn(contentPatchLogV1);

        when(contentPatchLogRepository.findById(2)).thenReturn(Optional.of(contentPatchLogV2));
        when(contentPatchLogRepository.getById(2)).thenReturn(contentPatchLogV2);

        ContentPatchLog contentPatchLogExample = new ContentPatchLog();
        contentPatchLogExample.setPostId(2);
        contentPatchLogExample.setStatus(PostStatus.DRAFT);
        Example<ContentPatchLog> example = Example.of(contentPatchLogExample);
        when(contentPatchLogRepository.exists(example)).thenReturn(true);

        when(contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(2,
            PostStatus.DRAFT)).thenReturn(contentPatchLogV2);

        when(contentPatchLogRepository.findByPostIdAndVersion(2, 1))
            .thenReturn(contentPatchLogV1);

    }

    @Test
    public void createOrUpdate() {
        // record will be created
        ContentPatchLog created =
            contentPatchLogService.createOrUpdate(1, "国破山河在，城春草木深\n", "<p>国破山河在，城春草木深</p>\n");
        assertThat(created).isNotNull();
        assertThat(created.getVersion()).isEqualTo(1);
        assertThat(created.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(created.getSourceId()).isEqualTo(0);
        assertThat(created.getContentDiff()).isEqualTo(created.getContentDiff());
        assertThat(created.getOriginalContentDiff()).isEqualTo(created.getOriginalContentDiff());

        // record will be updated
        ContentPatchLog updated =
            contentPatchLogService.createOrUpdate(2, "<p>好雨知时节，当春乃发生。</p>\n", "好雨知时节，当春乃发生。\n");
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(2);
        assertThat(updated.getContentDiff()).isEqualTo("[{\"source\":{\"position\":0,"
            + "\"lines\":[\"<p>望岳</p>\",\"<p>岱宗夫如何，齐鲁青未了。</p>\"],\"changePosition\":null},"
            + "\"target\":{\"position\":0,\"lines\":[\"<p>好雨知时节，当春乃发生。</p>\"],"
            + "\"changePosition\":null},\"type\":\"CHANGE\"}]");
        assertThat(updated.getOriginalContentDiff()).isEqualTo("[{\"source"
            + "\":{\"position\":0,\"lines\":[\"望岳\"],\"changePosition\":null},"
            + "\"target\":{\"position\":0,\"lines\":[\"好雨知时节，当春乃发生。\"],\"changePosition\":null},"
            + "\"type\":\"CHANGE\"},{\"source\":{\"position\":2,\"lines\":[\"岱宗夫如何，齐鲁青未了。\",\"\"],"
            + "\"changePosition\":null},\"target\":{\"position\":2,\"lines\":[],"
            + "\"changePosition\":null},\"type\":\"DELETE\"}]");
    }

    @Test
    public void applyPatch() {
        ContentPatchLog contentPatchLogV2 = new ContentPatchLog();
        contentPatchLogV2.setId(2);
        contentPatchLogV2.setSourceId(1);
        contentPatchLogV2.setPostId(2);
        contentPatchLogV2.setVersion(2);
        contentPatchLogV2.setStatus(PostStatus.DRAFT);
        contentPatchLogV2.setCreateTime(new Date());
        contentPatchLogV2.setUpdateTime(new Date());
        contentPatchLogV2.setContentDiff("[{\"source\":{\"position\":2,\"lines\":[],"
            + "\"changePosition\":null},\"target\":{\"position\":2,"
            + "\"lines\":[\"<p>造化钟神秀，阴阳割昏晓。</p>\"],\"changePosition\":null},\"type\":\"INSERT\"}]");
        contentPatchLogV2.setOriginalContentDiff("[{\"source\":{\"position\":4,\"lines\":[],"
            + "\"changePosition\":null},\"target\":{\"position\":4,\"lines\":[\"造化钟神秀，阴阳割昏晓。\","
            + "\"\"],\"changePosition\":null},\"type\":\"INSERT\"}]");

        PatchedContent patchedContent =
            contentPatchLogService.applyPatch(contentPatchLogV2);

        assertThat(patchedContent).isNotNull();
        assertThat(patchedContent.getContent()).isEqualTo("<p>望岳</p>\n"
            + "<p>岱宗夫如何，齐鲁青未了。</p>\n"
            + "<p>造化钟神秀，阴阳割昏晓。</p>\n");
        assertThat(patchedContent.getOriginalContent()).isEqualTo("望岳\n\n岱宗夫如何，齐鲁青未了。\n"
            + "\n造化钟神秀，阴阳割昏晓。\n");
    }

    @Test
    public void generateDiff() {
        ContentDiff contentDiff =
            contentPatchLogService.generateDiff(2, "<p>随风潜入夜，润物细无声。</p>", "随风潜入夜，润物细无声。");

        assertThat(contentDiff).isNotNull();
        assertThat(contentDiff.getDiff()).isEqualTo("[{\"source\":{\"position\":0,"
            + "\"lines\":[\"<p>望岳</p>\",\"<p>岱宗夫如何，齐鲁青未了。</p>\",\"\"],\"changePosition\":null},"
            + "\"target\":{\"position\":0,\"lines\":[\"<p>随风潜入夜，润物细无声。</p>\"],"
            + "\"changePosition\":null},\"type\":\"CHANGE\"}]");
        assertThat(contentDiff.getOriginalDiff()).isEqualTo("[{\"source\":{\"position\":0,"
            + "\"lines\":[\"望岳\",\"\",\"岱宗夫如何，齐鲁青未了。\",\"\"],\"changePosition\":null},"
            + "\"target\":{\"position\":0,\"lines\":[\"随风潜入夜，润物细无声。\"],\"changePosition\":null},"
            + "\"type\":\"CHANGE\"}]");
    }

    @Test
    public void getPatchedContentById() {
        PatchedContent patchedContent = contentPatchLogService.getPatchedContentById(2);

        assertThat(patchedContent).isNotNull();
        assertThat(patchedContent.getContent()).isEqualTo("<p>望岳</p>\n"
            + "<p>岱宗夫如何，齐鲁青未了。</p>\n"
            + "<p>造化钟神秀，阴阳割昏晓。</p>\n");
        assertThat(patchedContent.getOriginalContent()).isEqualTo("望岳\n\n"
            + "岱宗夫如何，齐鲁青未了。\n\n"
            + "造化钟神秀，阴阳割昏晓。\n");
    }
}
