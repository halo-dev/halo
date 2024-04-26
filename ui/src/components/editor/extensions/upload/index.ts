import {
  CoreEditor,
  Extension,
  Plugin,
  PluginKey,
} from "@halo-dev/richtext-editor";
import { handleFileEvent } from "../../utils/upload";

export const Upload = Extension.create({
  name: "upload",

  addProseMirrorPlugins() {
    const { editor }: { editor: CoreEditor } = this;

    return [
      new Plugin({
        key: new PluginKey("upload"),
        props: {
          handlePaste: (view, event: ClipboardEvent) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false;
            }

            if (!event.clipboardData) {
              return false;
            }

            const types = event.clipboardData.types;
            if (!(types.length === 1 && types[0].toLowerCase() === "files")) {
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

export default Upload;
