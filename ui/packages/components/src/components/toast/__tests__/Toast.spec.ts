import { mount, type VueWrapper } from "@vue/test-utils";
import { createToastManager } from "reka-ui";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { nextTick } from "vue";
import { createToastApi } from "../toast-manager";
import ToastHost from "../ToastHost.vue";
import type { ToastEntry } from "../types";

let wrapper: VueWrapper;
let toast: ToastEntry;

function toastElements() {
  return [...document.body.querySelectorAll<HTMLElement>(".toast-wrapper")];
}

function toastElement() {
  return document.body.querySelector<HTMLElement>(".toast-wrapper");
}

function states() {
  return toastElements().map((element) => element.getAttribute("data-state"));
}

function flushFrames() {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => requestAnimationFrame(() => resolve()));
  });
}

beforeEach(() => {
  const manager = createToastManager();
  toast = createToastApi(manager);
  wrapper = mount(ToastHost, {
    props: { manager },
    attachTo: document.body,
  });
});

afterEach(() => {
  wrapper.unmount();
  document.body.innerHTML = "";
  toast.configure({
    placement: "top-center",
    duration: 3000,
    maxCount: 5,
  });
  vi.useRealTimers();
});

describe("Toast", () => {
  it("announces errors assertively", async () => {
    toast.error("Failed");
    await nextTick();
    await flushFrames();

    const alerts = document.body.querySelectorAll('[role="alert"]');

    expect(alerts).toHaveLength(1);
    expect(alerts[0].getAttribute("aria-live")).toBe("assertive");
    expect(alerts[0].textContent).toContain("Failed");
  });

  it("announces other messages politely", async () => {
    toast.success("Saved");
    await nextTick();
    await flushFrames();

    const alerts = document.body.querySelectorAll('[role="alert"]');

    expect(alerts[0].getAttribute("aria-live")).toBe("polite");
  });

  it("renders a native close button", async () => {
    toast.success("Saved");
    await nextTick();

    const button = toastElement()?.querySelector("button.toast-control");

    expect(button).not.toBeNull();
    expect(button?.getAttribute("aria-label")).toBe("Close");
  });

  it("closes from the close button", async () => {
    const instance = toast.success("Saved");
    await nextTick();

    toastElement()
      ?.querySelector<HTMLButtonElement>("button.toast-control")
      ?.click();
    await nextTick();

    expect(instance.id).toBeTruthy();
    expect(toastElement()?.getAttribute("data-state")).toBe("closed");
  });

  it("merges repeated messages and keeps ids unique", async () => {
    const first = toast.success("Saved");
    const second = toast.success("Saved");
    await nextTick();

    expect(second.id).toBe(first.id);
    expect(toastElements()).toHaveLength(1);
    expect(toastElement()?.querySelector(".toast-count")?.textContent).toBe(
      "1"
    );

    const other = toast.info("Synced");
    await nextTick();

    expect(other.id).not.toBe(first.id);
    expect(toastElements()).toHaveLength(2);
  });

  it("does not merge different types or opt-out toasts", async () => {
    toast.success("Done");
    toast.error("Done");
    toast.success("Saved", { grouping: false });
    toast.success("Saved", { grouping: false });
    await nextTick();

    expect(toastElements()).toHaveLength(4);
  });

  it("updates an existing toast when a key is reused", async () => {
    const first = toast.success("First", { key: "sync" });
    const second = toast.success("Second", { key: "sync" });
    await nextTick();

    expect(second.id).toBe(first.id);
    expect(toastElements()).toHaveLength(1);
    expect(toastElement()?.textContent).toContain("Second");
  });

  it("closes after the duration but keeps persistent toasts", async () => {
    vi.useFakeTimers();

    toast.success("Auto", { duration: 100 });
    toast.info("Forever", { duration: 0 });
    await nextTick();

    vi.advanceTimersByTime(120);
    await nextTick();

    expect(states()).toContain("closed");
    expect(states()).toContain("open");
  });

  it("calls onClose when a toast closes", async () => {
    const onClose = vi.fn();
    const instance = toast.success("Saved", { onClose });
    await nextTick();

    instance.close();
    await nextTick();

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("clears every toast", async () => {
    toast.success("One");
    toast.error("Two");
    await nextTick();

    toast.clear();
    await nextTick();

    expect(states()).toEqual(["closed", "closed"]);
  });

  it("enforces maxCount by closing the oldest toast", async () => {
    toast.configure({ maxCount: 2 });

    toast.success("One");
    toast.success("Two");
    toast.success("Three");
    await nextTick();

    expect(states().filter((state) => state === "open")).toHaveLength(2);
  });

  it("limits the default number of live toasts to five", async () => {
    for (let index = 1; index <= 6; index++) {
      toast.success(`消息 ${index}`);
    }
    await nextTick();

    expect(states().filter((state) => state === "open")).toHaveLength(5);
  });

  it("renders title and action", async () => {
    const onClick = vi.fn();
    toast.error("Sync failed", {
      title: "Sync interrupted",
      action: { label: "Retry", onClick },
    });
    await nextTick();

    expect(toastElement()?.textContent).toContain("Sync interrupted");
    expect(toastElement()?.textContent).toContain("Sync failed");

    toastElement()?.querySelector<HTMLButtonElement>(".toast-action")?.click();
    await nextTick();

    expect(onClick).toHaveBeenCalledTimes(1);
    expect(toastElement()?.getAttribute("data-state")).toBe("closed");
  });

  it("turns a loading toast into a success toast", async () => {
    let resolveUpload: (name: string) => void = () => undefined;
    const upload = new Promise<string>((resolve) => {
      resolveUpload = resolve;
    });

    const pending = toast.promise(upload, {
      loading: "Uploading",
      success: (name) => `Uploaded ${name}`,
      error: "Upload failed",
    });

    await nextTick();
    expect(toastElement()?.textContent).toContain("Uploading");
    expect(toastElement()?.getAttribute("data-status")).toBe("loading");

    resolveUpload("a.png");
    await pending;
    await nextTick();

    expect(toastElement()?.textContent).toContain("Uploaded a.png");
    expect(toastElement()?.getAttribute("data-status")).toBe("success");
  });

  it("mounts the shared host on first use", async () => {
    vi.resetModules();
    const { Toast } = await import("../toast-manager");

    Toast.success("Shared host", { duration: 0 });
    await nextTick();
    await flushFrames();

    expect(toastElement()?.textContent).toContain("Shared host");
  });

  it("applies the configured placement", async () => {
    toast.configure({ placement: "bottom-right" });
    toast.success("Saved");
    await nextTick();

    expect(
      document.body.querySelector(".toast-container")?.className
    ).toContain("toast-placement-bottom-right");
  });
});
