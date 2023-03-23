package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.zafarkhaja.semver.Version;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

/**
 * Tests for {@link DefaultSystemVersionSupplier}.
 *
 * @author guqing
 * @since 2.0.0
 */

class DefaultSystemVersionSupplierTest {

    private DefaultSystemVersionSupplier systemVersionSupplier;

    @BeforeEach
    void setUp() {
        systemVersionSupplier = new DefaultSystemVersionSupplier();
    }

    @Test
    void getWhenBuildPropertiesNotSet() {
        Version version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("0.0.0");
    }

    @Test
    void getWhenBuildPropertiesButVersionIsNull() {
        Properties properties = new Properties();
        BuildProperties buildProperties = new BuildProperties(properties);
        systemVersionSupplier.setBuildProperties(buildProperties);

        Version version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("0.0.0");
    }

    @Test
    void getWhenBuildPropertiesAndVersionNotEmpty() {
        Properties properties = new Properties();
        properties.put("version", "2.0.0");
        BuildProperties buildProperties = new BuildProperties(properties);
        systemVersionSupplier.setBuildProperties(buildProperties);

        Version version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("2.0.0");

        properties.put("version", "2.0.0-SNAPSHOT");
        buildProperties = new BuildProperties(properties);
        systemVersionSupplier.setBuildProperties(buildProperties);
        version = systemVersionSupplier.get();
        assertThat(version.toString()).isEqualTo("2.0.0-SNAPSHOT");
        assertThat(version.getPreReleaseVersion()).isEqualTo("SNAPSHOT");
    }
}