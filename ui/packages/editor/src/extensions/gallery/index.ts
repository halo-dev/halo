import { BlockActionSeparator } from "@/components";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
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
import MdiImageMultiple from "~icons/mdi/image-multiple";
import MdiImagePlus from "~icons/mdi/image-plus";
import BubbleItemAddImage from "./BubbleItemAddImage.vue";
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
};

export const GALLERY_BUBBLE_MENU_KEY = new PluginKey("galleryBubbleMenu");

export type ExtensionGalleryOptions = ExtensionOptions & {
  groupSize?: number;
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
          return Array.from(element.querySelectorAll("img")).map((img) => {
            return {
              src: img.getAttribute("src") || "",
              aspectRatio: Number(img.getAttribute("data-aspect-ratio")) || 0,
            };
          });
        },
      },
      groupSize: {
        default: 3,
        parseHTML: (element) => {
          return Number(element.getAttribute("data-group-size")) || 3;
        },
      },
      layout: {
        default: "auto",
        parseHTML: (element) => {
          return element.getAttribute("data-layout") || "auto";
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
    const groupSize = node.attrs.groupSize || this.options?.groupSize || 3;
    const layout = node.attrs.layout || "auto";
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
          style:
            "display: flex; flex-direction: row; justify-content: center; gap: 0.5rem;",
        },
        ...items.map((image: ExtensionGalleryImageItem) => {
          return [
            "div",
            {
              style: `flex: ${layout === "square" ? "1" : image.aspectRatio} 1 0%;${layout === "square" ? "aspect-ratio: 1/1;" : ""}`,
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
      },
      ["div", { style: "display: grid; gap: 0.5rem;" }, ...imageGroupElements],
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
            icon: markRaw(MdiImageMultiple),
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
          icon: markRaw(MdiImageMultiple),
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
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
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
