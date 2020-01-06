package run.halo.app.service;

import run.halo.app.model.entity.RequestRecord;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

public interface RequestRecordService {
    /**
     * save full info about this request to database
     * @param request
     */
    void save(HttpServletRequest request);

    /**
     * save complete record with location info to database
     * @param record
     */
    void save(RequestRecord record);

    /**
     * get record which has location info by ip
     * @param ip
     * @return
     */
    Optional<RequestRecord> getRecordByIp(String ip);

    /**
     * get record list which do not have location info
     * @return
     */
    List<RequestRecord> getIncompleteRecordList();
}
