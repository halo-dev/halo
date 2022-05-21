package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.VersionInfoDTO;
import run.halo.app.service.HaloVersionCtrlService;

/**
 * Version Control Controller
 *
 * @author Chen_Kunqiu
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/version")
public class VersionCtrlController {
    @Autowired
    HaloVersionCtrlService versionCtrlService;

    @GetMapping("releases")
    @ApiOperation("Lists all release info of halo jar")
    List<VersionInfoDTO> getAllReleaseInfo() {
        return versionCtrlService.getAllReleasesInfo();
    }

    @GetMapping("releases/latest")
    @ApiOperation("Lists the latest release info of halo")
    VersionInfoDTO getLatestReleaseInfo() {
        return versionCtrlService.getLatestReleaseInfo();
    }

    @GetMapping("releases/tags/{tagName:.+}")
    @ApiOperation("Lists the release info with specified version")
    VersionInfoDTO getReleaseInfo(@PathVariable(name = "tagName") String tagName) {
        return versionCtrlService.getReleaseInfoByTag(tagName);
    }

    @GetMapping("download/latest")
    @ApiOperation("Downloads the latest release jar")
    String downloadLatest() {
        versionCtrlService.downloadLatestJar();
        return "success";
    }

    @GetMapping("download/tags/{tagName:.+}")
    @ApiOperation("Downloads the specified jar")
    String download(@PathVariable(name = "tagName") String tagName) {
        versionCtrlService.downloadSpecifiedJarToRepo(tagName);
        return "success";
    }

    @GetMapping("switch/latest")
    @ApiOperation("Switch halo to latest version")
    String switchLatestVersion() {
        versionCtrlService.switchLatest();
        return "success";
    }

    @GetMapping("switch/tags/{tagName:.+}")
    @ApiOperation("Switch halo to specified version")
    String switchVersion(@PathVariable(name = "tagName") String tagName) {
        versionCtrlService.switchVersion(tagName);
        return "success";
    }

    @GetMapping("downloadswitch/latest")
    @ApiOperation("Downloads latest version to local and switch halo to it")
    String downloadSwitchLatestVersion() {
        versionCtrlService.downloadAndSwitchLatest();
        return "success";
    }

    @GetMapping("downloadswitch/tags/{tagName:.+}")
    @ApiOperation("Downloads specified version to local and switch halo to it")
    String downLoadSwitchVersion(@PathVariable(name = "tagName") String tagName) {
        versionCtrlService.downloadAndSwitch(tagName);
        return "success";
    }


}
