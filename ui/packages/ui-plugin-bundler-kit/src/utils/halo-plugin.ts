import fs from "node:fs";
import type { Plugin as HaloPlugin } from "@halo-dev/api-client";
import yaml from "js-yaml";
import { gte, minVersion } from "semver";

const UI_BUNDLE_MIN_HALO_VERSION = "2.25.0";
const UI_BUNDLE_LOCATION = "ui";
const CONSOLE_BUNDLE_LOCATION = "console";
const THEME_MODULE_NAME_PREFIX = "theme:";

interface HaloThemeManifest {
  metadata: {
    name: string;
  };
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
