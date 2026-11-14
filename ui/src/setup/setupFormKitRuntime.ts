import * as FormKitCore from "@formkit/core";
import * as FormKitVue from "@formkit/vue";

const runtime = globalThis as typeof globalThis & {
  FormKitCore?: typeof FormKitCore;
  FormKitVue?: typeof FormKitVue;
};

runtime.FormKitCore = FormKitCore;
// TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
runtime.FormKitVue = FormKitVue;
