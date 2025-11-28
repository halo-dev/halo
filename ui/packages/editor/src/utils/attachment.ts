import type { Content } from "@/tiptap";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";

export function convertToMediaContents(
  attachments: AttachmentLike[]
): Content[] {
  return attachments
    .map((attachment) => {
      if (typeof attachment === "string") {
        return {
          type: "image",
          attrs: {
            src: attachment,
          },
        };
      }

      const attachmentSimple = utils.attachment.convertToSimple(attachment);

      if (!attachmentSimple) {
        return;
      }

      const { mediaType, alt, url } = attachmentSimple;

      if (mediaType?.startsWith("image/")) {
        return {
          type: "image",
          attrs: {
            src: url,
            alt,
          },
        };
      }

      if (mediaType?.startsWith("video/")) {
        return {
          type: "video",
          attrs: {
            src: url,
          },
        };
      }

      if (mediaType?.startsWith("audio/")) {
        return {
          type: "audio",
          attrs: {
            src: url,
          },
        };
      }

      return {
        type: "text",
        marks: [
          {
            type: "link",
            attrs: {
              href: url,
            },
          },
        ],
        text: alt || url,
      };
    })
    .filter(Boolean) as Content[];
}
