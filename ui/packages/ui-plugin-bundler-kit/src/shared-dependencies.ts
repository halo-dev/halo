import fs from "node:fs";
import path from "node:path";
import type { HaloSharedInventory, SharedPackageRoot } from "./inventory";
import {
  SHARED_PACKAGE_ROOTS,
  isSharedPackageRoot,
  validateResolvedSharedPackage,
} from "./inventory";

interface SharedDependencyValidatorOptions {
  inventory: HaloSharedInventory;
  providerRoot: string;
  warn: (message: string) => void;
}

export class SharedDependencyValidator {
  readonly #inventory: HaloSharedInventory;
  readonly #providerRoot: string;
  readonly #warn: (message: string) => void;
  readonly #validatedRoots = new Set<SharedPackageRoot>();
  readonly #resolvedPackages = new Map<
    SharedPackageRoot,
    Awaited<ReturnType<typeof validateResolvedSharedPackage>>
  >();
  readonly #resolvingPackages = new Map<
    SharedPackageRoot,
    ReturnType<typeof validateResolvedSharedPackage>
  >();
  readonly #warnings = new Set<string>();
  #usesEditor = false;
  #usesEditorInternals = false;

  constructor(options: SharedDependencyValidatorOptions) {
    this.#inventory = options.inventory;
    this.#providerRoot = path.resolve(options.providerRoot);
    this.#warn = options.warn;
  }

  async validateSource(code: string, sourceId: string) {
    for (const imported of parseImports(code)) {
      await this.validateImport(imported.specifier, imported.names, sourceId);
    }
    if (this.#usesEditor && this.#usesEditorInternals) {
      this.warnOnce(
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
      this.warnOnce(
        `${sourceId} uses a namespace or dynamic import from ${specifier}; runtime properties cannot be fully checked against Halo ${this.#inventory.haloVersion}.`
      );
      return true;
    }

    const supportedExports = new Set(
      this.#inventory.packages[specifier].exports
    );
    const unsupported = names.filter((name) => !supportedExports.has(name));
    if (unsupported.length > 0) {
      throw new Error(
        `${sourceId} imports unsupported ${specifier} export(s): ${unsupported.join(", ")}. ` +
          `Halo ${this.#inventory.haloVersion} exposes ${supportedExports.size} root exports. ` +
          "Raise spec.requires with an updated bundler inventory, change the import, or select IIFE output."
      );
    }
    return true;
  }

  getValidatedRoots() {
    return [...this.#validatedRoots];
  }

  getBuildSummary() {
    return this.getValidatedRoots().map((root) => {
      const resolved = this.#resolvedPackages.get(root) as Awaited<
        ReturnType<typeof validateResolvedSharedPackage>
      >;
      const host = this.#inventory.packages[root];
      return `${root}: provider ${resolved.version}, host ${host.version}, accepted ${host.range}`;
    });
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
        this.#inventory,
        sourceId
      );
      this.#resolvingPackages.set(root, resolving);
    }
    const resolved = await resolving;
    this.#validatedRoots.add(root);
    this.#resolvedPackages.set(root, resolved);
    if (resolved.newerThanHost) {
      const entry = this.#inventory.packages[root];
      this.warnOnce(
        `${root} ${resolved.version} is newer than Halo ${this.#inventory.haloVersion}'s host ${entry.version} but is admitted by ${entry.range}; compatibility is best-effort.`
      );
    }
  }

  private warnOnce(message: string) {
    if (!this.#warnings.has(message)) {
      this.#warnings.add(message);
      this.#warn(`[ui-plugin-bundler-kit] ${message}`);
    }
  }
}

interface ParsedImport {
  specifier: string;
  names: string[] | "namespace";
}

export function parseImports(code: string): ParsedImport[] {
  const imports: ParsedImport[] = [];
  const fromPattern =
    /\b(?:import\s+((?:type\s+)?(?:(?:[$A-Z_a-z][$\w]*\s*,\s*)?(?:\*\s+as\s+[$A-Z_a-z][$\w]*|\{[^}]*\})|[$A-Z_a-z][$\w]*))|export\s+((?:type\s+)?(?:\*(?:\s+as\s+[$A-Z_a-z][$\w]*)?|\{[^}]*\})))\s+from\s*["']([^"']+)["']/g;
  for (const match of code.matchAll(fromPattern)) {
    imports.push({
      specifier: match[3],
      names: parseImportClause(match[1] || match[2]),
    });
  }

  const sideEffectPattern = /\bimport\s*["']([^"']+)["']/g;
  for (const match of code.matchAll(sideEffectPattern)) {
    imports.push({ specifier: match[1], names: [] });
  }

  const dynamicPattern = /\bimport\s*\(\s*["']([^"']+)["']\s*\)/g;
  for (const match of code.matchAll(dynamicPattern)) {
    imports.push({ specifier: match[1], names: "namespace" });
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
