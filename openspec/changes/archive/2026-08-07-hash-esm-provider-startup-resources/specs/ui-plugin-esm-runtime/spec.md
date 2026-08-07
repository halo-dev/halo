## MODIFIED Requirements

### Requirement: UI provider lifecycle and cache boundary

Halo SHALL treat a full Console or User Center page load as the supported module replacement boundary and SHALL provide canonical cache-safe resource URLs for output produced by the default provider presets.

#### Scenario: Provider is installed, upgraded, enabled, disabled, or activated

- **WHEN** a plugin or theme UI provider changes after the page module graph has started
- **THEN** Halo SHALL require or prompt a full page reload
- **THEN** Halo SHALL NOT hot-unload or hot-replace the running provider module

#### Scenario: Production ESM assets from default presets are cached

- **WHEN** production provider ESM resources are emitted without overriding the supported bundler-kit resource naming defaults
- **THEN** entry and startup-style filenames SHALL contain content-derived hashes
- **THEN** their descriptor URLs SHALL use the manifest-selected provider-relative paths without appending a query cache key
- **THEN** asynchronous chunks and assets emitted by the default presets SHALL use provider-relative content-hashed URLs
- **THEN** legacy aggregate URLs SHALL include the current catalog version as a cache key
- **THEN** provider discovery metadata SHALL be revalidated so it reflects currently enabled providers

#### Scenario: A provider chunk imports the provider entry

- **WHEN** an asynchronous ESM chunk statically imports a binding from the provider entry
- **THEN** the chunk reference and the descriptor entry SHALL resolve to the same canonical URL
- **THEN** the browser SHALL NOT fetch or evaluate a second entry module solely because Halo appended a query cache key

#### Scenario: Provider overrides startup or secondary resource names

- **WHEN** caller bundler configuration or hooks emit the ESM entry, startup stylesheet, asynchronous chunks, stylesheets, or assets with stable or otherwise custom filenames
- **THEN** Halo SHALL NOT rewrite those resources or append a query cache key to the ESM entry and startup-style URLs
- **THEN** cache invalidation for the overridden resources SHALL be the provider developer's responsibility

#### Scenario: Development provider assets are cached

- **WHEN** a development plugin or theme provider is described repeatedly without changing its directly loaded build output
- **THEN** its manifest-selected entry and startup-style URLs SHALL remain unchanged
- **WHEN** its manifest, entry, or startup stylesheet changes
- **THEN** its content-hashed startup filenames and the catalog version SHALL change
- **THEN** another unchanged provider SHALL retain its existing direct resource URLs
