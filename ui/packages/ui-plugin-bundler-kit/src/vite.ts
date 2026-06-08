import Vue from "@vitejs/plugin-vue";
import {
  defineConfig,
  mergeConfig,
  UserConfig,
  UserConfigFnObject,
} from "vite";
import {
  DEFAULT_OUT_DIR_PROD,
  DEFAULT_THEME_OUT_DIR,
  getDefaultOutDirDev,
} from "./constants/build";
import { EXTERNALS, GLOBALS } from "./constants/externals";
import {
  DEFAULT_PLUGIN_MANIFEST_PATH,
  DEFAULT_THEME_MANIFEST_PATH,
} from "./constants/halo-plugin";
import {
  getHaloPluginBundleLocation,
  getHaloPluginManifest,
  getHaloThemeAssetPublicPath,
  getHaloThemeManifest,
  getHaloThemeModuleName,
  getManifestName,
} from "./utils/halo-plugin";

type Provider = "plugin" | "theme";

export interface ViteUserConfig {
  /**
   * UI plugin provider type.
   *
   * @default "plugin"
   */
  provider?: "plugin" | "theme";

  /**
   * Halo plugin or theme manifest path.
   *
   * @default "../src/main/resources/plugin.yaml" for plugins, "../theme.yaml" for themes
   */
  manifestPath?: string;

  /**
   * Custom Vite config.
   */
  vite: UserConfig | UserConfigFnObject;
}

function createVitePresetsConfig(provider: Provider, manifestPath: string) {
  const defaults =
    provider === "theme"
      ? getThemeProviderDefaults(manifestPath)
      : getPluginProviderDefaults(manifestPath);

  return defineConfig(({ mode }) => {
    const isProduction = mode === "production";

    return {
      mode: mode || "production",
      base: defaults.base,
      plugins: [Vue()],
      define: { "process.env.NODE_ENV": "'production'" },
      build: {
        outDir: isProduction ? defaults.outDir.prod : defaults.outDir.dev,
        emptyOutDir: true,
        lib: {
          entry: "src/index.ts",
          name: defaults.moduleName,
          formats: ["iife"],
          fileName: () => "main.js",
          cssFileName: "style",
        },
        rollupOptions: {
          external: EXTERNALS,
          output: {
            globals: GLOBALS,
            extend: true,
          },
        },
      },
    };
  });
}

function getPluginProviderDefaults(manifestPath: string) {
  const manifest = getHaloPluginManifest(manifestPath);
  const bundleLocation = getHaloPluginBundleLocation(manifest);

  return {
    moduleName: getManifestName(manifest),
    outDir: {
      prod: DEFAULT_OUT_DIR_PROD,
      dev: getDefaultOutDirDev(bundleLocation),
    },
    base: undefined,
  };
}

function getThemeProviderDefaults(manifestPath: string) {
  const manifest = getHaloThemeManifest(manifestPath);

  return {
    moduleName: getHaloThemeModuleName(manifest),
    outDir: {
      prod: DEFAULT_THEME_OUT_DIR,
      dev: DEFAULT_THEME_OUT_DIR,
    },
    base: getHaloThemeAssetPublicPath(manifest),
  };
}

function getProvider(config?: ViteUserConfig): Provider {
  return config?.provider || "plugin";
}

function getManifestPath(provider: Provider, config?: ViteUserConfig) {
  if (config?.manifestPath) {
    return config.manifestPath;
  }
  return provider === "theme"
    ? DEFAULT_THEME_MANIFEST_PATH
    : DEFAULT_PLUGIN_MANIFEST_PATH;
}

/**
 * Vite config for Halo UI Plugin.
 *
 * @example
 * ```ts
 * import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";
 *
 * export default viteConfig({
 *   vite: {
 *     // your custom vite config
 *   },
 * });
 * ```
 */
export function viteConfig(config?: ViteUserConfig) {
  const provider = getProvider(config);
  const presetsConfigFn = createVitePresetsConfig(
    provider,
    getManifestPath(provider, config)
  );
  return defineConfig((env) => {
    const presetsConfig = presetsConfigFn(env);
    const userConfig =
      typeof config?.vite === "function"
        ? config.vite(env)
        : config?.vite || {};
    return mergeConfig(presetsConfig, userConfig);
  });
}
