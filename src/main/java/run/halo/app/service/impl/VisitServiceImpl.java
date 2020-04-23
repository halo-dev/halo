package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import run.halo.app.model.entity.Visit;
import run.halo.app.repository.VisitRepository;
import run.halo.app.service.VisitService;
import run.halo.app.service.base.AbstractCrudService;

import java.util.Calendar;
import java.util.Optional;

/**
 * VisitService implementation class.
 *
 * @author chao19991005
 * @date 2019-03-14
 */
@Slf4j
@Service
public class VisitServiceImpl extends AbstractCrudService<Visit, Integer> implements VisitService {
    private final VisitRepository visitRepository;

    public VisitServiceImpl(VisitRepository visitRepository) {
        super(visitRepository);
        this.visitRepository = visitRepository;
    }


    @Override
    @Transactional
    public Visit create(Visit visit) {
        Assert.notNull(visit, "Category to create must not be null");
        return super.create(visit);
    }

    @Override
    public long countVisitToday() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        return Optional.ofNullable(visitRepository.countVisitToday(day,month + 1,year)).orElse(0L);
    }

    @Override
    public long countVisitYesterday() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int maxDay = 0;
        if (month - 1 == 1 || month - 1 == 3 || month - 1 == 5 || month - 1 == 7 || month - 1 == 8 || month - 1 == 10 || month - 1 == 12)
            maxDay = 31;
        else if (month - 1 == 4 || month - 1 == 6 || month - 1 == 9 || month - 1 == 11)
            maxDay = 30;
        else {
            if (year % 4 != 0) maxDay = 28;
            else maxDay = 29;
        }
        if (day - 1 != 0)
            return Optional.ofNullable(visitRepository.countVisitYesterday1(day,month + 1,year)).orElse(0L);
        else
            return Optional.ofNullable(visitRepository.countVisitYesterday2(day,month + 1,year)).orElse(0L);
    }

    @Override
    public long countVisitThisMonth() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        return Optional.ofNullable(visitRepository.countVisitThisMonth(month + 1,year)).orElse(0L);
    }

    @Override
    public long countVisitThisYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return Optional.ofNullable(visitRepository.countVisitThisYear(year)).orElse(0L);
    }
}
