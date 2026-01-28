import { ExtensionAudio } from "@/extensions/audio";
import { ExtensionImage } from "@/extensions/image";
import { ExtensionVideo } from "@/extensions/video";
import { Editor, PMNode } from "@/tiptap";
import { ucApiClient, type Attachment } from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import type { AxiosRequestConfig } from "axios";
import { chunk } from "es-toolkit";

export interface FileProps {
  file: File;
  editor: Editor;
}

/**
 * Handles file events, determining if the file is an image and triggering the appropriate upload process.
 *
 * @param {FileProps} { file, editor } - File and editor instances
 * @returns {boolean} - True if a file is handled, otherwise false
 */
export const handleFileEvent = ({ file, editor }: FileProps) => {
  if (!file) {
    return false;
  }

  if (
    !utils.permission.has([
      "uc:attachments:manage",
      "system:attachments:manage",
    ])
  ) {
    return false;
  }

  if (file.type.startsWith("image/")) {
    uploadImage({ file, editor });
    return true;
  }

  if (file.type.startsWith("video/")) {
    uploadVideo({ file, editor });
    return true;
  }

  if (file.type.startsWith("audio/")) {
    uploadAudio({ file, editor });
    return true;
  }

  return true;
};

/**
 * Uploads an image file and inserts it into the editor.
 *
 * @param {FileProps} { file, editor } - File to be uploaded and the editor instance
 */
export const uploadImage = ({ file, editor }: FileProps) => {
  const { view } = editor;
  const node = view.props.state.schema.nodes[ExtensionImage.name].create({
    file: file,
  });
  editor.view.dispatch(editor.view.state.tr.replaceSelectionWith(node));
};

/**
 * Uploads a video file and inserts it into the editor.
 *
 * @param {FileProps} { file, editor } - File to be uploaded and the editor instance
 */
export const uploadVideo = ({ file, editor }: FileProps) => {
  const { view } = editor;
  const node = view.props.state.schema.nodes[ExtensionVideo.name].create({
    file: file,
  });
  editor.view.dispatch(editor.view.state.tr.replaceSelectionWith(node));
};

/**
 * Uploads an audio file and inserts it into the editor.
 *
 * @param {FileProps} { file, editor } - File to be uploaded and the editor instance
 */
export const uploadAudio = ({ file, editor }: FileProps) => {
  const { view } = editor;
  const node = view.props.state.schema.nodes[ExtensionAudio.name].create({
    file: file,
  });
  editor.view.dispatch(editor.view.state.tr.replaceSelectionWith(node));
};

export interface UploadFetchResponse {
  controller: AbortController;
  onUploadProgress: (progress: number) => void;
  onFinish: (attachment?: Attachment) => void;
  onError: (error: Error) => void;
}

/**
 * Uploads a file with progress monitoring, cancellation support, and callbacks for completion and errors.
 *
 * @param {File} file - File to be uploaded
 * @param {Function} upload - Function to handle the file upload, should return a Promise
 * @returns {Promise<UploadFetchResponse>} - Returns an object with control and callback methods
 */
export const uploadFile = async (
  file: File,
  upload: (file: File, options?: AxiosRequestConfig) => Promise<Attachment>,
  uploadResponse: UploadFetchResponse
) => {
  const { signal } = uploadResponse.controller;

  upload(file, {
    signal,
    onUploadProgress(progressEvent) {
      const progress = Math.round(
        (progressEvent.loaded * 100) / (progressEvent.total || 0)
      );
      uploadResponse.onUploadProgress(progress);
    },
  })
    .then((attachment) => {
      uploadResponse.onFinish(attachment);
    })
    .catch((error) => {
      uploadResponse.onError(error);
    });
};

/**
 * Converts a file to a Base64 string.
 *
 * @param {File} file - File to be converted
 * @returns {Promise<string>} - A promise that resolves with the Base64 string
 */
export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = function () {
      resolve(reader.result as string);
    };
    reader.onerror = function (error) {
      reject(error);
    };
    reader.readAsDataURL(file);
  });
}

export function containsFileClipboardIdentifier(types: readonly string[]) {
  const fileTypes = ["files", "application/x-moz-file", "public.file-url"];
  return types.some((type) => fileTypes.includes(type.toLowerCase()));
}

export async function batchUploadExternalLink(
  editor: Editor,
  nodes: { node: PMNode; pos: number; index: number; parent: PMNode | null }[],
  trustedDomains: string[] = []
) {
  const chunks = chunk(nodes, 5);

  for (const chunkNodes of chunks) {
    await Promise.all(
      chunkNodes.map((node) => uploadExternalLink(editor, node, trustedDomains))
    );
  }
}

export async function uploadExternalLink(
  editor: Editor,
  nodeWithPos: {
    node: PMNode;
    pos: number;
    index: number;
    parent: PMNode | null;
  },
  trustedDomains: string[] = []
) {
  const { node, pos } = nodeWithPos;
  const { src } = node.attrs;

  if (!isExternalAsset(src, trustedDomains)) {
    return;
  }

  try {
    const { data } = await ucApiClient.storage.attachment.uploadAttachmentForUc(
      {
        url: src,
      }
    );

    const url = data.status?.permalink;
    const name = data.spec.displayName;
    const tr = editor.view.state.tr;
    tr.setNodeMarkup(pos, node.type, {
      ...node.attrs,
      src: url,
      name,
    });
    editor.view.dispatch(tr);
  } catch (error) {
    console.error("Failed to upload external link:", error);
  }
}

export function isExternalAsset(src: string, trustedDomains: string[] = []) {
  if (!src) {
    return false;
  }

  if (src.startsWith("/")) {
    return false;
  }

  const localProtocols = ["data:", "blob:", "file:"];
  if (localProtocols.some((protocol) => src.startsWith(protocol))) {
    return false;
  }

  const currentOrigin = window.location.origin;
  if (src.startsWith(currentOrigin)) {
    return false;
  }

  // Check against trusted domains whitelist
  if (
    trustedDomains.length > 0 &&
    (src.startsWith("http://") || src.startsWith("https://"))
  ) {
    try {
      const url = new URL(src);
      const hostname = url.hostname;
      // Check if hostname exactly matches or is a subdomain of any trusted domain
      const isTrusted = trustedDomains.some((domain) => {
        const normalizedDomain = domain.trim();
        return (
          hostname === normalizedDomain ||
          hostname.endsWith(`.${normalizedDomain}`)
        );
      });
      if (isTrusted) {
        return false;
      }
    } catch (e) {
      // Invalid URL, continue to external check
      console.warn("Failed to parse URL for trusted domain check:", src, e);
    }
  }

  return src.startsWith("http://") || src.startsWith("https://");
}
