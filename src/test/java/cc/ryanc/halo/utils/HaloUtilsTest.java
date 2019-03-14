package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.support.HaloConst;
import com.qiniu.common.Zone;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HaloUtils test.
 *
 * @author johnniang
 */
public class HaloUtilsTest {

//    @Test
//    public void getDefaultPageSizeTest() {
//        // Get page size
//        int pageSize = HaloUtils.getDefaultPageSize();
//        assertEquals(HaloUtils.DEFAULT_PAGE_SIZE, pageSize);
//
//        // Cover the default page size
//        HaloConst.OPTIONS.put(BlogPropertiesEnum.INDEX_POSTS.getProp(), String.valueOf(5));
//
//        // Get page size again
//        pageSize = HaloUtils.getDefaultPageSize();
//        assertEquals(5, pageSize);
//    }

    @Test
    public void getDefaultQiniuZoneTest() {
        Zone zone = HaloUtils.getDefaultQiniuZone();
        assertEquals(Zone.autoZone().getRegion(), zone.getRegion());

        // Set zone manually
        HaloConst.OPTIONS.put("qiniu_zone", "z0");
        // Set zone manually
        zone = HaloUtils.getDefaultQiniuZone();
        assertEquals(Zone.zone0().getRegion(), zone.getRegion());

        // Set zone manually
        HaloConst.OPTIONS.put("qiniu_zone", "z1");
        // Set zone manually
        zone = HaloUtils.getDefaultQiniuZone();
        assertEquals(Zone.zone1().getRegion(), zone.getRegion());

        // Set zone manually
        HaloConst.OPTIONS.put("qiniu_zone", "z2");
        // Set zone manually
        zone = HaloUtils.getDefaultQiniuZone();
        assertEquals(Zone.zone2().getRegion(), zone.getRegion());

        // Set zone manually
        HaloConst.OPTIONS.put("qiniu_zone", "na0");
        // Set zone manually
        zone = HaloUtils.getDefaultQiniuZone();
        assertEquals(Zone.zoneNa0().getRegion(), zone.getRegion());

        // Set zone manually
        HaloConst.OPTIONS.put("qiniu_zone", "as0");
        // Set zone manually
        zone = HaloUtils.getDefaultQiniuZone();
        assertEquals(Zone.zoneAs0().getRegion(), zone.getRegion());
    }
}