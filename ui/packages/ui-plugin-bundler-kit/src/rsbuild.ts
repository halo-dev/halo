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
import {
  getHaloPluginBundleLocation,
  getHaloPluginManifest,
  getHaloThemeAssetPublicPath,
  getHaloThemeManifest,
  getHaloThemeModuleName,
  getManifestName,
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
   * Custom Rsbuild config.
   */
  rsbuild: RsbuildConfig | ((env: ConfigParams) => RsbuildConfig);
}

function createRsbuildPresetsConfig(provider: Provider, manifestPath: string) {
  const defaults =
    provider === "theme"
      ? getThemeProviderDefaults(manifestPath)
      : getPluginProviderDefaults(manifestPath);

  return defineConfig(({ envMode }) => {
    const isProduction = envMode === "production";

    const outDir = isProduction ? defaults.outDir.prod : defaults.outDir.dev;

    return {
      mode: (envMode as RsbuildMode) || "production",
      plugins: [pluginVue()],
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
            library: {
              type: "window",
              export: "default",
              name: defaults.moduleName,
            },
            globalObject: "window",
            iife: true,
          },
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
        externals: GLOBALS,
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
    getManifestPath(provider, config)
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
