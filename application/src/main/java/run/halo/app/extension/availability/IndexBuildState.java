package run.halo.app.extension.availability;

import org.springframework.boot.availability.AvailabilityState;

public enum IndexBuildState implements AvailabilityState {
    BUILDING,
    BUILT;
}
