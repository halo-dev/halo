import { describe, expect, it, vi } from "vitest";
import { ref } from "vue";
import type { Editor, PMNode } from "@/tiptap";
import type { MatchAttachmentPermalinks, UploadExternalUrl } from "@/utils";
import {
  collectCurrentUnmatchedExternalNodes,
  getUnmatchedExternalNodes,
  matchAttachmentPermalinks,
  showExternalAssetPrompt,
  type ExtensionUploadStorage,
  type ExternalAssetNode,
} from "./index";

describe("ExtensionUpload external asset matching", () => {
  it("keeps matched Attachment URLs out of unmatched external nodes", async () => {
    const matcher = vi.fn<MatchAttachmentPermalinks>(async (urls) =>
      urls.map((url) => ({
        url,
        matched: url.includes("owned"),
      }))
    );
    const storage = createStorage(matcher, vi.fn());
    const nodes = [
      assetNode("https://cdn.example.com/owned.png"),
      assetNode("https://remote.example.com/new.png"),
      assetNode("/upload/local.png"),
    ];

    const unmatched = await getUnmatchedExternalNodes(storage, nodes);

    expect(matcher).toHaveBeenCalledWith([
      "https://cdn.example.com/owned.png",
      "https://remote.example.com/new.png",
    ]);
    expect(unmatched.map((node) => node.node.attrs.src)).toEqual([
      "https://remote.example.com/new.png",
    ]);
    expect(storage.matchCache.get("https://cdn.example.com/owned.png")).toBe(
      true
    );
    expect(storage.matchCache.get("https://remote.example.com/new.png")).toBe(
      false
    );
  });

  it("does not show a prompt when URL transfer is unavailable", async () => {
    const matcher = vi.fn<MatchAttachmentPermalinks>(async (urls) =>
      urls.map((url) => ({
        url,
        matched: false,
      }))
    );
    const storage = createStorage(matcher);

    await showExternalAssetPrompt(storage, [
      assetNode("https://remote.example.com/new.png"),
    ]);

    expect(matcher).not.toHaveBeenCalled();
    expect(storage.externalAssetPrompt.visible.value).toBe(false);
  });

  it("shows a non-blocking prompt without uploading immediately", async () => {
    const uploadExternalUrl = vi.fn<UploadExternalUrl>();
    const storage = createStorage(
      async (urls) =>
        urls.map((url) => ({
          url,
          matched: false,
        })),
      uploadExternalUrl
    );

    await showExternalAssetPrompt(storage, [
      assetNode("https://remote.example.com/new.png"),
    ]);

    expect(storage.externalAssetPrompt.visible.value).toBe(true);
    expect(storage.externalAssetPrompt.count.value).toBe(1);
    expect(uploadExternalUrl).not.toHaveBeenCalled();
  });

  it("collects unmatched external nodes from the current document before transfer", async () => {
    let nodes = [assetNode("https://remote.example.com/pasted.png")];
    const editor = editorWithDynamicNodes(() => nodes);
    const storage = createStorage(
      async (urls) =>
        urls.map((url) => ({
          url,
          matched: false,
        })),
      vi.fn()
    );

    await showExternalAssetPrompt(storage, nodes);
    nodes = [assetNode("https://remote.example.com/current.png")];

    const unmatched = await collectCurrentUnmatchedExternalNodes(
      editor,
      storage
    );

    expect(unmatched.map((node) => node.node.attrs.src)).toEqual([
      "https://remote.example.com/current.png",
    ]);
  });
});

function createStorage(
  matcher?: MatchAttachmentPermalinks,
  uploadExternalUrl?: UploadExternalUrl
) {
  const storage = {
    matchCache: new Map<string, boolean>(),
    cacheVersion: ref(0),
    externalAssetPrompt: {
      visible: ref(false),
      count: ref(0),
      transferring: ref(false),
    },
    uploadExternalUrl,
    matchAttachmentPermalinks: async (urls: string[]) =>
      matchAttachmentPermalinks(matcher, storage, urls),
    transferExternalAssets: async () => undefined,
    dismissExternalAssetsPrompt: () => undefined,
  } as ExtensionUploadStorage;

  return storage;
}

function assetNode(src: string): ExternalAssetNode {
  return {
    node: {
      type: {
        name: "image",
      },
      attrs: {
        src,
      },
    } as unknown as PMNode,
    pos: 0,
    index: 0,
    parent: null,
  };
}

function editorWithDynamicNodes(getNodes: () => ExternalAssetNode[]) {
  return {
    state: {
      doc: {
        descendants(
          callback: (
            node: PMNode,
            pos: number,
            parent: PMNode | null,
            index: number
          ) => void
        ) {
          getNodes().forEach((nodeWithPos, index) => {
            callback(nodeWithPos.node, index, null, index);
          });
        },
      },
    },
  } as unknown as Editor;
}
