import { DOMParser, Editor, elementFromString, type Content } from "@/tiptap";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";

export function convertToMediaContents(
  editor: Editor,
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

      const { mediaType, alt, url, caption } = attachmentSimple;

      if (mediaType?.startsWith("image/")) {
        return createFigureContent(editor, {
          contentType: "image",
          url,
          alt,
          caption,
        });
      }

      if (mediaType?.startsWith("video/")) {
        return createFigureContent(editor, {
          contentType: "video",
          url,
          caption,
        });
      }

      if (mediaType?.startsWith("audio/")) {
        return createFigureContent(editor, {
          contentType: "audio",
          url,
          caption,
        });
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

function createFigureContent(
  editor: Editor,
  {
    contentType,
    url,
    alt,
    caption,
  }: {
    contentType: "image" | "video" | "audio";
    url: string;
    alt?: string;
    caption?: string;
  }
) {
  const baseContent: Content = {
    type: "figure",
    attrs: {
      contentType,
    },
    content: [
      {
        type: contentType,
        attrs: {
          src: url,
          alt,
        },
      },
    ],
  };

  if (caption) {
    const dom = elementFromString(caption);
    const content = DOMParser.fromSchema(editor.schema).parse(dom).toJSON();
    baseContent.content?.push({
      type: "figureCaption",
      // TODO: Find a better way to handle the caption content
      content: content.content[0].content,
    });
  }

  return baseContent;
}
