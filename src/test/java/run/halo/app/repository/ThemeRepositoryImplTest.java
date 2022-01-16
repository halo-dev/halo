package run.halo.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static run.halo.app.model.properties.PrimaryProperties.THEME;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.support.HaloConst;

/**
 * Theme repository impl test.
 *
 * @author johnniang
 */
class ThemeRepositoryImplTest {

    @InjectMocks
    @Spy
    ThemeRepositoryImpl themeRepository;

    @Mock
    OptionRepository optionRepository;

    @Mock
    HaloProperties haloProperties;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getActivatedThemeBySingleThread() {
        ThemeProperty expectedTheme = new ThemeProperty();
        expectedTheme.setId(HaloConst.DEFAULT_THEME_ID);
        expectedTheme.setActivated(true);

        given(optionRepository.findByKey(THEME.getValue())).willReturn(Optional.empty());
        doReturn(Optional.of(expectedTheme)).when(themeRepository)
            .fetchThemePropertyByThemeId(HaloConst.DEFAULT_THEME_ID);

        ThemeProperty resultTheme = themeRepository.getActivatedThemeProperty();
        assertEquals(expectedTheme, resultTheme);

        verify(optionRepository, times(1)).findByKey(any());
        verify(themeRepository, times(1)).fetchThemePropertyByThemeId(any());
    }

    @Test
    void getActivatedThemeByMultiThread() throws InterruptedException {
        ThemeProperty expectedTheme = new ThemeProperty();
        expectedTheme.setId(HaloConst.DEFAULT_THEME_ID);
        expectedTheme.setActivated(true);

        given(optionRepository.findByKey(THEME.getValue())).willReturn(Optional.empty());
        doReturn(Optional.of(expectedTheme)).when(themeRepository)
            .fetchThemePropertyByThemeId(HaloConst.DEFAULT_THEME_ID);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // define tasks
        List<Callable<ThemeProperty>> tasks = IntStream.range(0, 10)
            .mapToObj(
                i -> (Callable<ThemeProperty>) () -> themeRepository.getActivatedThemeProperty())
            .collect(Collectors.toList());

        // invoke and get results
        executorService.invokeAll(tasks).forEach(future -> {
            try {
                assertEquals(expectedTheme, future.get(100, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                throw new RuntimeException("Failed to get task result!", e);
            }
        });

        verify(optionRepository, times(1)).findByKey(any());
        verify(themeRepository, times(1)).fetchThemePropertyByThemeId(any());
    }

}