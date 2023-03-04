export const imageTypes: string[] = [
  "image/jpeg",
  "image/jpg",
  "image/png",
  "image/gif",
  "image/webp",
  "image/svg+xml",
];

export function isImage(mediaType: string | undefined): boolean {
  if (!mediaType) {
    return false;
  }
  return imageTypes.includes(mediaType);
}
