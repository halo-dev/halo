package run.halo.app.actuator;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionMetadata;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatabaseInfoContributor implements InfoContributor {
    private static final String DATABASE_INFO_KEY = "database";

    private final ConnectionFactory connectionFactory;

    public DatabaseInfoContributor(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail(DATABASE_INFO_KEY, contributorMap());
    }

    public Map<String, Object> contributorMap() {
        var map = new HashMap<String, Object>();
        var connectionMetadata = getConnectionMetadata().block();
        if (Objects.isNull(connectionMetadata)) {
            return map;
        }
        map.put("name", connectionMetadata.getDatabaseProductName());
        map.put("version", connectionMetadata.getDatabaseVersion());
        return map;
    }

    private Mono<ConnectionMetadata> getConnectionMetadata() {
        return Mono.from(this.connectionFactory.create())
            .flatMap(connection -> {
                var metadata = connection.getMetadata();
                return Mono.from(connection.close()).thenReturn(metadata);
            });
    }
}
