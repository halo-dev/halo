import Vue, { type Options as VuePluginOptions } from "@vitejs/plugin-vue";
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
import { selectHaloHostRuntimeSnapshot } from "./runtime-snapshot";
import {
  getHaloPluginBundleLocation,
  getHaloPluginManifest,
  getHaloThemeAssetPublicPath,
  getHaloThemeManifest,
  getHaloThemeModuleName,
  getManifestName,
  getManifestRequires,
  ProviderFormat,
  selectProviderFormat,
} from "./utils/halo-plugin";
import { createViteEsmProviderPlugin } from "./vite-esm";

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
   * Provider output format.
   *
   * @default "auto"
   */
  format?: ProviderFormat;

  /** Explicit Halo target used when ESM cannot be derived from spec.requires. */
  targetHaloVersion?: string;

  /** Options for the built-in Vue plugin. */
  vue?: VuePluginOptions;

  /**
   * Custom Vite config.
   */
  vite: UserConfig | UserConfigFnObject;
}

function createVitePresetsConfig(
  provider: Provider,
  manifestPath: string,
  requestedFormat?: ProviderFormat,
  targetHaloVersion?: string,
  vueOptions?: VuePluginOptions
) {
  const defaults =
    provider === "theme"
      ? getThemeProviderDefaults(manifestPath)
      : getPluginProviderDefaults(manifestPath);
  const selection = selectProviderFormat({
    format: requestedFormat,
    requires: defaults.requires,
    targetHaloVersion,
  });
  reportFormatSelection(selection);
  const selectedSnapshot =
    selection.format === "esm"
      ? selectHaloHostRuntimeSnapshot(selection.targetHaloVersion as string)
      : undefined;
  if (selectedSnapshot?.reusedOlderSnapshot) {
    console.warn(
      `[ui-plugin-bundler-kit] Halo ${selection.targetHaloVersion} is newer than bundled host runtime snapshots; reusing ${selectedSnapshot.snapshot.haloVersion}. Update the bundler to use newly introduced exports.`
    );
  }

  return defineConfig(({ mode }) => {
    const isProduction = mode === "production";

    return {
      mode: mode || "production",
      base: selection.format === "esm" ? "./" : defaults.legacyBase,
      plugins: [
        Vue(vueOptions),
        ...(selectedSnapshot
          ? [
              createViteEsmProviderPlugin({
                snapshot: selectedSnapshot.snapshot,
                providerRoot: process.cwd(),
              }),
            ]
          : []),
      ],
      define: { "process.env.NODE_ENV": "'production'" },
      build: {
        outDir: isProduction ? defaults.outDir.prod : defaults.outDir.dev,
        emptyOutDir: true,
        ...(selection.format === "iife"
          ? {
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
                  // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
                  globals: GLOBALS,
                  extend: true,
                },
              },
            }
          : {
              cssCodeSplit: true,
              // Vite 8 keeps this as a rolldownOptions alias; use the shared name
              // while the bundler kit also supports Vite 6 and 7.
              rollupOptions: {
                input: "src/index.ts",
                preserveEntrySignatures: "allow-extension",
                output: {
                  format: "es",
                  entryFileNames: "main.js",
                  chunkFileNames: "chunks/[name].[hash].js",
                  assetFileNames: "assets/[name].[hash][extname]",
                },
              },
            }),
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
    legacyBase: undefined,
    requires: getManifestRequires(manifest),
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
    legacyBase: getHaloThemeAssetPublicPath(manifest),
    requires: getManifestRequires(manifest),
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
    getManifestPath(provider, config),
    config?.format,
    config?.targetHaloVersion,
    config?.vue
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

function reportFormatSelection(
  selection: ReturnType<typeof selectProviderFormat>
) {
  for (const warning of selection.warnings) {
    console.warn(`[ui-plugin-bundler-kit] ${warning}`);
  }
  const reason = selection.reason.replace("-", " ");
  console.info(
    `[ui-plugin-bundler-kit] Output: ${selection.format.toUpperCase()} (${reason}${selection.targetHaloVersion ? `; target Halo ${selection.targetHaloVersion}` : ""}).`
  );
}
