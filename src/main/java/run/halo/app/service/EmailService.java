package run.halo.app.service;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.dto.TagWithPostCountDTO;
import run.halo.app.model.entity.Email;
import run.halo.app.model.params.EmailParam;
import run.halo.app.model.vo.EmailVO;
import run.halo.app.model.vo.LinkTeamVO;
import run.halo.app.service.base.CrudService;

import java.util.List;

/**
 * Email service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
public interface EmailService extends CrudService<Email, Integer> {

    /**
     * List Email dtos.
     *
     * @param sort sort
     * @return all emails
     */
    @NonNull
    List<EmailDTO> listDTOs(@NonNull Sort sort);

    /**
     * Lists link team vos.
     *
     * @param sort must not be null
     * @return a list of link team vo
     */
    @NonNull
    List<EmailVO> listEmailVOs(@NonNull Sort sort);

    /**
     * Creates Email by Email param.
     *
     * @param emailParam must not be null
     * @return create Email
     */
    @NonNull
    Email createBy(@NonNull EmailParam emailParam);

    /**
     * Updates Email by Email param.
     *
     *
     * @param id must not be null
     * @param emailParam must not be null
     * @return updated Email
     */
    @NonNull
    Email updateBy(Integer id, @NonNull EmailParam emailParam);

    /**
     * Exists by email.
     *
     * @param email must not be blank
     * @return true if exists; false otherwise
     */
    boolean existByEmail(String email);

    /**
     * List all email.
     *
     * @return a list of email.
     */
    List<String> listAllEmails();

    /**
     * List all Email teams by random
     *
     * @return a list of teams by random
     */
    @NonNull
    List<Email> listAllByRandom();


    /**
     * Converts to email dto.
     *
     * @param email email must not be null
     * @return email dto
     */
    @NonNull
    EmailDTO convertTo(@NonNull Email email);

    /**
     * Converts to email dtos.
     *
     * @param emails email list
     * @return a list of email output dto
     */
    @NonNull
    List<EmailDTO> convertTo(@Nullable List<Email> emails);

    /**
     * delete to email and post email.
     *
     * @param id email id
     * @return
     */
    void deletePermanently(@Nullable Integer id);
}
