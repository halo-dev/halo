package run.halo.app.infra.actuator;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionMetadata;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.ReactiveUtils;

@Slf4j
@Component
class DatabaseInfoContributor implements InfoContributor, InitializingBean {

    private static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;

    private static final String DATABASE_INFO_KEY = "database";

    private final ConnectionFactory connectionFactory;

    @Nullable
    private ConnectionMetadata connectionMetadata;

    public DatabaseInfoContributor(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        var connectionMetadata = Mono.usingWhen(
                this.connectionFactory.create(),
                connection -> Mono.just(connection.getMetadata()),
                Connection::close
            )
            .blockOptional(BLOCKING_TIMEOUT)
            .orElseThrow(() -> new IllegalStateException("Unable to get database metadata"));
        if (log.isDebugEnabled()) {
            log.debug("Database Metadata initialized: name={}, version={}",
                connectionMetadata.getDatabaseProductName(),
                connectionMetadata.getDatabaseVersion());
        }
        this.connectionMetadata = connectionMetadata;
    }

    @Override
    public void contribute(Info.Builder builder) {
        if (this.connectionMetadata != null) {
            builder.withDetail(DATABASE_INFO_KEY, Map.of(
                "name", this.connectionMetadata.getDatabaseProductName(),
                "version", this.connectionMetadata.getDatabaseVersion()
            ));
        }
    }

}
