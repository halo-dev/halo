package cc.ryanc.halo.listener;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.utils.HaloUtils;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

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

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // save halo version to database
        // TODO Complete cache option value
        // TODO Complete load active theme
        // TODO Complete load option
        // TODO Load themes
        // TODO Load owo

        this.printStartInfo();
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
}
