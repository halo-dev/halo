package run.halo.app.localization;

import java.time.Instant;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;

/**
 * Read-only catalog and presentation adapter for non-Extension data, registered by its owning plugin.
 *
 * <p>Implementations use their existing repository and return detached display DTOs. They enforce read authorization
 * and public source eligibility before exposing a result, including when the current user is an administrator. Drafts,
 * private records and operational data must not be returned. Listing filters and counts must use source values and the
 * same public-read conditions. No database write hooks, localization or machine translation belong in this adapter. A
 * fresh fetch is required when a translation job executes or a suggestion is adopted.
 *
 * @param <T> source display DTO
 * @since 2.28.0
 */
public interface LocalizableResourceSource<T> extends ExtensionPoint {
    /** Single schema declaration shared by catalog, editor, jobs and public display. */
    LocalizableResourceDescriptor<T> descriptor();

    /** Loads a fresh authorized, publicly eligible source snapshot, or completes empty when absent or inaccessible. */
    Mono<T> fetch(String id);

    /** Reads an authorized page using source ordering and totals. */
    Mono<ListResult<T>> list(PageRequest page);

    /** Optional revision used for optimistic catalog/editor checks, never a translated value. */
    default String revision(T source) {
        return "";
    }

    /** Optional source update time for catalog ordering and display. */
    default Instant updatedAt(T source) {
        return null;
    }

    /** Optional original public URL, owned by the domain router. */
    default String permalink(T source) {
        return "";
    }
}
