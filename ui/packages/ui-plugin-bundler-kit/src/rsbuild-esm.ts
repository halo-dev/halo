import type { RsbuildPlugin } from "@rsbuild/core";
import type { HaloSharedInventory } from "./inventory";
import { SHARED_PACKAGE_ROOTS } from "./inventory";
import {
  ESM_PROVIDER_MANIFEST,
  validateEsmProviderManifest,
} from "./provider-manifest";
import { SharedDependencyValidator } from "./shared-dependencies";

interface RsbuildEsmPluginOptions {
  inventory: HaloSharedInventory;
  providerRoot: string;
}

export function createRsbuildEsmProviderPlugin(
  options: RsbuildEsmPluginOptions
): RsbuildPlugin {
  const validator = new SharedDependencyValidator({
    inventory: options.inventory,
    providerRoot: options.providerRoot,
    warn: console.warn,
  });

  return {
    name: "halo:esm-ui-provider",
    setup(api) {
      api.modifyRspackConfig((config) => {
        const aliases = config.resolve?.alias || {};
        for (const root of SHARED_PACKAGE_ROOTS) {
          if (Object.hasOwn(aliases, root)) {
            throw new Error(
              `Rsbuild alias for shared dependency ${root} would bypass Halo inventory validation.`
            );
          }
        }
        if (
          config.output?.library &&
          typeof config.output.library === "object" &&
          "type" in config.output.library &&
          config.output.library.type !== "module"
        ) {
          throw new Error(
            "ESM UI provider output requires Rspack output.library.type module."
          );
        }

        config.experiments = {
          ...config.experiments,
          outputModule: true,
        };
        config.externalsType = "module";
        config.output = {
          ...config.output,
          module: true,
          iife: false,
          chunkFormat: "module",
          chunkLoading: "import",
          chunkFilename: "chunks/[name].[contenthash:8].js",
          library: { type: "module" },
        };
        config.externals = [
          ...(Array.isArray(config.externals)
            ? config.externals
            : config.externals
              ? [config.externals]
              : []),
          Object.fromEntries(SHARED_PACKAGE_ROOTS.map((root) => [root, root])),
        ];
      });

      api.resolve(({ resolveData }) => {
        validator.shouldExternalize(
          resolveData.request,
          resolveData.contextInfo.issuer || "provider entry"
        );
      });

      api.transform(
        {
          test: /\.[cm]?[jt]sx?$/,
          enforce: "post",
        },
        async ({ code, resourcePath }) => {
          await validator.validateSource(code, resourcePath);
          return code;
        }
      );

      api.processAssets(
        { stage: "summarize" },
        async ({ assets, compilation, sources }) => {
          const entry = assets["main.js"];
          if (!entry) {
            throw new Error("ESM UI provider output is missing main.js.");
          }
          const entryCode = entry.source().toString();
          await validator.validateSource(entryCode, "main.js");
          if (
            !/\bexport\s+default\b/.test(entryCode) &&
            !/\bexport\s*\{[^}]*\bdefault\b[^}]*\}/s.test(entryCode)
          ) {
            throw new Error(
              "ESM UI provider output must expose a default PluginModule export."
            );
          }
          const styles = Object.keys(assets)
            .filter((fileName) => fileName.endsWith(".css"))
            .sort()
            .map((fileName) => `./${fileName}`);
          const manifest = validateEsmProviderManifest({
            format: "esm",
            entry: "./main.js",
            styles,
          });
          compilation.emitAsset(
            ESM_PROVIDER_MANIFEST,
            new sources.RawSource(`${JSON.stringify(manifest, null, 2)}\n`)
          );
          console.info(
            `[ui-plugin-bundler-kit] ESM output validated against Halo ${options.inventory.haloVersion}:\n${validator.getBuildSummary().join("\n") || "no shared roots"}`
          );
        }
      );
    },
  };
}
