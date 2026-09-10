## Purpose

Define durable menu-item bindings to built-in theme routes so rendered links remain correct when administrators change the corresponding route settings.

## ADDED Requirements

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

### Requirement: Console authors can manage route bindings
The Console SHALL offer `Article archives`, `Category list`, and `Tag list` as menu-item source types, with localized labels and localized default display names.

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
- **WHEN** an author changes a route-bound menu item back to a custom link
- **THEN** the Console preserves its display name
- **AND** copies the current rendered link into the editable custom-link URL

#### Scenario: Keep resource references locked during editing
- **WHEN** an author edits a menu item linked to a post, page, category, or tag resource
- **THEN** the Console does not allow changing that item to a custom link or built-in route binding

### Requirement: Existing menus are not silently rebound
The system SHALL preserve existing custom-link menu items unless an author explicitly converts them.

#### Scenario: Upgrade an existing installation
- **WHEN** an installation contains a custom link whose URL equals a current built-in route
- **THEN** an upgrade leaves that menu item as a custom link

#### Scenario: Initialize a new installation
- **WHEN** a new installation creates its default article archives menu item
- **THEN** that menu item is bound to the `archives` route
