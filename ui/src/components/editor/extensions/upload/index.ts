import { i18n } from "@/locales";
import { Dialog, Toast } from "@halo-dev/components";
import {
  CoreEditor,
  Extension,
  Plugin,
  PluginKey,
  PMNode,
  Slice,
} from "@halo-dev/richtext-editor";
import { UiExtensionAudio, UiExtensionImage, UiExtensionVideo } from "..";
import {
  batchUploadExternalLink,
  containsFileClipboardIdentifier,
  handleFileEvent,
  isExternalAsset,
} from "../../utils/upload";

export const Upload = Extension.create({
  name: "upload",

  addProseMirrorPlugins() {
    const { editor }: { editor: CoreEditor } = this;

    return [
      new Plugin({
        key: new PluginKey("upload"),
        props: {
          handlePaste: (view, event: ClipboardEvent, slice: Slice) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false;
            }

            if (!event.clipboardData) {
              return false;
            }

            const externalNodes = getAllExternalNodes(slice);
            if (externalNodes.length > 0) {
              Dialog.info({
                title: i18n.global.t("core.common.text.tip"),
                description: i18n.global.t(
                  "core.components.default_editor.extensions.upload.operations.transfer_in_batch.description"
                ),
                confirmText: i18n.global.t("core.common.buttons.confirm"),
                cancelText: i18n.global.t("core.common.buttons.cancel"),
                async onConfirm() {
                  await batchUploadExternalLink(editor, externalNodes);

                  Toast.success(
                    i18n.global.t("core.common.toast.save_success")
                  );
                },
              });
            }

            const types = event.clipboardData.types;
            // Only process when a single file is pasted.
            if (types.length > 1) {
              return false;
            }

            if (!containsFileClipboardIdentifier(types)) {
              return false;
            }

            const files = Array.from(event.clipboardData.files);

            if (files.length) {
              event.preventDefault();
              files.forEach((file) => {
                handleFileEvent({ editor, file });
              });
              return true;
            }

            return false;
          },
          handleDrop: (view, event) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false;
            }

            if (!event.dataTransfer) {
              return false;
            }

            const hasFiles = event.dataTransfer.files.length > 0;
            if (!hasFiles) {
              return false;
            }

            event.preventDefault();

            const files = Array.from(event.dataTransfer.files) as File[];
            if (files.length) {
              event.preventDefault();
              files.forEach((file: File) => {
                // TODO: For drag-and-drop uploaded files,
                // perhaps it is necessary to determine the
                // current position of the drag-and-drop
                // instead of inserting them directly at the cursor.
                handleFileEvent({ editor, file });
              });
              return true;
            }

            return false;
          },
        },
      }),
    ];
  },
});

const checkExternalLinkNodeTypes = [
  UiExtensionAudio.name,
  UiExtensionVideo.name,
  UiExtensionImage.name,
];
export function getAllExternalNodes(
  slice: Slice
): { node: PMNode; pos: number; index: number; parent: PMNode | null }[] {
  const externalNodes: {
    node: PMNode;
    pos: number;
    index: number;
    parent: PMNode | null;
  }[] = [];
  slice.content.descendants((node, pos, parent, index) => {
    if (checkExternalLinkNodeTypes.includes(node.type.name)) {
      if (isExternalAsset(node.attrs.src)) {
        externalNodes.push({
          node,
          pos,
          parent,
          index,
        });
      }
    }
  });
  return externalNodes;
}

export default Upload;
