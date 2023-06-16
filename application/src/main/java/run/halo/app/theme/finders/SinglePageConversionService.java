package run.halo.app.theme.finders;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.theme.ReactiveSinglePageContentHandler;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * A service that converts {@link SinglePage} to {@link SinglePageVo}.
 *
 * @author guqing
 * @since 2.6.0
 */
public interface SinglePageConversionService {

    /**
     * Converts the given {@link SinglePage} to {@link SinglePageVo} and populate content by
     * given snapshot name.
     *
     * @param singlePage the single page must not be null
     * @param snapshotName the snapshot name to get content must not be blank
     * @return the converted single page vo
     * @see #convertToVo(SinglePage)
     */
    Mono<SinglePageVo> convertToVo(SinglePage singlePage, String snapshotName);

    /**
     * Converts the given {@link SinglePage} to {@link SinglePageVo}.
     * <p>This method will query the additional information of the {@link SinglePageVo} needed to
     * populate.</p>
     * <p>This method will try to find {@link ReactiveSinglePageContentHandler}s to extend the
     * content.</p>
     *
     * @param singlePage the single page must not be null
     * @return the converted single page vo
     * @see #getContent(String)
     */
    Mono<SinglePageVo> convertToVo(@NonNull SinglePage singlePage);

    /**
     * Gets content by given page name.
     * <p>This method will get released content by page name and try to find
     * {@link ReactiveSinglePageContentHandler}s to extend the content.</p>
     *
     * @param pageName page name must not be blank
     * @return content of the specified page
     * @since 2.7.0
     */
    Mono<ContentVo> getContent(String pageName);

    Mono<ListedSinglePageVo> convertToListedVo(SinglePage singlePage);
}
