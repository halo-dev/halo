// image drag and paste upload
import { CoreEditor } from "@halo-dev/richtext-editor";
import type { Attachment } from "@halo-dev/api-client";
import Image from "../extensions/image";
import type { AxiosRequestConfig } from "axios";

export interface FileProps {
  file: File;
  editor: CoreEditor;
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

  if (file.type.startsWith("image/")) {
    uploadImage({ file, editor });
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

export interface UploadFetchResponse {
  controller?: AbortController;
  onUploadProgress: (progress: number) => void;
  onFinish: (attachment?: Attachment) => void;
  onError: (error: Error) => void;
}

const REQUEST_TIMEOUT_MS = 1000 * 60;

/**
 * Uploads a file with progress monitoring, cancellation support, and callbacks for completion and errors.
 *
 * @param {File} file - File to be uploaded
 * @param {Function} upload - Function to handle the file upload, should return a Promise
 * @returns {Promise<UploadFetchResponse>} - Returns an object with control and callback methods
 */
export const uploadFile = async (
  file: File,
  upload?: (file: File, options?: AxiosRequestConfig) => Promise<Attachment>
): Promise<UploadFetchResponse> => {
  const controller = new AbortController();
  const requestTimeoutId = setTimeout(
    () => controller.abort(),
    REQUEST_TIMEOUT_MS
  );
  const { signal } = controller;

  const response: UploadFetchResponse = {
    controller: controller,
    onUploadProgress: (progress) => (callback: (progress: number) => void) => {
      callback(progress);
    },
    onFinish:
      (attachment?: Attachment) =>
      (callback: (attachment?: Attachment) => void) => {
        callback(attachment);
      },
    onError: (error) => (callback: (error: Error) => void) => {
      callback(error);
    },
  };

  return new Promise((resolve, reject) => {
    if (!upload) {
      reject(new Error("upload function is not defined"));
      return;
    }

    upload(file, {
      signal,
      onUploadProgress(progressEvent) {
        requestTimeoutId && clearTimeout(requestTimeoutId);
        const progress = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        response.onUploadProgress(progress);
      },
    })
      .then((attachment) => {
        response.onFinish(attachment);
      })
      .catch((error) => {
        response.onError(error);
      });

    resolve(response);
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
