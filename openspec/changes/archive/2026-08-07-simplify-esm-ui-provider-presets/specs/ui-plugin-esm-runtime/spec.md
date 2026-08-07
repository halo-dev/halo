## MODIFIED Requirements

### Requirement: UI provider lifecycle and cache boundary

Halo SHALL treat a full Console or User Center page load as the supported module replacement boundary and SHALL provide cache-safe resource URLs for output produced by the default provider presets.

#### Scenario: Provider is installed, upgraded, enabled, disabled, or activated

- **WHEN** a plugin or theme UI provider changes after the page module graph has started
- **THEN** Halo SHALL require or prompt a full page reload
- **THEN** Halo SHALL NOT hot-unload or hot-replace the running provider module

#### Scenario: Production ESM assets from default presets are cached

- **WHEN** production provider ESM resources are emitted without overriding the supported bundler-kit resource naming defaults
- **THEN** entry and direct startup-style URLs SHALL include a cache key derived from that provider's Halo-managed identity and installed version
- **THEN** legacy aggregate URLs SHALL include the current catalog version as a cache key
- **THEN** asynchronous chunks and assets emitted by the default presets SHALL use provider-relative content-hashed URLs
- **THEN** provider discovery metadata SHALL be revalidated so it reflects currently enabled providers

#### Scenario: Provider overrides secondary resource names

- **WHEN** caller bundler configuration or hooks emit asynchronous chunks, stylesheets, or assets with stable or otherwise custom filenames
- **THEN** Halo SHALL NOT rewrite those resources or propagate the entry query cache key to their relative requests
- **THEN** cache invalidation for the overridden resources SHALL be the provider developer's responsibility

#### Scenario: Development provider assets are cached

- **WHEN** a development plugin or theme provider is described repeatedly without changing its directly loaded build output
- **THEN** its entry and startup-style URLs SHALL retain the same provider-specific cache key
- **WHEN** its manifest, entry, or startup stylesheet changes
- **THEN** its provider-specific cache key and the catalog version SHALL change
- **THEN** another unchanged provider SHALL retain its existing direct resource URLs
