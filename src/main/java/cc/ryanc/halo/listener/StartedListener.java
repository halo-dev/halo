package cc.ryanc.halo.listener;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.model.support.HaloConst;
import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.utils.ThemeUtils;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * <pre>
 *     应用启动完成后所执行的方法
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/12/5
 */
@Slf4j
@Configuration
public class StartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private freemarker.template.Configuration configuration;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HaloProperties haloProperties;

    @Autowired
    private OptionService optionService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // save halo version to database
        // TODO Complete cache option value
        this.cacheOptions();
        this.cacheThemes();
        this.cacheOwo();
        this.getActiveTheme();
        this.printStartInfo();
    }

    /**
     * Cache options to map
     */
    private void cacheOptions() {
        HaloConst.OPTIONS = optionService.listOptions();
    }

    /**
     * Cache themes to map
     */
    private void cacheThemes() {
        final List<Theme> themes = ThemeUtils.getThemes();
        if (null != themes) {
            HaloConst.THEMES = themes;
        }
    }

    /**
     * Get active theme
     */
    private void getActiveTheme() {
        final String themeValue = optionService.getByKey("theme");
        if (StrUtil.isNotEmpty(themeValue) && !StrUtil.equals(themeValue, null)) {
            BaseContentController.THEME = themeValue;
        } else {
            BaseContentController.THEME = "anatole";
        }
        try {
            configuration.setSharedVariable("themeName", BaseContentController.THEME);
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }

    private void printStartInfo() {
        // Get server port
        String serverPort = applicationContext.getEnvironment().getProperty("server.port");

        String blogUrl = getBlogUrl();

        log.info("Halo started at    {}", blogUrl);
        log.info("Halo admin is at   {}/admin", blogUrl);
        if (!haloProperties.getDocDisabled()) {
            log.debug("Halo doc enable at {}/swagger-ui.html", blogUrl);
        }
    }

    /**
     * Gets blog url.
     *
     * @return blog url (If blog url isn't present, current machine IP address will be default)
     */
    private String getBlogUrl() {
        // Get server port
        String serverPort = applicationContext.getEnvironment().getProperty("server.port");

        String blogUrl = null;

        if (StrUtil.isNotBlank(blogUrl)) {
            blogUrl = StrUtil.removeSuffix(blogUrl, "/");
        } else {
            blogUrl = String.format("http://%s:%s", HaloUtils.getMachineIP(), serverPort);
        }

        return blogUrl;
    }

    /**
     * Cache Owo
     */
    private void cacheOwo() {
        try {
            File file = new File(ResourceUtils.getURL("classpath:").getPath(), "static/halo-common/OwO/OwO.path.json");
            HaloConst.OWO = JSONUtil.readJSONObject(file, Charset.forName("UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
