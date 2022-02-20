package run.halo.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;

/**
 * Content patch log repository test.
 *
 * @author guqing
 * @date 2022-02-19
 */
@DataJpaTest
@AutoConfigureDataJpa
public class ContentPatchLogRepositoryTest {
    @Autowired
    private ContentPatchLogRepository contentPatchLogRepository;

    /**
     * Creates some test data for {@link ContentPatchLog}.
     */
    @BeforeEach
    public void setUp() {
        List<ContentPatchLog> list = new ArrayList<>();

        ContentPatchLog record1 = create(2, "<h2 id=\"关于页面\">关于页面</h2>\n"
                + "<p>这是一个自定义页面，你可以在后台的 <code>页面</code> -&gt; <code>所有页面</code> -&gt; "
                + "<code>自定义页面</code> 找到它，你可以用于新建关于页面、留言板页面等等。发挥你自己的想象力！</p>\n"
                + "<blockquote>\n"
                + "<p>这是一篇自动生成的页面，你可以在后台删除它。</p>\n"
                + "</blockquote>\n",
            "## 关于页面\n"
                + "\n这是一个自定义页面，你可以在后台的 `页面` -> `所有页面` -> `自定义页面` 找到它，你可以用于新"
                + "建关于页面、留言板页面等等。发挥你自己的想象力！\n"
                + "\n> 这是一篇自动生成的页面，你可以在后台删除它。",
            2, new Date(), 0, 1);
        list.add(record1);

        ContentPatchLog record2 = create(3, "<p><strong>登高</strong></p>\n"
                + "<p>风急天高猿啸哀，渚清沙白鸟飞回。</p>\n"
                + "<p>无边落木萧萧下，不尽长江滚滚来。</p>\n"
                + "<p>万里悲秋常作客，百年多病独登台。</p>\n"
                + "<p>艰难苦恨繁霜鬓，潦倒新停浊酒杯。</p>\n",
            "**登高**\n"
                + "\n"
                + "风急天高猿啸哀，渚清沙白鸟飞回。\n"
                + "\n"
                + "无边落木萧萧下，不尽长江滚滚来。\n"
                + "\n"
                + "万里悲秋常作客，百年多病独登台。\n"
                + "\n"
                + "艰难苦恨繁霜鬓，潦倒新停浊酒杯。",
            3, new Date(), 0, 1);
        list.add(record2);

        ContentPatchLog record3 = create(4, "<p>望岳</p>\n"
                + "<p>岱宗夫如何，齐鲁青未了。</p>\n",
            "望岳\n"
                + "\n"
                + "岱宗夫如何，齐鲁青未了。\n",
            4, new Date(), 0, 1);
        list.add(record3);

        ContentPatchLog record4 = create(5, "[{\"source\":{\"position\":2,\"lines\":[],"
                + "\"changePosition\":null},\"target\":{\"position\":2,"
                + "\"lines\":[\"<p>造化钟神秀，阴阳割昏晓。</p>\"],\"changePosition\":null},"
                + "\"type\":\"INSERT\"}]",
            "[{\"source\":{\"position\":4,\"lines\":[],\"changePosition\":null},"
                + "\"target\":{\"position\":4,\"lines\":[\"造化钟神秀，阴阳割昏晓。\",\"\"],"
                + "\"changePosition\":null},\"type\":\"INSERT\"}]",
            4, new Date(), 4, 2);
        list.add(record4);

        ContentPatchLog record5 = create(6, "[{\"source\":{\"position\":2,\"lines\":[],"
                + "\"changePosition\":null},\"target\":{\"position\":2,"
                + "\"lines\":[\"<p>造化钟神秀，阴阳割昏晓。</p>\",\"<p>荡胸生曾云，决眦入归鸟。</p>\"],"
                + "\"changePosition\":null},\"type\":\"INSERT\"}]",
            "[{\"source\":{\"position\":4,\"lines\":[],\"changePosition\":null},"
                + "\"target\":{\"position\":4,\"lines\":[\"造化钟神秀，阴阳割昏晓。\",\"\",\"荡胸生曾云，决眦入归鸟。\","
                + "\"\"],\"changePosition\":null},\"type\":\"INSERT\"}]",
            4, new Date(), 5, 3);
        list.add(record5);

        ContentPatchLog record6 = create(7, "[{\"source\":{\"position\":2,\"lines\":[],"
                + "\"changePosition\":null},\"target\":{\"position\":2,"
                + "\"lines\":[\"<p>造化钟神秀，阴阳割昏晓。</p>\",\"<p>荡胸生曾云，决眦入归鸟。</p>\","
                + "\"<p>会当凌绝顶，一览众山小。</p>\"],\"changePosition\":null},\"type\":\"INSERT\"}]",
            "[{\"source\":{\"position\":4,\"lines\":[],\"changePosition\":null},"
                + "\"target\":{\"position\":4,\"lines\":[\"造化钟神秀，阴阳割昏晓。\",\"\",\"荡胸生曾云，决眦入归鸟。\","
                + "\"\",\"会当凌绝顶，一览众山小。\"],\"changePosition\":null},\"type\":\"INSERT\"}]",
            4, new Date(), 6, 4);
        list.add(record6);

        ContentPatchLog record7 = create(8, "[{\"source\":{\"position\":2,\"lines\":[],"
                + "\"changePosition\":null},\"target\":{\"position\":2,"
                + "\"lines\":[\"<p>造化钟神秀，阴阳割昏晓。</p>\",\"<p>荡胸生曾云，决眦入归鸟。</p>\","
                + "\"<p>会当凌绝顶，一览众山小。</p>\"],\"changePosition\":null},\"type\":\"INSERT\"}]",
            "[{\"source\":{\"position\":4,\"lines\":[],\"changePosition\":null},"
                + "\"target\":{\"position\":4,\"lines\":[\"造化钟神秀，阴阳割昏晓。\",\"\",\"荡胸生曾云，决眦入归鸟。\","
                + "\"\",\"会当凌绝顶，一览众山小。\"],\"changePosition\":null},\"type\":\"INSERT\"}]",
            4, new Date(), 7, 5);
        list.add(record7);

        // Save records
        contentPatchLogRepository.saveAll(list);
    }

    private ContentPatchLog create(Integer id, String contentDiff, String originalContentDiff,
        Integer postId, Date publishTime, Integer sourceId, Integer version) {
        ContentPatchLog record = new ContentPatchLog();
        record.setId(id);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setContentDiff(contentDiff);
        record.setOriginalContentDiff(originalContentDiff);
        record.setPostId(postId);
        record.setPublishTime(publishTime);
        record.setSourceId(sourceId);
        record.setStatus(PostStatus.PUBLISHED);
        record.setVersion(version);
        return record;
    }

    @Test
    public void findAllByPostId() {
        List<ContentPatchLog> patchLogs = contentPatchLogRepository.findAllByPostId(4);
        assertThat(patchLogs).isNotEmpty();
        assertThat(patchLogs).hasSize(5);
    }

    @Test
    public void findByPostIdAndVersion() {
        ContentPatchLog v1 = contentPatchLogRepository.findByPostIdAndVersion(4, 1);

        assertThat(v1).isNotNull();
        assertThat(v1.getOriginalContentDiff()).isEqualTo("望岳\n\n岱宗夫如何，齐鲁青未了。\n");
        assertThat(v1.getContentDiff()).isEqualTo("<p>望岳</p>\n<p>岱宗夫如何，齐鲁青未了。</p>\n");
        assertThat(v1.getSourceId()).isEqualTo(0);
        assertThat(v1.getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    public void findFirstByPostIdOrderByVersionDesc() {
        ContentPatchLog latest =
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(4);

        assertThat(latest).isNotNull();
        assertThat(latest.getId()).isEqualTo(8);
        assertThat(latest.getVersion()).isEqualTo(5);
    }

    @Test
    public void findFirstByPostIdAndStatusOrderByVersionDesc() {
        // finds the latest draft record
        ContentPatchLog latestDraft =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(4,
                PostStatus.DRAFT);
        assertThat(latestDraft).isNull();

        // finds the latest published record
        ContentPatchLog latestPublished =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(4,
                PostStatus.PUBLISHED);
        assertThat(latestPublished).isNotNull();
        assertThat(latestPublished.getId()).isEqualTo(8);
        assertThat(latestPublished.getVersion()).isEqualTo(5);
    }
}
