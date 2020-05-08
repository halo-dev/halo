package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import run.halo.app.listener.post.PostVisitEventListener;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class VisitIpServiceImplTest {

    @Autowired
    private PostVisitIpServiceImpl postVisitIpService;

    @Autowired
    private PostVisitEventListener postVisitEventListener;

    public void recordsAmountTest(int postId, String requestIp, int amount) {
        Assert.assertEquals(amount, postVisitIpService.getIpRecordsByPostId(postId).size());
    }

    public void ipRecordsTest(int postId) {
        log.debug("Current ip records of post id [{}] are: [{}]", postId, postVisitIpService.getIpRecordsByPostId(postId));
    }

    @Test
    public void visitWithSameIpTest() {
        int postId = 123;
        String requestIp = "127.0.0.1";

        ipRecordsTest(postId);
        recordsAmountTest(postId, requestIp, 0);
        Assert.assertFalse(postVisitEventListener.checkIpRecord(123, "127.0.0.1"));

        ipRecordsTest(postId);
        Assert.assertTrue(postVisitIpService.checkIpRecordByPostIdAndIp(postId, requestIp));
        recordsAmountTest(postId, requestIp, 1);

        ipRecordsTest(postId);
        Assert.assertTrue(postVisitIpService.checkIpRecordByPostIdAndIp(postId, requestIp));
        recordsAmountTest(postId, requestIp, 1);

        ipRecordsTest(postId);
    }

}
