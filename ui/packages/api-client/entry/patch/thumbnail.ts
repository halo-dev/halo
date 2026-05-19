/**
 * Note: This export is for compatibility with older versions, it will be removed in the future, please do not use it.
 * See：https://github.com/halo-dev/halo/issues/10012
 * @deprecated
 */
export interface ThumbnailSpec {
  imageSignature: string;
  imageUri: string;
  size: ThumbnailSpecSizeEnum;
  thumbnailUri: string;
}

/**
 * Note: This export is for compatibility with older versions, it will be removed in the future, please do not use it.
 * See：https://github.com/halo-dev/halo/issues/10012
 * @deprecated use GetThumbnailByUriSizeEnum instead
 */
export const ThumbnailSpecSizeEnum = {
  S: "S",
  M: "M",
  L: "L",
  Xl: "XL",
} as const;

/**
 * Note: This export is for compatibility with older versions, it will be removed in the future, please do not use it.
 * See：https://github.com/halo-dev/halo/issues/10012
 * @deprecated use GetThumbnailByUriSizeEnum instead
 */
export type ThumbnailSpecSizeEnum =
  (typeof ThumbnailSpecSizeEnum)[keyof typeof ThumbnailSpecSizeEnum];
