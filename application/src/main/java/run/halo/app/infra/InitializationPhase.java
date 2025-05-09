package run.halo.app.infra;

/**
 * Phase of system initialization.
 *
 * @author johnniang
 */
public enum InitializationPhase {

    FIRST(Integer.MIN_VALUE),

    SCHEME(Integer.MIN_VALUE + 100),

    EXTENSION_RESOURCES,

    THEME_ROUTER_FUNCTIONS,

    GC_CONTROLLER,

    CONTROLLERS,

    LAST(Integer.MAX_VALUE),
    ;

    private static final int GAP = 100;

    private final int phase;

    InitializationPhase() {
        this.phase = ordinal() * GAP;
    }

    InitializationPhase(int phase) {
        this.phase = phase;
    }

    public int getPhase() {
        return phase;
    }
}
