package run.halo.app.utils;

import lombok.Data;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import run.halo.app.model.support.HaloConst;

import java.io.IOException;

/**
 * @author Holldean
 * @date 2020-5-12
 */
public class IpUtils {

    private static DbSearcher DB_SEARCHER;

    static {
        ClassPathResource cpr = new ClassPathResource(HaloConst.IP2REGION_DATABASE_PATH);
        try {
            byte[] dbBinStr = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            DB_SEARCHER = new DbSearcher(new DbConfig(), dbBinStr);
        } catch (DbMakerConfigException | IOException e) {
            e.printStackTrace();
        }
    }

    public static IpRegion getRegion(String ipAddress) throws IOException {
        DataBlock dataBlock = DB_SEARCHER.memorySearch(ipAddress);
        String[] details = dataBlock.toString().split("\\|");
        return new IpRegion(dataBlock.getCityId(), details[1], details[3], details[4], details[5]);
    }

    public static String processProvinceName(String province) {
        if (province.endsWith("维吾尔自治区")) {
            return province.substring(0, province.length() - 6);
        } else if (province.endsWith("回族自治区") || province.endsWith("特别行政区")) {
            return province.substring(0, province.length() - 5);
        } else if (province.endsWith("自治区")) {
            return province.substring(0, province.length() - 3);
        } else if (province.endsWith("省") || province.endsWith("市")) {
            return province.substring(0, province.length() - 1);
        } else {
            return province;
        }
    }

    @Data
    public static class IpRegion {
        private final Integer cityId;
        private final String country;
        private final String province;
        private final String city;
        private final String isp;

        public IpRegion(Integer cityId, String country, String province, String city, String isp) {
            if (cityId == 0) {
                this.cityId = null;
            } else {
                this.cityId = cityId;
            }

            if (country.equals("0")) {
                this.country = "";
            } else {
                this.country = country;
            }

            if (province.equals("0")) {
                this.province = "";
            } else {
                this.province = processProvinceName(province);
            }

            if (city.equals("0") || city.equals("内网IP")) {
                this.city = "";
            } else {
                this.city = city;
            }

            if (isp.equals("0")) {
                this.isp = "";
            } else {
                this.isp = isp;
            }
        }

        @Override
        public String toString() {
            return this.country + this.province + this.city + this.isp;
        }
    }
}
