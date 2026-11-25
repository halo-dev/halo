import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { defineComponent, h, nextTick } from "vue";
import { VAccordion, VAccordionItem } from "..";

interface ItemOptions {
  value: string;
  title?: string;
  props?: Record<string, unknown>;
  content?: string;
}

function makeItem({
  value,
  title = value,
  props = {},
  content = `Content ${value}`,
}: ItemOptions) {
  return h(
    VAccordionItem,
    { value, title, ...props },
    {
      default: () => h("div", { "data-testid": `content-${value}` }, content),
    }
  );
}

function mountAccordion(
  props: Record<string, unknown> = {},
  items: ItemOptions[] = [{ value: "one" }, { value: "two" }]
) {
  return mount(VAccordion, {
    props,
    slots: {
      default: () => items.map(makeItem),
    },
    attachTo: document.body,
  });
}

// Reka's Presence waits for a tick before toggling mounted content.
async function settle() {
  await nextTick();
  await nextTick();
}

afterEach(() => {
  document.body.innerHTML = "";
});

describe("Accordion", () => {
  it("points aria-controls at the controlled content", async () => {
    const wrapper = mountAccordion({ type: "single", collapsible: true });
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");
    expect(triggers).toHaveLength(2);
    expect(triggers[0].attributes("aria-expanded")).toBe("false");

    // Reka keeps the generated content id off the reactive context, so the
    // trigger can render an empty `aria-controls` before the content mounts.
    for (const trigger of triggers) {
      expect(trigger.attributes("aria-controls")).not.toBe("");
    }

    await triggers[0].trigger("click");
    await settle();

    expect(triggers[0].attributes("aria-expanded")).toBe("true");
    const controls = triggers[0].attributes("aria-controls");
    expect(controls).toBeTruthy();
    expect(wrapper.find(`[id="${controls}"]`).exists()).toBe(true);

    // Content stays mounted by default so form state survives collapsing.
    await triggers[0].trigger("click");
    await settle();
    expect(wrapper.find('[data-testid="content-one"]').exists()).toBe(true);

    wrapper.unmount();
  });

  it("opens and closes a collapsible single item", async () => {
    const wrapper = mountAccordion({
      type: "single",
      collapsible: true,
    });
    await settle();

    const trigger = wrapper.findAll("button.v-accordion-trigger")[0];

    await trigger.trigger("click");
    await settle();
    expect(trigger.attributes("aria-expanded")).toBe("true");
    expect(wrapper.emitted("update:modelValue")?.at(-1)).toEqual(["one"]);

    await trigger.trigger("click");
    await settle();
    expect(trigger.attributes("aria-expanded")).toBe("false");
    expect(wrapper.emitted("update:modelValue")?.at(-1)).toEqual([undefined]);

    wrapper.unmount();
  });

  it("keeps a single item open when collapsible is false", async () => {
    const wrapper = mountAccordion({ type: "single" });
    await settle();

    const trigger = wrapper.findAll("button.v-accordion-trigger")[0];

    await trigger.trigger("click");
    await settle();
    await trigger.trigger("click");
    await settle();

    expect(trigger.attributes("aria-expanded")).toBe("true");
    expect(wrapper.emitted("update:modelValue")).toHaveLength(1);

    wrapper.unmount();
  });

  it("switches the open item in single mode", async () => {
    const wrapper = mountAccordion({ type: "single" });
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");

    await triggers[0].trigger("click");
    await settle();
    await triggers[1].trigger("click");
    await settle();

    expect(triggers[0].attributes("aria-expanded")).toBe("false");
    expect(triggers[1].attributes("aria-expanded")).toBe("true");

    wrapper.unmount();
  });

  it("accumulates open items in multiple mode", async () => {
    const wrapper = mountAccordion({ type: "multiple" });
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");

    await triggers[0].trigger("click");
    await settle();
    await triggers[1].trigger("click");
    await settle();

    expect(triggers[0].attributes("aria-expanded")).toBe("true");
    expect(triggers[1].attributes("aria-expanded")).toBe("true");
    expect(wrapper.emitted("update:modelValue")?.at(-1)).toEqual([
      ["one", "two"],
    ]);

    wrapper.unmount();
  });

  it("respects a controlled model value", async () => {
    const wrapper = mountAccordion({ type: "single", modelValue: "one" });
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");
    expect(triggers[0].attributes("aria-expanded")).toBe("true");

    await triggers[1].trigger("click");
    await settle();

    expect(wrapper.emitted("update:modelValue")?.at(-1)).toEqual(["two"]);
    // The parent did not update the prop, so the first item stays open.
    expect(triggers[0].attributes("aria-expanded")).toBe("true");
    expect(triggers[1].attributes("aria-expanded")).toBe("false");

    wrapper.unmount();
  });

  it("opens defaultValue items", async () => {
    const wrapper = mountAccordion({ type: "single", defaultValue: "two" });
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");
    expect(triggers[0].attributes("aria-expanded")).toBe("false");
    expect(triggers[1].attributes("aria-expanded")).toBe("true");

    wrapper.unmount();
  });

  it("applies the configured header element", async () => {
    const wrapper = mountAccordion({}, [
      { value: "one", props: { headerAs: "h4" } },
    ]);
    await settle();

    expect(wrapper.find("h4.v-accordion-header").exists()).toBe(true);

    wrapper.unmount();
  });

  it("supports custom trigger and icon slots", async () => {
    const wrapper = mount(VAccordion, {
      slots: {
        default: () =>
          h(
            VAccordionItem,
            { value: "one" },
            {
              trigger: ({ open }: { open: boolean }) =>
                h(
                  "span",
                  { "data-testid": "custom-trigger" },
                  open ? "open" : "closed"
                ),
              icon: () => h("span", { "data-testid": "custom-icon" }, "+"),
              default: () => h("div", "Content"),
            }
          ),
      },
      attachTo: document.body,
    });
    await settle();

    expect(wrapper.find('[data-testid="custom-trigger"]').text()).toBe(
      "closed"
    );
    expect(wrapper.find('[data-testid="custom-icon"]').exists()).toBe(true);
    expect(wrapper.find(".v-accordion-icon").exists()).toBe(false);

    await wrapper.get("button.v-accordion-trigger").trigger("click");
    await settle();

    expect(wrapper.find('[data-testid="custom-trigger"]').text()).toBe("open");

    wrapper.unmount();
  });

  it("unmounts hidden content unless force mounted", async () => {
    const unmounted = mountAccordion({ unmountOnHide: true }, [
      { value: "one" },
    ]);
    await settle();
    expect(unmounted.find('[data-testid="content-one"]').exists()).toBe(false);
    unmounted.unmount();

    const forceMounted = mountAccordion({ unmountOnHide: true }, [
      { value: "one", props: { forceMount: true } },
    ]);
    await settle();
    expect(forceMounted.find('[data-testid="content-one"]').exists()).toBe(
      true
    );
    forceMounted.unmount();
  });

  it("does not open when disabled", async () => {
    const wrapper = mountAccordion({ disabled: true });
    await settle();

    const trigger = wrapper.findAll("button.v-accordion-trigger")[0];
    expect(trigger.attributes("disabled")).toBeDefined();

    await trigger.trigger("click");
    await settle();

    expect(trigger.attributes("aria-expanded")).toBe("false");
    expect(wrapper.emitted("update:modelValue")).toBeUndefined();

    wrapper.unmount();
  });

  it("moves focus between triggers with arrow keys", async () => {
    const wrapper = mountAccordion();
    await settle();

    const triggers = wrapper.findAll("button.v-accordion-trigger");
    (triggers[0].element as HTMLElement).focus();

    await triggers[0].trigger("keydown", { key: "ArrowDown" });
    await nextTick();

    expect(document.activeElement).toBe(triggers[1].element);

    wrapper.unmount();
  });

  it("applies variant classes", async () => {
    for (const variant of [
      "default",
      "bordered",
      "filled",
      "separated",
      "plain",
    ]) {
      const wrapper = mountAccordion({ variant });
      await settle();
      expect(wrapper.classes()).toContain(`v-accordion--${variant}`);
      wrapper.unmount();
    }
  });

  it("places the chevron on the configured side", async () => {
    const right = mountAccordion();
    await settle();
    expect(
      right
        .get("button.v-accordion-trigger")
        .attributes("data-chevron-position")
    ).toBe("right");
    right.unmount();

    const left = mountAccordion({ chevronPosition: "left" });
    await settle();
    expect(
      left.get("button.v-accordion-trigger").attributes("data-chevron-position")
    ).toBe("left");
    left.unmount();
  });

  it("can disable chevron rotation", async () => {
    const rotating = mountAccordion();
    await settle();
    expect(
      rotating
        .get("button.v-accordion-trigger")
        .attributes("data-chevron-rotation")
    ).toBe("true");
    rotating.unmount();

    const staticChevron = mountAccordion({
      disableChevronRotation: true,
    });
    await settle();
    expect(
      staticChevron
        .get("button.v-accordion-trigger")
        .attributes("data-chevron-rotation")
    ).toBe("false");
    staticChevron.unmount();
  });

  it("applies the transition duration through a CSS variable", async () => {
    const wrapper = mountAccordion({ transitionDuration: 400 });
    await settle();
    expect(wrapper.attributes("style")).toContain(
      "--v-accordion-transition-duration: 400ms"
    );
    expect(wrapper.classes()).not.toContain("v-accordion--no-transition");
    wrapper.unmount();

    const instant = mountAccordion({ transitionDuration: 0 });
    await settle();
    expect(instant.classes()).toContain("v-accordion--no-transition");
    expect(instant.attributes("style")).toContain(
      "--v-accordion-transition-duration: 0ms"
    );
    instant.unmount();
  });

  it("renders a leading icon before the label", async () => {
    const LeadingIcon = defineComponent({
      render: () => h("span", { "data-testid": "leading-icon" }, "*"),
    });

    const wrapper = mountAccordion({}, [
      { value: "one", props: { leadingIcon: LeadingIcon } },
    ]);
    await settle();

    const trigger = wrapper.get("button.v-accordion-trigger");
    expect(trigger.find('[data-testid="leading-icon"]').exists()).toBe(true);
    expect(trigger.find(".v-accordion-leading").exists()).toBe(true);

    wrapper.unmount();
  });

  it("supports a custom chevron on the item and on the root", async () => {
    const itemChevron = mount(VAccordion, {
      slots: {
        default: () =>
          h(
            VAccordionItem,
            { value: "one", title: "One" },
            {
              chevron: () =>
                h("span", { "data-testid": "item-chevron" }, "item"),
              default: () => h("div", "Content"),
            }
          ),
      },
      attachTo: document.body,
    });
    await settle();
    expect(itemChevron.find('[data-testid="item-chevron"]').exists()).toBe(
      true
    );
    expect(itemChevron.find(".v-accordion-icon").exists()).toBe(false);
    itemChevron.unmount();

    const rootChevron = mount(VAccordion, {
      slots: {
        default: () => [makeItem({ value: "one" }), makeItem({ value: "two" })],
        chevron: () => h("span", { "data-testid": "root-chevron" }, "root"),
      },
      attachTo: document.body,
    });
    await settle();
    expect(rootChevron.findAll('[data-testid="root-chevron"]')).toHaveLength(2);
    expect(rootChevron.find(".v-accordion-icon").exists()).toBe(false);
    rootChevron.unmount();
  });

  it("uses the root order as the default heading level", async () => {
    const wrapper = mountAccordion({ order: 4 }, [
      { value: "one" },
      { value: "two", props: { headerAs: "h2" } },
    ]);
    await settle();

    expect(wrapper.findAll("h4.v-accordion-header")).toHaveLength(1);
    expect(wrapper.findAll("h2.v-accordion-header")).toHaveLength(1);

    wrapper.unmount();
  });

  it("merges per-part class names", async () => {
    const wrapper = mountAccordion({}, [
      {
        value: "one",
        props: {
          classNames: {
            item: "custom-item",
            trigger: "custom-trigger",
            label: "custom-label",
            content: "custom-content",
          },
        },
      },
    ]);
    await settle();

    expect(wrapper.find(".custom-item").exists()).toBe(true);
    expect(wrapper.find("button.custom-trigger").exists()).toBe(true);
    expect(wrapper.find(".custom-label").exists()).toBe(true);
    expect(wrapper.find(".custom-content").exists()).toBe(true);

    wrapper.unmount();
  });
});
