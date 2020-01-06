package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import run.halo.app.model.entity.RequestRecord;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * RequestRecord entity.
 *
 * @author ijkzen
 */
@Repository
public interface RequestRecordRepository extends JpaRepository<RequestRecord, Long> {

    List<RequestRecord> findRequestRecordsByCity(String city);

    Optional<RequestRecord> findFirstByIpAndCityNotContaining(String ip, String city);
}
