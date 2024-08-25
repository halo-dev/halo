package run.halo.app.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import run.halo.app.infra.exception.NotFoundException;

class CompositeTemplateResolverTest {

    @Test
    void shouldGetBlankNameIfNoResolvers() {
        var resolver = new CompositeTemplateResolver(null);
        assertEquals("", resolver.getName());
    }

    @Test
    void shouldGetNameIfResolversProvided() {
        var resolverA = mock(ITemplateResolver.class);
        when(resolverA.getName()).thenReturn("A");
        var resolverB = mock(ITemplateResolver.class);
        when(resolverB.getName()).thenReturn("B");
        var resolver = new CompositeTemplateResolver(List.of(resolverB, resolverA));
        assertEquals("B, A", resolver.getName());
    }

    @Test
    void shouldGetNullOrder() {
        var resolver = new CompositeTemplateResolver(null);
        assertNull(resolver.getOrder());
    }

    @Test
    void shouldThrowNotFoundExceptionIfNoResolvers() {
        var resolver = new CompositeTemplateResolver(null);
        assertThrows(
            NotFoundException.class,
            () -> resolver.resolveTemplate(null, null, null, null)
        );
    }

    @Test
    void shouldThrowNotFoundExceptionIfAllResolversReturnNull() {
        var resolverA = mock(ITemplateResolver.class);
        when(resolverA.resolveTemplate(null, null, null, null)).thenReturn(null);
        var resolverB = mock(ITemplateResolver.class);
        when(resolverB.resolveTemplate(null, null, null, null)).thenReturn(null);
        var resolver = new CompositeTemplateResolver(List.of(resolverA, resolverB));
        assertThrows(
            NotFoundException.class,
            () -> resolver.resolveTemplate(null, null, null, null)
        );
    }

    @Test
    void shouldResolveTemplateIfResolvedByOneOfResolvers() {
        var resolverA = mock(ITemplateResolver.class);
        var resolution = mock(TemplateResolution.class);
        when(resolverA.resolveTemplate(null, null, null, null))
            .thenReturn(resolution);
        var resolverB = mock(ITemplateResolver.class);
        when(resolverB.resolveTemplate(null, null, null, null))
            .thenReturn(null);
        var resolver = new CompositeTemplateResolver(List.of(resolverA, resolverB));
        assertEquals(resolution, resolver.resolveTemplate(null, null, null, null));
    }
}