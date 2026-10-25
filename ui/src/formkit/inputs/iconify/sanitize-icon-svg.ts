import DOMPurify from "dompurify";

/** Sanitize at every inline SVG sink, including values saved before this fix. */
export function sanitizeIconSvg(value?: string): string {
  return DOMPurify.sanitize(value || "", {
    USE_PROFILES: { svg: true, svgFilters: true },
    // SVG stylesheets otherwise apply to the surrounding administration UI.
    FORBID_TAGS: ["style"],
  });
}
