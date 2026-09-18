// @vitest-environment jsdom

import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import GalleryImageReplace from "./GalleryImageReplace.vue";

const permissions = vi.hoisted(() => ({ current: [] as string[] }));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: {
    permission: {
      has: (required: string[]) =>
        required.some((permission) => permissions.current.includes(permission)),
    },
    attachment: { getUrl: (attachment: string) => attachment },
  },
}));
vi.mock("@halo-dev/components", () => ({
  VDropdown: { template: '<div><slot /><slot name="popper" /></div>' },
  VDropdownItem: { template: "<button><slot /></button>" },
}));

describe("GalleryImageReplace", () => {
  it("emits the selected upload file and attachment URL", async () => {
    permissions.current = ["uc:attachments:manage"];
    const file = new File(["image"], "replacement.png", { type: "image/png" });
    const onUpload = vi.fn();
    const onReplace = vi.fn();
    const click = vi
      .spyOn(HTMLInputElement.prototype, "click")
      .mockImplementation(function (this: HTMLInputElement) {
        Object.defineProperty(this, "files", {
          value: [file],
          configurable: true,
        });
        this.dispatchEvent(new Event("change"));
      });
    const wrapper = mount(GalleryImageReplace, {
      props: { uploadEnabled: true, onUpload, onReplace },
      global: {
        directives: { tooltip: {} },
        stubs: { AttachmentSelectorModal: true },
      },
    });
    await wrapper.findAll("button")[1].trigger("click");
    expect(click).toHaveBeenCalled();
    expect(onUpload).toHaveBeenCalledWith(file);
    await wrapper.findAll("button")[2].trigger("click");
    wrapper
      .findComponent({ name: "AttachmentSelectorModal" })
      .vm.$emit("select", ["/new.jpg"]);
    expect(onReplace).toHaveBeenCalledWith("/new.jpg");
    click.mockRestore();
    wrapper.unmount();
  });

  it.each([
    { granted: [], uploadEnabled: true, buttons: 0 },
    {
      granted: ["system:attachments:manage"],
      uploadEnabled: false,
      buttons: 0,
    },
    { granted: ["system:attachments:manage"], uploadEnabled: true, buttons: 2 },
    { granted: ["system:attachments:view"], uploadEnabled: false, buttons: 2 },
    { granted: ["uc:attachments:manage"], uploadEnabled: true, buttons: 3 },
  ])(
    "shows only usable operations with $granted and upload=$uploadEnabled",
    ({ granted, uploadEnabled, buttons }) => {
      permissions.current = granted;
      const wrapper = mount(GalleryImageReplace, {
        props: { uploadEnabled },
        global: {
          directives: { tooltip: {} },
          stubs: { AttachmentSelectorModal: true },
        },
      });
      expect(wrapper.findAll("button")).toHaveLength(buttons);
      wrapper.unmount();
    }
  );
});
