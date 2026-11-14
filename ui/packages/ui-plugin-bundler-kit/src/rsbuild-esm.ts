import type { RsbuildPlugin } from "@rsbuild/core";
import {
  ESM_PROVIDER_MANIFEST,
  validateEsmProviderManifest,
} from "./provider-manifest";
import type { HaloHostRuntimeSnapshot } from "./runtime-snapshot";
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
          const entryFiles =
            compilation.entrypoints.get("main")?.getFiles() || [];
          const entryScripts = entryFiles.filter((fileName) =>
            fileName.endsWith(".js")
          );
          if (entryScripts.length !== 1) {
            throw new Error(
              "ESM UI provider output must contain exactly one entry JavaScript file."
            );
          }
          const entryFile = entryScripts[0];
          const entry = assets[entryFile];
          if (!entry) {
            throw new Error(
              `ESM UI provider output is missing its entry asset ${entryFile}.`
            );
          }
          const entryCode = entry.source().toString();
          await validator.validateSource(entryCode, entryFile);
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
            entry: `./${entryFile}`,
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
