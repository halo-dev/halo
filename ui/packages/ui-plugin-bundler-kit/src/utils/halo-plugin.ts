import type { Plugin as HaloPlugin } from "@halo-dev/api-client";
import fs from "fs";
import yaml from "js-yaml";

export function getHaloPluginManifest(manifestPath: string) {
  const manifest = yaml.load(
    fs.readFileSync(manifestPath, "utf8")
  ) as HaloPlugin;

  return manifest;
}
