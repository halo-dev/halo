// @vitest-environment jsdom

import { shallowMount, type VueWrapper } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { type Content, type NodeViewProps, VueEditor } from "@/tiptap";
import { ExtensionDocument } from "../document";
import { ExtensionText } from "../text";
import GalleryView from "./GalleryView.vue";
import { ExtensionGallery, type ExtensionGalleryImageItem } from "./index";

vi.mock("@halo-dev/components", () => ({
  VButton: { template: "<button><slot /></button>" },
  VSpace: { template: "<div><slot /></div>" },
}));

vi.mock("@halo-dev/ui-shared", () => ({
  utils: {
    permission: { has: () => false },
    attachment: { getUrl: () => undefined },
  },
}));

vi.mock("./useGalleryImages", () => ({
  useUploadGalleryImage: () => ({ openFileDialog: vi.fn() }),
}));

describe("GalleryView", () => {
  it("preserves image aspect ratios across an HTML round trip", () => {
    const sourceEditor = createEditor({
      type: "doc",
      content: [
        {
          type: "gallery",
          attrs: {
            images: [{ src: "/image.jpg", aspectRatio: 2 }],
            groupSize: 3,
            layout: "auto",
            gap: 8,
            file: null,
          },
        },
      ],
    });
    const html = sourceEditor.getHTML();
    sourceEditor.destroy();

    const parsedEditor = createEditor(html);
    const images = parsedEditor.state.doc.nodeAt(0)?.attrs
      .images as ExtensionGalleryImageItem[];
    parsedEditor.destroy();

    expect(images).toEqual([{ src: "/image.jpg", aspectRatio: 2 }]);
  });

  it("retains support for aspect ratios stored on image elements", () => {
    const editor = createEditor(`
      <div data-type="gallery">
        <div>
          <div>
            <img src="/image.jpg" data-aspect-ratio="2" />
          </div>
        </div>
      </div>
    `);
    const images = editor.state.doc.nodeAt(0)?.attrs
      .images as ExtensionGalleryImageItem[];
    editor.destroy();

    expect(images).toEqual([{ src: "/image.jpg", aspectRatio: 2 }]);
  });

  it("prefers aspect ratios stored on image wrappers", () => {
    const editor = createEditor(`
      <div data-type="gallery">
        <div>
          <div data-aspect-ratio="2">
            <img src="/image.jpg" data-aspect-ratio="1" />
          </div>
        </div>
      </div>
    `);
    const images = editor.state.doc.nodeAt(0)?.attrs
      .images as ExtensionGalleryImageItem[];
    editor.destroy();

    expect(images).toEqual([{ src: "/image.jpg", aspectRatio: 2 }]);
  });

  it("does not update attributes when an existing image already has an aspect ratio", async () => {
    const { wrapper, updateAttributes } = mountGallery({
      src: "/image.jpg",
      aspectRatio: 2,
    });

    await loadImage(wrapper, 200, 100);

    expect(updateAttributes).not.toHaveBeenCalled();
  });

  it("fills a missing aspect ratio without rewriting the image source", async () => {
    const { wrapper, updateAttributes } = mountGallery({
      src: "/image.jpg",
      aspectRatio: 0,
    });

    await loadImage(wrapper, 200, 100);

    expect(updateAttributes).toHaveBeenCalledWith({
      images: [{ src: "/image.jpg", aspectRatio: 2 }],
    });
  });
});

function createEditor(content: Content) {
  return new VueEditor({
    extensions: [ExtensionDocument, ExtensionText, ExtensionGallery],
    content,
  });
}

function mountGallery(image: ExtensionGalleryImageItem) {
  const updateAttributes = vi.fn();
  const wrapper = shallowMount(GalleryView, {
    props: {
      node: {
        attrs: {
          images: [image],
          groupSize: 3,
          layout: "auto",
          gap: 8,
        },
      },
      updateAttributes,
      editor: {},
      extension: { options: {} },
      getPos: () => 0,
      selected: false,
    } as unknown as NodeViewProps,
    global: {
      stubs: {
        NodeViewWrapper: { template: "<div><slot /></div>" },
        AttachmentSelectorModal: true,
        MingcuteDelete2Line: true,
      },
      directives: {
        tooltip: {},
      },
    },
  });

  return { wrapper, updateAttributes };
}

async function loadImage(
  wrapper: VueWrapper,
  naturalWidth: number,
  naturalHeight: number
) {
  const image = wrapper.get("img");
  Object.defineProperties(image.element, {
    naturalWidth: { value: naturalWidth },
    naturalHeight: { value: naturalHeight },
  });
  await image.trigger("load");
}
