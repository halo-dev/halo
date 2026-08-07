import fs from "node:fs";
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
  readonly #namespaceImports = new Map<SharedPackageRoot, Set<string>>();
  readonly #compatibilityNotes = new Set<string>();
  #usesEditor = false;
  #usesEditorInternals = false;

  constructor(options: SharedDependencyValidatorOptions) {
    this.#snapshot = options.snapshot;
    this.#providerRoot = path.resolve(options.providerRoot);
  }

  async validateSource(code: string, sourceId: string) {
    for (const imported of parseImports(code)) {
      await this.validateImport(imported.specifier, imported.names, sourceId);
    }
    if (this.#usesEditor && this.#usesEditorInternals) {
      this.#compatibilityNotes.add(
        "Direct @tiptap/* or prosemirror-* imports remain provider-private while @halo-dev/richtext-editor is shared; editor identity is best-effort."
      );
    }
  }

  async validateImport(
    specifier: string,
    names: readonly string[] | "namespace",
    sourceId: string
  ) {
    if (specifier === "@halo-dev/richtext-editor") {
      this.#usesEditor = true;
    }
    if (
      specifier.startsWith("@tiptap/") ||
      specifier.startsWith("prosemirror-")
    ) {
      this.#usesEditorInternals = true;
      return false;
    }

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
    if (names === "namespace") {
      const sources = this.#namespaceImports.get(specifier) || new Set();
      sources.add(this.formatSourceLabel(sourceId));
      this.#namespaceImports.set(specifier, sources);
      return true;
    }

    const supportedExports = new Set(
      this.#snapshot.packages[specifier].exports
    );
    const unsupported = names.filter((name) => !supportedExports.has(name));
    if (unsupported.length > 0) {
      throw new Error(
        `${sourceId} imports unsupported ${specifier} export(s): ${unsupported.join(", ")}. ` +
          `Halo ${this.#snapshot.haloVersion} exposes ${supportedExports.size} root exports. ` +
          "Raise spec.requires with an updated bundler snapshot, change the import, or select IIFE output."
      );
    }
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
      `[ui-plugin-bundler-kit] ESM validation passed (Halo ${this.#snapshot.haloVersion} snapshot).`,
      ...(rows.length > 0
        ? [
            "  Shared dependencies",
            formatTableRow(["package", "provider", "Halo host"], widths),
            ...rows.map((row) => formatTableRow(row, widths)),
          ]
        : ["  Shared dependencies: none"]),
    ].join("\n");

    const warningSections: string[] = [];
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
    if (versionNotes.length > 0) {
      warningSections.push("  Version differences", ...versionNotes);
    }

    const namespaceNotes = roots.flatMap((root) => {
      const sources = this.#namespaceImports.get(root);
      if (!sources?.size) {
        return [];
      }
      return [
        `    ${root}`,
        ...[...sources].sort().map((source) => `      - ${source}`),
      ];
    });
    if (namespaceNotes.length > 0) {
      warningSections.push(
        "  Namespace or dynamic imports not fully checked",
        ...namespaceNotes
      );
    }

    if (this.#compatibilityNotes.size > 0) {
      warningSections.push(
        "  Other",
        ...[...this.#compatibilityNotes].sort().map((note) => `    ${note}`)
      );
    }

    return {
      summary,
      ...(warningSections.length > 0
        ? {
            warning: [
              "[ui-plugin-bundler-kit] Compatibility notes:",
              ...warningSections,
            ].join("\n"),
          }
        : {}),
    };
  }

  async assertBundlerResolution(
    root: SharedPackageRoot,
    resolvedId: string,
    sourceId: string
  ) {
    await this.validateResolvedRoot(root, sourceId);
    const expectedRoot = this.#resolvedPackages.get(root)?.packageRoot;
    if (
      !expectedRoot ||
      resolvedId.startsWith("\0") ||
      !path.isAbsolute(resolvedId)
    ) {
      return;
    }
    const cleanId = resolvedId.split("?")[0];
    const actualPath = fs.existsSync(cleanId)
      ? fs.realpathSync(cleanId)
      : path.normalize(cleanId);
    if (
      actualPath !== expectedRoot &&
      !actualPath.startsWith(`${expectedRoot}${path.sep}`)
    ) {
      throw new Error(
        `${root} imported by ${sourceId} resolved through the bundler to ${actualPath}, ` +
          `outside the validated package root ${expectedRoot}. Remove aliases or conditional resolution overrides.`
      );
    }
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
    const resolved = await resolving;
    this.#validatedRoots.add(root);
    this.#resolvedPackages.set(root, resolved);
  }

  private formatSourceLabel(sourceId: string) {
    const cleanId = sourceId.split(/[?#]/, 1)[0];
    const normalizedId = cleanId.split(path.sep).join("/");
    const nodeModulesMarker = "/node_modules/";
    const nodeModulesIndex = normalizedId.lastIndexOf(nodeModulesMarker);
    if (nodeModulesIndex !== -1) {
      return normalizedId.slice(nodeModulesIndex + nodeModulesMarker.length);
    }

    if (!path.isAbsolute(cleanId)) {
      return normalizedId.replace(/^\.\//, "");
    }
    return path.relative(this.#providerRoot, cleanId).split(path.sep).join("/");
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

interface ParsedImport {
  specifier: string;
  names: string[] | "namespace";
}

export function parseImports(code: string): ParsedImport[] {
  const imports: ParsedImport[] = [];
  const [esmImports] = parseEsmImports(code);
  for (const imported of esmImports) {
    if (!imported.n || imported.d === -2) {
      continue;
    }
    if (imported.d >= 0) {
      imports.push({ specifier: imported.n, names: "namespace" });
      continue;
    }

    const statement = code.slice(imported.ss, imported.se);
    const match = statement.match(
      /\b(?:import\s+((?:type\s+)?(?:(?:[$A-Z_a-z][$\w]*\s*,\s*)?(?:\*\s+as\s+[$A-Z_a-z][$\w]*|\{[^}]*\})|[$A-Z_a-z][$\w]*))|export\s+((?:type\s+)?(?:\*(?:\s+as\s+[$A-Z_a-z][$\w]*)?|\{[^}]*\})))\s+from\s*["']/
    );
    imports.push({
      specifier: imported.n,
      names: match ? parseImportClause(match[1] || match[2]) : [],
    });
  }
  return imports;
}

function parseImportClause(clause: string): string[] | "namespace" {
  const normalized = clause.trim();
  if (normalized === "*" || normalized.startsWith("* as ")) {
    return "namespace";
  }

  const names: string[] = [];
  const namedStart = normalized.indexOf("{");
  const namedEnd = normalized.lastIndexOf("}");
  const defaultImport = (
    namedStart === -1 ? normalized : normalized.slice(0, namedStart)
  )
    .replace(/,$/, "")
    .trim();
  if (defaultImport && !defaultImport.startsWith("type ")) {
    names.push("default");
  }
  if (namedStart !== -1 && namedEnd > namedStart) {
    for (const item of normalized.slice(namedStart + 1, namedEnd).split(",")) {
      const importedName = item
        .trim()
        .replace(/^type\s+/, "")
        .split(/\s+as\s+/)[0]
        .trim();
      if (importedName) {
        names.push(importedName);
      }
    }
  }
  return names;
}
