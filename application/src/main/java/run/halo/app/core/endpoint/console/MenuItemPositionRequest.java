package run.halo.app.core.endpoint.console;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/**
 * Request to move a MenuItem within its owning Menu.
 *
 * @param menuName selected Menu metadata.name
 * @param parentName target parent MenuItem metadata.name, or null for root
 * @param beforeName target next sibling MenuItem metadata.name, or null to append
 */
@Schema(name = "MenuItemPositionRequest")
public record MenuItemPositionRequest(
        @Schema(requiredMode = REQUIRED) String menuName,
        @Nullable String parentName,
        @Nullable String beforeName) {}
