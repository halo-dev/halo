import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import type { AnnotationSetting } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
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

async function waitForInput() {
  await new Promise((resolve) => setTimeout(resolve, 250));
  await settle();
}

async function mountAnnotationsForm(value?: Record<string, string>) {
  const wrapper = mount(AnnotationsForm, {
    attachTo: document.body,
    props: {
      group: "",
      kind: "User",
      value: value ?? null,
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
  return wrapper;
}

function findButton(
  wrapper: Awaited<ReturnType<typeof mountAnnotationsForm>>,
  text: string
) {
  return wrapper.findAll("button").find((button) => button.text() === text);
}

afterEach(() => {
  document.body.innerHTML = "";
});

describe("AnnotationsForm", () => {
  it("commits an open custom annotation before submitting", async () => {
    const wrapper = await mountAnnotationsForm({
      "custom.halo.run/key": "old",
    });

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

  it("blocks editing a custom annotation key to a saved key", async () => {
    const wrapper = await mountAnnotationsForm({
      "custom.halo.run/a": "1",
      "custom.halo.run/b": "2",
    });

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");
    expect(triggers).toHaveLength(3);
    await triggers[2].trigger("click");
    await settle();

    const inputs = wrapper.findAll(
      ".v-accordion-content .v-accordion-content input"
    );
    expect(inputs).toHaveLength(2);
    await inputs[0].setValue("custom.halo.run/a");
    await waitForInput();

    await findButton(wrapper, "Save")?.trigger("click");
    await waitForInput();

    expect(wrapper.text()).toContain("The current Key is already in use");
    expect(
      wrapper.findAll("button.v-accordion-trigger")[2].attributes("data-state")
    ).toBe("open");

    await inputs[0].setValue("custom.halo.run/c");
    await waitForInput();
    await findButton(wrapper, "Save")?.trigger("click");
    await waitForInput();

    await wrapper.vm.handleSubmit();
    await settle();

    expect(wrapper.vm.customFormInvalid).toBe(false);
    expect(wrapper.vm.customAnnotations).toEqual({
      "custom.halo.run/a": "1",
      "custom.halo.run/c": "2",
    });
    wrapper.unmount();
  });

  it("blocks a new custom annotation key that duplicates a saved key", async () => {
    const wrapper = await mountAnnotationsForm({
      "custom.halo.run/a": "1",
    });

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    await findButton(wrapper, "Add")?.trigger("click");
    await settle();

    const inputs = wrapper.findAll(
      ".v-accordion-content .v-accordion-content input"
    );
    expect(inputs).toHaveLength(2);
    await inputs[0].setValue("custom.halo.run/a");
    await waitForInput();

    await findButton(wrapper, "Save")?.trigger("click");
    await waitForInput();

    expect(wrapper.text()).toContain("The current Key is already in use");
    expect(wrapper.text()).toContain("New item");

    await inputs[0].setValue("custom.halo.run/b");
    await waitForInput();
    await findButton(wrapper, "Save")?.trigger("click");
    await waitForInput();

    await wrapper.vm.handleSubmit();
    await settle();

    expect(wrapper.vm.customFormInvalid).toBe(false);
    expect(wrapper.vm.customAnnotations).toEqual({
      "custom.halo.run/a": "1",
      "custom.halo.run/b": "",
    });
    wrapper.unmount();
  });

  it("waits for the annotation settings form before resolving", async () => {
    vi.mocked(
      ucApiClient.core.annotationSetting.listAvailableAnnotationSettings
    ).mockResolvedValueOnce({
      data: [
        {
          apiVersion: "core.halo.run/v1alpha1",
          kind: "AnnotationSetting",
          metadata: { name: "test-setting" },
          spec: {
            targetRef: { group: "", kind: "User" },
            formSchema: [
              { $formkit: "text", name: "themeKey", label: "Theme key" },
            ],
          },
        } satisfies AnnotationSetting,
      ],
    } as Awaited<
      ReturnType<
        typeof ucApiClient.core.annotationSetting.listAvailableAnnotationSettings
      >
    >);

    const wrapper = await mountAnnotationsForm();
    expect(wrapper.text()).toContain("Theme key");

    await wrapper.vm.handleSubmit();
    await settle();

    expect(wrapper.vm.specFormInvalid).toBe(false);
    expect(wrapper.vm.customFormInvalid).toBe(false);
    wrapper.unmount();
  });
});
