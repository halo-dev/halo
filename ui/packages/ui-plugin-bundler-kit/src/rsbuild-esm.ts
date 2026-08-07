import type { RsbuildPlugin } from "@rsbuild/core";
import {
  ESM_PROVIDER_MANIFEST,
  validateEsmProviderManifest,
} from "./provider-manifest";
import type { HaloHostRuntimeSnapshot } from "./runtime-snapshot";
import { SHARED_PACKAGE_ROOTS } from "./runtime-snapshot";
import { SharedDependencyValidator } from "./shared-dependencies";

interface RsbuildEsmPluginOptions {
  snapshot: HaloHostRuntimeSnapshot;
  providerRoot: string;
}

export function createRsbuildEsmProviderPlugin(
  options: RsbuildEsmPluginOptions
): RsbuildPlugin {
  const validator = new SharedDependencyValidator({
    snapshot: options.snapshot,
    providerRoot: options.providerRoot,
  });

  return {
    name: "halo:esm-ui-provider",
    setup(api) {
      api.modifyRspackConfig((config) => {
        const aliases = config.resolve?.alias || {};
        for (const root of SHARED_PACKAGE_ROOTS) {
          if (Object.hasOwn(aliases, root)) {
            throw new Error(
              `Rsbuild alias for shared dependency ${root} would bypass Halo snapshot validation.`
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
        if (config.output?.publicPath !== "auto") {
          throw new Error(
            `ESM UI provider output requires the automatic Rsbuild public path so provider resources follow the loaded entry URL; received ${JSON.stringify(config.output?.publicPath)}.`
          );
        }

        assertRspackValue(
          "experiments.outputModule",
          config.experiments?.outputModule,
          true
        );
        assertRspackValue("externalsType", config.externalsType, "module");
        assertRspackValue("output.module", config.output?.module, true);
        assertRspackValue("output.iife", config.output?.iife, false);
        assertRspackValue(
          "output.chunkFormat",
          config.output?.chunkFormat,
          "module"
        );
        assertRspackValue(
          "output.chunkLoading",
          config.output?.chunkLoading,
          "import"
        );
        assertOnlyHaloExternals(config.externals);

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
        config.externals = Object.fromEntries(
          SHARED_PACKAGE_ROOTS.map((root) => [root, root])
        );
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
            throw new Error(
              "ESM UI provider output is missing main.js. Remove entry filename overrides because Halo owns the manifest entry name."
            );
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
          const entryStyles =
            compilation.entrypoints
              .get("main")
              ?.getFiles()
              .filter((fileName) => fileName.endsWith(".css")) || [];
          if (entryStyles.length > 1) {
            throw new Error(
              "ESM UI provider output must contain at most one entry stylesheet."
            );
          }
          const manifest = validateEsmProviderManifest({
            format: "esm",
            entry: "./main.js",
            ...(entryStyles[0] ? { style: `./${entryStyles[0]}` } : {}),
          });
          compilation.emitAsset(
            ESM_PROVIDER_MANIFEST,
            new sources.RawSource(`${JSON.stringify(manifest, null, 2)}\n`)
          );
          const report = validator.getBuildReport();
          console.info(report.summary);
          if (report.warning) {
            console.warn(report.warning);
          }
        }
      );
    },
  };
}

function assertRspackValue(
  path: string,
  actual: unknown,
  expected: string | boolean
) {
  if (actual !== undefined && actual !== expected) {
    throw new Error(
      `ESM UI provider output requires Rspack ${path} to be ${JSON.stringify(expected)}; received ${JSON.stringify(actual)}.`
    );
  }
}

function assertOnlyHaloExternals(externals: unknown) {
  const values = Array.isArray(externals) ? externals : [externals];
  for (const value of values) {
    if (!value) {
      continue;
    }
    if (
      typeof value !== "object" ||
      value instanceof RegExp ||
      Object.keys(value).some(
        (root) => !SHARED_PACKAGE_ROOTS.includes(root as never)
      )
    ) {
      throw new Error(
        "ESM UI provider output cannot externalize dependencies that Halo does not provide. Remove custom Rspack externals or select IIFE output."
      );
    }
  }
}
