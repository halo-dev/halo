package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.model.entity.BaseVisitIp;
import run.halo.app.repository.base.BaseVisitIpRepository;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BasePostVisitIpService;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Base post visit ip address service implementation.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Slf4j
public abstract class BasePostVisitIpServiceImpl<VISITIP extends BaseVisitIp> extends AbstractCrudService<VISITIP, Long> implements BasePostVisitIpService<VISITIP> {

    private final BaseVisitIpRepository<VISITIP> baseVisitIpRepository;

    protected BasePostVisitIpServiceImpl(BaseVisitIpRepository<VISITIP> baseVisitIpRepository) {
        super(baseVisitIpRepository);
        this.baseVisitIpRepository = baseVisitIpRepository;
    }

    @Override
    @Transactional
    @NotNull
    public VISITIP create(@NonNull VISITIP visitIp) {
        return super.create(visitIp);
    }

    @Override
    @Transactional
    @NotNull
    public VISITIP createBy(VISITIP visitIp) {
        Assert.notNull(visitIp, "BasePostVisitIp to create must not be null");
        return create(visitIp);
    }

    public List<VISITIP> getIpRecordsByPostId(@NonNull Integer postId) {
        return baseVisitIpRepository.findAllByPostId(postId);
    }

    public boolean checkIpRecordByPostIdAndIp(@NonNull Integer postId, @NonNull String requestIp) {
        List<VISITIP> ipList = baseVisitIpRepository.findByPostIdAndIp(postId, requestIp);

        log.debug("Check if ip [{}] Previously requested for article [{}], result: [{}]", requestIp, postId, ipList.isEmpty());
        log.debug("Current ip database for post [{}]: {}", postId, baseVisitIpRepository.findAllByPostId(postId));

        return !ipList.isEmpty();
    }
}
