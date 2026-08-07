import fs from "node:fs";
import type { Plugin as HaloPlugin } from "@halo-dev/api-client";
import yaml from "js-yaml";
import { gte, minVersion, parse } from "semver";

const UI_BUNDLE_MIN_HALO_VERSION = "2.25.0";
const ESM_PROVIDER_MIN_HALO_VERSION = "2.26.0";
const UI_BUNDLE_LOCATION = "ui";
const CONSOLE_BUNDLE_LOCATION = "console";
const THEME_MODULE_NAME_PREFIX = "theme:";

export interface HaloThemeManifest {
  metadata: {
    name: string;
  };
  spec?: {
    requires?: string;
  };
}

export type ProviderFormat = "auto" | "iife" | "esm";

export interface ProviderFormatSelection {
  format: Exclude<ProviderFormat, "auto">;
  reason: "explicit" | "automatic" | "automatic-fallback";
  targetHaloVersion?: string;
  warnings: string[];
}

export function getHaloPluginManifest(manifestPath: string) {
  return readManifest<HaloPlugin>(manifestPath);
}

export function getHaloThemeManifest(manifestPath: string) {
  return readManifest<HaloThemeManifest>(manifestPath);
}

export function getManifestName(
  manifest: Pick<HaloPlugin, "metadata"> | HaloThemeManifest
) {
  return manifest.metadata.name;
}

export function getHaloThemeModuleName(manifest: HaloThemeManifest) {
  return `${THEME_MODULE_NAME_PREFIX}${getManifestName(manifest)}`;
}

export function getHaloThemeAssetPublicPath(manifest: HaloThemeManifest) {
  return `/themes/${getManifestName(manifest)}/ui-plugin/assets/`;
}

export function getHaloPluginBundleLocation(manifest: HaloPlugin) {
  const requiresMinVersion = getRequiresMinVersion(manifest.spec.requires);
  return requiresMinVersion &&
    gte(requiresMinVersion, UI_BUNDLE_MIN_HALO_VERSION)
    ? UI_BUNDLE_LOCATION
    : CONSOLE_BUNDLE_LOCATION;
}

export function getManifestRequires(
  manifest: Pick<HaloPlugin, "spec"> | HaloThemeManifest
) {
  return manifest.spec?.requires;
}

export function selectProviderFormat(options: {
  format?: ProviderFormat;
  requires?: string;
  targetHaloVersion?: string;
}): ProviderFormatSelection {
  const requestedFormat = options.format || "auto";
  if (requestedFormat === "iife") {
    return { format: "iife", reason: "explicit", warnings: [] };
  }

  const derivedTarget = parseSimpleStableTarget(options.requires);
  if (requestedFormat === "auto") {
    if (!derivedTarget) {
      return {
        format: "iife",
        reason: "automatic-fallback",
        warnings: [
          `Cannot derive a simple stable Halo target from spec.requires ${JSON.stringify(options.requires)}; using IIFE output.`,
        ],
      };
    }
    if (!gte(derivedTarget, ESM_PROVIDER_MIN_HALO_VERSION)) {
      return {
        format: "iife",
        reason: "automatic",
        targetHaloVersion: derivedTarget,
        warnings: [],
      };
    }
    return {
      format: "esm",
      reason: "automatic",
      targetHaloVersion: derivedTarget,
      warnings: [],
    };
  }

  const explicitTarget = options.targetHaloVersion
    ? parse(options.targetHaloVersion)
    : undefined;
  const target = derivedTarget || explicitTarget?.version;
  if (!target) {
    throw new Error(
      "Explicit ESM output requires a simple stable spec.requires target or targetHaloVersion."
    );
  }

  const warnings: string[] = [];
  if (!derivedTarget) {
    warnings.push(
      `Explicit ESM output uses target Halo ${target}, but spec.requires ${JSON.stringify(options.requires)} does not prove a minimum Halo version of ${ESM_PROVIDER_MIN_HALO_VERSION} or newer. Update spec.requires so older Halo releases do not install this ESM-only artifact.`
    );
  } else if (!gte(derivedTarget, ESM_PROVIDER_MIN_HALO_VERSION)) {
    warnings.push(
      `Explicit ESM output targets Halo ${derivedTarget}, which predates ESM UI provider support in Halo ${ESM_PROVIDER_MIN_HALO_VERSION}.`
    );
  }
  return {
    format: "esm",
    reason: "explicit",
    targetHaloVersion: target,
    warnings,
  };
}

function parseSimpleStableTarget(requires: string | undefined) {
  const match = requires
    ?.trim()
    .match(/^(?:>=)?(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$/);
  if (!match) {
    return;
  }
  return `${match[1]}.${match[2]}.${match[3]}`;
}

function getRequiresMinVersion(requires: string | undefined) {
  const normalizedRequires = requires?.trim();

  if (!normalizedRequires) {
    return;
  }

  try {
    return minVersion(normalizedRequires);
  } catch {
    console.warn(
      `[ui-plugin-bundler-kit] Invalid semver range in plugin manifest "spec.requires": "${requires}". ` +
        `Falling back to "${CONSOLE_BUNDLE_LOCATION}" bundle location.`
    );
    return;
  }
}

function readManifest<T>(manifestPath: string) {
  return yaml.load(fs.readFileSync(manifestPath, "utf8")) as T;
}
