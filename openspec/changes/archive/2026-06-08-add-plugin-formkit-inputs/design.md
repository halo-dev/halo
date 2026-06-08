## Context

Halo's frontend installs FormKit from `setupComponents()` before enabled UI plugin modules are loaded. Built-in inputs are registered through Halo's FormKit `defaultConfig({ inputs })`, while plugins can currently only register global Vue components, routes, and extension points after FormKit is already installed.

The requested behavior is for plugin-provided FormKit field types to behave like true FormKit inputs, not plain schema `$cmp` component renderers. That requires plugin input definitions to be present before FormKit creates the global input library.

## Goals / Non-Goals

**Goals:**

- Provide a public UI plugin module API for registering custom FormKit inputs.
- Register plugin inputs through FormKit's supported global input configuration path.
- Preserve Halo built-in input behavior when names conflict.
- Keep plugin loading failures from blocking the core Console or User Center UI.
- Share Halo's `@formkit/vue` runtime with plugins that create FormKit inputs.
- Cover the input collection and conflict behavior with focused frontend tests.

**Non-Goals:**

- Do not add support for plugin-provided FormKit plugins, validation rules, themes, locales, or icons.
- Do not require plugin input type names to include a plugin namespace.
- Do not support asynchronous `formkit.inputs` factory functions.
- Do not wrap plugin input runtime rendering in a custom error boundary.
- Do not change backend plugin APIs, OpenAPI contracts, or database schema.

## Decisions

### Plugin API shape

Use a `formkit` namespace on `PluginModule`:

```ts
export default definePlugin({
  formkit: {
    inputs: {
      myPluginInput: createInput(MyPluginInput),
    },
  },
});
```

This keeps the top-level plugin module contract tidy and leaves room for future FormKit-related capabilities without committing to them now. The first supported property is only `formkit.inputs`.

Alternative considered: a new extension point such as `formkit:inputs:create`. That was rejected because FormKit input registration is startup configuration, not a per-view runtime extension. An extension point would introduce avoidable async and repeated-execution questions.

### Startup order

Split plugin bundle loading from plugin module initialization. For authenticated users, load the enabled plugin bundle early, collect plugin `formkit.inputs`, install FormKit with built-in plus accepted plugin inputs, and then initialize plugin components, routes, and extension points from the already-loaded modules.

Anonymous users keep the existing effective behavior: they do not load enabled plugin modules, and FormKit starts with built-in inputs only before the router redirects them through the auth guard.

Alternative considered: install FormKit first and dynamically mutate FormKit's root config later. That was rejected because FormKit's global input library is created during `defaultConfig({ inputs })`, and late mutation depends on more subtle internals.

### Conflict handling

Halo built-in inputs take precedence. If a plugin registers an input name that already exists in built-in inputs, Halo skips that plugin input and prints a warning. If multiple plugins register the same input name, the first loaded plugin wins and later duplicates are skipped with a warning.

This avoids allowing plugins to change core input behavior such as `select`, `attachment`, or `secret`, while keeping the Console and User Center usable when a plugin chooses a conflicting name.

### Supported input definition format

`formkit.inputs` is a synchronous object map. Plugins that want lazy component loading can use `defineAsyncComponent` inside a FormKit input definition, for example with `createInput(defineAsyncComponent(...))`.

Asynchronous input factories are out of scope because they complicate startup sequencing and error handling while adding little value beyond async Vue components.

### Shared FormKit runtime

Expose `@formkit/vue` as a plugin external. Plugin authors can import `createInput` from `@formkit/vue`, and the bundler maps it to Halo's shared runtime.

Only `@formkit/vue` is added as a runtime external in this change. `@formkit/core` may appear as a type-only dependency for exported types, but `@formkit/inputs` runtime helpers are not introduced as shared plugin externals in this scope.

### Error handling

Plugin bundle loading failure falls back to Halo's built-in FormKit inputs and continues core startup with the existing plugin entry/style load failure toast behavior.

Input collection performs defensive checks and warnings for invalid shapes or conflicts. Plugin input runtime errors are left to Vue and FormKit's normal runtime handling, consistent with existing plugin-provided components and routes.

## Risks / Trade-offs

- Public plugin API surface grows through `@halo-dev/ui-shared` → Mitigation: keep the API narrow and optional, with only `formkit.inputs` in this change.
- Startup order changes can accidentally affect plugin initialization → Mitigation: split loading and initialization explicitly, preserve current component/route initialization semantics, and keep anonymous behavior unchanged.
- Plugin input names can conflict → Mitigation: built-in inputs always win, duplicate plugin names are skipped, and warnings make the issue visible.
- Exposing `@formkit/vue` as an external couples plugins to Halo's FormKit major version → Mitigation: this is intentional for lifecycle compatibility; document that plugin inputs use Halo's provided FormKit runtime.
- Plugin bundle failure now happens earlier in startup → Mitigation: failure still degrades to built-in FormKit inputs and does not block core UI mounting.

## Migration Plan

No migration is required for existing plugins because `formkit` is optional. Existing `$cmp` schema usage and plugin components/routes/extension points continue to work.

Plugin authors who need FormKit lifecycle support can opt in by adding `formkit.inputs` and using the registered input type in their FormKit schema.

Rollback is straightforward: remove the optional `formkit` API handling and the `@formkit/vue` external mapping. Existing plugins that did not opt in are unaffected.

## Open Questions

None.
