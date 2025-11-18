import { i18n } from "@/locales";
import {
  Extension,
  type ExtensionOptions,
  GALLERY_BUBBLE_MENU_KEY,
} from "@halo-dev/richtext-editor";
import { markRaw } from "vue";
import MdiImagePlus from "~icons/mdi/image-plus";
import type { UiGalleryOptions } from ".";
import BubbleItemAddImage from "./BubbleItemAddImage.vue";

export const GalleryBubble = Extension.create<
  ExtensionOptions & UiGalleryOptions
>({
  name: "gallery-bubble",
  addOptions() {
    const { parent } = this;
    return {
      ...parent?.(),
      getBubbleMenu: () => {
        return {
          extendsKey: GALLERY_BUBBLE_MENU_KEY,
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
          ],
        };
      },
    };
  },
});
