package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.PostVisitIp;
import run.halo.app.repository.PostVisitIpRepository;
import run.halo.app.service.PostVisitIpService;

/**
 * Post visit ip address service implementation.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Service
public class PostVisitIpServiceImpl extends BasePostVisitIpServiceImpl<PostVisitIp> implements PostVisitIpService {

    protected PostVisitIpServiceImpl(PostVisitIpRepository postVisitIpRepository) {
        super(postVisitIpRepository);
    }
}
