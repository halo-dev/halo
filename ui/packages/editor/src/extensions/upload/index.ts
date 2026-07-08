import { Dialog, Toast } from "@halo-dev/components";
import { ref, type Ref } from "vue";
import { i18n } from "@/locales";
import { Editor, Extension, Plugin, PluginKey, PMNode, Slice } from "@/tiptap";
import {
  batchUploadExternalLink,
  containsFileClipboardIdentifier,
  handleFileEvent,
  isExternalAsset,
  type MatchAttachmentPermalinks,
  type UploadExternalUrl,
} from "@/utils/upload";
import { ExtensionAudio } from "../audio";
import { ExtensionImage } from "../image";
import { ExtensionVideo } from "../video";

export interface ExtensionUploadOptions {
  matchAttachmentPermalinks?: MatchAttachmentPermalinks;
  uploadExternalUrl?: UploadExternalUrl;
}

export interface ExtensionUploadStorage {
  matchCache: Map<string, boolean>;
  cacheVersion: Ref<number>;
  uploadExternalUrl?: UploadExternalUrl;
  matchAttachmentPermalinks: (urls: string[]) => Promise<void>;
}

export interface ExternalAssetNode {
  node: PMNode;
  pos: number;
  index: number;
  parent: PMNode | null;
}

export const ExtensionUpload = Extension.create<
  ExtensionUploadOptions,
  ExtensionUploadStorage
>({
  name: "upload",

  addOptions() {
    return {
      matchAttachmentPermalinks: undefined,
      uploadExternalUrl: undefined,
    };
  },

  addStorage() {
    return {
      matchCache: new Map<string, boolean>(),
      cacheVersion: ref(0),
      uploadExternalUrl: undefined,
      matchAttachmentPermalinks: async () => undefined,
    };
  },

  addProseMirrorPlugins() {
    const { editor }: { editor: Editor } = this;
    const storage = this.storage;

    storage.uploadExternalUrl = this.options.uploadExternalUrl;
    storage.matchAttachmentPermalinks = (urls) =>
      matchAttachmentPermalinks(
        this.options.matchAttachmentPermalinks,
        storage,
        urls
      );

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

            void showExternalAssetTransferDialog(
              editor,
              storage,
              getAllAssetNodes(slice)
            );

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

export function getAllExternalNodes(slice: Slice): ExternalAssetNode[] {
  return getAllAssetNodes(slice).filter((nodeWithPos) =>
    isExternalAsset(nodeWithPos.node.attrs.src)
  );
}

export function getAllAssetNodes(slice: Slice): ExternalAssetNode[] {
  const assetNodes: ExternalAssetNode[] = [];
  slice.content.descendants((node, pos, parent, index) => {
    if (
      [ExtensionAudio.name, ExtensionVideo.name, ExtensionImage.name].includes(
        node.type.name
      )
    ) {
      assetNodes.push({
        node,
        pos,
        parent,
        index,
      });
    }
  });
  return assetNodes;
}

export async function showExternalAssetTransferDialog(
  editor: Editor,
  storage: ExtensionUploadStorage,
  nodes: ExternalAssetNode[]
) {
  if (!storage.uploadExternalUrl || !nodes.length) {
    return;
  }

  try {
    const uploadExternalUrl = storage.uploadExternalUrl;
    const externalNodes = await getUnmatchedExternalNodes(storage, nodes);
    if (externalNodes.length) {
      Dialog.info({
        title: i18n.global.t("editor.common.text.tip"),
        description: i18n.global.t(
          "editor.extensions.upload.operations.transfer_in_batch.description",
          { count: externalNodes.length }
        ),
        confirmText: i18n.global.t("editor.common.button.confirm"),
        cancelText: i18n.global.t("editor.common.button.cancel"),
        async onConfirm() {
          await batchUploadExternalLink(
            editor,
            externalNodes,
            uploadExternalUrl
          );

          Toast.success(i18n.global.t("editor.common.toast.save_success"));
        },
      });
    }
  } catch (error) {
    console.error("Failed to match attachment permalinks:", error);
  }
}

export async function getUnmatchedExternalNodes(
  storage: ExtensionUploadStorage,
  nodes: ExternalAssetNode[]
) {
  const externalNodes = nodes.filter((nodeWithPos) =>
    isExternalAsset(nodeWithPos.node.attrs.src)
  );

  await storage.matchAttachmentPermalinks(
    externalNodes.map((nodeWithPos) => nodeWithPos.node.attrs.src)
  );

  return externalNodes.filter((nodeWithPos) => {
    const { src } = nodeWithPos.node.attrs;
    return storage.matchCache.get(src) === false;
  });
}

export async function matchAttachmentPermalinks(
  matcher: MatchAttachmentPermalinks | undefined,
  storage: ExtensionUploadStorage,
  urls: string[]
) {
  const unmatchedUrls = [...new Set(urls)]
    .filter((url) => typeof url === "string" && url)
    .filter((url) => !storage.matchCache.has(url));

  if (!unmatchedUrls.length) {
    return;
  }

  if (!matcher) {
    for (const url of unmatchedUrls) {
      storage.matchCache.set(url, false);
    }
    storage.cacheVersion.value++;
    return;
  }

  const results = await matcher(unmatchedUrls);
  const resultMap = new Map(
    results.map((result) => [result.url, result.matched])
  );

  for (const url of unmatchedUrls) {
    storage.matchCache.set(url, resultMap.get(url) ?? false);
  }

  storage.cacheVersion.value++;
}
