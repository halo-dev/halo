import { enableAutoUnmount, flushPromises, mount } from "@vue/test-utils";
import {
  afterEach,
  describe,
  expect,
  it,
  vi,
  type MockInstance,
} from "vite-plus/test";
import { nextTick } from "vue";
import { setComponentsLocale } from "../../../locales";
import { Dialog, VDialog } from "../index";

enableAutoUnmount(afterEach);

function getAlertDialog() {
  return document.querySelector<HTMLElement>('[role="alertdialog"]');
}

function getConfirmButton() {
  return document.querySelector<HTMLButtonElement>("[data-dialog-confirm]");
}

function getCancelButton() {
  return document.querySelector<HTMLButtonElement>("[data-dialog-cancel]");
}

async function settle() {
  await nextTick();
  await nextTick();
}

async function mountDialog(props: Record<string, unknown> = {}) {
  const wrapper = mount(VDialog, {
    attachTo: document.body,
    props: {
      visible: true,
      ...props,
    },
  });

  await settle();

  return wrapper;
}

function pressEscape() {
  window.dispatchEvent(
    new KeyboardEvent("keydown", { key: "Escape", cancelable: true })
  );
}

afterEach(() => {
  Dialog.destroyAll();
  setComponentsLocale("en");
  vi.restoreAllMocks();
});

describe("Dialog", () => {
  it("should render", () => {
    expect(mount(VDialog)).toBeDefined();
  });

  it("renders an alertdialog labelled by its title and description", async () => {
    await mountDialog({
      title: "Delete post",
      description: "This action cannot be undone.",
    });

    const dialog = getAlertDialog();

    expect(dialog).not.toBeNull();
    expect(dialog?.getAttribute("role")).toBe("alertdialog");

    const labelledby = dialog?.getAttribute("aria-labelledby");
    expect(labelledby).toBeTruthy();
    expect(
      document.getElementById(labelledby as string)?.textContent?.trim()
    ).toBe("Delete post");

    const describedby = dialog?.getAttribute("aria-describedby");
    expect(describedby).toBeTruthy();
    expect(
      document.getElementById(describedby as string)?.textContent?.trim()
    ).toBe("This action cannot be undone.");
  });

  it("always provides a description for assistive technology", async () => {
    await mountDialog({ title: "Notice" });

    const describedby = getAlertDialog()?.getAttribute("aria-describedby");

    expect(describedby).toBeTruthy();
    expect(document.getElementById(describedby as string)).not.toBeNull();
  });

  it("falls back to localized confirm and cancel labels", async () => {
    setComponentsLocale("zh-CN");
    await mountDialog();

    expect(getConfirmButton()?.textContent?.trim()).toBe("确定");
    expect(getCancelButton()?.textContent?.trim()).toBe("取消");
  });

  it("exposes the close affordance as a native button", async () => {
    await mountDialog({ title: "Notice" });

    const close = document.querySelector<HTMLButtonElement>(
      "[data-dialog-close]"
    );

    expect(close?.tagName).toBe("BUTTON");
    expect(close?.getAttribute("aria-label")).toBe("Close");
  });

  it("runs onConfirm and closes on success", async () => {
    const onConfirm = vi.fn();
    const wrapper = await mountDialog({ title: "Notice", onConfirm });

    getConfirmButton()?.click();
    await flushPromises();

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(wrapper.emitted("update:visible")?.[0]).toEqual([false]);
    expect(wrapper.emitted("close")).toHaveLength(1);
  });

  it("keeps the dialog open when onConfirm rejects", async () => {
    const errorSpy: MockInstance = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});
    const onConfirm = vi.fn().mockRejectedValue(new Error("request failed"));
    const wrapper = await mountDialog({ title: "Notice", onConfirm });

    getConfirmButton()?.click();
    await flushPromises();
    await settle();

    expect(errorSpy).toHaveBeenCalled();
    expect(wrapper.emitted("close")).toBeUndefined();
    expect(getAlertDialog()).not.toBeNull();
    expect(getConfirmButton()?.getAttribute("aria-busy")).toBeNull();
  });

  it("blocks cancel and escape while the confirmation is pending", async () => {
    let resolveConfirm = () => {};
    const onConfirm = vi.fn(
      () =>
        new Promise<void>((resolve) => {
          resolveConfirm = resolve;
        })
    );
    const onCancel = vi.fn();
    const wrapper = await mountDialog({ title: "Notice", onConfirm, onCancel });

    getConfirmButton()?.click();
    await settle();

    expect(getCancelButton()?.disabled).toBe(true);

    getCancelButton()?.click();
    pressEscape();
    await settle();

    expect(onCancel).not.toHaveBeenCalled();
    expect(getAlertDialog()).not.toBeNull();

    resolveConfirm();
    await flushPromises();
    await settle();

    expect(wrapper.emitted("close")).toHaveLength(1);
  });

  it("calls onCancel before closing", async () => {
    const onCancel = vi.fn();
    const wrapper = await mountDialog({ title: "Notice", onCancel });

    getCancelButton()?.click();
    await settle();

    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(wrapper.emitted("close")).toHaveLength(1);
  });

  it("closes on escape by default and can opt out", async () => {
    const wrapper = await mountDialog({ title: "Notice" });

    pressEscape();
    await settle();

    expect(wrapper.emitted("close")).toHaveLength(1);

    const other = await mountDialog({ title: "Notice", closeOnEsc: false });

    pressEscape();
    await settle();

    expect(other.emitted("close")).toBeUndefined();
    expect(getAlertDialog()).not.toBeNull();
  });

  describe("manager api", () => {
    it("mounts into the document and can be destroyed", async () => {
      const instance = Dialog.info({ title: "Notice" });

      await settle();

      expect(getAlertDialog()).not.toBeNull();

      instance?.destroy();

      expect(getAlertDialog()).toBeNull();
    });

    it("skips duplicate dialogs sharing a uniqueId", async () => {
      const first = Dialog.warning({ title: "First", uniqueId: "same-id" });
      const second = Dialog.warning({ title: "Second", uniqueId: "same-id" });

      await settle();

      expect(first).toBeDefined();
      expect(second).toBeUndefined();
      expect(document.querySelectorAll('[role="alertdialog"]')).toHaveLength(1);

      Dialog.destroyAll();
    });

    it("closes programmatically without calling onCancel", async () => {
      const onCancel = vi.fn();
      const instance = Dialog.info({ title: "Notice", onCancel });

      await settle();
      instance?.close();
      await settle();

      expect(onCancel).not.toHaveBeenCalled();
      expect(getAlertDialog()).toBeNull();
    });

    it("removes the dialog after the leave animation when confirmed", async () => {
      const instance = Dialog.info({ title: "Notice" });

      await settle();

      getConfirmButton()?.click();
      await flushPromises();

      await new Promise((resolve) => setTimeout(resolve, 250));
      await settle();

      expect(getAlertDialog()).toBeNull();
      expect(instance).toBeDefined();
    });

    it("destroys every open dialog", async () => {
      Dialog.info({ title: "First" });
      Dialog.success({ title: "Second" });

      await settle();

      expect(document.querySelectorAll('[role="alertdialog"]')).toHaveLength(2);

      Dialog.destroyAll();

      expect(getAlertDialog()).toBeNull();
    });
  });
});
