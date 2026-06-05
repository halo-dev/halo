## ADDED Requirements

### Requirement: Theme UI static assets

Halo SHALL serve root-level theme UI resources from `/themes/{name}/ui/assets/**`.

#### Scenario: Serve theme UI asset

- **WHEN** an installed theme named `earth` provides a readable resource at `ui/main.js`
- **THEN** Halo SHALL make it available at `/themes/earth/ui/assets/main.js`

#### Scenario: Serve theme UI chunk asset

- **WHEN** an installed theme named `earth` provides a readable resource at `ui/chunks/view.js`
- **THEN** Halo SHALL make it available at `/themes/earth/ui/assets/chunks/view.js`

#### Scenario: Preserve public-site theme asset route

- **WHEN** a theme provides a public-site asset at `templates/assets/main.css`
- **THEN** Halo SHALL continue to serve it from `/themes/{name}/assets/main.css`
- **AND** the theme UI route SHALL NOT change how `/themes/{name}/assets/**` resolves resources

#### Scenario: Reject theme UI path traversal

- **WHEN** a client requests a theme UI asset path that would resolve outside the configured theme root
- **THEN** Halo SHALL reject the request instead of serving filesystem content outside the theme root

### Requirement: Theme UI bundle status

Halo SHALL report theme UI bundle URLs on `Theme.status` when bundle entry files are present.

#### Scenario: Theme provides JS entry

- **WHEN** the Theme reconciler reconciles an installed Theme named `earth`
- **AND** the directory for `earth` contains a readable `ui/main.js`
- **THEN** the reconciled Theme status SHALL set `entry` to a URL for `/themes/earth/ui/assets/main.js`

#### Scenario: Theme provides stylesheet

- **WHEN** the Theme reconciler reconciles an installed Theme named `earth`
- **AND** the directory for `earth` contains a readable `ui/style.css`
- **THEN** the reconciled Theme status SHALL set `stylesheet` to a URL for `/themes/earth/ui/assets/style.css`

#### Scenario: Theme status bundle URL includes version

- **WHEN** the Theme reconciler reports a theme UI bundle URL
- **AND** the Theme spec has a non-blank version
- **THEN** the reported bundle URL SHALL include that version as a cache query parameter

#### Scenario: Theme omits UI bundle files

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the theme root does not contain readable `ui/main.js` or `ui/style.css`
- **THEN** the reconciled Theme status SHALL NOT expose missing UI bundle URLs

### Requirement: Active theme UI bundle loading

Halo SHALL load only the activated theme's UI bundle during Console and UC startup.

#### Scenario: Fetch active theme JS bundle

- **WHEN** a client requests `/apis/api.console.halo.run/v1alpha1/themes/-/bundle.js`
- **THEN** Halo SHALL resolve the JS bundle from the activated theme only

#### Scenario: Fetch active theme CSS bundle

- **WHEN** a client requests `/apis/api.console.halo.run/v1alpha1/themes/-/bundle.css`
- **THEN** Halo SHALL resolve the CSS bundle from the activated theme only

#### Scenario: Activated theme provides UI bundle

- **WHEN** the activated Theme is `earth`
- **AND** `earth` provides `ui/main.js`
- **THEN** Console and UC startup SHALL load the `earth` JS bundle
- **AND** the frontend SHALL register the module as `theme:earth`

#### Scenario: Inactive theme provides UI bundle

- **WHEN** the activated Theme is `earth`
- **AND** another installed Theme named `mars` provides `ui/main.js`
- **THEN** Console and UC startup SHALL NOT load or register the `mars` UI bundle

#### Scenario: Activated theme has no UI bundle

- **WHEN** the activated Theme does not provide `ui/main.js` or `ui/style.css`
- **THEN** Console and UC startup SHALL continue without a theme-provided UI module

### Requirement: Theme UI module contract

Theme UI bundles SHALL use the same frontend module shape as plugin UI bundles.

#### Scenario: Theme module defines Console and UC routes

- **WHEN** the activated theme UI bundle exports a module with `routes` and `ucRoutes`
- **THEN** Console SHALL register the `routes`
- **AND** UC SHALL register the `ucRoutes`

#### Scenario: Theme module defines extension points

- **WHEN** the activated theme UI bundle exports a module with `extensionPoints`
- **THEN** Halo frontend extension point consumers SHALL resolve those extension points from the registered theme module
