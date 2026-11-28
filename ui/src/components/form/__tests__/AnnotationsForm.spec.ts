import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import messages from "@intlify/unplugin-vue-i18n/messages";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { nextTick } from "vue";
import { createI18n } from "vue-i18n";
import { array } from "@/formkit/inputs/array";
import ArrayInput from "@/formkit/inputs/array/ArrayInput.vue";
import AnnotationsForm from "../AnnotationsForm.vue";

vi.mock("@halo-dev/api-client", async () => {
  const actual = await vi.importActual("@halo-dev/api-client");
  return {
    ...actual,
    ucApiClient: {
      core: {
        annotationSetting: {
          listAvailableAnnotationSettings: vi
            .fn()
            .mockResolvedValue({ data: [] }),
        },
      },
    },
  };
});

async function settle() {
  await flushPromises();
  await nextTick();
  await flushPromises();
}

afterEach(() => {
  document.body.innerHTML = "";
});

describe("AnnotationsForm", () => {
  it("commits an open custom annotation before submitting", async () => {
    const wrapper = mount(AnnotationsForm, {
      attachTo: document.body,
      props: {
        group: "",
        kind: "User",
        value: {
          "custom.halo.run/key": "old",
        },
      },
      global: {
        directives: {
          tooltip: () => undefined,
        },
        plugins: [
          [
            formKitPlugin,
            defaultConfig({
              inputs: { array },
              library: { ArrayInput },
            }),
          ],
          createI18n({
            legacy: false,
            locale: "en",
            messages,
          }),
        ],
      },
    });
    await settle();

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    const arrayItemTrigger = wrapper.findAll("button.v-accordion-trigger")[1];
    expect(arrayItemTrigger).toBeTruthy();
    await arrayItemTrigger.trigger("click");
    await settle();

    const inputs = wrapper.findAll(
      ".v-accordion-content .v-accordion-content input"
    );
    expect(inputs).toHaveLength(2);
    await inputs[0].setValue("custom.halo.run/updated");
    await inputs[1].setValue("new");
    await settle();

    await wrapper.vm.handleSubmit();
    await settle();

    expect(wrapper.vm.customFormInvalid).toBe(false);
    expect(wrapper.vm.customAnnotations).toEqual({
      "custom.halo.run/updated": "new",
    });
    wrapper.unmount();
  });
});
