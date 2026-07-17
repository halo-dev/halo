## ADDED Requirements

### Requirement: Eligible theme templates use Halo message fallback
When an active theme supplies a template at the same logical path as a Halo-provided template, the system SHALL use the Halo template's message bundles as fallback sources for that theme template.

#### Scenario: Theme overrides a Halo template
- **WHEN** the selected template resource is inside the active theme's `templates` directory and Halo provides a template at the same logical path
- **THEN** the system includes messages adjacent to the Halo template in the selected template's message lookup

#### Scenario: Halo has no corresponding template
- **WHEN** a selected active-theme template has no Halo-provided template at the same logical path
- **THEN** the system does not include any Halo message bundle solely because a similarly located properties file exists

#### Scenario: Selected resource is not an active-theme template
- **WHEN** a selected template resource comes from a plugin, Halo itself, or a file outside the active theme's `templates` directory
- **THEN** the system does not add active-theme-to-Halo fallback processing for that resource

### Requirement: Message fallback merges by key
For each eligible theme template, the system SHALL merge message maps by key in ascending precedence order: theme-global messages, corresponding Halo template messages, and selected theme template messages.

#### Scenario: Theme omits a Halo key
- **WHEN** an eligible theme template bundle does not define a key that is defined by the corresponding Halo template bundle
- **THEN** resolving that key returns the Halo-provided value

#### Scenario: Theme overrides a Halo key
- **WHEN** both the eligible theme template bundle and corresponding Halo template bundle define the same key
- **THEN** resolving that key returns the theme-provided value

#### Scenario: Halo template message conflicts with theme-global message
- **WHEN** a corresponding Halo template bundle and the active theme's global `i18n` bundle define the same key and the selected theme template bundle does not
- **THEN** resolving that key returns the Halo template bundle value

#### Scenario: Theme template message conflicts with all lower-precedence sources
- **WHEN** the selected theme template bundle, corresponding Halo template bundle, and theme-global `i18n` bundle define the same key
- **THEN** resolving that key returns the selected theme template bundle value

### Requirement: Each message source resolves locale fallback independently
The system SHALL apply Thymeleaf locale fallback within each message source before applying source precedence.

#### Scenario: Theme base bundle overrides a locale-specific Halo value
- **WHEN** the requested locale has a value in a locale-specific Halo bundle, the theme has no matching locale-specific bundle, and the theme base bundle defines the same key
- **THEN** resolving that key returns the theme base-bundle value

#### Scenario: Theme locale bundle overrides its base bundle
- **WHEN** the theme base bundle and a more locale-specific theme bundle define the same key
- **THEN** resolving that key returns the value from the more locale-specific theme bundle

#### Scenario: Theme has no value in any applicable locale bundle
- **WHEN** no applicable theme bundle defines a key and an applicable Halo bundle defines it
- **THEN** resolving that key returns the most locale-specific applicable Halo value

### Requirement: Fallback applies per template in the template stack
The system SHALL evaluate message fallback independently for every eligible template resource processed by Thymeleaf, including top-level templates, fragments, and templates in nested directories.

#### Scenario: Theme overrides a nested fragment
- **WHEN** an active theme overrides a Halo fragment in a nested template path and omits a message defined beside the Halo fragment
- **THEN** resolving that message while processing the fragment returns the Halo fragment's value

#### Scenario: Outer template and fragment use separate bundles
- **WHEN** a rendered page and one of its fragments each have template-specific message bundles
- **THEN** each template uses the fallback sources associated with its own logical path and resource origin

### Requirement: Standard absent-message and error behavior is preserved
The system SHALL preserve Thymeleaf's existing behavior when no source supplies a key or when an existing message resource cannot be parsed.

#### Scenario: Key is absent from all message sources
- **WHEN** a message key is absent from the theme template bundle, corresponding Halo template bundle, and theme-global bundle
- **THEN** a normal Thymeleaf message expression produces the standard locale-qualified absent-message representation

#### Scenario: Locale-specific bundle does not exist
- **WHEN** an optional locale-specific message resource does not exist
- **THEN** the system continues resolving less-specific resources without failing template rendering

#### Scenario: Existing bundle cannot be parsed
- **WHEN** an applicable existing theme or Halo message resource contains content that cannot be parsed as a properties bundle
- **THEN** template rendering fails with the standard message-resource parsing error

### Requirement: Fallback follows configured system template resource settings
The system SHALL locate corresponding Halo templates using the configured Thymeleaf template prefix, suffix, and character encoding.

#### Scenario: Default template settings are used
- **WHEN** Halo uses its default Thymeleaf template resource settings
- **THEN** the fallback resolves the corresponding packaged Halo template and adjacent messages

#### Scenario: Template resource settings are configured
- **WHEN** the Thymeleaf template prefix, suffix, or encoding is configured to a supported non-default value
- **THEN** the fallback uses those configured values rather than hard-coded resource settings

### Requirement: Message caching remains scoped to the theme template engine
The system SHALL use Thymeleaf's existing template-message cache behavior and SHALL NOT introduce a message cache shared across theme template engines.

#### Scenario: Theme engine cache is cleared
- **WHEN** the cached template engine for a theme is cleared and recreated
- **THEN** subsequent renders resolve theme and Halo fallback messages through the new engine and message resolver

#### Scenario: Template is non-cacheable
- **WHEN** Thymeleaf marks the selected template as non-cacheable
- **THEN** the merged message map is resolved without adding a separate fallback cache
