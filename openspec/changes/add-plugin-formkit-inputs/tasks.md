## 1. Plugin Module API

- [x] 1.1 Add an optional `formkit.inputs` contract to the shared UI plugin module type.
- [x] 1.2 Use FormKit type-only imports so the shared package exposes the input definition shape without adding unnecessary runtime coupling.
- [x] 1.3 Keep existing plugin module declarations source-compatible when `formkit` is omitted.

## 2. FormKit Input Collection

- [x] 2.1 Extract Halo built-in FormKit input definitions into a reusable object that can be merged with plugin inputs.
- [x] 2.2 Implement a small utility to collect synchronous plugin `formkit.inputs` maps.
- [x] 2.3 Preserve built-in input precedence and skip conflicting plugin inputs with warnings.
- [x] 2.4 Preserve first-loaded plugin precedence and skip later duplicate plugin inputs with warnings.
- [x] 2.5 Ignore invalid plugin FormKit input shapes defensively with warnings.

## 3. Startup Flow

- [x] 3.1 Split enabled plugin bundle loading from plugin module initialization.
- [x] 3.2 For authenticated Console startup, load plugin modules before installing FormKit, collect accepted plugin inputs, install FormKit, then initialize core and plugin modules.
- [x] 3.3 Apply the same authenticated startup behavior to User Center.
- [x] 3.4 Keep anonymous startup on built-in FormKit inputs only and avoid loading plugin FormKit inputs before auth redirect.
- [x] 3.5 If plugin bundle loading fails during input collection, continue core startup with built-in inputs and preserve existing plugin load failure notification behavior.

## 4. Plugin External Runtime

- [x] 4.1 Add `@formkit/vue` to the main UI external library injection configuration.
- [x] 4.2 Add `@formkit/vue` to the UI plugin bundler external/global mappings.
- [x] 4.3 Verify plugins can import `createInput` from `@formkit/vue` without bundling their own FormKit Vue runtime.

## 5. Tests and Documentation

- [x] 5.1 Add focused frontend tests for collecting plugin FormKit inputs.
- [x] 5.2 Add focused frontend tests for built-in and plugin duplicate-name conflict handling.
- [x] 5.3 Update custom FormKit input documentation with the plugin `formkit.inputs` API, a lazy component example, conflict behavior, and naming guidance.

## 6. Validation

- [x] 6.1 Run the relevant frontend unit tests.
- [x] 6.2 Run `pnpm -C ui typecheck`.
- [x] 6.3 Run `pnpm -C ui lint`.
- [x] 6.4 Run `openspec validate add-plugin-formkit-inputs --strict`.
