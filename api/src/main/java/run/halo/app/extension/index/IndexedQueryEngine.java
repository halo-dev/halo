package run.halo.app.extension.index;

import java.util.List;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;

/**
 * <p>An interface for querying indexed object records from the index store.</p>
 * <p>It provides a way to retrieve the object records by the given {@link GroupVersionKind} and
 * {@link ListOptions}, the final result will be ordered by the index what {@link ListOptions}
 * used and specified by the {@link PageRequest#getSort()}.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexedQueryEngine {

    /**
     * Page retrieve the object records by the given {@link GroupVersionKind} and
     * {@link ListOptions}.
     *
     * @param type the type of the object must exist in
     * {@link run.halo.app.extension.SchemeManager}.
     * @param options the list options to use for retrieving the object records.
     * @param page which page to retrieve and how large the page should be.
     * @return a collection of {@link Metadata#getName()} for the given page.
     */
    ListResult<String> retrieve(GroupVersionKind type, ListOptions options, PageRequest page);

    /**
     * Retrieve all the object records by the given {@link GroupVersionKind} and
     * {@link ListOptions}.
     *
     * @param type the type of the object must exist in {@link run.halo.app.extension.SchemeManager}
     * @param options the list options to use for retrieving the object records
     * @param sort the sort to use for retrieving the object records
     * @return a collection of {@link Metadata#getName()}
     */
    List<String> retrieveAll(GroupVersionKind type, ListOptions options, Sort sort);
}
