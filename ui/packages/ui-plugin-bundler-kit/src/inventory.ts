import fs from "node:fs";
import path from "node:path";
import { compare, lte, parse, satisfies } from "semver";
import halo226Inventory from "./inventories/halo-2.26.0.json";

export const SHARED_PACKAGE_ROOTS = [
  "vue",
  "vue-router",
  "pinia",
  "axios",
  "@formkit/vue",
  "@formkit/core",
  "@halo-dev/ui-shared",
  "@halo-dev/components",
  "@halo-dev/api-client",
  "@halo-dev/richtext-editor",
] as const;

export type SharedPackageRoot = (typeof SHARED_PACKAGE_ROOTS)[number];

export interface SharedPackageInventoryEntry {
  version: string;
  range: string;
  exports: string[];
  runtime: {
    bridge: string;
    global: string;
    identity: "singleton" | "shared";
  };
}

export interface HaloSharedInventory {
  haloVersion: string;
  packages: Record<SharedPackageRoot, SharedPackageInventoryEntry>;
}

const sharedPackageRootSet = new Set<string>(SHARED_PACKAGE_ROOTS);

export const HALO_SHARED_INVENTORIES = Object.freeze([
  validateHaloSharedInventory(halo226Inventory),
]);

export function validateHaloSharedInventory(
  value: unknown
): HaloSharedInventory {
  if (!isRecord(value) || !parseStableVersion(value.haloVersion)) {
    throw new Error("Inventory haloVersion must be a stable semantic version.");
  }
  if (!isRecord(value.packages)) {
    throw new Error("Inventory packages must be an object.");
  }

  const packageRoots = Object.keys(value.packages).sort();
  const supportedRoots = [...SHARED_PACKAGE_ROOTS].sort();
  if (
    packageRoots.length !== supportedRoots.length ||
    packageRoots.some((root, index) => root !== supportedRoots[index])
  ) {
    throw new Error(
      `Inventory must expose exactly: ${SHARED_PACKAGE_ROOTS.join(", ")}.`
    );
  }

  const packages = Object.fromEntries(
    SHARED_PACKAGE_ROOTS.map((root) => [
      root,
      validateInventoryEntry(root, value.packages[root]),
    ])
  ) as Record<SharedPackageRoot, SharedPackageInventoryEntry>;

  return deepFreeze({ haloVersion: value.haloVersion, packages });
}

export function selectHaloSharedInventory(
  targetHaloVersion: string,
  inventories: readonly HaloSharedInventory[] = HALO_SHARED_INVENTORIES
) {
  const target = parse(targetHaloVersion);
  if (!target) {
    throw new Error(`Invalid target Halo version: ${targetHaloVersion}.`);
  }

  const eligible = inventories
    .filter(
      (inventory) =>
        lte(inventory.haloVersion, target.version) ||
        (target.prerelease.length > 0 &&
          inventory.haloVersion ===
            `${target.major}.${target.minor}.${target.patch}`)
    )
    .sort((left, right) => compare(right.haloVersion, left.haloVersion));
  const inventory = eligible[0];
  if (!inventory) {
    throw new Error(
      `No ESM shared dependency inventory is available for Halo ${targetHaloVersion}. ` +
        "Update @halo-dev/ui-plugin-bundler-kit or select IIFE output."
    );
  }

  return {
    inventory,
    reusedOlderInventory:
      inventory.haloVersion !== target.version &&
      target.prerelease.length === 0,
  };
}

export function resolveSharedPackage(
  root: SharedPackageRoot,
  providerRoot: string,
  sourceId?: string
) {
  const packageRoot = findInstalledPackageRoot(
    root,
    getPackageResolutionBase(providerRoot, sourceId)
  );
  const packageJson = JSON.parse(
    fs.readFileSync(path.join(packageRoot, "package.json"), "utf8")
  ) as { name?: string; version?: string; module?: string; main?: string };

  if (packageJson.name !== root || !packageJson.version) {
    throw new Error(
      `Shared dependency ${root} resolved to ${packageJson.name || "an unnamed package"} at ${packageRoot}.`
    );
  }

  return {
    name: packageJson.name,
    version: packageJson.version,
    entry: fs.realpathSync(
      path.resolve(
        packageRoot,
        packageJson.module || packageJson.main || "index.js"
      )
    ),
    packageRoot,
  };
}

export function validateResolvedSharedPackage(
  root: SharedPackageRoot,
  providerRoot: string,
  inventory: HaloSharedInventory,
  sourceId?: string
) {
  const resolved = resolveSharedPackage(root, providerRoot, sourceId);
  const expected = inventory.packages[root];
  if (!satisfies(resolved.version, expected.range)) {
    throw new Error(
      `${root} resolved to ${resolved.version} at ${resolved.packageRoot}, outside ` +
        `Halo ${inventory.haloVersion}'s accepted range ${expected.range} ` +
        `(host ${expected.version}). Align the dependency or select IIFE output.`
    );
  }
  return {
    ...resolved,
    newerThanHost: compare(resolved.version, expected.version) > 0,
  };
}

export function isSharedPackageRoot(value: string): value is SharedPackageRoot {
  return sharedPackageRootSet.has(value);
}

function validateInventoryEntry(
  root: SharedPackageRoot,
  value: unknown
): SharedPackageInventoryEntry {
  if (!isRecord(value) || !parseStableVersion(value.version)) {
    throw new Error(`${root} inventory version must be stable semver.`);
  }
  if (
    typeof value.range !== "string" ||
    !satisfies(value.version, value.range)
  ) {
    throw new Error(
      `${root} inventory range must admit host ${value.version}.`
    );
  }
  if (
    !Array.isArray(value.exports) ||
    value.exports.length === 0 ||
    value.exports.some(
      (exportName) =>
        typeof exportName !== "string" || !/^[$A-Z_a-z][$\w]*$/.test(exportName)
    ) ||
    new Set(value.exports).size !== value.exports.length
  ) {
    throw new Error(`${root} inventory exports must be unique identifiers.`);
  }
  if (
    !isRecord(value.runtime) ||
    typeof value.runtime.bridge !== "string" ||
    !/^[a-z0-9-]+$/.test(value.runtime.bridge) ||
    typeof value.runtime.global !== "string" ||
    !/^[$A-Z_a-z][$\w]*$/.test(value.runtime.global) ||
    (value.runtime.identity !== "singleton" &&
      value.runtime.identity !== "shared")
  ) {
    throw new Error(`${root} inventory runtime descriptor is invalid.`);
  }
  return {
    version: value.version,
    range: value.range,
    exports: [...value.exports].sort(),
    runtime: {
      bridge: value.runtime.bridge,
      global: value.runtime.global,
      identity: value.runtime.identity,
    },
  };
}

function findInstalledPackageRoot(root: string, providerRoot: string) {
  let current = path.resolve(providerRoot);
  while (true) {
    const candidate = path.join(current, "node_modules", root);
    const packageJsonPath = path.join(candidate, "package.json");
    if (fs.existsSync(packageJsonPath)) {
      return fs.realpathSync(candidate);
    }
    const parent = path.dirname(current);
    if (parent === current) {
      throw new Error(
        `Cannot resolve shared dependency ${root} from ${providerRoot}.`
      );
    }
    current = parent;
  }
}

function getPackageResolutionBase(providerRoot: string, sourceId?: string) {
  const cleanSourceId = sourceId?.split(/[?#]/, 1)[0];
  if (cleanSourceId && path.isAbsolute(cleanSourceId)) {
    return path.dirname(cleanSourceId);
  }
  return providerRoot;
}

function parseStableVersion(value: unknown) {
  if (typeof value !== "string") {
    return;
  }
  const version = parse(value);
  return version && version.prerelease.length === 0 ? version : undefined;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function deepFreeze<T>(value: T): T {
  if (value && typeof value === "object" && !Object.isFrozen(value)) {
    Object.freeze(value);
    for (const child of Object.values(value)) {
      deepFreeze(child);
    }
  }
  return value;
}
