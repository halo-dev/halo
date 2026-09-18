## Purpose

Define durable menu-item bindings to built-in theme routes so rendered links remain correct when administrators change the corresponding route settings.

## Requirements

### Requirement: Menu items can reference built-in theme routes
The system SHALL allow a menu item to reference exactly one of the `archives`, `categories`, or `tags` built-in theme routes through an optional route reference.

#### Scenario: Create an archives route binding
- **WHEN** a menu item is saved with the `archives` route reference and a display name
- **THEN** the system accepts the route reference as the menu item's link source

#### Scenario: Preserve existing link sources
- **WHEN** a menu item has no route reference
- **THEN** the system continues to resolve its existing resource reference or custom link without behavior changes

### Requirement: Bound links are derived from current route settings
The system SHALL derive a route-bound menu item's rendered link from the current theme route rules while keeping its display name user-controlled.

#### Scenario: Resolve a bound link
- **WHEN** an `archives` route-bound menu item is reconciled while the archives route is configured as `timeline`
- **THEN** its rendered link is `/timeline`
- **AND** its rendered display name matches the menu item's configured display name

#### Scenario: Use default route rules
- **WHEN** a route-bound menu item is reconciled without an overriding route-rule configuration
- **THEN** the system resolves the link using the built-in default for that route

#### Scenario: Do not persist a URL snapshot
- **WHEN** a route-bound menu item is saved
- **THEN** its route reference is the source of truth
- **AND** the system does not require a custom-link URL snapshot for fallback

### Requirement: Route changes refresh bound menu links
The system SHALL eventually reconcile affected route-bound menu items after their corresponding theme route rule changes.

#### Scenario: Update an affected route binding
- **WHEN** the categories route changes from `categories` to `topics`
- **THEN** every menu item bound to the categories route eventually renders `/topics`
- **AND** menu items bound to other built-in routes remain unchanged

#### Scenario: Isolate refresh failures
- **WHEN** refreshing one affected menu item fails
- **THEN** the system continues requesting refreshes for the other affected menu items
- **AND** the failed item remains eligible for normal later reconciliation

#### Scenario: Retain the last valid result for an invalid route rule
- **WHEN** an invalid route-rule update cannot be normalized
- **THEN** the system does not replace an existing bound menu item's last valid rendered link with an invalid value

### Requirement: Invalid link-source combinations do not render
The system SHALL treat conflicting or incomplete route-binding data as invalid rather than silently selecting another link source.

#### Scenario: Route and resource references conflict
- **WHEN** a menu item contains both a route reference and a resource reference
- **THEN** the system clears its derived display name and link
- **AND** records a diagnostic error

#### Scenario: Route binding has no display name
- **WHEN** a menu item contains a route reference without a display name
- **THEN** the system clears its derived display name and link
- **AND** records a diagnostic error

### Requirement: Console authors can manage menu item sources
The Console SHALL offer `Article archives`, `Category list`, and `Tag list` as menu-item source types, with localized labels and localized default display names. Authors SHALL be able to switch existing supported menu items among custom links, these three routes, and Post, SinglePage, Category, and Tag resource references.

#### Scenario: Create a route-bound menu item
- **WHEN** an author selects a built-in route while creating a menu item
- **THEN** the Console requires a display name
- **AND** does not show a link field

#### Scenario: Hide an existing resolved link
- **WHEN** an author edits an existing route-bound menu item
- **THEN** the Console does not show a link field

#### Scenario: Convert a custom link
- **WHEN** an author converts a custom-link menu item to a built-in route binding
- **THEN** the Console preserves the existing display name
- **AND** replaces the custom link source with the selected route reference

#### Scenario: Switch a built-in route binding
- **WHEN** an author changes a route-bound menu item to another supported built-in route
- **THEN** the Console preserves the display name
- **AND** saves the newly selected route reference

#### Scenario: Remove a route binding
- **WHEN** an author changes an existing route-bound menu item to a custom link for the first time in the editing session
- **THEN** the Console preserves its display name
- **AND** seeds the editable custom-link URL from the rendered link fetched when opening the editor
- **AND** subsequent source switches preserve the author's custom-link draft instead of overwriting it with that snapshot

#### Scenario: Convert a resource reference to a custom link
- **WHEN** an author changes a supported resource-backed item to a custom link
- **THEN** the Console seeds its editable name and URL from the existing resolved values if no corresponding draft has been initialized
- **AND** requires the author to supply missing required values
- **AND** saves the custom link without a resource or route reference

#### Scenario: Convert a resource reference to a route
- **WHEN** an author changes a supported resource-backed item to a built-in route
- **THEN** the Console preserves an initialized editable name draft or seeds it from the existing resolved name
- **AND** uses the localized route default only if no name draft has been initialized and no resolved name is available
- **AND** saves the route reference without a resource reference or explicit URL

#### Scenario: Convert a custom link or route to a resource reference
- **WHEN** an author changes a custom-link or route-bound item to a supported resource type
- **THEN** the Console requires selecting a resource of that type
- **AND** saves the selected reference without a route reference, explicit display name, or explicit URL

#### Scenario: Switch between resource types
- **WHEN** an author switches a supported resource-backed item to another supported resource type
- **THEN** the previous resource name is cleared and selecting a resource of the new type is required
- **AND** returning to a previously visited resource type requires selecting its resource again

#### Scenario: Keep the source unchanged
- **WHEN** an author edits a supported resource-backed item without changing its source type or selected resource
- **THEN** saving preserves the original resource reference

### Requirement: Source edits preserve menu identity and hierarchy
The Console SHALL convert the source of the existing menu item without recreating it or implicitly changing its hierarchy, opening target, or annotations.

#### Scenario: Convert an item with children
- **WHEN** an author changes only the source of an item with a parent and children
- **THEN** the saved item retains its name, menu membership, parent, ordering, opening target, and annotations
- **AND** its children remain attached to the same item

#### Scenario: Change source and parent together
- **WHEN** an author explicitly changes both a menu item's source and parent
- **THEN** the Console saves the source change and uses the existing parent-movement behavior
- **AND** if the source save succeeds but the movement fails, the Console reports the partial failure and refreshes the saved item instead of reporting complete success

### Requirement: Temporary source changes preserve editable drafts
During one editing session, the Console SHALL preserve initialized editable display-name and custom-URL drafts across source changes and SHALL leave them intact when a save request fails. This SHALL apply to both creation and editing.

#### Scenario: Return to an edited custom link
- **WHEN** an author enters a name and URL, switches to a route or resource source, and returns to custom
- **THEN** the editor shows the entered name and URL
- **AND** does not replace them with values from the originally fetched item

#### Scenario: Keep an intentionally cleared field
- **WHEN** an author clears an initialized editable name or URL, switches away, and returns to a mode displaying that field
- **THEN** the field remains empty
- **AND** its required validation prevents saving until corrected

#### Scenario: Retry after a rejected save
- **WHEN** saving a selected source fails before the content update succeeds
- **THEN** the selected source, resource selection, editable text drafts, and annotation edits remain available
- **AND** switching back to a text-based source reveals the preserved drafts

#### Scenario: Do not resolve an unsaved binding as a custom URL
- **WHEN** an author switches among unsaved route or resource selections and then enters custom mode
- **THEN** the Console uses the initialized custom-URL draft or the existing item's modal-open rendered URL as its initial seed
- **AND** if neither exists, it requires the author to enter a URL rather than fabricating one for the unsaved selection

### Requirement: Unknown resource references remain opaque to Console editing
The Console SHALL recognize built-in resource references by supported group, version, and kind together. References outside `content.halo.run/v1alpha1` Post, SinglePage, Category, and Tag SHALL remain locked and SHALL retain their original source fields when unrelated fields are saved.

#### Scenario: Preserve an unsupported kind
- **WHEN** an author opens an item with an unsupported resource kind and saves unrelated fields
- **THEN** the existing reference identity is shown without enabling source conversion
- **AND** the original reference and other source fields are preserved

#### Scenario: Preserve a colliding kind or unsupported version
- **WHEN** an existing reference uses a built-in kind name with a different group or unsupported version
- **THEN** the Console does not reinterpret it as a built-in resource
- **AND** saving unrelated fields preserves its complete original reference

### Requirement: Annotation context follows the selected source
The Console SHALL expose source fields consistent with the selected source to annotation schemas before validation and submission, without clearing annotation drafts during source changes.

#### Scenario: Switch source with unsaved annotations
- **WHEN** an author enters annotation values and changes the menu source
- **THEN** the annotation schema context reflects the newly selected source and excludes inactive source fields
- **AND** the annotation values remain available for validation and saving

#### Scenario: A required resource has not been selected
- **WHEN** an author changes to a resource type but has not selected its resource
- **THEN** the form does not submit a menu-item write request
- **AND** annotation context does not expose the previous resource as the selected resource

### Requirement: Existing menus are not silently rebound
The system SHALL preserve existing custom-link menu items unless an author explicitly converts them.

#### Scenario: Upgrade an existing installation
- **WHEN** an installation contains a custom link whose URL equals a current built-in route
- **THEN** an upgrade leaves that menu item as a custom link

#### Scenario: Initialize a new installation
- **WHEN** a new installation creates its default article archives menu item
- **THEN** that menu item is bound to the `archives` route
