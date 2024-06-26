import type { AttachmentLike } from "@halo-dev/console-shared";
import type { Content } from "@halo-dev/richtext-editor";

export function getContents(attachments: AttachmentLike[]): Content[] {
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

      if ("url" in attachment) {
        return {
          type: "image",
          attrs: {
            src: attachment.url,
            alt: attachment.type,
          },
        };
      }

      if ("spec" in attachment) {
        const { mediaType, displayName } = attachment.spec;
        const { permalink } = attachment.status || {};
        if (mediaType?.startsWith("image/")) {
          return {
            type: "image",
            attrs: {
              src: permalink,
              alt: displayName,
            },
          };
        }

        if (mediaType?.startsWith("video/")) {
          return {
            type: "video",
            attrs: {
              src: permalink,
            },
          };
        }

        if (mediaType?.startsWith("audio/")) {
          return {
            type: "audio",
            attrs: {
              src: permalink,
            },
          };
        }

        return {
          type: "text",
          marks: [
            {
              type: "link",
              attrs: {
                href: permalink,
              },
            },
          ],
          text: displayName,
        };
      }
    })
    .filter(Boolean) as Content[];
}

export interface AttachmentAttr {
  url?: string;
  name?: string;
}

export function getAttachmentUrl(attachment: AttachmentLike): AttachmentAttr {
  let permalink: string | undefined = undefined;
  let displayName: string | undefined = undefined;
  if (typeof attachment === "string") {
    permalink = attachment;
  } else if ("url" in attachment) {
    permalink = attachment.url;
  } else if ("spec" in attachment) {
    permalink = attachment.status?.permalink;
    displayName = attachment.spec.displayName;
  }

  return {
    url: permalink,
    name: displayName,
  };
}
