package run.halo.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.event.StaticStorageChangedEvent;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static run.halo.app.utils.HaloUtils.URL_SEPARATOR;
import static run.halo.app.utils.HaloUtils.ensureBoth;

/**
 * @author ryanwang
 * @date 2020-03-24
 */
@Slf4j
public class HaloRequestMappingHandlerMapping extends RequestMappingHandlerMapping
        implements ApplicationListener<StaticStorageChangedEvent> {

    private final Set<String> blackPatterns = new HashSet<>(16);

    private final PathMatcher pathMatcher;

    private final HaloProperties haloProperties;

    public HaloRequestMappingHandlerMapping(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
        this.initBlackPatterns();
        pathMatcher = new AntPathMatcher();
    }

    @Override
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        log.debug("Looking path: [{}]", lookupPath);
        for (String blackPattern : blackPatterns) {
            if (this.pathMatcher.match(blackPattern, lookupPath)) {
                log.debug("Skipped path [{}] with pattern: [{}]", lookupPath, blackPattern);
                return null;
            }
        }
        return super.lookupHandlerMethod(lookupPath, request);
    }

    private void initBlackPatterns() {
        String uploadUrlPattern = ensureBoth(haloProperties.getUploadUrlPrefix(), URL_SEPARATOR) + "**";
        String adminPathPattern = ensureBoth(haloProperties.getAdminPath(), URL_SEPARATOR) + "?*/**";

        blackPatterns.add("/themes/**");
        blackPatterns.add("/js/**");
        blackPatterns.add("/images/**");
        blackPatterns.add("/fonts/**");
        blackPatterns.add("/css/**");
        blackPatterns.add("/assets/**");
        blackPatterns.add("/color.less");
        blackPatterns.add("/swagger-ui.html");
        blackPatterns.add("/swagger-ui/**");
        blackPatterns.add("/csrf");
        blackPatterns.add("/webjars/**");
        blackPatterns.add(uploadUrlPattern);
        blackPatterns.add(adminPathPattern);
    }

    @Override
    public void onApplicationEvent(StaticStorageChangedEvent event) {
        Path staticPath = event.getStaticPath();
        try (Stream<Path> rootPathStream = Files.list(staticPath)) {
            synchronized (this) {
                blackPatterns.clear();
                initBlackPatterns();
                rootPathStream.forEach(rootPath -> {
                            if (Files.isDirectory(rootPath)) {
                                String directoryPattern = "/" + rootPath.getFileName().toString() + "/**";
                                blackPatterns.add(directoryPattern);
                                log.debug("Exclude for folder path pattern: [{}]", directoryPattern);
                            } else {
                                String pathPattern = "/" + rootPath.getFileName().toString();
                                blackPatterns.add(pathPattern);
                                log.debug("Exclude for file path pattern: [{}]", pathPattern);
                            }
                        }
                );
            }
        } catch (IOException e) {
            log.error("Failed to refresh static directory mapping", e);
        }
    }

}
