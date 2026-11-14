// @vitest-environment jsdom

import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, nextTick } from "vue";
import type { Editor } from "@/tiptap";
import { useTableCommands } from "./useTableCommands";

describe("useTableCommands", () => {
  it("tracks editor transactions and releases its listener on unmount", async () => {
    let allowed = false;
    let transactionListener: (() => void) | undefined;
    const on = vi.fn((_event: string, listener: () => void) => {
      transactionListener = listener;
    });
    const off = vi.fn();
    const editor = {
      on,
      off,
      can: () =>
        new Proxy(
          {},
          {
            get: () => () => allowed,
          }
        ),
      getAttributes: () => ({}),
    } as unknown as Editor;
    const component = defineComponent({
      setup() {
        const { can } = useTableCommands(editor);
        return () => h("span", String(can.addRowBefore.value));
      },
    });
    const wrapper = mount(component);

    expect(wrapper.text()).toBe("false");
    expect(on).toHaveBeenCalledWith("transaction", expect.any(Function));

    allowed = true;
    transactionListener?.();
    await nextTick();
    expect(wrapper.text()).toBe("true");

    wrapper.unmount();
    expect(off).toHaveBeenCalledWith("transaction", transactionListener);
  });
});
