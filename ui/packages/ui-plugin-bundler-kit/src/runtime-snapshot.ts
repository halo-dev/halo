import fs from "node:fs";
import path from "node:path";
import { resolvePackageJSON } from "pkg-types";
import { compare, lte, parse } from "semver";
import { rawHaloHostRuntimeSnapshots } from "./runtime-snapshots";

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

export interface HostRuntimeSnapshotEntry {
  version: string;
  exports: string[];
  runtime: {
    bridge: string;
    global: string;
    identity: "singleton" | "shared";
  };
}

export interface HaloHostRuntimeSnapshot {
  haloVersion: string;
  packages: Record<SharedPackageRoot, HostRuntimeSnapshotEntry>;
}

const sharedPackageRootSet = new Set<string>(SHARED_PACKAGE_ROOTS);

export const HALO_HOST_RUNTIME_SNAPSHOTS = Object.freeze(
  rawHaloHostRuntimeSnapshots.map(validateHaloHostRuntimeSnapshot)
);

export function validateHaloHostRuntimeSnapshot(
  value: unknown
): HaloHostRuntimeSnapshot {
  if (
    !isRecord(value) ||
    typeof value.haloVersion !== "string" ||
    !parseStableVersion(value.haloVersion)
  ) {
    throw new Error("Host runtime snapshot haloVersion must be stable semver.");
  }
  if (!isRecord(value.packages)) {
    throw new Error("Host runtime snapshot packages must be an object.");
  }
  const snapshotPackages = value.packages;

  const packageRoots = Object.keys(value.packages).sort();
  const supportedRoots = [...SHARED_PACKAGE_ROOTS].sort();
  if (
    packageRoots.length !== supportedRoots.length ||
    packageRoots.some((root, index) => root !== supportedRoots[index])
  ) {
    throw new Error(
      `Host runtime snapshot must expose exactly: ${SHARED_PACKAGE_ROOTS.join(", ")}.`
    );
  }

  const packages = Object.fromEntries(
    SHARED_PACKAGE_ROOTS.map((root) => [
      root,
      validateSnapshotEntry(root, snapshotPackages[root]),
    ])
  ) as Record<SharedPackageRoot, HostRuntimeSnapshotEntry>;

  return deepFreeze({ haloVersion: value.haloVersion, packages });
}

export function selectHaloHostRuntimeSnapshot(
  targetHaloVersion: string,
  snapshots: readonly HaloHostRuntimeSnapshot[] = HALO_HOST_RUNTIME_SNAPSHOTS
) {
  const target = parse(targetHaloVersion);
  if (!target) {
    throw new Error(`Invalid target Halo version: ${targetHaloVersion}.`);
  }

  const eligible = snapshots
    .filter(
      (snapshot) =>
        lte(snapshot.haloVersion, target.version) ||
        (target.prerelease.length > 0 &&
          snapshot.haloVersion ===
            `${target.major}.${target.minor}.${target.patch}`)
    )
    .sort((left, right) => compare(right.haloVersion, left.haloVersion));
  const snapshot = eligible[0];
  if (!snapshot) {
    throw new Error(
      `No ESM host runtime snapshot is available for Halo ${targetHaloVersion}. ` +
        "Update @halo-dev/ui-plugin-bundler-kit or select IIFE output."
    );
  }

  return {
    snapshot,
    reusedOlderSnapshot:
      snapshot.haloVersion !==
      `${target.major}.${target.minor}.${target.patch}`,
  };
}

export async function resolveSharedPackage(
  root: SharedPackageRoot,
  providerRoot: string,
  sourceId?: string
) {
  const resolutionBase = getPackageResolutionBase(providerRoot, sourceId);
  let packageJsonPath: string;
  try {
    packageJsonPath = await resolvePackageJSON(root, {
      from: resolutionBase,
      conditions: ["browser", "import", "default"],
    });
  } catch (error) {
    throw new Error(
      `Cannot resolve shared dependency ${root} from ${resolutionBase}.`,
      { cause: error }
    );
  }
  const packageRoot = fs.realpathSync(path.dirname(packageJsonPath));
  const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, "utf8")) as {
    name?: string;
    version?: string;
  };

  if (packageJson.name !== root || !packageJson.version) {
    throw new Error(
      `Shared dependency ${root} resolved to ${packageJson.name || "an unnamed package"} at ${packageRoot}.`
    );
  }

  return {
    name: packageJson.name,
    version: packageJson.version,
    packageRoot,
  };
}

export async function validateResolvedSharedPackage(
  root: SharedPackageRoot,
  providerRoot: string,
  snapshot: HaloHostRuntimeSnapshot,
  sourceId?: string
) {
  const resolved = await resolveSharedPackage(root, providerRoot, sourceId);
  const hostVersion = snapshot.packages[root].version;
  const resolvedVersion = parse(resolved.version);
  const parsedHostVersion = parse(hostVersion);
  if (!resolvedVersion || !parsedHostVersion) {
    throw new Error(
      `${root} resolved to invalid version ${resolved.version} at ${resolved.packageRoot}. ` +
        "Install a valid published package or select IIFE output."
    );
  }
  return {
    ...resolved,
    newerThanHost: compare(resolvedVersion, parsedHostVersion) > 0,
    differentMajor: resolvedVersion.major !== parsedHostVersion.major,
  };
}

export function isSharedPackageRoot(value: string): value is SharedPackageRoot {
  return sharedPackageRootSet.has(value);
}

function validateSnapshotEntry(
  root: SharedPackageRoot,
  value: unknown
): HostRuntimeSnapshotEntry {
  if (
    !isRecord(value) ||
    typeof value.version !== "string" ||
    !parseStableVersion(value.version)
  ) {
    throw new Error(`${root} snapshot version must be stable semver.`);
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
    throw new Error(`${root} snapshot exports must be unique identifiers.`);
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
    throw new Error(`${root} snapshot runtime descriptor is invalid.`);
  }
  return {
    version: value.version,
    exports: [...value.exports].sort(),
    runtime: {
      bridge: value.runtime.bridge,
      global: value.runtime.global,
      identity: value.runtime.identity,
    },
  };
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
