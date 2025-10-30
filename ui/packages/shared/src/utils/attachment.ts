import type { AttachmentLike, AttachmentSimple } from "@/plugin";
import type { GetThumbnailByUriSizeEnum } from "@halo-dev/api-client";

/**
 * Mapping of thumbnail size enums to their corresponding widths in pixels
 *
 * @remarks
 * - XL: 1600px - Extra large thumbnails
 * - L: 1200px - Large thumbnails
 * - M: 800px - Medium thumbnails
 * - S: 400px - Small thumbnails
 */
export const THUMBNAIL_WIDTH_MAP: Record<GetThumbnailByUriSizeEnum, number> = {
  XL: 1600,
  L: 1200,
  M: 800,
  S: 400,
};

export class AttachmentUtils {
  /**
   * Generates a thumbnail URL for the given image URL with the specified size
   *
   * @param url - The original image URL (can be absolute, relative, or external)
   * @param size - The desired thumbnail size (XL, L, M, or S)
   * @returns The thumbnail URL with width parameter, or original URL if size is invalid
   *
   * @remarks
   * This method handles three scenarios:
   * 1. If URL starts with current origin: Appends `?width={size}` query parameter
   * 2. If URL is a relative path (starts with "/"): Appends `?width={size}` query parameter
   * 3. If URL is external: Routes through Halo's thumbnail API endpoint
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   *
   * // Local image
   * utils.attachment.getThumbnailUrl("/uploads/image.jpg", "M");
   * // Returns: "/uploads/image.jpg?width=800"
   *
   * // Same origin image
   * utils.attachment.getThumbnailUrl("https://example.com/image.jpg", "S");
   * // Returns: "https://example.com/image.jpg?width=400" (if current origin is example.com)
   *
   * // External image
   * utils.attachment.getThumbnailUrl("https://external.com/image.jpg", "L");
   * // Returns: "/apis/api.storage.halo.run/v1alpha1/thumbnails/-/via-uri?uri=https%3A%2F%2Fexternal.com%2Fimage.jpg&width=1200"
   * ```
   */
  getThumbnailUrl(url: string, size: GetThumbnailByUriSizeEnum) {
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

  /**
   * Extracts the URL from an attachment-like object
   *
   * @param attachment - The attachment object (can be a string URL, Attachment object, or AttachmentSimple)
   * @returns The URL string extracted from the attachment
   * @throws {Error} When the attachment type is invalid or unrecognized
   *
   * @remarks
   * This method handles three types of attachments:
   * 1. String: Returns the string directly as URL
   * 2. Attachment object (with "spec"): Returns the permalink from status
   * 3. AttachmentSimple (with "url"): Returns the url property
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   *
   * // String URL
   * utils.attachment.getUrl("https://example.com/image.jpg");
   * // Returns: "https://example.com/image.jpg"
   *
   * // Attachment object
   * utils.attachment.getUrl(attachmentObject);
   * // Returns: attachmentObject.status?.permalink
   *
   * // AttachmentSimple object
   * utils.attachment.getUrl({ url: "https://example.com/image.jpg" });
   * // Returns: "https://example.com/image.jpg"
   * ```
   */
  getUrl(attachment: AttachmentLike) {
    if (typeof attachment === "string") {
      return attachment;
    }
    if ("spec" in attachment) {
      return attachment.status?.permalink;
    }
    if ("url" in attachment) {
      return attachment.url;
    }
    throw new Error("Invalid attachment");
  }

  /**
   * Converts an attachment-like object to a simplified attachment format
   *
   * @param attachment - The attachment object to convert (can be a string URL, Attachment object, or AttachmentSimple)
   * @returns A simplified attachment object with url, alt, and mediaType properties, or undefined
   * @throws {Error} When the attachment type is invalid or unrecognized
   *
   * @remarks
   * This method normalizes different attachment formats into a consistent AttachmentSimple structure:
   * 1. String: Converts to object with only url property
   * 2. Attachment object (with "spec"): Extracts permalink, displayName, and mediaType
   * 3. AttachmentSimple (with "url"): Returns as-is since it's already in the correct format
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   *
   * // String URL
   * utils.attachment.convertToSimple("https://example.com/image.jpg");
   * // Returns: { url: "https://example.com/image.jpg" }
   *
   * // Attachment object
   * utils.attachment.convertToSimple(attachmentObject);
   * // Returns: {
   * //   url: attachmentObject.status?.permalink || "",
   * //   alt: attachmentObject.spec.displayName,
   * //   mediaType: attachmentObject.spec.mediaType
   * // }
   *
   * // AttachmentSimple object
   * utils.attachment.convertToSimple({ url: "https://example.com/image.jpg", alt: "Image" });
   * // Returns: { url: "https://example.com/image.jpg", alt: "Image" }
   * ```
   */
  convertToSimple(attachment: AttachmentLike): AttachmentSimple | undefined {
    if (typeof attachment === "string") {
      return {
        url: attachment,
      };
    }
    if ("spec" in attachment) {
      return {
        url: attachment.status?.permalink || "",
        alt: attachment.spec.displayName,
        mediaType: attachment.spec.mediaType,
      };
    }
    if ("url" in attachment) {
      return attachment;
    }
    throw new Error("Invalid attachment");
  }
}
