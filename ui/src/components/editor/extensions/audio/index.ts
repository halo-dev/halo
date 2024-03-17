import { ExtensionAudio, VueNodeViewRenderer } from "@halo-dev/richtext-editor";
import type { AxiosRequestConfig } from "axios";
import type { Attachment } from "@halo-dev/api-client";
import AudioView from "./AudioView.vue";

interface UiAudioOptions {
  uploadAudio?: (
    file: File,
    options?: AxiosRequestConfig
  ) => Promise<Attachment>;
}

const Audio = ExtensionAudio.extend<UiAudioOptions>({
  addOptions() {
    const { parent } = this;
    return {
      ...parent?.(),
      uploadAudio: undefined,
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
    return VueNodeViewRenderer(AudioView);
  },
});

export default Audio;
