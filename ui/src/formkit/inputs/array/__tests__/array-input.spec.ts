/* eslint-disable vue/one-component-per-file -- Test-local array harnesses keep each mode isolated. */
import { getNode } from "@formkit/core";
import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import { DOMWrapper, flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { defineComponent, nextTick } from "vue";
import { createI18n } from "vue-i18n";
import { array } from "..";
import ArrayFormModal from "../ArrayFormModal.vue";
import ArrayInput from "../ArrayInput.vue";
import ArrayItemForm from "../ArrayItemForm.vue";

const messages = {
  "core.formkit.array.empty_text": "No items",
  "core.formkit.array.image_tooltip": "View image: {value}",
  "core.formkit.array.item_modal.title": "Edit item",
  "core.formkit.array.new_item": "New item",
  "core.common.buttons.add": "Add",
  "core.common.buttons.cancel": "Cancel",
  "core.common.buttons.cancel_and_shortcut": "Cancel (Esc)",
  "core.common.buttons.delete": "Delete",
  "core.common.buttons.save": "Save",
  "core.common.buttons.submit": "Submit",
};

let mountSeed = 0;

function mountArray(props: Record<string, unknown> = {}) {
  const id = `array-test-${mountSeed++}`;
  const harness = defineComponent({
    name: "ArrayHarness",
    setup() {
      return { props, ArrayInput };
    },
    template: `
      <FormKit type="form" :id="'${id}-form'" :actions="false">
        <FormKit
          :id="'${id}-items'"
          type="array"
          name="items"
          :library="{ ArrayInput }"
          v-bind="props"
        >
          <FormKit type="text" name="key" label="Key" />
          <FormKit type="text" name="value" label="Value" />
        </FormKit>
      </FormKit>
    `,
  });

  const wrapper = mount(harness, {
    attachTo: document.body,
    global: {
      directives: {
        tooltip: () => undefined,
      },
      plugins: [
        [formKitPlugin, defaultConfig({ inputs: { array } })],
        createI18n({
          legacy: false,
          locale: "en",
          messages: { en: messages },
        }),
      ],
    },
  });

  return { wrapper, nodeId: `${id}-items` };
}

async function settle() {
  await flushPromises();
  await flushPromises();
  await nextTick();
}

function findButton(
  wrapper: ReturnType<typeof mountArray>["wrapper"],
  text: string
) {
  return wrapper.findAll("button").find((button) => button.text() === text);
}

afterEach(() => {
  document.body.innerHTML = "";
});

describe("array input", () => {
  it("registers itemFormMode on the FormKit input definition", () => {
    expect(array.props).toContain("itemFormMode");
  });

  it("uses the modal by default", async () => {
    const { wrapper } = mountArray({
      value: [{ key: "alpha", value: "1" }],
    });
    await settle();

    expect(wrapper.find(".v-accordion").exists()).toBe(false);
    expect(wrapper.findComponent(ArrayFormModal).exists()).toBe(false);

    await wrapper.get(".group\\/item").trigger("click");
    await settle();

    expect(wrapper.findComponent(ArrayFormModal).exists()).toBe(true);
    wrapper.unmount();
  });

  it("saves an existing item through the default modal", async () => {
    const { wrapper, nodeId } = mountArray({
      value: [{ key: "alpha", value: "1" }],
    });
    await settle();

    await wrapper.get(".group\\/item").trigger("click");
    await settle();

    const dialog = document.querySelector<HTMLElement>('[role="dialog"]');
    expect(dialog).toBeTruthy();

    const inputs = Array.from(dialog!.querySelectorAll("input")).map(
      (input) => new DOMWrapper(input)
    );
    await inputs[0].setValue("updated");
    await inputs[1].setValue("2");

    const form = dialog!.querySelector("form");
    expect(form).toBeTruthy();
    const formNode = getNode(form!.id);
    const submitButton = Array.from(dialog!.querySelectorAll("button")).find(
      (button) => button.textContent?.trim() === "Submit"
    );
    await new DOMWrapper(submitButton!).trigger("click");
    await formNode?.settled;
    await settle();

    expect(getNode(nodeId)?.value).toEqual([{ key: "updated", value: "2" }]);
    wrapper.unmount();
  });

  it("expands an item inline in accordion mode", async () => {
    const { wrapper } = mountArray({
      value: [{ key: "alpha", value: "1" }],
      itemFormMode: "accordion",
    });
    await settle();

    expect(wrapper.find(".v-accordion").exists()).toBe(true);
    expect(wrapper.findAll(".v-accordion-content input")).toHaveLength(0);

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    expect(wrapper.findAll(".v-accordion-content input")).toHaveLength(2);
    expect(wrapper.text()).toContain("Save");
    expect(wrapper.text()).toContain("Cancel");
    wrapper.unmount();
  });

  it("seeds the minimum number of items when the initial value is empty", async () => {
    const { wrapper, nodeId } = mountArray({
      min: 1,
      itemFormMode: "accordion",
    });
    await settle();

    expect(wrapper.findAll("button.v-accordion-trigger")).toHaveLength(1);
    expect(wrapper.text()).not.toContain("No items");
    expect(getNode(nodeId)?.value).toEqual([{}]);
    wrapper.unmount();
  });

  it("saves an existing item from the inline form", async () => {
    const { wrapper, nodeId } = mountArray({
      value: [{ key: "alpha", value: "1" }],
      itemFormMode: "accordion",
    });
    await settle();

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    const inputs = wrapper.findAll(".v-accordion-content input");
    await inputs[0].setValue("updated");
    await inputs[1].setValue("2");

    const form = wrapper.get(".v-accordion-content form");
    const formNode = getNode(form.attributes("id")!);
    expect(formNode).toBeTruthy();
    expect(wrapper.findComponent(ArrayItemForm).vm.submit).toBeTypeOf(
      "function"
    );

    await findButton(wrapper, "Save")?.trigger("click");
    await formNode?.settled;
    await settle();

    expect(getNode(nodeId)?.value).toEqual([{ key: "updated", value: "2" }]);
    wrapper.unmount();
  });

  it("creates a new item from the inline form", async () => {
    const { wrapper, nodeId } = mountArray({
      value: [{ key: "alpha", value: "1" }],
      itemFormMode: "accordion",
    });
    await settle();

    await findButton(wrapper, "Add")?.trigger("click");
    await settle();

    expect(wrapper.text()).toContain("New item");
    expect(wrapper.findAll(".v-accordion-content input")).toHaveLength(2);

    const inputs = wrapper.findAll(".v-accordion-content input");
    await inputs[0].setValue("beta");
    await inputs[1].setValue("2");

    const form = wrapper.get(".v-accordion-content form");
    const formNode = getNode(form.attributes("id")!);

    await findButton(wrapper, "Save")?.trigger("click");
    await formNode?.settled;
    await settle();

    expect(getNode(nodeId)?.value).toEqual([
      { key: "alpha", value: "1" },
      { key: "beta", value: "2" },
    ]);
    wrapper.unmount();
  });

  it("discards a new item when cancelled", async () => {
    const { wrapper, nodeId } = mountArray({
      value: [{ key: "alpha", value: "1" }],
      itemFormMode: "accordion",
    });
    await settle();

    await findButton(wrapper, "Add")?.trigger("click");
    await settle();
    await findButton(wrapper, "Cancel")?.trigger("click");
    await settle();

    expect(wrapper.text()).not.toContain("New item");
    expect(getNode(nodeId)?.value).toEqual([{ key: "alpha", value: "1" }]);
    wrapper.unmount();
  });

  it("removes an item from the inline form", async () => {
    const { wrapper, nodeId } = mountArray({
      value: [
        { key: "alpha", value: "1" },
        { key: "beta", value: "2" },
      ],
      itemFormMode: "accordion",
    });
    await settle();

    await wrapper.findAll("button.v-accordion-trigger")[0].trigger("click");
    await settle();
    await findButton(wrapper, "Delete")?.trigger("click");
    await settle();

    expect(getNode(nodeId)?.value).toEqual([{ key: "beta", value: "2" }]);
    wrapper.unmount();
  });

  it("renders image labels without interactive content in accordion headers", async () => {
    const { wrapper } = mountArray({
      value: [{ cover: "/upload/test.png" }],
      itemFormMode: "accordion",
      itemLabels: [{ type: "image", label: "$value.cover" }],
    });
    await settle();

    const trigger = wrapper.get("button.v-accordion-trigger");
    expect(trigger.find("a").exists()).toBe(false);
    expect(trigger.get("img").attributes("alt")).toContain("/upload/test.png");
    wrapper.unmount();
  });

  it("keeps image labels linked in the list row", async () => {
    const { wrapper } = mountArray({
      value: [{ cover: "/upload/test.png" }],
      itemLabels: [{ type: "image", label: "$value.cover" }],
    });
    await settle();

    const link = wrapper.get(".group\\/item a");
    expect(link.attributes("href")).toBe("/upload/test.png");
    expect(link.get("img").attributes("alt")).toContain("/upload/test.png");
    wrapper.unmount();
  });

  it("renders nested array values and saves changes from the inline form", async () => {
    const id = `array-test-nested-${mountSeed++}`;
    const harness = defineComponent({
      name: "NestedArrayHarness",
      setup() {
        return { id, ArrayInput };
      },
      template: `
        <FormKit type="form" :id="'${id}-form'" :actions="false">
          <FormKit
            :id="'${id}-items'"
            type="array"
            name="items"
            item-form-mode="accordion"
            :library="{ ArrayInput }"
            :value="[{ name: 'A', children: [{ label: 'A1' }] }]"
          >
            <FormKit type="text" name="name" label="Name" />
            <FormKit
              type="array"
              name="children"
              item-form-mode="accordion"
              :item-labels="[{ type: 'text', label: '$value.label' }]"
              min="1"
            >
              <FormKit type="text" name="label" label="Label" />
            </FormKit>
          </FormKit>
        </FormKit>
      `,
    });

    const wrapper = mount(harness, {
      attachTo: document.body,
      global: {
        directives: { tooltip: () => undefined },
        plugins: [
          [formKitPlugin, defaultConfig({ inputs: { array } })],
          createI18n({
            legacy: false,
            locale: "en",
            messages: { en: messages },
          }),
        ],
      },
    });
    await settle();

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();
    await wrapper
      .get(".v-accordion-content button.v-accordion-trigger")
      .trigger("click");
    await settle();

    const nestedInput = wrapper.get(
      ".v-accordion-content .v-accordion-content input"
    );
    expect(nestedInput.attributes("value")).toBe("A1");

    await nestedInput.setValue("A1 updated");
    const nestedForm = wrapper.get(
      ".v-accordion-content .v-accordion-content form"
    );
    const nestedFormNode = getNode(nestedForm.attributes("id")!);
    await findButton(wrapper, "Save")?.trigger("click");
    await nestedFormNode?.settled;
    await settle();
    await new Promise((resolve) => setTimeout(resolve, 250));

    await findButton(wrapper, "Save")?.trigger("click");
    await settle();

    expect(getNode(`${id}-items`)?.value).toEqual([
      {
        name: "A",
        children: [{ label: "A1 updated" }],
      },
    ]);
    wrapper.unmount();
  });

  it("commits open accordion edits when the parent requests it", async () => {
    const { wrapper, nodeId } = mountArray({
      value: [{ key: "alpha", value: "1" }],
      itemFormMode: "accordion",
    });
    await settle();

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    const inputs = wrapper.findAll(".v-accordion-content input");
    await inputs[0].setValue("updated");
    await inputs[1].setValue("2");

    const commit = (
      getNode(nodeId)?.context as
        | { commitOpenAccordionItem?: () => Promise<boolean> }
        | undefined
    )?.commitOpenAccordionItem;
    expect(commit).toBeTypeOf("function");

    await expect(commit?.()).resolves.toBe(true);
    await settle();

    expect(getNode(nodeId)?.value).toEqual([{ key: "updated", value: "2" }]);
    expect(wrapper.find(".v-accordion-content input").exists()).toBe(false);
    wrapper.unmount();
  });
});
