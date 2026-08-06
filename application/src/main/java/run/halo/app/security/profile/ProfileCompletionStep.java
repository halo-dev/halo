package run.halo.app.security.profile;

import java.net.URI;

/** Describes where and why an incomplete user must complete their profile. */
public record ProfileCompletionStep(URI location, URI problemType, String problemDetail) {}
