package run.halo.app.security.session;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.session.FindByIndexNameSessionRepository;

class ReactiveFindByIndexNameSessionRepositoryTest {

    @Test
    void principalNameIndexNameTest() {
        assertThat(ReactiveFindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME)
            .isEqualTo(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
    }
}
