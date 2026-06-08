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

**For Vite users**, you need to install Vite:

```bash
npm install vite
```

**For Rsbuild users**, you need to install Rsbuild:

```bash
npm install @rsbuild/core
```

## Usage

### Vite Configuration

Create or update `vite.config.ts` file in your UI plugin project root:

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";

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

> **Note**: Vue plugin is pre-configured, no need to add it manually.

### Rsbuild Configuration

Create or update `rsbuild.config.ts` file in your UI plugin project root:

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit";

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

> **Note**: Vue plugin is pre-configured, no need to add it manually.

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
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";

export default viteConfig({
  provider: "theme",
  vite: {},
});
```

Rsbuild:

```typescript
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit";

export default rsbuildConfig({
  provider: "theme",
  rsbuild: {},
});
```

The theme provider reads `../theme.yaml`, outputs to `dist`, registers the module as `theme:{metadata.name}`, and configures assets for `/themes/{metadata.name}/ui-plugin/assets/`. Halo reads only `ui-plugin/dist/**` from the theme package.

### Legacy Configuration (Deprecated)

> ⚠️ **Note**: The `HaloUIPluginBundlerKit` function is deprecated. Please use `viteConfig` or `rsbuildConfig` instead. It does not support `provider: "theme"`.

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

  /**
   * Custom Rsbuild configuration
   */
  rsbuild: RsbuildConfig | ((env: ConfigParams) => RsbuildConfig);
}
```

## Advanced Configuration Examples

### Adding Path Aliases (Vite)

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";
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
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit";

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
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";
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
import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit";
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
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";

export default viteConfig({
  manifestPath: "application/src/main/resources/plugin.yaml", // Custom manifest file path
  vite: {
    // Other configurations...
  },
});
```

### Custom Theme Manifest Path

```typescript
import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";

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

## Requirements

- **Node.js**: ^18.0.0 || >=20.0.0
- **Peer Dependencies**:
  - `@rsbuild/core`: ^1.0.0 (when using Rsbuild)
  - `@rsbuild/plugin-vue`: ^1.0.0 (when using Rsbuild)
  - `@vitejs/plugin-vue`: ^4.0.0 || ^5.0.0 (when using Vite)
  - `vite`: ^4.0.0 || ^5.0.0 || ^6.0.0 (when using Vite)

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
| Code Splitting    | ❌ Limited   | ✅ Excellent |
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
