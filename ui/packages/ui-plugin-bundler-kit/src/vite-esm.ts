import type { OutputChunk } from "rollup";
import type { Plugin, ResolvedConfig } from "vite";
import {
  ESM_PROVIDER_MANIFEST,
  validateEsmProviderManifest,
} from "./provider-manifest";
import type { HaloHostRuntimeSnapshot } from "./runtime-snapshot";
import { isSharedPackageRoot, SHARED_PACKAGE_ROOTS } from "./runtime-snapshot";
import { parseImports, SharedDependencyValidator } from "./shared-dependencies";

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
  let resolvePackage: ReturnType<ResolvedConfig["createResolver"]>;

  return {
    name: "halo:esm-ui-provider",
    enforce: "post",
    config(config) {
      const userExternal = config.build?.rollupOptions?.external;
      return {
        build: {
          rollupOptions: {
            external(source, importer, isResolved) {
              if (
                validator.shouldExternalize(
                  source,
                  importer || "provider entry"
                )
              ) {
                return true;
              }
              return matchesExternal(
                userExternal,
                source,
                importer,
                isResolved
              );
            },
          },
        },
      };
    },
    configResolved(config) {
      resolvedConfig = config;
      resolvePackage = config.createResolver();
      if (!config.build.cssCodeSplit) {
        throw new Error(
          "ESM UI provider output requires Vite build.cssCodeSplit to remain enabled."
        );
      }
      const formats = Array.isArray(config.build.lib)
        ? []
        : config.build.lib?.formats;
      if (
        !formats?.includes("es") ||
        formats.some((format) => format !== "es")
      ) {
        throw new Error(
          "ESM UI provider output requires Vite build.lib.formats to contain only es."
        );
      }
      const aliases = Array.isArray(config.resolve.alias)
        ? config.resolve.alias
        : Object.entries(config.resolve.alias || {}).map(([find]) => ({
            find,
          }));
      for (const alias of aliases) {
        if (
          typeof alias.find === "string" &&
          SHARED_PACKAGE_ROOTS.includes(alias.find as never)
        ) {
          throw new Error(
            `Vite alias for shared dependency ${alias.find} would bypass Halo snapshot validation.`
          );
        }
      }
    },
    async transform(code, id) {
      if (!id.includes("\0")) {
        await validator.validateSource(code, id);
        for (const imported of parseImports(code)) {
          if (isSharedPackageRoot(imported.specifier)) {
            const resolved = await resolvePackage(imported.specifier, id);
            if (resolved) {
              await validator.assertBundlerResolution(
                imported.specifier,
                resolved,
                id
              );
            }
          }
        }
      }
    },
    async generateBundle(_outputOptions, bundle) {
      const chunks = Object.values(bundle).filter(
        (item): item is OutputChunk => item.type === "chunk"
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
        ...((entries[0] as ViteOutputChunk).viteMetadata?.importedCss || []),
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
  };
}

interface ViteOutputChunk extends OutputChunk {
  viteMetadata?: {
    importedCss?: Set<string>;
  };
}

function matchesExternal(
  external: unknown,
  source: string,
  importer: string | undefined,
  isResolved: boolean
) {
  if (typeof external === "function") {
    return external(source, importer, isResolved);
  }
  if (external instanceof RegExp) {
    return external.test(source);
  }
  if (Array.isArray(external)) {
    return external.some((entry) =>
      entry instanceof RegExp ? entry.test(source) : entry === source
    );
  }
  return false;
}
