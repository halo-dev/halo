import { Toast } from "@halo-dev/components";
import { markRaw, ref, type Ref } from "vue";
import ExternalAssetTransferToolbarItem from "@/components/upload/ExternalAssetTransferToolbarItem.vue";
import { i18n } from "@/locales";
import { Editor, Extension, Plugin, PluginKey, PMNode, Slice } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
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

export interface ExtensionUploadOptions extends ExtensionOptions {
  matchAttachmentPermalinks?: MatchAttachmentPermalinks;
  uploadExternalUrl?: UploadExternalUrl;
}

export interface ExternalAssetItem {
  url: string;
  count: number;
}

export interface ExternalAssetPromptState {
  visible: Ref<boolean>;
  count: Ref<number>;
  items: Ref<ExternalAssetItem[]>;
  scanning: Ref<boolean>;
  transferring: Ref<boolean>;
}

export interface ExtensionUploadStorage {
  matchCache: Map<string, boolean>;
  cacheVersion: Ref<number>;
  externalAssetPrompt: ExternalAssetPromptState;
  uploadExternalUrl?: UploadExternalUrl;
  matchAttachmentPermalinks: (urls: string[]) => Promise<void>;
  scanExternalAssets: () => Promise<ExternalAssetItem[]>;
  transferExternalAssets: () => Promise<void>;
  dismissExternalAssetsPrompt: () => void;
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
      ...this.parent?.(),
      matchAttachmentPermalinks: undefined,
      uploadExternalUrl: undefined,
      getToolbarItems({ editor }) {
        const storage = (editor.storage as { upload?: ExtensionUploadStorage })
          .upload;

        if (!storage?.uploadExternalUrl) {
          return [];
        }

        return {
          priority: 26,
          component: markRaw(ExternalAssetTransferToolbarItem),
          props: {
            editor,
            isActive: false,
          },
        };
      },
    };
  },

  addStorage() {
    return {
      matchCache: new Map<string, boolean>(),
      cacheVersion: ref(0),
      externalAssetPrompt: {
        visible: ref(false),
        count: ref(0),
        items: ref([]),
        scanning: ref(false),
        transferring: ref(false),
      },
      uploadExternalUrl: undefined,
      matchAttachmentPermalinks: async () => undefined,
      scanExternalAssets: async () => [],
      transferExternalAssets: async () => undefined,
      dismissExternalAssetsPrompt: () => undefined,
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
    storage.scanExternalAssets = () => scanExternalAssets(editor, storage);
    storage.transferExternalAssets = () =>
      transferExternalAssets(editor, storage);
    storage.dismissExternalAssetsPrompt = () =>
      dismissExternalAssetsPrompt(storage);

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

            void showExternalAssetPrompt(storage, getAllAssetNodes(slice));

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

export function getAllAssetNodesFromDoc(editor: Editor): ExternalAssetNode[] {
  const assetNodes: ExternalAssetNode[] = [];
  editor.state.doc.descendants((node, pos, parent, index) => {
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

export async function showExternalAssetPrompt(
  storage: ExtensionUploadStorage,
  nodes: ExternalAssetNode[]
) {
  if (!storage.uploadExternalUrl || !nodes.length) {
    return;
  }

  try {
    const externalNodes = await getUnmatchedExternalNodes(storage, nodes);
    if (externalNodes.length) {
      updateExternalAssetsPrompt(storage, externalNodes);
    }
  } catch (error) {
    console.error("Failed to match attachment permalinks:", error);
  }
}

export async function scanExternalAssets(
  editor: Editor,
  storage: ExtensionUploadStorage
) {
  if (!storage.uploadExternalUrl) {
    dismissExternalAssetsPrompt(storage);
    return [];
  }

  storage.externalAssetPrompt.scanning.value = true;

  try {
    const externalNodes = await collectCurrentUnmatchedExternalNodes(
      editor,
      storage
    );
    updateExternalAssetsPrompt(storage, externalNodes);
    return storage.externalAssetPrompt.items.value;
  } catch (error) {
    console.error("Failed to scan external assets:", error);
    return [];
  } finally {
    storage.externalAssetPrompt.scanning.value = false;
  }
}

async function transferExternalAssets(
  editor: Editor,
  storage: ExtensionUploadStorage
) {
  if (!storage.uploadExternalUrl) {
    dismissExternalAssetsPrompt(storage);
    return;
  }

  storage.externalAssetPrompt.transferring.value = true;

  try {
    const externalNodes = await collectCurrentUnmatchedExternalNodes(
      editor,
      storage
    );

    if (!externalNodes.length) {
      dismissExternalAssetsPrompt(storage);
      return;
    }

    updateExternalAssetsPrompt(storage, externalNodes);
    await batchUploadExternalLink(
      editor,
      externalNodes,
      storage.uploadExternalUrl
    );
    dismissExternalAssetsPrompt(storage);
    Toast.success(i18n.global.t("editor.common.toast.save_success"));
  } catch (error) {
    console.error("Failed to upload external assets:", error);
  } finally {
    storage.externalAssetPrompt.transferring.value = false;
  }
}

export async function collectCurrentUnmatchedExternalNodes(
  editor: Editor,
  storage: ExtensionUploadStorage
) {
  return getUnmatchedExternalNodes(storage, getAllAssetNodesFromDoc(editor));
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

export function summarizeExternalAssetNodes(
  nodes: ExternalAssetNode[]
): ExternalAssetItem[] {
  const itemMap = new Map<string, ExternalAssetItem>();

  for (const nodeWithPos of nodes) {
    const { src } = nodeWithPos.node.attrs;
    const item = itemMap.get(src);

    if (item) {
      item.count++;
      continue;
    }

    itemMap.set(src, {
      url: src,
      count: 1,
    });
  }

  return [...itemMap.values()];
}

function updateExternalAssetsPrompt(
  storage: ExtensionUploadStorage,
  nodes: ExternalAssetNode[]
) {
  storage.externalAssetPrompt.items.value = summarizeExternalAssetNodes(nodes);
  storage.externalAssetPrompt.count.value = nodes.length;
  storage.externalAssetPrompt.visible.value = nodes.length > 0;
}

function dismissExternalAssetsPrompt(storage: ExtensionUploadStorage) {
  storage.externalAssetPrompt.visible.value = false;
  storage.externalAssetPrompt.count.value = 0;
  storage.externalAssetPrompt.items.value = [];
}
