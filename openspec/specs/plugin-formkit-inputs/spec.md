## ADDED Requirements

### Requirement: Plugin FormKit input contract

Halo SHALL allow enabled UI plugins to declare custom FormKit inputs through an optional `formkit.inputs` object on the plugin module.

#### Scenario: Plugin declares a FormKit input

- **WHEN** an enabled UI plugin module exports `formkit.inputs` containing a FormKit input definition
- **THEN** Halo SHALL register the input definition under the provided key for FormKit schema usage

#### Scenario: Plugin omits FormKit configuration

- **WHEN** an enabled UI plugin module does not export `formkit`
- **THEN** Halo SHALL continue loading the plugin module without requiring any FormKit-specific configuration

#### Scenario: Plugin uses lazy input component

- **WHEN** a plugin input definition is created with a lazy Vue component inside the synchronous `formkit.inputs` object
- **THEN** Halo SHALL accept the input definition without requiring an asynchronous input factory

### Requirement: Startup registration timing

Halo SHALL collect plugin FormKit inputs before installing the global FormKit plugin for authenticated Console and User Center sessions.

#### Scenario: Authenticated startup includes plugin inputs

- **WHEN** an authenticated user starts the Console or User Center
- **THEN** Halo SHALL load enabled plugin UI modules before installing FormKit
- **THEN** Halo SHALL install FormKit with Halo built-in inputs plus accepted plugin inputs

#### Scenario: Anonymous startup uses built-in inputs

- **WHEN** the current user is anonymous during Console or User Center startup
- **THEN** Halo SHALL install FormKit with Halo built-in inputs only
- **THEN** Halo SHALL NOT load plugin FormKit inputs before the authentication redirect flow

### Requirement: Input name conflict handling

Halo SHALL preserve built-in FormKit input precedence and SHALL NOT allow plugin inputs to override already registered input names.

#### Scenario: Plugin input conflicts with built-in input

- **WHEN** a plugin declares a FormKit input with the same name as a Halo built-in input
- **THEN** Halo SHALL keep the built-in input definition
- **THEN** Halo SHALL skip the plugin input
- **THEN** Halo SHALL print a warning describing the conflict

#### Scenario: Plugin input conflicts with earlier plugin input

- **WHEN** multiple enabled plugins declare the same FormKit input name
- **THEN** Halo SHALL keep the first accepted plugin input definition
- **THEN** Halo SHALL skip later conflicting plugin input definitions
- **THEN** Halo SHALL print a warning describing the conflict

### Requirement: Plugin loading fallback

Halo SHALL continue starting the core Console or User Center UI when plugin bundle loading fails during FormKit input collection.

#### Scenario: Plugin bundle fails before FormKit installation

- **WHEN** Halo fails to load the enabled plugin UI bundle while collecting plugin FormKit inputs
- **THEN** Halo SHALL install FormKit with built-in inputs only
- **THEN** Halo SHALL continue core UI startup
- **THEN** Halo SHALL surface the existing plugin bundle loading failure notification behavior

### Requirement: Shared FormKit Vue runtime for plugins

Halo SHALL expose `@formkit/vue` as a shared UI plugin runtime external.

#### Scenario: Plugin imports FormKit createInput

- **WHEN** a UI plugin imports `createInput` from `@formkit/vue`
- **THEN** the Halo plugin bundler SHALL externalize `@formkit/vue`
- **THEN** the plugin SHALL use Halo's shared `@formkit/vue` runtime in the Console or User Center
