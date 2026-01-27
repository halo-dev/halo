import { i18n } from "@/locales";
import { Editor, Extension, Plugin, PluginKey, PMNode, Slice } from "@/tiptap";
import {
  batchUploadExternalLink,
  containsFileClipboardIdentifier,
  handleFileEvent,
  isExternalAsset,
} from "@/utils/upload";
import { consoleApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { ExtensionAudio } from "../audio";
import { ExtensionImage } from "../image";
import { ExtensionVideo } from "../video";

// Cache for trusted domains to avoid repeated API calls
let trustedDomainsCache: string[] = [];
let lastFetchTime = 0;
const CACHE_DURATION = 60000; // 1 minute

async function getTrustedDomains(): Promise<string[]> {
  const now = Date.now();

  // Return cached value if still valid
  if (now - lastFetchTime < CACHE_DURATION && trustedDomainsCache.length >= 0) {
    return trustedDomainsCache;
  }

  try {
    const { data } =
      await consoleApiClient.configMap.system.getSystemConfigByGroup({
        group: "post",
      });

    const trustedImageDomains = (data as Record<string, unknown>)
      ?.trustedImageDomains as string;

    if (trustedImageDomains) {
      // Split by comma and trim whitespace
      trustedDomainsCache = trustedImageDomains
        .split(",")
        .map((domain) => domain.trim())
        .filter((domain) => domain.length > 0);
    } else {
      trustedDomainsCache = [];
    }

    lastFetchTime = now;
    return trustedDomainsCache;
  } catch (error) {
    console.warn(
      "Failed to fetch trusted domains from system settings:",
      error
    );
    return [];
  }
}

export const ExtensionUpload = Extension.create({
  name: "upload",

  addProseMirrorPlugins() {
    const { editor }: { editor: Editor } = this;

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

            // Fetch trusted domains and check for external nodes
            getTrustedDomains().then((trustedDomains) => {
              const externalNodes = getAllExternalNodes(slice, trustedDomains);
              if (externalNodes.length > 0) {
                Dialog.info({
                  title: i18n.global.t("editor.common.text.tip"),
                  description: i18n.global.t(
                    "editor.extensions.upload.operations.transfer_in_batch.description"
                  ),
                  confirmText: i18n.global.t("editor.common.button.confirm"),
                  cancelText: i18n.global.t("editor.common.button.cancel"),
                  async onConfirm() {
                    await batchUploadExternalLink(
                      editor,
                      externalNodes,
                      trustedDomains
                    );

                    Toast.success(
                      i18n.global.t("editor.common.toast.save_success")
                    );
                  },
                });
              }
            });

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
  slice: Slice,
  trustedDomains: string[] = []
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
      if (isExternalAsset(node.attrs.src, trustedDomains)) {
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
