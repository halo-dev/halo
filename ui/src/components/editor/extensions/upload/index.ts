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
              if (confirm("检测到具有外部链接，是否需要自动上传到附件库？")) {
                batchUploadExternalLink(editor, externalNodes);
              }
            }

            const types = event.clipboardData.types;

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
          handleDrop: (view, event, slice) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false;
            }

            if (!event.dataTransfer) {
              return false;
            }

            const externalNodes = getAllExternalNodes(slice);
            if (externalNodes.length > 0) {
              if (confirm("检测到具有外部链接，是否需要自动上传到附件库？")) {
                batchUploadExternalLink(editor, externalNodes);
              }
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
