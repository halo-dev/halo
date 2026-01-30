import { i18n } from "@/locales";
import { Editor, Extension, Plugin, PluginKey, PMNode, Slice } from "@/tiptap";
import {
  containsFileClipboardIdentifier,
  handleFileEvent,
  isExternalAsset,
} from "@/utils/upload";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
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
  if (now - lastFetchTime < CACHE_DURATION) {
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
            getTrustedDomains().then(async (trustedDomains) => {
              const externalNodes = getAllExternalNodes(slice, trustedDomains);
              if (externalNodes.length === 0) {
                return;
              }

              // If pasted URLs already exist as attachment permalinks, skip prompting.
              const nodesToPrompt =
                await filterNodesNotInAttachmentLibrary(externalNodes);

              if (nodesToPrompt.length > 0) {
                // Non-blocking notification for external links
                const count = nodesToPrompt.length;
                const message =
                  count === 1
                    ? i18n.global.t(
                        "editor.extensions.upload.external_link_detected_singular"
                      )
                    : i18n.global.t(
                        "editor.extensions.upload.external_link_detected_plural",
                        { count }
                      );
                Toast.info(message, { duration: 5000 });
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
