export function generateThumbnailUrl(url: string, size: string) {
  return `/apis/api.storage.halo.run/v1alpha1/thumbnails/-/via-uri?uri=${encodeURIComponent(
    url
  )}&size=${size}`;
}
