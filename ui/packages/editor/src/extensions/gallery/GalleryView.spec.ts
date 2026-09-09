// @vitest-environment jsdom

import { mount, shallowMount, type VueWrapper } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { type Content, type NodeViewProps, VueEditor } from "@/tiptap";
import { ExtensionDocument } from "../document";
import { ExtensionText } from "../text";
import GalleryImageAlt from "./GalleryImageAlt.vue";
import GalleryView from "./GalleryView.vue";
import { ExtensionGallery, type ExtensionGalleryImageItem } from "./index";

vi.mock("@halo-dev/components", () => ({
  VDropdown: {
    name: "VDropdown",
    props: ["shown"],
    emits: ["show", "update:shown"],
    template: '<div><slot /><slot name="popper" /></div>',
  },
  VButton: { template: "<button><slot /></button>" },
  VSpace: { template: "<div><slot /></div>" },
}));

vi.mock("@halo-dev/ui-shared", () => ({
  utils: {
    permission: { has: () => false },
    attachment: {
      getUrl: () => undefined,
      getThumbnailUrl: (url: string, size: string) =>
        `${url}?thumbnail=${size}`,
    },
  },
}));

vi.mock("./useGalleryImages", () => ({
  useUploadGalleryImage: () => ({ openFileDialog: vi.fn() }),
}));

describe("GalleryView", () => {
  it.each(['山间的 "日出" & 云海', ""])(
    "preserves alt %j across an HTML round trip",
    (alt) => {
      const editor = createEditor({
        type: "doc",
        content: [
          {
            type: "gallery",
            attrs: { images: [{ src: "/image.jpg", aspectRatio: 2, alt }] },
          },
        ],
      });
      const parsedEditor = createEditor(editor.getHTML());
      expect(parsedEditor.state.doc.nodeAt(0)?.attrs.images[0]).toEqual({
        src: "/image.jpg",
        aspectRatio: 2,
        alt,
      });
      parsedEditor.destroy();
      editor.destroy();
    }
  );

  it("updates only the target image alt and renders existing alternative text", () => {
    const images = [
      { src: "/same.jpg", aspectRatio: 2, alt: "First" },
      { src: "/same.jpg", aspectRatio: 1, alt: "Second" },
    ];
    const { wrapper, updateAttributes } = mountGallery(images);
    expect(
      wrapper.findAll("img").map((image) => image.attributes("alt"))
    ).toEqual(["First", "Second"]);
    wrapper.findAllComponents(GalleryImageAlt)[1].vm.$emit("update:alt", "");
    expect(updateAttributes).toHaveBeenCalledWith({
      images: [images[0], { ...images[1], alt: "" }],
    });
    expect(images[1].alt).toBe("Second");
  });

  it("preserves alt when learning an aspect ratio", async () => {
    const { wrapper, updateAttributes } = mountGallery({
      src: "/image.jpg",
      aspectRatio: 0,
      alt: "Sunrise",
    });
    await loadImage(wrapper, 200, 100);
    expect(updateAttributes).toHaveBeenCalledWith({
      images: [{ src: "/image.jpg", aspectRatio: 2, alt: "Sunrise" }],
    });
  });

  it("submits and clears alternative text through the input form", async () => {
    const updateAlt = vi.fn();
    const wrapper = mount(GalleryImageAlt, {
      props: { alt: "Existing", "onUpdate:alt": updateAlt },
    });
    wrapper.findComponent({ name: "VDropdown" }).vm.$emit("update:shown", true);
    wrapper.findComponent({ name: "VDropdown" }).vm.$emit("show");
    await wrapper.vm.$nextTick();
    expect(wrapper.get("input").element.value).toBe("Existing");
    await wrapper.get("input").setValue("Sunrise");
    await wrapper.get("form").trigger("submit");
    wrapper.findComponent({ name: "VDropdown" }).vm.$emit("update:shown", true);
    await wrapper.vm.$nextTick();
    wrapper.get("input").element.value = "";
    await wrapper.get("input").trigger("input");
    await wrapper.get("form").trigger("submit");
    expect(updateAlt.mock.calls).toEqual([["Sunrise"], [""]]);
    await wrapper.setProps({ alt: "Updated" });
    wrapper.findComponent({ name: "VDropdown" }).vm.$emit("update:shown", true);
    wrapper.findComponent({ name: "VDropdown" }).vm.$emit("show");
    await wrapper.vm.$nextTick();
    expect(wrapper.get("input").element.value).toBe("Updated");
    wrapper.get("input").element.value = "Unsaved";
    await wrapper.get("input").trigger("input");
    await wrapper.get("input").trigger("keydown", { key: "Escape" });
    expect(updateAlt).toHaveBeenCalledTimes(2);
    wrapper.unmount();
  });
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

    expect(html).toContain('src="/image.jpg"');
    expect(html).not.toContain("thumbnail");

    const parsedEditor = createEditor(html);
    const images = parsedEditor.state.doc.nodeAt(0)?.attrs
      .images as ExtensionGalleryImageItem[];
    parsedEditor.destroy();

    expect(images).toEqual([{ src: "/image.jpg", aspectRatio: 2, alt: "" }]);
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

    expect(images).toEqual([{ src: "/image.jpg", aspectRatio: 2, alt: "" }]);
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

    expect(images).toEqual([{ src: "/image.jpg", aspectRatio: 2, alt: "" }]);
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

  it.each([
    { count: 1, expectedSize: "XL" },
    { count: 2, expectedSize: "M" },
    { count: 3, expectedSize: "M" },
    { count: 4, expectedSize: "S" },
  ])(
    "uses $expectedSize thumbnails for a row containing $count images",
    ({ count, expectedSize }) => {
      const images = Array.from({ length: count }, (_, index) => ({
        src: `/image-${index + 1}.jpg`,
        aspectRatio: 2,
      }));
      const { wrapper } = mountGallery(images, count);

      expect(
        wrapper.findAll("img").map((image) => image.attributes("src"))
      ).toEqual(
        images.map((image) => `${image.src}?thumbnail=${expectedSize}`)
      );
    }
  );

  it("recalculates thumbnail sizes from actual rows when group size changes", async () => {
    const images = Array.from({ length: 4 }, (_, index) => ({
      src: `/image-${index + 1}.jpg`,
      aspectRatio: 2,
    }));
    const { wrapper } = mountGallery(images, 4);

    expect(
      wrapper.findAll("img").map((image) => image.attributes("src"))
    ).toEqual(images.map((image) => `${image.src}?thumbnail=S`));

    await wrapper.setProps({
      node: {
        attrs: {
          images,
          groupSize: 3,
          layout: "auto",
          gap: 8,
        },
      } as unknown as NodeViewProps["node"],
    });

    expect(
      wrapper.findAll("img").map((image) => image.attributes("src"))
    ).toEqual([
      "/image-1.jpg?thumbnail=M",
      "/image-2.jpg?thumbnail=M",
      "/image-3.jpg?thumbnail=M",
      "/image-4.jpg?thumbnail=XL",
    ]);
  });

  it("eagerly loads images until their aspect ratio is known", () => {
    const { wrapper } = mountGallery([
      { src: "/known.jpg", aspectRatio: 2 },
      { src: "/unknown.jpg", aspectRatio: 0 },
    ]);

    expect(
      wrapper.findAll("img").map((image) => image.attributes("loading"))
    ).toEqual(["lazy", "eager"]);
  });
});

function createEditor(content: Content) {
  return new VueEditor({
    extensions: [ExtensionDocument, ExtensionText, ExtensionGallery],
    content,
  });
}

function mountGallery(
  image: ExtensionGalleryImageItem | ExtensionGalleryImageItem[],
  groupSize = 3
) {
  const images = Array.isArray(image) ? image : [image];
  const updateAttributes = vi.fn();
  const wrapper = shallowMount(GalleryView, {
    props: {
      node: {
        attrs: {
          images,
          groupSize,
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
