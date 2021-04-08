package run.halo.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import run.halo.app.model.properties.AliOssProperties;
import run.halo.app.service.OptionService;

class OptionFilterTest {

    @Mock
    OptionService optionService;

    @InjectMocks
    OptionFilter optionFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFilterForDefaultPrivateOptions() {
        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.empty());

        var optionNames = Set.of(AliOssProperties.OSS_ACCESS_SECRET.getValue());
        var filteredOptionNames = optionFilter.filter(optionNames);
        assertTrue(filteredOptionNames.isEmpty());

        var filteredOptionName =
            optionFilter.filter(AliOssProperties.OSS_ACCESS_SECRET.getValue());
        assertTrue(filteredOptionName.isEmpty());
    }

    @Test
    void shouldFilterForConfiguredPrivateOptions() {
        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.of("hello"));

        var optionNames = Set.of("hello", "world");
        var filteredOptionNames = optionFilter.filter(optionNames);
        var filteredOptionName = optionFilter.filter("hello");
        assertEquals(Set.of("world"), filteredOptionNames);
        assertTrue(filteredOptionName.isEmpty());

        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.of("hello,world"));

        optionNames = Set.of("hello");
        filteredOptionNames = optionFilter.filter(optionNames);
        filteredOptionName = optionFilter.filter("hello");
        assertTrue(filteredOptionNames.isEmpty());
        assertTrue(filteredOptionName.isEmpty());

        optionNames = Set.of("hello", "world");
        filteredOptionNames = optionFilter.filter(optionNames);
        filteredOptionName = optionFilter.filter("hello");
        assertTrue(filteredOptionNames.isEmpty());
        assertTrue(filteredOptionName.isEmpty());
    }

    @Test
    void shouldFilterForConfiguredPrivateOptionsWithSpace() {
        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.of("   hello "));

        var optionNames = Set.of("hello", "world");
        var filteredOptionNames = optionFilter.filter(optionNames);
        var filteredOptionName = optionFilter.filter("hello");
        assertEquals(Set.of("world"), filteredOptionNames);
        assertTrue(filteredOptionName.isEmpty());

        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.of(" hello ,    world "));

        optionNames = Set.of("hello");
        filteredOptionNames = optionFilter.filter(optionNames);
        filteredOptionName = optionFilter.filter("hello");
        assertTrue(filteredOptionNames.isEmpty());
        assertTrue(filteredOptionName.isEmpty());

        optionNames = Set.of("hello", "world");
        filteredOptionNames = optionFilter.filter(optionNames);
        filteredOptionName = optionFilter.filter("world");
        assertTrue(filteredOptionNames.isEmpty());
        assertTrue(filteredOptionName.isEmpty());
    }

    @Test
    void shouldFilterForBothOfThem() {
        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.of("hello,world"));

        var optionNames =
            Set.of("hello", "world", "halo", AliOssProperties.OSS_ACCESS_SECRET.getValue());
        var filteredOptionNames = optionFilter.filter(optionNames);
        assertEquals(Set.of("halo"), filteredOptionNames);
    }

    @Test
    void shouldFilterNothing() {
        given(optionService.getByKey(any(), eq(String.class)))
            .willReturn(Optional.of(",world"));

        var optionNames =
            Set.of("hello", "halo");
        var filteredOptionNames = optionFilter.filter(optionNames);
        var filteredOptionName = optionFilter.filter("halo");
        assertEquals(optionNames, filteredOptionNames);
        assertEquals(Optional.of("halo"), filteredOptionName);
    }
}