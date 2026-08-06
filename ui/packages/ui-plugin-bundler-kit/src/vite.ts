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

  /**
   * Custom Vite config.
   */
  vite: UserConfig | UserConfigFnObject;
}

function createVitePresetsConfig(
  provider: Provider,
  manifestPath: string,
  requestedFormat?: ProviderFormat,
  targetHaloVersion?: string
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
      base: selection.format === "esm" ? defaults.esmBase : defaults.legacyBase,
      plugins: [
        Vue(),
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
        ...(selection.format === "esm" ? { cssCodeSplit: true } : {}),
        outDir: isProduction ? defaults.outDir.prod : defaults.outDir.dev,
        emptyOutDir: true,
        lib: {
          entry: "src/index.ts",
          ...(selection.format === "iife" ? { name: defaults.moduleName } : {}),
          formats: [selection.format === "iife" ? "iife" : "es"],
          fileName: () => "main.js",
          cssFileName: "style",
        },
        ...(selection.format === "iife"
          ? {
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
              rollupOptions: {
                output: {
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
    esmBase: `/plugins/${getManifestName(manifest)}/assets/${bundleLocation}/`,
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
    esmBase: getHaloThemeAssetPublicPath(manifest),
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
    config?.targetHaloVersion
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
