import type { Plugin as HaloPlugin } from "@halo-dev/api-client";
import yaml from "js-yaml";
import fs from "node:fs";

export function getHaloPluginManifest(manifestPath: string) {
  const manifest = yaml.load(
    fs.readFileSync(manifestPath, "utf8")
  ) as HaloPlugin;

  return manifest;
}
