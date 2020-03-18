//package run.halo.app.controller.admin.api;
//
//import io.swagger.annotations.ApiOperation;
//import org.springframework.web.bind.annotation.*;
//import run.halo.app.service.DataProcessService;
//import run.halo.app.service.ThemeSettingService;
//
///**
// * @author ryanwang
// * @date 2019-12-29
// */
//@RestController
//@RequestMapping("/api/admin/data/process")
//public class DataProcessController {
//
//    private final DataProcessService dataProcessService;
//
//    private final ThemeSettingService themeSettingService;
//
//    public DataProcessController(DataProcessService dataProcessService,
//                                 ThemeSettingService themeSettingService) {
//        this.dataProcessService = dataProcessService;
//        this.themeSettingService = themeSettingService;
//    }
//
//    @PutMapping("url/replace")
//    @ApiOperation("Replace url in all table.")
//    public void replaceUrl(@RequestParam("oldUrl") String oldUrl,
//                           @RequestParam("newUrl") String newUrl) {
//        dataProcessService.replaceAllUrl(oldUrl, newUrl);
//    }
//
//    @DeleteMapping("themes/settings/inactivated")
//    @ApiOperation("Delete inactivated theme settings.")
//    public void deleteInactivatedThemeSettings() {
//        themeSettingService.deleteInactivated();
//    }
//
//    @DeleteMapping("tags/unused")
//    @ApiOperation("Delete unused tags")
//    public void deleteUnusedTags() {
//        // TODO
//    }
//
//    @DeleteMapping("categories/unused")
//    @ApiOperation("Delete unused categories")
//    public void deleteUnusedCategories() {
//        // TODO
//    }
//}
