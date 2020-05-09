package run.halo.app.service.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import run.halo.app.model.entity.Visit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class VisitServiceImplTest {
    @Autowired
    private VisitServiceImpl visitService;

    private static DatabaseReader reader;

    /**
     * 根据ip获取国家对象,不存在则返回null
     * @param ip
     * @return
     */
    public static Country getCountry(String ip){
        try{
            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = reader.country(ipAddress);
            Country country = response.getCountry();
            return country;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 根据ip获取国家代码,不存在则返回null
     * @param ip
     * @return
     */
    public static String getCountryCode(String ip){
        Country country = getCountry(ip);
        return country != null ? country.getIsoCode() : null;
    }

    /**
     * 根据ip获取国家名称,不存在则返回null
     * @param ip
     * @return
     */
    public static String getCountryName(String ip){
        Country country = getCountry(ip);
        return country != null ? country.getName() : null;
    }

    @Test
    public void dayCountTest() {
        Visit visit = new Visit();
        visit.setPostId(1);
        visit.setVisitId(1);

        long today = visitService.countVisitToday();
        long yesterday = visitService.countVisitYesterday();
        long month = visitService.countVisitThisMonth();
        long year = visitService.countVisitThisYear();
        //List<String> district = visitService.findAllVisitorDistrict();
        log.debug(String.valueOf(today));
        log.debug(String.valueOf(yesterday));
        log.debug(String.valueOf(month));
        log.debug(String.valueOf(year));

    }
}
