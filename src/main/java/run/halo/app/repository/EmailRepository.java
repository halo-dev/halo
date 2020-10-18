package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.Email;
import run.halo.app.model.entity.Link;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Email repository.
 *
 * @author johnniang
 */
public interface EmailRepository extends BaseRepository<Email, Integer> {

    /**
     * List all email.
     *
     * @return a list of email
     */
    @Query(value = "select distinct e.value from Email e")
    List<String> findAllEmails();

    /**
     * Exists by email.
     *
     * @param value
     * @return
     */
    boolean existsByValue(String value);

    /**
     * Exists by email.
     *
     * @param value
     * @return
     */
    Optional<Email> getByValue(String value);
}
