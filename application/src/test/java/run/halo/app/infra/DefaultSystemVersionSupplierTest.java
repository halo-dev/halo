package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.zafarkhaja.semver.Version;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

/**
 * Tests for {@link DefaultSystemVersionSupplier}.
 *
 * @author guqing
 * @since 2.0.0
 */

@ExtendWith(MockitoExtension.class)
class DefaultSystemVersionSupplierTest {

    @InjectMocks
    private DefaultSystemVersionSupplier systemVersionSupplier;

    @Mock
    ObjectProvider<BuildProperties> buildPropertiesProvider;

    @Test
    void getWhenBuildPropertiesNotSet() {
        Version version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("0.0.0");
    }

    @Test
    void getWhenBuildPropertiesButVersionIsNull() {
        Properties properties = new Properties();
        BuildProperties buildProperties = new BuildProperties(properties);
        when(buildPropertiesProvider.getIfUnique()).thenReturn(buildProperties);

        Version version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("0.0.0");
    }

    @Test
    void getWhenBuildPropertiesAndVersionNotEmpty() {
        Properties properties = new Properties();
        properties.put("version", "2.0.0");
        BuildProperties buildProperties = new BuildProperties(properties);
        when(buildPropertiesProvider.getIfUnique()).thenReturn(buildProperties);

        Version version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("2.0.0");

        properties.put("version", "2.0.0-SNAPSHOT");
        buildProperties = new BuildProperties(properties);
        when(buildPropertiesProvider.getIfUnique()).thenReturn(buildProperties);
        version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("2.0.0-SNAPSHOT");
        assertThat(version.preReleaseVersion().orElseThrow()).isEqualTo("SNAPSHOT");
    }
}