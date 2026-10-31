import axios from "axios";

export const ICONIFY_BASE_URL = "https://api.iconify.design";

export const iconifyClient = axios.create({
  baseURL: ICONIFY_BASE_URL,
});

/**
 * Iconify's SVG endpoint is `/{prefix}/{name}.svg`.
 * Icon names stay `prefix:name` for storage and the Iconify component.
 */
export function buildIconifySvgUrl(
  iconName: string,
  options: { width?: string; color?: string }
): string {
  const separator = iconName.indexOf(":");
  const iconPath =
    separator === -1
      ? iconName
      : `${iconName.slice(0, separator)}/${iconName.slice(separator + 1)}`;
  const width = options.width ?? "";
  const color = encodeURIComponent(options.color ?? "");
  return `${ICONIFY_BASE_URL}/${iconPath}.svg?width=${width}&color=${color}`;
}
