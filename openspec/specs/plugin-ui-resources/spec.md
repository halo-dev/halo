# plugin-ui-resources Specification

## Purpose
TBD - created by archiving change prefer-plugin-ui-resources. Update Purpose after archive.
## Requirements
### Requirement: Plugin UI asset routes

Halo SHALL serve plugin static assets from both the preferred shared UI resource route and the legacy Console resource route.

#### Scenario: Serve assets from ui route

- **WHEN** a plugin provides a resource at `ui/main.js`
- **THEN** Halo SHALL make it available at `/plugins/{name}/assets/ui/main.js`

#### Scenario: Preserve console asset route

- **WHEN** a plugin provides a resource at `console/main.js`
- **THEN** Halo SHALL continue to make it available at `/plugins/{name}/assets/console/main.js`

### Requirement: Preferred plugin bundle directory

Halo SHALL select the plugin bundle directory per plugin using `ui` before `console`.

#### Scenario: Prefer ui bundle directory

- **WHEN** a plugin provides `ui/main.js` or `ui/style.css`
- **THEN** Halo SHALL use the `ui` directory for that plugin's bundle resources
- **THEN** Halo SHALL NOT aggregate bundle resources from the plugin's `console` directory

#### Scenario: Fall back to console bundle directory

- **WHEN** a plugin does not provide `ui/main.js` or `ui/style.css`
- **THEN** Halo SHALL use `console/main.js` and `console/style.css` if they exist

### Requirement: Plugin status bundle URLs

Halo SHALL report plugin status entry and stylesheet URLs from the selected plugin bundle directory.

#### Scenario: Status uses ui links when ui is selected

- **WHEN** a plugin provides `ui/main.js` or `ui/style.css`
- **THEN** `Plugin.status.entry` SHALL point to `/plugins/{name}/assets/ui/main.js` when `ui/main.js` exists
- **THEN** `Plugin.status.stylesheet` SHALL point to `/plugins/{name}/assets/ui/style.css` when `ui/style.css` exists

#### Scenario: Status falls back to console links

- **WHEN** a plugin does not provide `ui/main.js` or `ui/style.css`
- **THEN** `Plugin.status.entry` SHALL point to `/plugins/{name}/assets/console/main.js` when `console/main.js` exists
- **THEN** `Plugin.status.stylesheet` SHALL point to `/plugins/{name}/assets/console/style.css` when `console/style.css` exists

