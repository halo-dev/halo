import type { GetThumbnailByUriSizeEnum } from "@halo-dev/api-client";

export const THUMBNAIL_WIDTH_MAP: Record<GetThumbnailByUriSizeEnum, number> = {
  XL: 1600,
  L: 1200,
  M: 800,
  S: 400,
};

export function generateThumbnailUrl(
  url: string,
  size: GetThumbnailByUriSizeEnum
) {
  const { origin } = location;

  const width = THUMBNAIL_WIDTH_MAP[size];

  if (!width) {
    return url;
  }

  if (url.startsWith(origin) || url.startsWith("/")) {
    return `${url}?width=${width}`;
  }

  return `/apis/api.storage.halo.run/v1alpha1/thumbnails/-/via-uri?uri=${encodeURIComponent(
    url
  )}&width=${width}`;
}
