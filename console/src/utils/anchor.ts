export function generateAnchor(text: string) {
  return encodeURIComponent(
    String(text).trim().toLowerCase().replace(/\s+/g, "-")
  );
}
