//package run.halo.app.controller.admin.api;
//
//import cn.hutool.core.io.FileUtil;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//import run.halo.app.model.properties.NetlifyStaticDeployProperties;
//import run.halo.app.model.support.StaticPageFile;
//import run.halo.app.service.OptionService;
//import run.halo.app.service.StaticPageService;
//
//import java.io.FileNotFoundException;
//import java.nio.file.Path;
//import java.util.Collections;
//import java.util.List;
//
///**
// * Static page controller.
// *
// * @author ryanwang
// * @date 2019-12-25
// */
//@RestController
//@RequestMapping("/api/admin/static_page")
//public class StaticPageController {
//
//    private final static String DEPLOY_API = "https://api.netlify.com/api/v1/sites/%s/deploys";
//
//    private final OptionService optionService;
//
//    private final RestTemplate httpsRestTemplate;
//
//    private final StaticPageService staticPageService;
//
//    public StaticPageController(StaticPageService staticPageService,
//                                OptionService optionService,
//                                RestTemplate httpsRestTemplate) {
//        this.staticPageService = staticPageService;
//        this.optionService = optionService;
//        this.httpsRestTemplate = httpsRestTemplate;
//
//        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
//        this.httpsRestTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
//    }
//
//    @GetMapping
//    @ApiOperation("List static page files.")
//    public List<StaticPageFile> list() {
//        return staticPageService.listFile();
//    }
//
//    @GetMapping("generate")
//    @ApiOperation("Generate static page files.")
//    public void generate() {
//        staticPageService.generate();
//    }
//
//    @PostMapping("deploy")
//    @ApiOperation("Deploy static page to remove platform")
//    public void deploy() {
//        staticPageService.deploy();
//    }
//
//    @GetMapping("netlify")
//    public void testNetlify() throws FileNotFoundException {
//        String domain = optionService.getByPropertyOfNonNull(NetlifyStaticDeployProperties.NETLIFY_DOMAIN).toString();
//        String siteId = optionService.getByPropertyOfNonNull(NetlifyStaticDeployProperties.NETLIFY_SITE_ID).toString();
//        String token = optionService.getByPropertyOfNonNull(NetlifyStaticDeployProperties.NETLIFY_TOKEN).toString();
//
//        HttpHeaders headers = new HttpHeaders();
//
//        headers.set("Content-Type", "application/zip");
//        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//
//        Path path = staticPageService.zipStaticPagesDirectory();
//
//        byte[] bytes = FileUtil.readBytes(path.toFile());
//
//        HttpEntity<byte[]> httpEntity = new HttpEntity<>(bytes, headers);
//
//        ResponseEntity<Object> responseEntity = httpsRestTemplate.postForEntity(String.format(DEPLOY_API, siteId), httpEntity, Object.class);
//    }
//}
