package run.halo.app.extension.availability;

import org.springframework.boot.actuate.availability.AvailabilityStateHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;

@Component
public class IndexBuildStateHealthIndicator extends AvailabilityStateHealthIndicator {
    /**
     * Create a {@link IndexBuildStateHealthIndicator} instance by {@link ApplicationAvailability}.
     * Mapping {@link IndexBuildState} to {@link Status}.
     *
     * @see IndexBuildState
     */
    public IndexBuildStateHealthIndicator(ApplicationAvailability availability) {
        super(availability, IndexBuildState.class, (statusMappings) -> {
            statusMappings.add(IndexBuildState.BUILT, Status.UP);
            statusMappings.add(IndexBuildState.BUILDING, Status.OUT_OF_SERVICE);
        });
    }
}
