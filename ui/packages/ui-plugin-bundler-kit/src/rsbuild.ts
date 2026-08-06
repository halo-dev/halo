import {
  defineConfig,
  mergeRsbuildConfig,
  type ConfigParams,
  type RsbuildConfig,
  type RsbuildMode,
} from "@rsbuild/core";
import { pluginVue } from "@rsbuild/plugin-vue";
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
import { selectHaloSharedInventory, SHARED_PACKAGE_ROOTS } from "./inventory";
import { createRsbuildEsmProviderPlugin } from "./rsbuild-esm";
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

  /**
   * Custom Rsbuild config.
   */
  rsbuild: RsbuildConfig | ((env: ConfigParams) => RsbuildConfig);
}

function createRsbuildPresetsConfig(
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
  const selectedInventory =
    selection.format === "esm"
      ? selectHaloSharedInventory(selection.targetHaloVersion as string)
      : undefined;
  if (selectedInventory?.reusedOlderInventory) {
    console.warn(
      `[ui-plugin-bundler-kit] Halo ${selection.targetHaloVersion} is newer than bundled inventories; reusing ${selectedInventory.inventory.haloVersion}. Update the bundler to use newly introduced exports.`
    );
  }

  return defineConfig(({ envMode }) => {
    const isProduction = envMode === "production";

    const outDir = isProduction ? defaults.outDir.prod : defaults.outDir.dev;

    return {
      mode: (envMode as RsbuildMode) || "production",
      plugins: [
        pluginVue(),
        ...(selectedInventory
          ? [
              createRsbuildEsmProviderPlugin({
                inventory: selectedInventory.inventory,
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
            moduleIds: "named",
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
            publicPath: defaults.publicPath,
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
              return "style.css";
            }
            return "[name].[contenthash:8].css";
          },
          js: (pathData) => {
            if (pathData.chunk?.name === "main") {
              return "main.js";
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
 * import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit";
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
  const presetsConfigFn = createRsbuildPresetsConfig(
    provider,
    getManifestPath(provider, config),
    config?.format,
    config?.targetHaloVersion
  );
  return defineConfig((env) => {
    const presetsConfig = presetsConfigFn(env);
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
  console.info(
    `[ui-plugin-bundler-kit] Selected ${selection.format.toUpperCase()} output (${selection.reason})${selection.targetHaloVersion ? ` for Halo ${selection.targetHaloVersion}` : ""}.`
  );
}
