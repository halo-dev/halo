package run.halo.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import run.halo.app.model.entity.ContentPatchLog;
import run.halo.app.model.enums.PostStatus;

/**
 * Content patch log repository test.
 *
 * @author guqing
 * @date 2022-02-19
 */
@DataJpaTest
@Sql("classpath:content-patch-logs.sql")
@AutoConfigureDataJpa
public class ContentPatchLogRepositoryTest {
    @Autowired
    private ContentPatchLogRepository contentPatchLogRepository;

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
