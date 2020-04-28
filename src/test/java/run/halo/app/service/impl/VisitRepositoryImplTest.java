package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class VisitRepositoryImplTest {
    @Autowired
    private VisitServiceImpl visitService;

    @Test
    public void dayCountTest() {
        long today = visitService.countVisitToday();
        long yesterday = visitService.countVisitYesterday();
        long month = visitService.countVisitThisMonth();
        long year = visitService.countVisitThisYear();
        log.debug(String.valueOf(today));
        log.debug(String.valueOf(yesterday));
        log.debug(String.valueOf(month));
        log.debug(String.valueOf(year));
    }
}
