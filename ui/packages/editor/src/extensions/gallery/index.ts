import { BlockActionSeparator } from "@/components";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  isActive,
  Node,
  PluginKey,
  VueNodeViewRenderer,
  type Editor,
  type Range,
} from "@/tiptap";
import type { EditorState } from "@/tiptap/pm";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { deleteNode } from "@/utils";
import type { Attachment } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";
import { markRaw } from "vue";
import MdiImagePlus from "~icons/mdi/image-plus";
import MingcutePhotoAlbumLine from "~icons/mingcute/photo-album-line";
import BubbleItemAddImage from "./BubbleItemAddImage.vue";
import BubbleItemGap from "./BubbleItemGap.vue";
import BubbleItemGroupSize from "./BubbleItemGroupSize.vue";
import BubbleItemLayout from "./BubbleItemLayout.vue";
import GalleryView from "./GalleryView.vue";
import { ExtensionGalleryBubble } from "./gallery-bubble";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    gallery: {
      setGallery: (options?: { images?: string[] }) => ReturnType;
    };
  }
}

export type ExtensionGalleryImageItem = {
  src: string;
  aspectRatio: number;
  alt?: string;
  caption?: string;
};

export const MAX_GALLERY_CAPTION_LENGTH = 500;
export const DEFAULT_GALLERY_GROUP_SIZE = 3;
export const DEFAULT_GALLERY_GAP = 0;
const GALLERY_CAPTION_NODE_TYPE = "gallery-caption";
const GALLERY_IMAGE_WRAPPER_NODE_TYPE = "gallery-image-wrapper";
const GALLERY_IMAGE_CAPTION_NODE_TYPE = "gallery-image-caption";

/**
 * Creates a normalized gallery image item.
 */
export function createGalleryImageItem(
  src: string,
  overrides: Partial<ExtensionGalleryImageItem> = {}
): ExtensionGalleryImageItem {
  return {
    src,
    aspectRatio: 0,
    alt: "",
    caption: "",
    ...overrides,
  };
}

function clampCaption(value?: string) {
  return (value || "").slice(0, MAX_GALLERY_CAPTION_LENGTH);
}

function isUnsafeUrl(url: string) {
  const normalized = url.trim().toLowerCase();
  return normalized.startsWith("javascript:");
}

function normalizeUrl(url: string) {
  return url.trim();
}

export const GALLERY_BUBBLE_MENU_KEY = new PluginKey("galleryBubbleMenu");

export type ExtensionGalleryOptions = ExtensionOptions & {
  groupSize?: number;
  gap?: number;
  allowBase64: boolean;
  HTMLAttributes: Record<string, unknown>;
  uploadImage?: (
    file: File,
    options?: AxiosRequestConfig
  ) => Promise<Attachment>;
};

export const ExtensionGallery = Node.create<
  ExtensionGalleryOptions,
  {
    images: ExtensionGalleryImageItem[];
  }
>({
  name: "gallery",

  group: "block",

  atom: true,

  draggable: true,

  fakeSelection: true,

  allowGapCursor: true,

  addAttributes() {
    return {
      images: {
        default: [],
        parseHTML: (element) => {
          const wrappers = Array.from(
            element.querySelectorAll(
              `div[data-type="${GALLERY_IMAGE_WRAPPER_NODE_TYPE}"]`
            )
          );

          if (wrappers.length > 0) {
            return wrappers
              .map((wrapper) => {
                const img = wrapper.querySelector("img");
                if (!img) {
                  return null;
                }
                const src = normalizeUrl(img.getAttribute("src") || "");
                if (!src || isUnsafeUrl(src)) {
                  return null;
                }
                const ratioFromImage = Number(
                  img.getAttribute("data-aspect-ratio")
                );
                const ratioFromWrapper = Number(
                  wrapper.getAttribute("data-aspect-ratio")
                );
                // 支持新的 figcaption 和旧的 span 结构
                const captionEl =
                  wrapper.querySelector("figcaption") ||
                  wrapper.querySelector(
                    `[data-type="${GALLERY_IMAGE_CAPTION_NODE_TYPE}"]`
                  );
                const caption = clampCaption(captionEl?.textContent || "");
                return createGalleryImageItem(src, {
                  alt: img.getAttribute("alt") || "",
                  caption,
                  aspectRatio: ratioFromImage || ratioFromWrapper || 0,
                });
              })
              .filter(Boolean) as ExtensionGalleryImageItem[];
          }

          return Array.from(element.querySelectorAll("img"))
            .map((img) => {
              const src = normalizeUrl(img.getAttribute("src") || "");
              if (!src || isUnsafeUrl(src)) {
                return null;
              }
              const ratioFromImage = Number(
                img.getAttribute("data-aspect-ratio")
              );
              const ratioFromWrapper = Number(
                img
                  .closest("[data-aspect-ratio]")
                  ?.getAttribute("data-aspect-ratio")
              );
              return createGalleryImageItem(src, {
                alt: img.getAttribute("alt") || "",
                caption: clampCaption(img.getAttribute("data-caption") || ""),
                aspectRatio: ratioFromImage || ratioFromWrapper || 0,
              });
            })
            .filter(Boolean) as ExtensionGalleryImageItem[];
        },
      },
      groupSize: {
        default: DEFAULT_GALLERY_GROUP_SIZE,
        parseHTML: (element) => {
          return (
            Number(element.getAttribute("data-group-size")) ||
            DEFAULT_GALLERY_GROUP_SIZE
          );
        },
      },
      layout: {
        default: "auto",
        parseHTML: (element) => {
          return element.getAttribute("data-layout") || "auto";
        },
      },
      gap: {
        default: 8,
        parseHTML: (element) => {
          const gap = Number(element.getAttribute("data-gap"));
          if (isNaN(gap) || gap < 0) {
            return 0;
          }
          return gap;
        },
      },
      caption: {
        default: "",
        parseHTML: (element) => {
          const captionEl = element.querySelector(
            `[data-type="${GALLERY_CAPTION_NODE_TYPE}"]`
          );
          if (captionEl) {
            return clampCaption(captionEl.textContent || "");
          }
          return clampCaption(element.getAttribute("data-caption") || "");
        },
      },
      file: {
        default: null,
        renderHTML() {
          return {};
        },
        parseHTML() {
          return null;
        },
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type="gallery"]',
      },
    ];
  },

  renderHTML({ node }) {
    const images: ExtensionGalleryImageItem[] = node.attrs.images || [];
    const groupSize =
      node.attrs.groupSize ||
      this.options?.groupSize ||
      DEFAULT_GALLERY_GROUP_SIZE;
    const layout = node.attrs.layout || "auto";
    const gap = node.attrs.gap || this.options?.gap || DEFAULT_GALLERY_GAP;
    const caption = clampCaption(node.attrs.caption || "");
    const imageGroups: ExtensionGalleryImageItem[][] = images.reduce(
      (
        acc: ExtensionGalleryImageItem[][],
        image: ExtensionGalleryImageItem,
        index: number
      ) => {
        const groupIndex = Math.floor(index / groupSize);
        acc[groupIndex] = acc[groupIndex] || [];
        acc[groupIndex].push(image);
        return acc;
      },
      []
    );
    const imageGroupElements = imageGroups.map(
      (items: ExtensionGalleryImageItem[]) => [
        "div",
        {
          "data-type": "gallery-group",
          style: `display: flex; flex-direction: row; justify-content: center; gap: ${gap}px;`,
        },
        ...items.map((image: ExtensionGalleryImageItem) => {
          const imageCaption = clampCaption(image.caption || "");
          const captionElement = imageCaption
            ? [
                "figcaption",
                {
                  "data-type": GALLERY_IMAGE_CAPTION_NODE_TYPE,
                  style:
                    "margin-top: 0.25rem; text-align: center; font-size: 0.875rem; color: #6b7280;",
                },
                imageCaption,
              ]
            : [
                "span",
                {
                  "data-type": GALLERY_IMAGE_CAPTION_NODE_TYPE,
                  style: "display: none;",
                },
                "",
              ];
          return [
            "figure",
            {
              style: `flex: ${layout === "square" ? "1" : image.aspectRatio} 1 0%; display: flex; flex-direction: column; margin: 0;${layout === "square" ? "aspect-ratio: 1/1;" : ""}`,
              "data-aspect-ratio": image.aspectRatio.toString(),
              "data-type": GALLERY_IMAGE_WRAPPER_NODE_TYPE,
            },
            [
              "img",
              {
                src: image.src,
                alt: image.alt || "",
                "data-type": "gallery-image",
                "data-aspect-ratio": image.aspectRatio.toString(),
                style:
                  "width: 100%; height: 100%; margin: 0; object-fit: cover;",
              },
            ],
            captionElement,
          ];
        }),
      ]
    );

    return [
      "div",
      {
        "data-type": "gallery",
        "data-group-size": groupSize.toString(),
        "data-layout": layout,
        "data-gap": gap?.toString(),
      },
      [
        "div",
        { style: `display: grid; gap: ${gap}px;` },
        ...imageGroupElements,
      ],
      [
        "div",
        { "data-type": GALLERY_CAPTION_NODE_TYPE, style: "display: none;" },
        caption,
      ],
    ];
  },

  addCommands() {
    return {
      setGallery:
        (options) =>
        ({ commands }) => {
          return commands.insertContent({
            type: this.name,
            attrs: {
              images: options?.images || [],
            },
          });
        },
    };
  },

  addNodeView() {
    return VueNodeViewRenderer(GalleryView);
  },

  addOptions() {
    return {
      ...this.parent?.(),
      allowBase64: false,
      HTMLAttributes: {},
      uploadImage: undefined,
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 15,
          component: markRaw(ToolboxItem),
          props: {
            editor,
            icon: markRaw(MingcutePhotoAlbumLine),
            title: i18n.global.t("editor.extensions.gallery.title"),
            action: () => {
              editor.chain().focus().setGallery({ images: [] }).run();
            },
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 96,
          icon: markRaw(MingcutePhotoAlbumLine),
          title: "editor.extensions.commands_menu.gallery",
          keywords: ["gallery", "hualang", "tupian", "images"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setGallery({ images: [] })
              .run();
          },
        };
      },
      getBubbleMenu(): NodeBubbleMenuType {
        return {
          pluginKey: GALLERY_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, ExtensionGallery.name);
          },
          options: {
            placement: "top-start",
          },
          items: [
            {
              priority: 10,
              component: markRaw(BubbleItemAddImage),
              key: "add-image",
              props: {
                icon: markRaw(MdiImagePlus),
                title: i18n.global.t("editor.extensions.gallery.add_image"),
              },
            },
            {
              priority: 20,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 30,
              component: markRaw(BubbleItemGroupSize),
            },
            {
              priority: 40,
              component: markRaw(BubbleItemLayout),
            },
            {
              priority: 50,
              component: markRaw(BubbleItemGap),
            },
            {
              priority: 60,
              props: {
                icon: markRaw(MingcuteDelete2Line),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  deleteNode(ExtensionGallery.name, editor);
                },
              },
            },
          ],
        };
      },
    };
  },

  addExtensions() {
    return [
      ...(this.parent?.() || []),
      ExtensionGalleryBubble.configure({
        uploadImage: this.options.uploadImage,
      }),
    ];
  },
});
