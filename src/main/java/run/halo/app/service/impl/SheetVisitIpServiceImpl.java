package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.model.entity.SheetVisitIp;
import run.halo.app.repository.SheetVisitIpRepository;
import run.halo.app.service.SheetVisitIpService;

/**
 * Sheet visit ip address service implementation.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Service
public class SheetVisitIpServiceImpl extends BasePostVisitIpServiceImpl<SheetVisitIp> implements SheetVisitIpService {

    protected SheetVisitIpServiceImpl(SheetVisitIpRepository sheetVisitIpRepository) {
        super(sheetVisitIpRepository);
    }

}
