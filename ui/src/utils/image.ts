export const imageTypes: string[] = [
  "image/jpeg",
  "image/jpg",
  "image/png",
  "image/gif",
  "image/webp",
  "image/svg+xml",
  "image/avif",
];

export function isImage(mediaType: string | undefined | null): boolean {
  if (!mediaType) {
    return false;
  }
  return imageTypes.includes(mediaType);
}
