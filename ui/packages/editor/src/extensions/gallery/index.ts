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
import { markRaw } from "vue";
import MdiImageMultiple from "~icons/mdi/image-multiple";
import MdiImagePlus from "~icons/mdi/image-plus";
import BubbleItemAddImage from "./BubbleItemAddImage.vue";
import GalleryView from "./GalleryView.vue";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    gallery: {
      setGallery: (options?: { images?: string[] }) => ReturnType;
    };
  }
}

export const GALLERY_BUBBLE_MENU_KEY = new PluginKey("galleryBubbleMenu");

const Gallery = Node.create<ExtensionOptions>({
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
          const imgElements = element.querySelectorAll("img");
          return Array.from(imgElements).map((img) => img.getAttribute("src"));
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
    const images = node.attrs.images || [];
    const imageElements = images.map((src: string) => [
      "img",
      { src, class: "gallery-image" },
    ]);

    return [
      "div",
      {
        "data-type": "gallery",
        class: "gallery-container",
      },
      ["div", { class: "gallery-grid" }, ...imageElements],
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
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenuType {
        return {
          pluginKey: GALLERY_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, Gallery.name);
          },
          options: {
            placement: "top-start",
          },
          items: [
            {
              priority: 10,
              component: markRaw(BubbleItemAddImage),
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
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  deleteNode(Gallery.name, editor);
                },
              },
            },
          ],
        };
      },
    };
  },
});

export default Gallery;
