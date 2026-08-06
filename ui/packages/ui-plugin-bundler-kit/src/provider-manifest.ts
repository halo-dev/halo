import path from "node:path";

export const ESM_PROVIDER_MANIFEST = "ui-plugin.json";

export interface EsmProviderManifest {
  format: "esm";
  entry: string;
  styles: string[];
}

export function validateEsmProviderManifest(
  value: unknown
): EsmProviderManifest {
  if (!isRecord(value)) {
    throw new Error("ESM provider manifest must be an object.");
  }
  const keys = Object.keys(value).sort();
  if (keys.join(",") !== "entry,format,styles" || value.format !== "esm") {
    throw new Error(
      'ESM provider manifest must contain only format, entry, and styles with format "esm".'
    );
  }
  if (typeof value.entry !== "string" || !Array.isArray(value.styles)) {
    throw new Error("ESM provider manifest entry and styles are required.");
  }
  return {
    format: "esm",
    entry: normalizeProviderResourcePath(value.entry),
    styles: value.styles.map((style) => {
      if (typeof style !== "string") {
        throw new Error("ESM provider manifest styles must be strings.");
      }
      return normalizeProviderResourcePath(style);
    }),
  };
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
