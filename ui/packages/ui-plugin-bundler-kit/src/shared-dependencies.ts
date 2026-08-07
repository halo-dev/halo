import path from "node:path";
import { parse as parseEsmImports } from "es-module-lexer/js";
import type {
  HaloHostRuntimeSnapshot,
  SharedPackageRoot,
} from "./runtime-snapshot";
import {
  SHARED_PACKAGE_ROOTS,
  isSharedPackageRoot,
  validateResolvedSharedPackage,
} from "./runtime-snapshot";

interface SharedDependencyValidatorOptions {
  snapshot: HaloHostRuntimeSnapshot;
  providerRoot: string;
}

interface SharedDependencyBuildReport {
  summary: string;
  warning?: string;
}

export class SharedDependencyValidator {
  readonly #snapshot: HaloHostRuntimeSnapshot;
  readonly #providerRoot: string;
  readonly #validatedRoots = new Set<SharedPackageRoot>();
  readonly #resolvedPackages = new Map<
    SharedPackageRoot,
    Awaited<ReturnType<typeof validateResolvedSharedPackage>>
  >();
  readonly #resolvingPackages = new Map<
    SharedPackageRoot,
    ReturnType<typeof validateResolvedSharedPackage>
  >();

  constructor(options: SharedDependencyValidatorOptions) {
    this.#snapshot = options.snapshot;
    this.#providerRoot = path.resolve(options.providerRoot);
  }

  async validateSource(code: string, sourceId: string) {
    for (const specifier of parseImports(code)) {
      await this.validateImport(specifier, sourceId);
    }
  }

  async validateImport(specifier: string, sourceId: string) {
    const deepRoot = SHARED_PACKAGE_ROOTS.find((root) =>
      specifier.startsWith(`${root}/`)
    );
    if (deepRoot) {
      throw new Error(
        `Unsupported shared dependency subpath ${specifier} imported by ${sourceId}. ` +
          `Import the ${deepRoot} package root or select IIFE output.`
      );
    }
    if (!isSharedPackageRoot(specifier)) {
      return false;
    }

    await this.validateResolvedRoot(specifier, sourceId);
    return true;
  }

  getValidatedRoots() {
    return [...this.#validatedRoots];
  }

  getBuildReport(): SharedDependencyBuildReport {
    const roots = SHARED_PACKAGE_ROOTS.filter((root) =>
      this.#validatedRoots.has(root)
    );
    const rows = roots.map((root) => {
      const resolved = this.#resolvedPackages.get(root) as Awaited<
        ReturnType<typeof validateResolvedSharedPackage>
      >;
      const host = this.#snapshot.packages[root];
      return [root, resolved.version, host.version] as const;
    });
    const widths = [
      Math.max("package".length, ...rows.map(([root]) => root.length)),
      Math.max(
        "provider".length,
        ...rows.map(([, providerVersion]) => providerVersion.length)
      ),
      Math.max(
        "Halo host".length,
        ...rows.map(([, , hostVersion]) => hostVersion.length)
      ),
    ];
    const summary = [
      `[ui-plugin-bundler-kit] ESM shared dependency versions (Halo ${this.#snapshot.haloVersion} snapshot).`,
      ...(rows.length > 0
        ? [
            "  Shared dependencies",
            formatTableRow(["package", "provider", "Halo host"], widths),
            ...rows.map((row) => formatTableRow(row, widths)),
          ]
        : ["  Shared dependencies: none"]),
    ].join("\n");

    const versionNotes = roots.flatMap((root) => {
      const resolved = this.#resolvedPackages.get(root) as Awaited<
        ReturnType<typeof validateResolvedSharedPackage>
      >;
      if (!resolved.differentMajor && !resolved.newerThanHost) {
        return [];
      }
      const hostVersion = this.#snapshot.packages[root].version;
      const reason = resolved.differentMajor
        ? "different major"
        : "provider is newer";
      return [
        `    ${root}: provider ${resolved.version}, Halo host ${hostVersion} (${reason}; best-effort)`,
      ];
    });
    return {
      summary,
      ...(versionNotes.length > 0
        ? {
            warning: [
              "[ui-plugin-bundler-kit] Compatibility notes:",
              "  Version differences",
              ...versionNotes,
            ].join("\n"),
          }
        : {}),
    };
  }

  shouldExternalize(specifier: string, sourceId: string) {
    const deepRoot = SHARED_PACKAGE_ROOTS.find((root) =>
      specifier.startsWith(`${root}/`)
    );
    if (deepRoot) {
      throw new Error(
        `Unsupported shared dependency subpath ${specifier} imported by ${sourceId}. ` +
          `Import the ${deepRoot} package root or select IIFE output.`
      );
    }
    return isSharedPackageRoot(specifier);
  }

  private async validateResolvedRoot(
    root: SharedPackageRoot,
    sourceId: string
  ) {
    if (this.#validatedRoots.has(root)) {
      return;
    }
    let resolving = this.#resolvingPackages.get(root);
    if (!resolving) {
      resolving = validateResolvedSharedPackage(
        root,
        this.#providerRoot,
        this.#snapshot,
        sourceId
      );
      this.#resolvingPackages.set(root, resolving);
    }
    let resolved: Awaited<ReturnType<typeof validateResolvedSharedPackage>>;
    try {
      resolved = await resolving;
    } catch {
      return;
    }
    this.#validatedRoots.add(root);
    this.#resolvedPackages.set(root, resolved);
  }
}

function formatTableRow(
  values: readonly [string, string, string],
  widths: readonly number[]
) {
  return `    ${values
    .map((value, index) => value.padEnd(widths[index]))
    .join("  ")}`.trimEnd();
}

export function parseImports(code: string): string[] {
  const imports: string[] = [];
  const [esmImports] = parseEsmImports(code);
  for (const imported of esmImports) {
    if (!imported.n || imported.d === -2) {
      continue;
    }
    imports.push(imported.n);
  }
  return imports;
}
