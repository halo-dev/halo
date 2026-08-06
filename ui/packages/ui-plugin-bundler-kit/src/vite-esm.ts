import type { OutputAsset, OutputChunk } from "rollup";
import type { Plugin, ResolvedConfig } from "vite";
import type { HaloSharedInventory } from "./inventory";
import { isSharedPackageRoot, SHARED_PACKAGE_ROOTS } from "./inventory";
import {
  ESM_PROVIDER_MANIFEST,
  validateEsmProviderManifest,
} from "./provider-manifest";
import { parseImports, SharedDependencyValidator } from "./shared-dependencies";

interface ViteEsmPluginOptions {
  inventory: HaloSharedInventory;
  providerRoot: string;
}

export function createViteEsmProviderPlugin(
  options: ViteEsmPluginOptions
): Plugin {
  const validator = new SharedDependencyValidator({
    inventory: options.inventory,
    providerRoot: options.providerRoot,
    warn: console.warn,
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
            `Vite alias for shared dependency ${alias.find} would bypass Halo inventory validation.`
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
      const styles = Object.values(bundle)
        .filter(
          (item): item is OutputAsset =>
            item.type === "asset" && item.fileName.endsWith(".css")
        )
        .map((asset) => `./${asset.fileName}`)
        .sort();
      const manifest = validateEsmProviderManifest({
        format: "esm",
        entry: `./${entries[0].fileName}`,
        styles,
      });
      this.emitFile({
        type: "asset",
        fileName: ESM_PROVIDER_MANIFEST,
        source: `${JSON.stringify(manifest, null, 2)}\n`,
      });

      resolvedConfig.logger.info(
        `[ui-plugin-bundler-kit] ESM output validated against Halo ${options.inventory.haloVersion}:\n${validator.getBuildSummary().join("\n") || "no shared roots"}`
      );
    },
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
