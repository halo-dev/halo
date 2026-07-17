import type { Attachment } from "@halo-dev/api-client";
import { Dialog } from "@halo-dev/components";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { ref } from "vue";
import type { Editor, PMNode } from "@/tiptap";
import type { MatchAttachmentPermalinks, Upload } from "@/utils";
import {
  getUnmatchedExternalNodes,
  matchAttachmentPermalinks,
  showExternalAssetTransferDialog,
  type ExtensionUploadStorage,
  type ExternalAssetNode,
} from "./index";

vi.mock("@halo-dev/components", () => ({
  Dialog: {
    info: vi.fn(),
  },
  Toast: {
    success: vi.fn(),
  },
}));

describe("ExtensionUpload external asset matching", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

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

  it("does not show a dialog when URL transfer is unavailable", async () => {
    const matcher = vi.fn<MatchAttachmentPermalinks>(async (urls) =>
      urls.map((url) => ({
        url,
        matched: false,
      }))
    );
    const storage = createStorage(matcher);

    await showExternalAssetTransferDialog(editorWithNodes([]), storage, [
      assetNode("https://remote.example.com/new.png"),
    ]);

    expect(matcher).not.toHaveBeenCalled();
    expect(Dialog.info).not.toHaveBeenCalled();
  });

  it("shows the paste dialog for unmatched external resources without uploading immediately", async () => {
    const upload = vi.fn<Upload>();
    const storage = createStorage(
      async (urls) =>
        urls.map((url) => ({
          url,
          matched: false,
        })),
      upload
    );

    await showExternalAssetTransferDialog(editorWithNodes([]), storage, [
      assetNode("https://remote.example.com/new.png"),
    ]);

    expect(Dialog.info).toHaveBeenCalledTimes(1);
    expect(upload).not.toHaveBeenCalled();
  });

  it("does not show the paste dialog for matched Attachment URLs", async () => {
    const storage = createStorage(
      async (urls) =>
        urls.map((url) => ({
          url,
          matched: true,
        })),
      vi.fn()
    );

    await showExternalAssetTransferDialog(editorWithNodes([]), storage, [
      assetNode("https://cdn.example.com/owned.png"),
    ]);

    expect(Dialog.info).not.toHaveBeenCalled();
  });

  it("passes unmatched resources to the dialog confirmation transfer", async () => {
    const upload = vi.fn<Upload>(async (url) =>
      attachment(`/upload/${String(url).split("/").pop()}`, "Uploaded asset")
    );
    const editor = editorWithNodes([
      assetNode("https://remote.example.com/new.png"),
    ]);
    const storage = createStorage(
      async (urls) =>
        urls.map((url) => ({
          url,
          matched: false,
        })),
      upload
    );

    await showExternalAssetTransferDialog(editor, storage, [
      assetNode("https://remote.example.com/new.png"),
    ]);

    const dialogOptions = vi.mocked(Dialog.info).mock.calls[0]?.[0];
    if (!dialogOptions) {
      throw new Error("Expected transfer dialog to be shown.");
    }
    await dialogOptions.onConfirm?.();

    expect(upload).toHaveBeenCalledWith("https://remote.example.com/new.png");
    expect(editor.view.state.tr.setNodeMarkup).toHaveBeenCalledWith(
      0,
      expect.anything(),
      expect.objectContaining({
        src: "/upload/new.png",
        name: "Uploaded asset",
      })
    );
  });
});

function createStorage(matcher?: MatchAttachmentPermalinks, upload?: Upload) {
  const storage = {
    matchCache: new Map<string, boolean>(),
    cacheVersion: ref(0),
    upload,
    matchAttachmentPermalinks: async (urls: string[]) =>
      matchAttachmentPermalinks(matcher, storage, urls),
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

function attachment(permalink: string, displayName: string): Attachment {
  return {
    apiVersion: "storage.halo.run/v1alpha1",
    kind: "Attachment",
    metadata: {
      name: displayName,
    },
    spec: {
      displayName,
    },
    status: {
      permalink,
    },
  };
}

function editorWithNodes(nodes: ExternalAssetNode[]) {
  return {
    state: {
      tr: {
        setNodeMarkup: vi.fn(),
      },
      doc: {
        descendants(
          callback: (
            node: PMNode,
            pos: number,
            parent: PMNode | null,
            index: number
          ) => void
        ) {
          nodes.forEach((nodeWithPos, index) => {
            callback(nodeWithPos.node, index, null, index);
          });
        },
      },
      selection: {
        from: 0,
      },
    },
    view: {
      state: {
        tr: {
          setNodeMarkup: vi.fn(),
        },
      },
      dispatch: vi.fn(),
    },
  } as unknown as Editor;
}
