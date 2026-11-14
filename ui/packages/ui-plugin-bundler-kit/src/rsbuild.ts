import {
  defineConfig,
  mergeRsbuildConfig,
  type ConfigParams,
  type RsbuildConfig,
  type RsbuildMode,
} from "@rsbuild/core";
import { pluginVue, type PluginVueOptions } from "@rsbuild/plugin-vue";
import {
  DEFAULT_OUT_DIR_PROD,
  DEFAULT_THEME_OUT_DIR,
  getDefaultOutDirDev,
} from "./constants/build";
import { GLOBALS } from "./constants/externals";
import {
  DEFAULT_PLUGIN_MANIFEST_PATH,
  DEFAULT_THEME_MANIFEST_PATH,
} from "./constants/halo-plugin";
import { createRsbuildEsmProviderPlugin } from "./rsbuild-esm";
import {
  selectHaloHostRuntimeSnapshot,
  SHARED_PACKAGE_ROOTS,
} from "./runtime-snapshot";
import {
  getHaloPluginBundleLocation,
  getHaloPluginManifest,
  getHaloThemeAssetPublicPath,
  getHaloThemeManifest,
  getHaloThemeModuleName,
  getManifestName,
  getManifestRequires,
  type ProviderFormat,
  selectProviderFormat,
} from "./utils/halo-plugin";

type Provider = "plugin" | "theme";

export interface RsBuildUserConfig {
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
  vue?: PluginVueOptions;

  /**
   * Custom Rsbuild config.
   */
  rsbuild: RsbuildConfig | ((env: ConfigParams) => RsbuildConfig);
}

function createRsbuildPresetsConfig(
  provider: Provider,
  manifestPath: string,
  requestedFormat?: ProviderFormat,
  targetHaloVersion?: string,
  vueOptions?: PluginVueOptions
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
      `[ui-plugin-bundler-kit] Halo ${selection.targetHaloVersion} is newer than bundled host runtime snapshots; reusing ${selectedSnapshot.snapshot.haloVersion}. Update the bundler kit for the target Halo dependency baseline.`
    );
  }

  const config = defineConfig(({ envMode }) => {
    const isProduction = envMode === "production";

    const outDir = isProduction ? defaults.outDir.prod : defaults.outDir.dev;

    return {
      mode: (envMode as RsbuildMode) || "production",
      plugins: [
        pluginVue(vueOptions),
        ...(selectedSnapshot
          ? [
              createRsbuildEsmProviderPlugin({
                snapshot: selectedSnapshot.snapshot,
                providerRoot: process.cwd(),
              }),
            ]
          : []),
      ],
      source: {
        entry: {
          main: "./src/index.ts",
        },
      },
      dev: {
        hmr: false,
        ...(selection.format === "esm" ? { assetPrefix: "auto" } : {}),
      },
      performance: {
        chunkSplit: {
          strategy: "custom",
        },
      },
      tools: {
        rspack: {
          optimization: {
            splitChunks: {
              chunks: "async",
            },
            ...(selection.format === "iife" ? { moduleIds: "named" } : {}),
          },
          experiments: {
            rspackFuture: {
              bundlerInfo: {
                force: false,
              },
            },
            ...(selection.format === "esm" ? { outputModule: true } : {}),
          },
          module: {
            parser: {
              javascript: {
                importMeta: false,
              },
            },
          },
          output: {
            publicPath:
              selection.format === "esm" ? "auto" : defaults.publicPath,
            // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
            library:
              selection.format === "iife"
                ? {
                    type: "window",
                    export: "default",
                    name: defaults.moduleName,
                  }
                : { type: "module" },
            globalObject: "window",
            iife: selection.format === "iife",
            ...(selection.format === "esm"
              ? {
                  module: true,
                  chunkFormat: "module",
                  chunkLoading: "import",
                }
              : {}),
          },
          ...(selection.format === "esm" ? { externalsType: "module" } : {}),
        },
        htmlPlugin: false,
      },
      output: {
        ...(selection.format === "esm" ? { assetPrefix: "auto" } : {}),
        distPath: {
          root: outDir,
          js: "",
          css: "",
          jsAsync: "chunks",
          cssAsync: "chunks",
        },
        cleanDistPath: true,
        filename: {
          css: (pathData) => {
            if (pathData.chunk?.name === "main") {
              return selection.format === "esm"
                ? "style.[contenthash:8].css"
                : "style.css";
            }
            return "[name].[contenthash:8].css";
          },
          js: (pathData) => {
            if (pathData.chunk?.name === "main") {
              return selection.format === "esm"
                ? "main.[contenthash:8].js"
                : "main.js";
            }
            return "[name].[contenthash:8].js";
          },
        },
        externals:
          selection.format === "iife"
            ? GLOBALS
            : Object.fromEntries(
                SHARED_PACKAGE_ROOTS.map((root) => [root, root])
              ),
      },
    } as RsbuildConfig;
  });
  return { selection, config };
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
    publicPath: `/plugins/${getManifestName(manifest)}/assets/${bundleLocation}/`,
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
    publicPath: getHaloThemeAssetPublicPath(manifest),
    requires: getManifestRequires(manifest),
  };
}

function getProvider(config?: RsBuildUserConfig): Provider {
  return config?.provider || "plugin";
}

function getManifestPath(provider: Provider, config?: RsBuildUserConfig) {
  if (config?.manifestPath) {
    return config.manifestPath;
  }
  return provider === "theme"
    ? DEFAULT_THEME_MANIFEST_PATH
    : DEFAULT_PLUGIN_MANIFEST_PATH;
}

/**
 * Rsbuild config for Halo UI Plugin.
 *
 * @example
 * ```ts
 * import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit/rsbuild";
 *
 * export default rsbuildConfig({
 *   rsbuild: {
 *     // your custom rsbuild config
 *   },
 * });
 * ```
 * @param config
 * @returns
 */
export function rsbuildConfig(
  config?: RsBuildUserConfig
): (env: ConfigParams) => RsbuildConfig {
  const provider = getProvider(config);
  const presets = createRsbuildPresetsConfig(
    provider,
    getManifestPath(provider, config),
    config?.format,
    config?.targetHaloVersion,
    config?.vue
  );
  return defineConfig((env) => {
    const presetsConfig = presets.config(env);
    const userConfig =
      typeof config?.rsbuild === "function"
        ? config.rsbuild(env)
        : config?.rsbuild || {};
    return mergeRsbuildConfig(presetsConfig, userConfig);
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
