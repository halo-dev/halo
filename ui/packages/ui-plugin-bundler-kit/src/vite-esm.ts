import type { Plugin, ResolvedConfig } from "vite";
import {
  ESM_PROVIDER_MANIFEST,
  validateEsmProviderManifest,
} from "./provider-manifest";
import type { HaloHostRuntimeSnapshot } from "./runtime-snapshot";
import { SharedDependencyValidator } from "./shared-dependencies";

interface ViteEsmPluginOptions {
  snapshot: HaloHostRuntimeSnapshot;
  providerRoot: string;
}

export function createViteEsmProviderPlugin(
  options: ViteEsmPluginOptions
): Plugin {
  const validator = new SharedDependencyValidator({
    snapshot: options.snapshot,
    providerRoot: options.providerRoot,
  });
  let resolvedConfig: ResolvedConfig;

  return {
    name: "halo:esm-ui-provider",
    enforce: "post",
    configResolved(config) {
      resolvedConfig = config;
    },
    async transform(code, id) {
      if (!id.includes("\0")) {
        await validator.validateSource(code, id);
      }
    },
    generateBundle: {
      order: "post",
      async handler(_outputOptions, bundle) {
        const chunks = Object.values(bundle).filter(
          (item) => item.type === "chunk"
        );
        for (const chunk of chunks) {
          await validator.validateSource(chunk.code, chunk.fileName);
        }

        const entries = chunks.filter((chunk) => chunk.isEntry);
        if (entries.length !== 1 || !entries[0].exports.includes("default")) {
          throw new Error(
            "ESM UI provider output must contain one entry with a default PluginModule export."
          );
        }
        const entryStyles = [
          ...((entries[0] as ViteOutputChunkMetadata).viteMetadata
            ?.importedCss || []),
        ].sort();
        if (entryStyles.length > 1) {
          throw new Error(
            "ESM UI provider output must contain at most one entry stylesheet."
          );
        }
        const manifest = validateEsmProviderManifest({
          format: "esm",
          entry: `./${entries[0].fileName}`,
          ...(entryStyles[0] ? { style: `./${entryStyles[0]}` } : {}),
        });
        this.emitFile({
          type: "asset",
          fileName: ESM_PROVIDER_MANIFEST,
          source: `${JSON.stringify(manifest, null, 2)}\n`,
        });

        const report = validator.getBuildReport();
        resolvedConfig.logger.info(report.summary);
        if (report.warning) {
          resolvedConfig.logger.warn(report.warning);
        }
      },
    },
  };
}

interface ViteOutputChunkMetadata {
  viteMetadata?: {
    importedCss?: Set<string>;
  };
}
