export function hasRspackContentHashPlaceholder(value: unknown) {
  return typeof value === "string" && /\[contenthash(?::\d+)?\]/.test(value);
}
