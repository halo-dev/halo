## Why

Plugins can currently render Vue components from FormKit schemas with `$cmp`, but those components do not become FormKit input types and therefore do not participate in FormKit input lifecycle, value handling, validation, features, or schema libraries. Halo should let UI plugins register custom FormKit inputs so plugin configuration forms can use plugin-provided field types in the same way they use Halo's built-in FormKit inputs.

## What Changes

- Add a `formkit.inputs` section to the Halo UI plugin module contract for registering custom FormKit input definitions.
- Load enabled plugin UI modules early enough to collect their FormKit inputs before installing FormKit.
- Merge plugin-provided inputs into Halo's FormKit configuration while preserving built-in input precedence.
- Expose `@formkit/vue` as a shared plugin external so plugin authors can use Halo's FormKit runtime when creating inputs.
- Document plugin FormKit input registration and add tests for input collection and conflict handling.

## Capabilities

### New Capabilities

- `plugin-formkit-inputs`: Allows enabled UI plugins to register custom FormKit input types for use in FormKit schemas.

### Modified Capabilities

- None.

## Impact

- Affects the frontend plugin module type exported by `@halo-dev/ui-shared`.
- Affects Console and User Center startup ordering for plugin bundle loading and FormKit installation.
- Affects plugin bundler externals and the main UI external library injection list by adding `@formkit/vue`.
- No backend API, database schema, OpenAPI, or generated API client changes are expected.
- Existing plugin modules remain compatible because `formkit` is optional.
