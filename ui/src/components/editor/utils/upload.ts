// image drag and paste upload
import { CoreEditor } from "@halo-dev/richtext-editor";
import type { Attachment } from "@halo-dev/api-client";
import Image from "../extensions/image";
import ExtensionVideo from "../extensions/video";
import ExtensionAudio from "../extensions/audio";
import type { AxiosRequestConfig } from "axios";
import { usePermission } from "@/utils/permission";

export interface FileProps {
  file: File;
  editor: CoreEditor;
}

const { currentUserHasPermission } = usePermission();

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

  if (!currentUserHasPermission(["uc:attachments:manage"])) {
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
  const node = view.props.state.schema.nodes[Image.name].create({
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
