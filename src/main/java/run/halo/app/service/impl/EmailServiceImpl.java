package run.halo.app.service.impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.entity.Email;
import run.halo.app.model.params.EmailParam;
import run.halo.app.model.vo.EmailVO;
import run.halo.app.repository.EmailRepository;
import run.halo.app.repository.PostEmailRepository;
import run.halo.app.service.EmailService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostEmailService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

/**
 * EmailService implementation class
 *
 * @author ryanwang
 * @date 2019-03-14
 */
@Service
public class EmailServiceImpl extends AbstractCrudService<Email, Integer> implements EmailService {

    private final EmailRepository emailRepository;

    private final OptionService optionService;

    private final PostEmailService postEmailService;

    protected EmailServiceImpl(EmailRepository emailRepository,
                               OptionService optionService,
                               PostEmailService postEmailService) {
        super(emailRepository);
        this.emailRepository = emailRepository;
        this.optionService = optionService;
        this.postEmailService = postEmailService;
    }

    /**
     * List Email dtos.
     *
     * @param sort sort
     * @return all emails
     */
    @Override
    public @NotNull List<EmailDTO> listDTOs(@NotNull Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return convertTo(listAll(sort));
    }

    @Override
    public @NotNull List<EmailVO> listEmailVOs(@NotNull Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all emails
        List<EmailDTO> emails = listDTOs(sort);

        // Get email
        Set<String> email = ServiceUtils.fetchProperty(emails, EmailDTO::getValue);

        // Convert to email link list map (Key: email, value: link list)
        Map<String, List<EmailDTO>> emailListMap = ServiceUtils.convertToListMap(email, emails, EmailDTO::getValue);

        List<EmailVO> result = new LinkedList<>();

        // Wrap link team vo list
        emailListMap.forEach((emailKey, emailValue) -> {
            // Build link team vo
            EmailVO emailVO = new EmailVO();
            emailVO.setEmail(emailKey);
            emailVO.setEmails(emailValue);
            // Add it to result
            result.add(emailVO);
        });
        return result;
    }

    /**
     * Creates Email by Email param.
     *
     * @param emailParam must not be null
     * @return create Email
     */
    @Override
    public @NotNull Email createBy(@NotNull EmailParam emailParam) {
        Assert.notNull(emailParam, "Email param must not be null");

        // Check the email
        boolean exist = existByEmail(emailParam.getValue());

        if (exist) {
            throw new AlreadyExistsException("邮箱 " + emailParam.getName() + " 已存在").setErrorData(emailParam.getValue());
        }

        return create(emailParam.convertTo());
    }

    /**
     * Updates Email by Email param.
     *
     * @param id         must not be null
     * @param emailParam must not be null
     * @return updated Email
     */
    @Override
    public Email updateBy(Integer id, EmailParam emailParam) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(emailParam, "Email param must not be null");

        // Check the email
        boolean exist = emailRepository.existsByValue(emailParam.getValue());
        if (exist) {
            throw new AlreadyExistsException("邮箱 " + emailParam.getName() + " 已存在").setErrorData(emailParam.getName());
        }

        Email email = getById(id);
        emailParam.update(email);

        return update(email);
    }

    /**
     * Exists by email.
     *
     * @param emailValue must not be blank
     * @return true if exists; false otherwise
     */
    @Override
    public boolean existByEmail(String emailValue) {
        Assert.hasText(emailValue, "email must not be blank");
        Email email = new Email();
        email.setValue(emailValue);

        return emailRepository.exists(Example.of(email));
    }

    /**
     * List all email.
     *
     * @return a list of email.
     */
    @Override
    public List<String> listAllEmails() {
        return emailRepository.findAllEmails();
    }


    /**
     * List all Email teams by random
     *
     * @return a list of teams by random
     */
    @Override
    public List<Email> listAllByRandom() {

        List<Email> allEmail = emailRepository.findAll();
        Collections.shuffle(allEmail);
        return allEmail;
    }

    @Override
    public EmailDTO convertTo(Email email) {
        Assert.notNull(email, "Email must not be null");

        EmailDTO emailDTO = new EmailDTO().convertFrom(email);

        StringBuilder fullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            fullPath.append(optionService.getBlogBaseUrl());
        }

        fullPath.append(URL_SEPARATOR)
                .append(optionService.getTagsPrefix())
                .append(URL_SEPARATOR)
                .append(email.getId())
                .append(optionService.getPathSuffix());

        emailDTO.setFullPath(fullPath.toString());

        return emailDTO;
    }

    @NonNull
    @Override
    public List<EmailDTO> convertTo(@Nullable List<Email> emails) {
        if (CollectionUtils.isEmpty(emails)) {
            return Collections.emptyList();
        }
        return emails.stream()
                .map(email -> (EmailDTO) new EmailDTO().convertFrom(email))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePermanently(@Nullable Integer id) {
        this.removeById(id);
        postEmailService.removeByEmailId(id);
    }
}
