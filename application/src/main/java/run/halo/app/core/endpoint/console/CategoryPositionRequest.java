package run.halo.app.core.endpoint.console;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/**
 * Request to move a Category in the Console category tree.
 *
 * @param parentName target parent Category metadata.name, or null for root
 * @param beforeName target next sibling Category metadata.name, or null to append
 */
@Schema(name = "CategoryPositionRequest")
public record CategoryPositionRequest(
        @Nullable String parentName, @Nullable String beforeName) {}
