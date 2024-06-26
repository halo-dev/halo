package run.halo.app.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import run.halo.app.search.SearchEngine;
import run.halo.app.search.lucene.LuceneSearchEngine;

class HaloConfigurationTest {

    @Nested
    @SpringBootTest
    class LuceneSearchEngineDisabled {

        @Test
        void shouldNotCreateLuceneSearchEngineBean(
            @Autowired ObjectProvider<SearchEngine> searchEngines) {
            var searchEngine = searchEngines.getIfAvailable();
            assertNull(searchEngine);
        }
    }

    @Nested
    @SpringBootTest(properties = "halo.search-engine.lucene.enabled=true")
    @DirtiesContext
    class LuceneSearchEngineEnabled {

        @Test
        void shouldCreateLuceneSearchEngineBean(
            @Autowired ObjectProvider<SearchEngine> searchEngines) {
            var searchEngine = searchEngines.getIfAvailable();
            assertNotNull(searchEngine);
            assertInstanceOf(LuceneSearchEngine.class, searchEngine);
        }
    }

}