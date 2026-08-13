# @halo-dev/ui-plugin-bundler-kit

A frontend build toolkit for Halo UI plugin development, supporting both Vite and Rsbuild build systems.

## Introduction

`@halo-dev/ui-plugin-bundler-kit` is a frontend build configuration toolkit specifically designed for Halo UI plugin development. It provides pre-configured build settings to help developers quickly set up and build frontend interfaces for Halo plugins and theme-provided UI plugins.

### Key Features

- 🚀 **Ready to Use** - Provides pre-configured Vite and Rsbuild build settings
- 📦 **Multi-Build Tool Support** - Supports both Vite and Rsbuild
- 🔧 **Flexible Configuration** - Supports custom build configurations
- 🎯 **Halo Optimized** - External dependencies and global variables optimized for Halo UI plugin development
- 📁 **Smart Output** - Automatically selects output directory based on environment

## Installation

```bash
# Using npm
npm install @halo-dev/ui-plugin-bundler-kit

# Using yarn
yarn add @halo-dev/ui-plugin-bundler-kit

# Using pnpm
pnpm add @halo-dev/ui-plugin-bundler-kit
```

### Additional Dependencies

**For Vite users**, install Vite and its Vue plugin:

```bash
npm install vite @vitejs/plugin-vue
```

**For Rsbuild users**, install Rsbuild and its Vue plugin:

```bash
npm install @rsbuild/core @rsbuild/plugin-vue
```

Since 2.26.0, import the configuration helper from its build-system-specific entry point. Imports from the package root are deprecated and will be removed in 2.27.0.

## Usage

### Vite Configuration

Create or update `vite.config.ts` file in your UI plugin project root:

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";

export default viteConfig({
  // provider defaults to "plugin"
  vite: {
    // Your custom Vite configuration
    plugins: [
      // Additional plugins (Vue plugin is already included)
    ],
    // Other configurations...
  },
});
```

> **Note**: Vue plugin is pre-configured. Pass its options through the top-level `vue` field instead of adding another `@vitejs/plugin-vue` instance.

### Rsbuild Configuration

Create or update `rsbuild.config.ts` file in your UI plugin project root:

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit/rsbuild";

export default rsbuildConfig({
  // provider defaults to "plugin"
  rsbuild: {
    // Your custom Rsbuild configuration
    plugins: [
      // Additional plugins (Vue plugin is already included)
    ],
    // Other configurations...
  },
});
```

> **Note**: Vue plugin is pre-configured. Pass its options through the top-level `vue` field instead of adding another `@rsbuild/plugin-vue` instance.

### Theme UI Plugin Configuration

For theme-provided Console/User Center UI plugins, place the frontend project under the theme's `ui-plugin/` directory:

```text
theme-root/
├── theme.yaml
└── ui-plugin/
    ├── package.json
    ├── src/index.ts
    └── vite.config.ts
```

Vite:

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";

export default viteConfig({
  provider: "theme",
  vite: {},
});
```

Rsbuild:

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit/rsbuild";

export default rsbuildConfig({
  provider: "theme",
  rsbuild: {},
});
```

The theme provider reads `../theme.yaml`, outputs to `dist`, registers the module as `theme:{metadata.name}`, and configures assets for `/themes/{metadata.name}/ui-plugin/assets/`. Halo reads only `ui-plugin/dist/**` from the theme package.

### Output Format and Halo Target

`viteConfig` and `rsbuildConfig` accept the same format options:

```typescript
export default viteConfig({
  format: "auto", // "auto" | "iife" | "esm"
  vite: {},
});
```

`auto` is the default. It emits ESM when `spec.requires` is a stable Halo version or a simple `>=MAJOR.MINOR.PATCH` target whose minimum is Halo 2.26.0 or newer. Missing, wildcard, composite, or otherwise unsupported ranges produce a warning and keep the compatible IIFE output. This fallback is intentional: ESM is optional, and Halo continues loading old IIFE plugin and theme artifacts throughout Halo 2.x.

Use an explicit target only when intentionally forcing ESM and a target cannot be derived:

```typescript
export default viteConfig({
  format: "esm",
  targetHaloVersion: "2.26.0",
  vite: {},
});
```

After ESM is selected, successful builds generate `ui-plugin.json`. This filename is reserved for the bundler kit: do not create, copy, or emit another file with that name. A provider artifact without this file remains legacy, even if its `spec.requires` also supports Halo 2.26 or newer.

ESM entries must default-export the existing `PluginModule`. The generated manifest records one optional main stylesheet; CSS belonging to asynchronous chunks stays out of the manifest and loads on demand with its JavaScript chunk. Halo starts every provider-owned startup stylesheet and entry in parallel, then commits modules in provider order without reloading after each entry. Top-level module effects, timers, listeners, and arbitrary asynchronous effects are not transactional; a full page reload remains the lifecycle and recovery boundary after provider changes.

The default Vite and Rsbuild presets provide ESM output, provider-relative resource paths, content-hashed entry and startup-style filenames, content-hashed secondary resources, and Halo shared-runtime externals. The generated manifest records the actual startup filenames. Raw `vite` and `rsbuild` configuration is merged after these defaults and is not inspected, rejected, or rewritten. If it changes entry names, formats, public paths, externals, or caching behavior, the caller owns the resulting manifest consistency, Import Map compatibility, shared dependency identity, resource relocation, and cache safety. Compatible IIFE overrides retain their previous stable `main.js` behavior.

### Shared Runtime Dependencies

ESM providers may import these package roots from Halo:

- `vue`
- `vue-router`
- `pinia`
- `axios`
- `@formkit/vue`
- `@formkit/core`
- `@halo-dev/ui-shared`
- `@halo-dev/components`
- `@halo-dev/api-client`
- `@halo-dev/richtext-editor`

The bundler discovers shared package root imports and reports the installed provider version beside the selected Halo host snapshot version when package metadata is available. A newer provider dependency emits a compatibility note, and a different major emits a stronger note, but version drift does not fail the build. Export usage, aliases, forks, and final bundler resolution are not inspected. Deep imports such as `vue/dist/vue.esm-bundler.js` still fail because Halo's Import Map exposes shared package roots only.

Vue, Vue Router, Pinia, and the FormKit Vue/Core graph share host identity. Other `@formkit/*` packages and VueUse stay provider-private. Non-shared dependencies are bundled by the default presets.

The shared `axios` import is the standard package module. Do not mutate its shared defaults or interceptors. For isolated clients, call `axios.create()`. `@halo-dev/api-client` exports Halo's separate authenticated `axiosInstance`; do not mutate that instance either.

### Querying Other UI Providers

Use the shared registration store instead of checking another provider's `window.PluginName` global:

```typescript
import { stores } from "@halo-dev/ui-shared";

const uiPlugins = stores.uiPlugins();

uiPlugins.isEnabled("plugin-search");
uiPlugins.isRegistered("plugin-search");
uiPlugins.get("plugin-search");
```

The reactive record contains only Halo-owned `name`, `type`, `version`, and `pending | registered | failed` status. `isEnabled` means the provider was discovered in the current descriptor; `isRegistered` becomes true after its current-page registration succeeds. Provider code treats this store as read-only and must not depend on another provider's evaluation order or module object.

### Legacy Configuration (Deprecated)

> ⚠️ **Note**: The `HaloUIPluginBundlerKit` function is deprecated and will be removed in 2.27.0. Import `viteConfig` from `@halo-dev/ui-plugin-bundler-kit/vite` or `rsbuildConfig` from `@halo-dev/ui-plugin-bundler-kit/rsbuild` instead. It does not support `provider: "theme"`.

```typescript
import { HaloUIPluginBundlerKit } from "@halo-dev/ui-plugin-bundler-kit";

export default {
  plugins: [
    HaloUIPluginBundlerKit({
      // Configuration options
    }),
  ],
};
```

## Configuration Options

### Vite Configuration Options

```typescript
interface ViteUserConfig {
  /**
   * UI plugin provider type
   * @default "plugin"
   */
  provider?: "plugin" | "theme";

  /**
   * Halo plugin or theme manifest file path
   * @default "../src/main/resources/plugin.yaml" for plugins, "../theme.yaml" for themes
   */
  manifestPath?: string;

  /** @default "auto" */
  format?: "auto" | "iife" | "esm";

  /** Required for explicit ESM when spec.requires has no derivable target. */
  targetHaloVersion?: string;

  /** Options for the built-in @vitejs/plugin-vue instance. */
  vue?: VuePluginOptions;

  /**
   * Custom Vite configuration
   */
  vite: UserConfig | UserConfigFnObject;
}
```

### Rsbuild Configuration Options

```typescript
interface RsBuildUserConfig {
  /**
   * UI plugin provider type
   * @default "plugin"
   */
  provider?: "plugin" | "theme";

  /**
   * Halo plugin or theme manifest file path
   * @default "../src/main/resources/plugin.yaml" for plugins, "../theme.yaml" for themes
   */
  manifestPath?: string;

  /** @default "auto" */
  format?: "auto" | "iife" | "esm";

  /** Required for explicit ESM when spec.requires has no derivable target. */
  targetHaloVersion?: string;

  /** Options for the built-in @rsbuild/plugin-vue instance. */
  vue?: PluginVueOptions;

  /**
   * Custom Rsbuild configuration
   */
  rsbuild: RsbuildConfig | ((env: ConfigParams) => RsbuildConfig);
}
```

## Advanced Configuration Examples

### Customizing the Vue Compiler

Vite:

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";

export default viteConfig({
  vue: {
    template: {
      compilerOptions: {
        isCustomElement: (tag) => tag === "halo-app-card",
      },
    },
  },
  vite: {},
});
```

Rsbuild:

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit/rsbuild";

export default rsbuildConfig({
  vue: {
    vueLoaderOptions: {
      compilerOptions: {
        isCustomElement: (tag) => tag === "halo-app-card",
      },
    },
  },
  rsbuild: {},
});
```

The helper owns the Vue plugin instance. Keep `@vitejs/plugin-vue` and `@rsbuild/plugin-vue` out of the nested `plugins` array to avoid running the SFC transform twice.

### Adding Path Aliases (Vite)

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";
import path from "path";

export default viteConfig({
  vite: {
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
        "@components": path.resolve(__dirname, "src/components"),
      },
    },
  },
});
```

### Adding Path Aliases (Rsbuild)

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit/rsbuild";

export default rsbuildConfig({
  rsbuild: {
    source: {
      alias: {
        "@": "./src",
        "@components": "./src/components",
      },
    },
  },
});
```

### Adding Additional Vite Plugins

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";
import { defineConfig } from "vite";
import UnoCSS from "unocss/vite";

export default viteConfig({
  vite: {
    plugins: [
      UnoCSS(), // Add UnoCSS plugin
    ],
  },
});
```

### Adding Additional Rsbuild Plugins

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit/rsbuild";
import { pluginSass } from "@rsbuild/plugin-sass";

export default rsbuildConfig({
  rsbuild: {
    plugins: [
      pluginSass(), // Add Sass plugin
    ],
  },
});
```

### Custom Plugin Manifest Path

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";

export default viteConfig({
  manifestPath: "application/src/main/resources/plugin.yaml", // Custom manifest file path
  vite: {
    // Other configurations...
  },
});
```

### Custom Theme Manifest Path

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit/vite";

export default viteConfig({
  provider: "theme",
  manifestPath: "../custom-theme.yaml",
  vite: {
    // Other configurations...
  },
});
```

## Development Scripts

Recommended scripts to add to your `package.json`:

```json
{
  "scripts": {
    "dev": "vite dev --mode=development --watch",
    "build": "vite build"
  }
}
```

For Rsbuild:

```json
{
  "scripts": {
    "dev": "rsbuild dev --env-mode=development --watch",
    "build": "rsbuild build"
  }
}
```

## Build Output

> Relative to the UI plugin project root

Plugin provider:

- **Development**: `../build/resources/main/ui` or `../build/resources/main/console`
- **Production**: `./build/dist`

Theme provider:

- **Development**: `dist`
- **Production**: `dist`

> **Note**: The production build output directory of `HaloUIPluginBundlerKit` is still `src/main/resources/console` to ensure compatibility.

An ESM output additionally contains the reserved, generated `ui-plugin.json` manifest and may contain content-hashed `chunks/` and `assets/`. The manifest contains `format`, the actual content-hashed `entry`, and an optional content-hashed `style`; asynchronous chunk CSS is not listed. Keep the complete output directory together. Halo serves these canonical paths through the existing plugin or theme static resource mapping without adding query cache keys, so imports back to the entry resolve to the same ESM module URL. Callers that replace the default content-hashed filenames accept the risk of stale resources under Halo's production static-resource cache. Legacy IIFE resources retain their version query behavior. The Halo 2.x compatibility `bundle.css` endpoint remains available for older callers, but now contains ordered `@import` rules pointing at those direct styles so relative asset URLs keep the correct provider base; it is not used by the new runtime.

## Maintaining Halo Host Runtime Snapshots

Snapshots capture the exact host version, statically importable root exports that are also present on the browser runtime global, bridge global, and identity category for each shared root. Synthetic namespace metadata such as `__esModule` is excluded when the browser global does not expose it. Snapshots do not claim an accepted provider version range. They are intentionally sparse: add a new immutable snapshot when Halo's shared runtime contract changes, not automatically for every Halo patch. A newer target selects the latest eligible older snapshot and emits a forward-compatibility warning; a target older than every packaged snapshot cannot build ESM.

When the host dependency graph changes:

1. Set `@halo-dev/ui-plugin-bundler-kit` to the Halo version represented by the snapshot.
2. Run `pnpm --filter @halo-dev/ui-plugin-bundler-kit snapshot:generate` to derive the versioned output path and capture resolved host versions and exports.
3. Run `pnpm --filter @halo-dev/ui-plugin-bundler-kit snapshot:check` and the compatibility fixtures before publishing.
4. Preserve older snapshot files while supported provider artifacts may still target them.

Snapshot generation is currently an explicit maintainer action; it is not coupled to every package build or CI job.

## Requirements

- **Node.js**: ^18.0.0 || >=20.0.0
- **Peer Dependencies**:
  - `@rsbuild/core`: ^1.0.0 || ^2.0.0 (when using Rsbuild)
  - `@rsbuild/plugin-vue`: ^1.0.0 || ^2.0.0 (when using Rsbuild)
  - `@vitejs/plugin-vue`: ^5.0.0 || ^6.0.0 (when using Vite)
  - `vite`: ^6.0.0 || ^7.0.0 || ^8.0.0 (when using Vite)

## Vite vs Rsbuild

Both Vite and Rsbuild are excellent build tools, but they have different strengths depending on your use case:

### When to Use Rsbuild

**Recommended for large-scale plugins**

- ✅ **Code Splitting Support** - Rsbuild provides excellent support for code splitting and lazy loading
- ✅ **Better Performance** - Generally faster build times and smaller bundle sizes for complex applications
- ✅ **Dynamic Imports** - Perfect for plugins with heavy frontend components

**Example with dynamic imports:**

```typescript
import { definePlugin } from "@halo-dev/ui-shared";
import { defineAsyncComponent } from "vue";
import { VLoading } from "@halo-dev/components";

export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "demo",
        name: "DemoPage",
        // Lazy load heavy components
        component: defineAsyncComponent({
          loader: () => import("./views/DemoPage.vue"),
          loadingComponent: VLoading,
        }),
      },
    },
  ],
  extensionPoints: {},
});
```

### When to Use Vite

**Recommended for simple to medium-scale plugins**

- ✅ **Vue Ecosystem Friendly** - Better integration with Vue ecosystem tools and plugins
- ✅ **Rich Plugin Ecosystem** - Extensive collection of Vite plugins available
- ✅ **Simple Configuration** - Easier to configure for straightforward use cases

### Summary

| Feature           | Vite         | Rsbuild      |
| ----------------- | ------------ | ------------ |
| Code Splitting    | ✅ ESM       | ✅ ESM       |
| Vue Ecosystem     | ✅ Excellent | ✅ Good      |
| Build Performance | ✅ Good      | ✅ Excellent |
| Dev Experience    | ✅ Excellent | ✅ Excellent |
| Plugin Ecosystem  | ✅ Rich      | ✅ Growing   |
| Configuration     | ✅ Simple    | ⚖️ Moderate  |

**Recommendation**: Use **Rsbuild** for complex plugins with large frontend codebases, and **Vite** for simpler plugins or when you need extensive Vue ecosystem integration.

## License

GPL-3.0

## Contributing

Issues and Pull Requests are welcome! Please check our [Contributing Guide](https://github.com/halo-dev/halo/blob/main/CONTRIBUTING.md) for more information.

## Related Links

- [Halo Website](https://www.halo.run/)
- [Halo Documentation](https://docs.halo.run/)
- [GitHub Repository](https://github.com/halo-dev/halo)
- [Plugin Development Guide](https://docs.halo.run/category/ui)
