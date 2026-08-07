import path from "node:path";

export const ESM_PROVIDER_MANIFEST = "ui-plugin.json";

export interface EsmProviderManifest {
  format: "esm";
  entry: string;
  style?: string;
}

export function validateEsmProviderManifest(
  value: unknown
): EsmProviderManifest {
  if (!isRecord(value)) {
    throw new Error("ESM provider manifest must be an object.");
  }
  const keys = Object.keys(value).sort();
  if (
    !keys.includes("entry") ||
    !keys.includes("format") ||
    keys.some((key) => !["entry", "format", "style"].includes(key)) ||
    value.format !== "esm"
  ) {
    throw new Error(
      'ESM provider manifest must contain format, entry, and optional style only with format "esm".'
    );
  }
  if (typeof value.entry !== "string") {
    throw new Error("ESM provider manifest entry is required.");
  }
  const manifest: EsmProviderManifest = {
    format: "esm",
    entry: normalizeProviderResourcePath(value.entry),
  };
  if ("style" in value) {
    if (typeof value.style !== "string") {
      throw new Error("ESM provider manifest style must be a string.");
    }
    manifest.style = normalizeProviderResourcePath(value.style);
  }
  return manifest;
}

export function normalizeProviderResourcePath(resourcePath: string) {
  const normalizedSlashes = resourcePath.replaceAll("\\", "/");
  if (
    !normalizedSlashes ||
    normalizedSlashes.startsWith("/") ||
    normalizedSlashes.startsWith("//") ||
    /^[a-zA-Z][a-zA-Z\d+.-]*:/.test(normalizedSlashes) ||
    normalizedSlashes.includes("?") ||
    normalizedSlashes.includes("#")
  ) {
    throw new Error(
      `Provider resource path must be provider-root-relative: ${resourcePath}.`
    );
  }
  const normalized = path.posix.normalize(normalizedSlashes);
  if (normalized === ".." || normalized.startsWith("../")) {
    throw new Error(
      `Provider resource path escapes its root: ${resourcePath}.`
    );
  }
  return `./${normalized.replace(/^\.\//, "")}`;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}
