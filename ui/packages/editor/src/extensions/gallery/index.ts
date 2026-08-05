import { markRaw } from "vue";
import MdiImagePlus from "~icons/mdi/image-plus";
import MingcutePhotoAlbumLine from "~icons/mingcute/photo-album-line";
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
import type { UploadFile } from "@/utils/upload";
import BubbleItemAddImage from "./BubbleItemAddImage.vue";
import BubbleItemGap from "./BubbleItemGap.vue";
import BubbleItemGroupSize from "./BubbleItemGroupSize.vue";
import BubbleItemLayout from "./BubbleItemLayout.vue";
import {
  DEFAULT_GALLERY_GAP,
  DEFAULT_GALLERY_GROUP_SIZE,
  DEFAULT_GALLERY_LAYOUT,
  GALLERY_LAYOUT_SQUARE,
  GALLERY_LAYOUTS,
} from "./constants";
import { ExtensionGalleryBubble } from "./gallery-bubble";
import GalleryView from "./GalleryView.vue";

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
};

export const GALLERY_BUBBLE_MENU_KEY = new PluginKey("galleryBubbleMenu");

export type ExtensionGalleryOptions = ExtensionOptions & {
  groupSize?: number;
  gap?: number;
  allowBase64: boolean;
  HTMLAttributes: Record<string, unknown>;
  uploadImage?: UploadFile;
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

  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A gallery that presents multiple images in configurable groups.",
        exposure: "available",
        useWhen: [
          "Several related images should be presented as one visual set.",
        ],
        avoidWhen: ["Only one image is needed."],
        attributeGuidance: {
          images: {
            description:
              "Ordered gallery items, each containing a source URL and aspect ratio.",
            format: "array of { src: string, aspectRatio: number }",
          },
          groupSize: {
            description: "Maximum number of images in each visual group.",
            examples: [2, DEFAULT_GALLERY_GROUP_SIZE, 4],
          },
          layout: {
            description: "Gallery layout strategy.",
            allowedValues: [...GALLERY_LAYOUTS],
          },
          gap: {
            description: "Gap between gallery images in pixels.",
            examples: [0, DEFAULT_GALLERY_GAP, 16],
          },
          file: {
            description:
              "Editor-only upload state that is not part of persisted article HTML.",
            omitWhen: ["Generating or editing persisted article content."],
          },
        },
        generation: {
          mode: "direct-html",
          guidelines: [
            "Use stable accessible image URLs; uploading local files requires a separate plugin capability.",
          ],
        },
        examples: [
          `<div data-type="gallery" data-group-size="2" data-layout="${DEFAULT_GALLERY_LAYOUT}" data-gap="${DEFAULT_GALLERY_GAP}"><div data-type="gallery-group"><div data-aspect-ratio="1.5"><img src="https://example.com/one.jpg"></div><div data-aspect-ratio="1.5"><img src="https://example.com/two.jpg"></div></div></div>`,
          `<div data-type="gallery" data-group-size="${DEFAULT_GALLERY_GROUP_SIZE}" data-layout="${GALLERY_LAYOUT_SQUARE}" data-gap="12"><div data-type="gallery-group"><div data-aspect-ratio="1"><img src="https://example.com/one.jpg"></div><div data-aspect-ratio="1.5"><img src="https://example.com/two.jpg"></div><div data-aspect-ratio="0.75"><img src="https://example.com/three.jpg"></div></div></div>`,
        ],
      },
    };
  },

  addAttributes() {
    return {
      images: {
        default: [],
        parseHTML: (element) => {
          return Array.from(element.querySelectorAll("img")).map((img) => {
            const aspectRatio = Number(
              img.parentElement?.getAttribute("data-aspect-ratio") ??
                img.getAttribute("data-aspect-ratio")
            );
            return {
              src: img.getAttribute("src") || "",
              aspectRatio: aspectRatio || 0,
            };
          });
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
        default: DEFAULT_GALLERY_LAYOUT,
        parseHTML: (element) => {
          return element.getAttribute("data-layout") || DEFAULT_GALLERY_LAYOUT;
        },
      },
      gap: {
        default: DEFAULT_GALLERY_GAP,
        parseHTML: (element) => {
          const gap = Number(element.getAttribute("data-gap"));
          if (isNaN(gap) || gap < 0) {
            return 0;
          }
          return gap;
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
    const layout = node.attrs.layout || DEFAULT_GALLERY_LAYOUT;
    const gap = node.attrs.gap || this.options?.gap || 0;
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
          return [
            "div",
            {
              style: `flex: ${layout === GALLERY_LAYOUT_SQUARE ? "1" : image.aspectRatio} 1 0%;${layout === GALLERY_LAYOUT_SQUARE ? "aspect-ratio: 1/1;" : ""}`,
              "data-aspect-ratio": image.aspectRatio.toString(),
            },
            [
              "img",
              {
                src: image.src,
                "data-type": "gallery-image",
                style:
                  "width: 100%; height: 100%; margin: 0; object-fit: cover;",
              },
            ],
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
