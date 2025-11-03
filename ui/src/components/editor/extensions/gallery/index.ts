import type { Attachment } from "@halo-dev/api-client";
import type { ExtensionOptions } from "@halo-dev/richtext-editor";
import {
  ExtensionGallery,
  VueNodeViewRenderer,
} from "@halo-dev/richtext-editor";
import type { AxiosRequestConfig } from "axios";
import GalleryView from "./GalleryView.vue";
import { GalleryBubble } from "./gallery-bubble";

export type GalleryImage = {
  src: string;
  aspectRatio: number;
};

export interface GalleryOptions {
  allowBase64: boolean;
  HTMLAttributes: Record<string, unknown>;
}

export interface UiGalleryOptions {
  uploadImage?: (
    file: File,
    options?: AxiosRequestConfig
  ) => Promise<Attachment>;
}

const Gallery = ExtensionGallery.extend<
  ExtensionOptions & Partial<GalleryOptions> & UiGalleryOptions
>({
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
    return VueNodeViewRenderer(GalleryView);
  },

  addExtensions() {
    return [
      ...(this.parent?.() || []),
      GalleryBubble.configure({
        uploadImage: this.options.uploadImage,
      }),
    ];
  },
});

export default Gallery;
