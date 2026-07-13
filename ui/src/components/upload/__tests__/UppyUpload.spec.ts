import type Uppy from "@uppy/core";
import Dashboard from "@uppy/vue/dashboard";
import { mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { nextTick } from "vue";
import UppyUpload from "../UppyUpload.vue";

const mocks = vi.hoisted(() => ({
  createHTMLContentModal: vi.fn(),
  locale: { value: "en-US" },
  toastError: vi.fn(),
}));

vi.mock("@halo-dev/components", () => ({
  Toast: {
    error: mocks.toastError,
  },
}));

vi.mock("@/locales", () => ({
  i18n: {
    global: {
      locale: mocks.locale,
      t: (key: string) => key,
    },
  },
}));

vi.mock("@/utils/modal", () => ({
  createHTMLContentModal: mocks.createHTMLContentModal,
}));

type UppyInstance = Uppy<Record<string, unknown>, Record<string, unknown>>;

function getUppy(wrapper: ReturnType<typeof mount>) {
  return wrapper.findComponent(Dashboard).props("uppy") as UppyInstance;
}

describe("UppyUpload", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.locale.value = "en-US";
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe() {}
        unobserve() {}
        disconnect() {}
      }
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("keeps the existing props contract when configuring Uppy", () => {
    const doneButtonHandler = vi.fn();
    const wrapper = mount(UppyUpload, {
      props: {
        endpoint: "/upload",
        restrictions: {
          maxNumberOfFiles: 1,
          allowedFileTypes: [".zip"],
        },
        meta: { source: "plugin" },
        autoProceed: true,
        allowedMetaFields: ["source"],
        name: "archive",
        note: "Upload a ZIP file",
        method: "PUT",
        disabled: true,
        width: "100%",
        height: "320px",
        doneButtonHandler,
      },
    });

    const dashboard = wrapper.findComponent(Dashboard);
    const uppy = getUppy(wrapper);
    const xhrUpload = uppy.getPlugin("XHRUpload");
    const dashboardPlugin = uppy.getPlugin("Dashboard");

    expect(uppy.opts.autoProceed).toBe(true);
    expect(uppy.opts.meta).toMatchObject({ source: "plugin" });
    expect(uppy.opts.restrictions).toMatchObject({
      maxNumberOfFiles: 1,
      allowedFileTypes: [".zip"],
    });
    expect(xhrUpload?.opts).toMatchObject({
      endpoint: "/upload",
      allowedMetaFields: ["source"],
      fieldName: "archive",
      method: "PUT",
    });
    expect(xhrUpload?.opts.shouldRetry?.({} as XMLHttpRequest)).toBe(false);
    expect(dashboardPlugin?.opts.hideRetryButton).toBe(false);
    expect(dashboard.props("props")).toMatchObject({
      disabled: true,
      note: "Upload a ZIP file",
      width: "100%",
      height: "320px",
      doneButtonHandler,
    });
  });

  it("renders the Uppy Dashboard", () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });

    expect(wrapper.find(".uppy-Dashboard").exists()).toBe(true);
  });

  it.each([
    ["zh-CN", "重置", "裁剪为横向（16:9）"],
    ["es-MX", "Revertir", "Recortar horizontal (16:9)"],
    ["fr-FR", "Reset", "Crop landscape (16:9)"],
  ])(
    "uses the official Uppy locale for the Image Editor in %s",
    (locale, revert, aspectRatioLandscape) => {
      mocks.locale.value = locale;
      const wrapper = mount(UppyUpload, {
        props: { endpoint: "/upload" },
      });
      const imageEditor = getUppy(wrapper).getPlugin("ImageEditor");

      expect(imageEditor?.i18n("revert")).toBe(revert);
      expect(imageEditor?.i18n("aspectRatioLandscape")).toBe(
        aspectRatioLandscape
      );
    }
  );

  it("keeps the Dashboard mounted when upload options change", async () => {
    const wrapper = mount(UppyUpload, {
      props: {
        endpoint: "/upload/first",
        meta: { policyName: "first", groupName: "" },
        restrictions: {
          maxNumberOfFiles: 1,
          allowedFileTypes: [".zip"],
        },
        allowedMetaFields: ["policyName", "groupName"],
      },
    });
    const initialUppy = getUppy(wrapper);
    initialUppy.addFile({
      name: "queued.zip",
      type: "application/zip",
      data: new File(["queued"], "queued.zip"),
    });

    await wrapper.setProps({
      endpoint: "/upload/second",
      meta: { policyName: "second" },
      restrictions: undefined,
      allowedMetaFields: ["policyName"],
      name: "attachment",
      method: "PUT",
    });

    const xhrUpload = initialUppy.getPlugin("XHRUpload");

    expect(wrapper.find(".uppy-Dashboard").exists()).toBe(true);
    expect(getUppy(wrapper)).toBe(initialUppy);
    expect(initialUppy.getFiles()).toHaveLength(0);
    expect(initialUppy.getState().meta).toEqual({ policyName: "second" });
    expect(initialUppy.opts.restrictions).toEqual({
      maxFileSize: null,
      minFileSize: null,
      maxTotalFileSize: null,
      maxNumberOfFiles: null,
      minNumberOfFiles: null,
      allowedFileTypes: null,
      requiredMetaFields: [],
    });
    expect(xhrUpload?.opts).toMatchObject({
      endpoint: "/upload/second",
      allowedMetaFields: ["policyName"],
      fieldName: "attachment",
      method: "PUT",
    });
  });

  it("accepts successful non-JSON responses like Uppy 3", async () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });
    const xhrUpload = getUppy(wrapper).getPlugin("XHRUpload");
    const xhr = {
      responseText: "Restored successfully!",
    } as XMLHttpRequest;

    await expect(xhrUpload?.opts.getResponseData?.(xhr)).resolves.toEqual({});
  });

  it("uses the server problem detail as the Uppy upload error", async () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });
    const xhrUpload = getUppy(wrapper).getPlugin("XHRUpload");
    const body = {
      title: "Method Not Allowed",
      detail: "The storage policy rejected this upload",
    };
    const xhr = {
      status: 405,
      statusText: "Method Not Allowed",
      responseText: JSON.stringify(body),
    } as XMLHttpRequest;

    await expect(xhrUpload?.opts.onAfterResponse?.(xhr, 0)).rejects.toThrow(
      "Method Not Allowed: The storage policy rejected this upload"
    );
  });

  it("emits the existing success response shape", async () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });
    const response = {
      status: 201,
      body: { name: "uploaded-file" },
      uploadURL: "/files/uploaded-file",
    };

    getUppy(wrapper).emit("upload-success", undefined, response);
    await nextTick();

    expect(wrapper.emitted("uploaded")).toEqual([[response]]);
  });

  it("adapts upload errors to the Uppy 3 response contract", async () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });
    const uppy = getUppy(wrapper);
    const fileId = uppy.addFile({
      name: "plugin.jar",
      type: "application/java-archive",
      data: new File(["plugin"], "plugin.jar"),
    });
    const file = uppy.getFile(fileId);
    const body = {
      title: "Conflict",
      detail: "Plugin already exists",
    };
    const xhr = {
      status: 409,
      statusText: "Conflict",
      response: JSON.stringify(body),
      responseText: JSON.stringify(body),
    } as XMLHttpRequest;

    (
      uppy.emit as unknown as (
        event: string,
        file: typeof file,
        error: Error,
        response: XMLHttpRequest
      ) => void
    )("upload-error", file, new Error("Upload error"), xhr);
    await nextTick();

    expect(wrapper.emitted("error")).toEqual([[file, { status: 409, body }]]);
    expect(mocks.toastError).toHaveBeenCalledWith(
      "Conflict: Plugin already exists",
      { duration: 5000 }
    );
  });

  it("keeps network errors without an HTTP response", async () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });
    const uppy = getUppy(wrapper);
    const xhr = {
      status: 0,
      statusText: "",
      response: "",
      responseText: "",
    } as XMLHttpRequest;

    (
      uppy.emit as unknown as (
        event: string,
        file: undefined,
        error: Error,
        response: XMLHttpRequest
      ) => void
    )("upload-error", undefined, new Error("Network error"), xhr);
    await nextTick();

    expect(wrapper.emitted("error")).toEqual([[undefined, undefined]]);
  });

  it("destroys the Uppy instance when unmounted", () => {
    const wrapper = mount(UppyUpload, {
      props: { endpoint: "/upload" },
    });
    const uppy = getUppy(wrapper);
    const destroy = vi.spyOn(uppy, "destroy");

    wrapper.unmount();

    expect(destroy).toHaveBeenCalledOnce();
  });
});
