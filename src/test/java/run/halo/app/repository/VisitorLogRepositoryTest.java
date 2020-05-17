package run.halo.app.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import run.halo.app.model.entity.VisitorLog;
import run.halo.app.model.projection.VisitorLogDayCountProjection;
import run.halo.app.model.projection.VisitorLogMonthCountProjection;
import run.halo.app.model.projection.VisitorLogRegionCountProjection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
public class VisitorLogRepositoryTest {

    @Autowired
    private VisitorLogRepository visitorLogRepository;

    @Test
    public void findByAccessDateAndIpAddressTest() {
        // insert a ip log into database
        Date current = new Date();
        visitorLogRepository.save(new VisitorLog(current, "HELLO IP"));

        // test findByAccessDateAndIpAddress
        Assert.assertNotNull(visitorLogRepository.findByAccessDateAndIpAddress(current, "HELLO IP"));
        Assert.assertNull(visitorLogRepository.findByAccessDateAndIpAddress(current, "HELLO"));
    }

    @Test
    public void findByAccessDateAfter() {
        Calendar calendar = Calendar.getInstance();
        // get date in the last month
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date lastMonth = calendar.getTime();

        // get date in the last month plus one day
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date lastMonthPlusAnHour = calendar.getTime();

        // get date in the last month minus one day
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        Date lastMonthMinusAnHour = calendar.getTime();

        // insert two ip logs into database
        visitorLogRepository.save(new VisitorLog(lastMonthPlusAnHour, "HELLO IP1"));
        visitorLogRepository.save(new VisitorLog(lastMonthMinusAnHour, "HELLO IP2"));

        // test findByAccessDateAndIpAddress
        List<VisitorLog> result = visitorLogRepository.findByAccessDateAfter(lastMonth);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(lastMonthPlusAnHour, result.get(0).getAccessDate());
    }

    @Test
    public void countByAccessDateAfterGroupByDayTest() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        // get date in the last month
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date lastMonth = calendar.getTime();

        // get date of yesterday
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = calendar.getTime();

        // get date of today
        calendar.setTime(new Date());
        Date today = calendar.getTime();

        // insert three ip logs into database
        visitorLogRepository.save(new VisitorLog(yesterday, "192.168.1.1"));
        visitorLogRepository.save(new VisitorLog(yesterday, "192.168.1.2"));
        visitorLogRepository.save(new VisitorLog(today, "192.168.1.1"));

        // test findByAccessDateAndIpAddress
        List<VisitorLogDayCountProjection> result = visitorLogRepository.countByAccessDateAfterGroupByDay(lastMonth);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

        Assert.assertEquals(formater.format(yesterday), result.get(0).getDate().toString());
        Assert.assertEquals((Long) 2L, result.get(0).getCount());
        Assert.assertEquals(formater.format(today), result.get(1).getDate().toString());
        Assert.assertEquals((Long) 1L, result.get(1).getCount());
    }

    @Test
    public void countByAccessDateAfterGroupByCountryTest() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        // get date in the last month
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date lastMonth = calendar.getTime();

        // get date of yesterday
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = calendar.getTime();

        // get date of today
        calendar.setTime(new Date());
        Date today = calendar.getTime();

        // insert three ip logs into database
        VisitorLog logOne = new VisitorLog(yesterday, "192.168.1.1");
        logOne.setCountry("中国");
        VisitorLog logTwo = new VisitorLog(yesterday, "192.168.1.2");
        logTwo.setCountry("美国");
        VisitorLog logThree = new VisitorLog(today, "192.168.1.1");
        logThree.setCountry("中国");
        visitorLogRepository.save(logOne);
        visitorLogRepository.save(logTwo);
        visitorLogRepository.save(logThree);

        // test countByAccessDateAfterGroupByCountry
        List<VisitorLogRegionCountProjection> result = visitorLogRepository.countByAccessDateAfterGroupByCountry(lastMonth);

        Assert.assertEquals("中国", result.get(0).getRegion());
        Assert.assertEquals((Long) 2L, result.get(0).getCount());
        Assert.assertEquals("美国", result.get(1).getRegion());
        Assert.assertEquals((Long) 1L, result.get(1).getCount());
    }

    @Test
    public void countByAccessDateAfterGroupByChineseProvinceTest() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        // get date in the last month
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date lastMonth = calendar.getTime();

        // get date of yesterday
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = calendar.getTime();

        // get date of today
        calendar.setTime(new Date());
        Date today = calendar.getTime();

        // insert four ip logs into database
        VisitorLog logOne = new VisitorLog(yesterday, "192.168.1.1");
        logOne.setCountry("中国");
        logOne.setProvince("北京");
        VisitorLog logTwo = new VisitorLog(yesterday, "192.168.1.2");
        logTwo.setCountry("中国");
        logTwo.setProvince("北京");
        VisitorLog logThree = new VisitorLog(today, "192.168.1.1");
        logThree.setCountry("中国");
        logThree.setProvince("上海");
        VisitorLog logFour = new VisitorLog(today, "192.168.1.3");
        logFour.setCountry("美国");
        visitorLogRepository.save(logOne);
        visitorLogRepository.save(logTwo);
        visitorLogRepository.save(logThree);
        visitorLogRepository.save(logFour);

        // test countByAccessDateAfterGroupByCountry
        List<VisitorLogRegionCountProjection> result = visitorLogRepository.countByAccessDateAfterGroupByChineseProvince(lastMonth);

        Assert.assertEquals("上海", result.get(0).getRegion());
        Assert.assertEquals((Long) 1L, result.get(0).getCount());
        Assert.assertEquals("北京", result.get(1).getRegion());
        Assert.assertEquals((Long) 2L, result.get(1).getCount());
    }

    @Test
    public void countByAccessDateAfterGroupByMonthTest() {
        Calendar calendar = Calendar.getInstance();
        // get date in the last year
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);
        Date lastYear = calendar.getTime();

        // get date of last month
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date lastMonth = calendar.getTime();

        // get date of today
        calendar.setTime(new Date());
        Date today = calendar.getTime();

        // insert three ip logs into database
        VisitorLog logOne = new VisitorLog(lastMonth, "192.168.1.1");
        VisitorLog logTwo = new VisitorLog(today, "192.168.1.2");
        VisitorLog logThree = new VisitorLog(today, "192.168.1.1");
        visitorLogRepository.save(logOne);
        visitorLogRepository.save(logTwo);
        visitorLogRepository.save(logThree);

        List<VisitorLogMonthCountProjection> result = visitorLogRepository.countByAccessDateAfterGroupByMonth(lastYear);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals((Long) 1L, result.get(0).getCount());
        Assert.assertEquals((Long) 2L, result.get(1).getCount());
        calendar.setTime(lastMonth);
        Assert.assertEquals((Integer) (calendar.get(Calendar.MONTH) + 1), result.get(0).getMonth());
        calendar.setTime(today);
        Assert.assertEquals((Integer) (calendar.get(Calendar.MONTH) + 1), result.get(1).getMonth());
    }
}
