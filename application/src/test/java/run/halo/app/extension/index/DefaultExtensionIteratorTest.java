package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Extension;

/**
 * Tests for {@link DefaultExtensionIterator}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultExtensionIteratorTest {

    @Mock
    private ExtensionPaginatedLister<Extension> lister;

    @Test
    @SuppressWarnings("unchecked")
    void testConstructor_loadsData() {
        Page<Extension> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(mock(Extension.class)));
        when(page.hasNext()).thenReturn(true);
        when(page.nextPageable()).thenReturn(
            PageRequest.of(1, DefaultExtensionIterator.DEFAULT_PAGE_SIZE, Sort.by("name")));
        when(lister.list(any())).thenReturn(page);

        var iterator = new DefaultExtensionIterator<>(lister);

        assertThat(iterator.hasNext()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasNext_whenNextPageExists_loadsNextPage() {
        Page<Extension> page1 = mock(Page.class);
        when(page1.getContent()).thenReturn(List.of(mock(Extension.class)));
        when(page1.hasNext()).thenReturn(true);
        when(page1.nextPageable()).thenReturn(
            PageRequest.of(1, DefaultExtensionIterator.DEFAULT_PAGE_SIZE, Sort.by("name")));

        Page<Extension> page2 = mock(Page.class);
        when(page2.getContent()).thenReturn(List.of(mock(Extension.class)));
        when(page2.hasNext()).thenReturn(false);

        when(lister.list(any(Pageable.class))).thenReturn(page1, page2);

        var iterator = new DefaultExtensionIterator<>(lister);
        // consume first page
        iterator.next();

        // should load the next page
        assertThat(iterator.hasNext()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void next_whenNoNextElement_throwsException() {
        Page<Extension> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(mock(Extension.class)));
        when(page.hasNext()).thenReturn(false);
        when(lister.list(any())).thenReturn(page);

        var iterator = new DefaultExtensionIterator<>(lister);
        // consume only element
        iterator.next();

        assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }
}
