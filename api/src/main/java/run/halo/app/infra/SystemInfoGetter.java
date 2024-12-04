package run.halo.app.infra;

import java.util.function.Supplier;
import reactor.core.publisher.Mono;

public interface SystemInfoGetter extends Supplier<Mono<SystemInfo>> {
}
