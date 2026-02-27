import { i18n } from "@/locales";
import { Editor, Extension, Plugin, PluginKey, PMNode, Slice } from "@/tiptap";
import { ActionNotificationManager } from "@/utils/action-notification-manager";
import {
  batchUploadExternalLink,
  containsFileClipboardIdentifier,
  handleFileEvent,
  isExternalAsset,
  type UploadFromUrlFn,
} from "@/utils/upload";
import { coreApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { ExtensionAudio } from "../audio";
import { ExtensionImage } from "../image";
import { ExtensionVideo } from "../video";

async function filterNodesNotInAttachmentLibrary(
  nodes: { node: PMNode; pos: number; index: number; parent: PMNode | null }[]
) {
  const srcList = Array.from(
    new Set(
      nodes
        .map((item) => item.node.attrs?.src as string | undefined)
        .filter(Boolean)
    )
  );

  if (srcList.length === 0) {
    return nodes;
  }

  const results = await Promise.all(
    srcList.map(async (src) => {
      try {
        const { data } = await coreApiClient.storage.attachment.listAttachment({
          // API page is 1-based; 0 means "no pagination"
          page: 1,
          size: 1,
          fieldSelector: [`status.permalink=${src}`],
        });
        return [src, data.total > 0] as const;
      } catch {
        // If we can't check, treat it as not found and keep original behavior.
        return [src, false] as const;
      }
    })
  );

  const existingSrcSet = new Set(
    results.filter(([, exists]) => exists).map(([src]) => src)
  );

  return nodes.filter((item) => !existingSrcSet.has(item.node.attrs?.src));
}

export interface ExtensionUploadOptions {
  uploadFromUrl?: UploadFromUrlFn;
}

export const ExtensionUpload = Extension.create<ExtensionUploadOptions>({
  name: "upload",

  addOptions() {
    return {
      uploadFromUrl: undefined,
    };
  },

  addProseMirrorPlugins() {
    const { editor }: { editor: Editor } = this;
    const uploadFromUrl = this.options.uploadFromUrl;

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

            // Check for external nodes that need to be uploaded
            (async () => {
              const externalNodes = getAllExternalNodes(slice);
              if (externalNodes.length === 0) {
                return;
              }

              // If pasted URLs already exist as attachment permalinks, skip prompting.
              const nodesToPrompt =
                await filterNodesNotInAttachmentLibrary(externalNodes);

              if (nodesToPrompt.length > 0) {
                // Show non-blocking notification with action buttons
                const count = nodesToPrompt.length;
                const title = i18n.global.t(
                  "editor.extensions.upload.external_link_notification_title"
                );
                const message =
                  count === 1
                    ? i18n.global.t(
                        "editor.extensions.upload.external_link_notification_singular"
                      )
                    : i18n.global.t(
                        "editor.extensions.upload.external_link_notification_plural",
                        { count }
                      );

                const notification = ActionNotificationManager.show({
                  type: "info",
                  title,
                  message,
                  closable: true,
                  actions: [
                    {
                      label: i18n.global.t("editor.common.button.cancel"),
                      type: "default",
                      onClick: () => {
                        notification.close();
                      },
                    },
                    {
                      label: i18n.global.t(
                        "editor.extensions.upload.upload_to_library"
                      ),
                      type: "primary",
                      onClick: async () => {
                        try {
                          await batchUploadExternalLink(
                            editor,
                            nodesToPrompt,
                            uploadFromUrl
                          );
                          notification.close();
                          Toast.success(
                            i18n.global.t("editor.common.toast.save_success")
                          );
                        } catch (error) {
                          Toast.error(
                            i18n.global.t("editor.extensions.upload.error")
                          );
                          console.error(
                            "Failed to upload external links:",
                            error
                          );
                        }
                      },
                    },
                  ],
                });
              }
            })();

            const types = event.clipboardData.types;
            if (!containsFileClipboardIdentifier(types)) {
              return false;
            }

            // If the copied content is Excel, do not process it.
            if (isExcelPasted(event.clipboardData)) {
              return false;
            }

            const files = Array.from(event.clipboardData.files);

            if (files.length) {
              event.preventDefault();
              handleFileEvent(editor, files);
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
              // TODO: For drag-and-drop uploaded files,
              // perhaps it is necessary to determine the
              // current position of the drag-and-drop
              // instead of inserting them directly at the cursor.
              handleFileEvent(editor, files);
              return true;
            }

            return false;
          },
        },
      }),
    ];
  },
});

function isExcelPasted(clipboardData: ClipboardEvent["clipboardData"]) {
  if (!clipboardData) {
    return false;
  }

  const types = clipboardData.types;
  if (
    types.includes("application/vnd.ms-excel") ||
    types.includes(
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
  ) {
    return true;
  }

  if (types.includes("text/html")) {
    try {
      const html = clipboardData.getData("text/html");
      if (
        html.includes('ProgId="Excel.Sheet"') ||
        html.includes('xmlns:x="urn:schemas-microsoft-com:office:excel"') ||
        html.includes("urn:schemas-microsoft-com:office:spreadsheet") ||
        html.includes("<x:ExcelWorkbook>")
      ) {
        return true;
      }
    } catch (e) {
      console.warn("Failed to read clipboard HTML data:", e);
    }
  }

  return false;
}

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
    if (
      [ExtensionAudio.name, ExtensionVideo.name, ExtensionImage.name].includes(
        node.type.name
      )
    ) {
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
