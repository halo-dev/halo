package run.halo.app.handler.staticdeploy;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import run.halo.app.model.enums.StaticDeployType;
import run.halo.app.model.properties.NetlifyStaticDeployProperties;
import run.halo.app.service.OptionService;
import run.halo.app.service.StaticPageService;

import java.nio.file.Path;

/**
 * Netlify deploy handler.
 *
 * @author ryanwang
 * @date 2019-12-26
 */
@Slf4j
@Component
public class NetlifyStaticDeployHandler implements StaticDeployHandler {

    private final static String DEPLOY_API = "https://api.netlify.com/api/v1/sites/%s/deploys";

    private final OptionService optionService;

    private final RestTemplate httpsRestTemplate;

    private final StaticPageService staticPageService;

    public NetlifyStaticDeployHandler(OptionService optionService,
                                      RestTemplate httpsRestTemplate,
                                      StaticPageService staticPageService) {
        this.optionService = optionService;
        this.httpsRestTemplate = httpsRestTemplate;
        this.staticPageService = staticPageService;
    }

    @Override
    public void deploy() {
        String domain = optionService.getByPropertyOfNonNull(NetlifyStaticDeployProperties.NETLIFY_DOMAIN).toString();
        String siteId = optionService.getByPropertyOfNonNull(NetlifyStaticDeployProperties.NETLIFY_SITE_ID).toString();
        String token = optionService.getByPropertyOfNonNull(NetlifyStaticDeployProperties.NETLIFY_TOKEN).toString();

        HttpHeaders headers = new HttpHeaders();

        headers.set("Content-Type", "application/zip");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        Path path = staticPageService.zipStaticPagesDirectory();

        byte[] bytes = FileUtil.readBytes(path.toFile());

        HttpEntity<byte[]> httpEntity = new HttpEntity<>(bytes, headers);

        ResponseEntity<Object> responseEntity = httpsRestTemplate.postForEntity(String.format(DEPLOY_API, siteId), httpEntity, Object.class);
    }

    @Override
    public boolean supportType(StaticDeployType type) {
        return StaticDeployType.NETLIFY.equals(type);
    }
}
