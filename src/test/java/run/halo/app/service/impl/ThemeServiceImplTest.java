package run.halo.app.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import run.halo.app.handler.theme.config.support.ThemeProperty;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class ThemeServiceImplTest {

    @Autowired
    private ThemeServiceImpl themeService;

    @Test
    public void fetchBranchesTest(){
        List<ThemeProperty> themeProperties = themeService.fetchBranches("https://github.com/halo-dev/halo-theme-hux");
        Assert.assertNotNull(themeProperties);
        for (ThemeProperty themeProperty: themeProperties){
            System.out.println(themeProperty);
        }
    }

    @Test
    public void fetchBranchTest(){
        ThemeProperty themeProperty = themeService.fetchBranch("https://github.com/halo-dev/halo-theme-casper", "master");
        Assert.assertNotNull(themeProperty);
    }

    @Test
    public void fetchLatestReleaseTest(){
        ThemeProperty themeProperty = themeService.fetchLatestRelease("https://github.com/halo-dev/halo-theme-next");
        Assert.assertNotNull(themeProperty);
    }


}
