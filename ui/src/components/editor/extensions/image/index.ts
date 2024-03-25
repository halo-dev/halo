import { ExtensionImage, VueNodeViewRenderer } from "@halo-dev/richtext-editor";
import ImageView from "./ImageView.vue";
import type { AxiosRequestConfig } from "axios";
import type { Attachment } from "@halo-dev/api-client";

export interface ImageOptions {
  inline: boolean;
  allowBase64: boolean;
  HTMLAttributes: Record<string, unknown>;
}

export interface UiImageOptions {
  uploadImage?: (
    file: File,
    options?: AxiosRequestConfig
  ) => Promise<Attachment>;
}

const Image = ExtensionImage.extend<ImageOptions & UiImageOptions>({
  addOptions() {
    const { parent } = this;
    return {
      ...parent?.(),
      uploadImage: undefined,
    };
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: "100%",
        parseHTML: (element) => {
          const width =
            element.getAttribute("width") || element.style.width || null;
          return width;
        },
        renderHTML: (attributes) => {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: "100%",
        parseHTML: (element) => {
          const height =
            element.getAttribute("height") || element.style.height || null;
          return height;
        },
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
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

  addNodeView() {
    return VueNodeViewRenderer(ImageView);
  },
});

export default Image;
