## 1. Provider Model

- [x] 1.1 Add a shared `provider?: "plugin" | "theme"` option for modern Vite and Rsbuild user configs, defaulting to `plugin`.
- [x] 1.2 Split manifest parsing into provider-aware helpers that preserve plugin manifest behavior and add minimal theme manifest parsing.
- [x] 1.3 Define provider defaults for manifest path, module name, output directory, asset public path, and bundle location without changing the deprecated legacy helper.

## 2. Bundler Configs

- [x] 2.1 Update `viteConfig` to apply plugin defaults unchanged when provider is omitted or `plugin`.
- [x] 2.2 Update `viteConfig` theme provider defaults for `../theme.yaml`, `dist`, `theme:{name}`, `/themes/{name}/ui-plugin/assets/`, `main.js`, and `style.css`.
- [x] 2.3 Update `rsbuildConfig` to apply plugin defaults unchanged when provider is omitted or `plugin`.
- [x] 2.4 Update `rsbuildConfig` theme provider defaults for `../theme.yaml`, `dist`, `theme:{name}`, `/themes/{name}/ui-plugin/assets/`, `main.js`, and `style.css`.
- [x] 2.5 Preserve user config override behavior by merging caller config after provider presets.

## 3. Tests and Documentation

- [x] 3.1 Add package-level unit tests for plugin default compatibility and theme provider Vite/Rsbuild config shape.
- [x] 3.2 Add a package `test:unit` script using existing workspace Vitest tooling if needed.
- [x] 3.3 Update the README with plugin default behavior, theme provider configuration, and the recommended `theme-root/ui-plugin` project layout.
- [x] 3.4 Document that the deprecated `HaloUIPluginBundlerKit` helper does not support theme provider builds.

## 4. Validation

- [x] 4.1 Run the focused `ui-plugin-bundler-kit` unit tests.
- [x] 4.2 Run `pnpm -C ui build:packages` to validate package build output.
- [x] 4.3 Run `pnpm -C ui typecheck` for workspace type compatibility.

## 5. Supplemental Test Coverage

- [x] 5.1 Add broader unit coverage for manifest utilities, plugin bundle location selection, legacy Vite plugin defaults, output overrides, custom manifest paths, and function-based user config merging.
