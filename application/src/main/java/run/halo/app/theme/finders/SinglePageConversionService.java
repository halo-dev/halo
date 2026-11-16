package run.halo.app.theme.finders;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
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
     * Converts the given {@link SinglePage} to {@link SinglePageVo} and populate content by given snapshot name.
     *
     * @param singlePage the single page must not be null
     * @param snapshotName the snapshot name to get content must not be blank
     * @return the converted single page vo
     * @see #convertToVo(SinglePage)
     */
    Mono<SinglePageVo> convertToVo(SinglePage singlePage, String snapshotName);

    /**
     * Converts the given {@link SinglePage} to {@link SinglePageVo}.
     *
     * <p>This method will query the additional information of the {@link SinglePageVo} needed to populate.
     *
     * <p>This method will try to find {@link ReactiveSinglePageContentHandler}s to extend the content.
     *
     * @param singlePage the single page must not be null
     * @return the converted single page vo
     * @see #getContent(SinglePage)
     */
    Mono<SinglePageVo> convertToVo(SinglePage singlePage);

    /**
     * Gets released content by page name. Callers requiring a visibility check must use the public finder.
     *
     * @param pageName page name must not be blank
     * @return content of the specified page
     * @since 2.7.0
     */
    Mono<ContentVo> getContent(String pageName);

    /**
     * Gets released content of an authorized page.
     *
     * <p>Callers must check page visibility before invoking this method. The content is processed by applicable
     * {@link ReactiveSinglePageContentHandler}s.
     *
     * @param singlePage page already admitted by the caller's visibility rules
     * @return content of the specified page
     * @since 2.28.0
     */
    default Mono<ContentVo> getContent(SinglePage singlePage) {
        return getContent(singlePage.getMetadata().getName());
    }

    Mono<ListedSinglePageVo> convertToListedVo(SinglePage singlePage);

    Mono<ListResult<ListedSinglePageVo>> listBy(ListOptions listOptions, PageRequest pageRequest);
}
