import * as FormKitCore from "@formkit/core";
import * as FormKitVue from "@formkit/vue";
import { describe, expect, it } from "vite-plus/test";
import "../setupFormKitRuntime";

describe("FormKit shared runtime", () => {
  it("exposes Vue and Core from the host's single module graph", () => {
    const runtime = globalThis as typeof globalThis & {
      FormKitCore: typeof FormKitCore;
      FormKitVue: typeof FormKitVue;
    };

    expect(runtime.FormKitCore.getNode).toBe(FormKitCore.getNode);
    expect(runtime.FormKitCore.submitForm).toBe(FormKitCore.submitForm);
    expect(runtime.FormKitCore.reset).toBe(FormKitCore.reset);
    expect(runtime.FormKitVue.submitForm).toBe(FormKitVue.submitForm);
  });
});
