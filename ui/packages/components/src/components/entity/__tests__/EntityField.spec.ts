import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { defineComponent, h } from "vue";
import { VEntityField } from "..";

const RouterLinkStub = defineComponent({
  name: "RouterLink",
  props: {
    to: {
      type: [String, Object],
      required: true,
    },
  },
  setup(_, { slots }) {
    return () => h("a", { href: "#" }, slots.default?.());
  },
});

describe("EntityField", () => {
  it("should render", () => {
    expect(mount(VEntityField)).toBeDefined();
  });

  it("renders the title and description", () => {
    const wrapper = mount(VEntityField, {
      props: {
        title: "Post title",
        description: "Post description",
      },
    });

    expect(wrapper.get(".entity-field-title").text()).toBe("Post title");
    expect(wrapper.get(".entity-field-description").text()).toBe(
      "Post description"
    );
  });

  it("renders no title or description body without content", () => {
    const wrapper = mount(VEntityField);

    expect(wrapper.find(".entity-field-title-body").exists()).toBe(false);
    expect(wrapper.find(".entity-field-description-body").exists()).toBe(false);
  });

  it("renders the title as a router link when a route is provided", () => {
    const route = { name: "PostEditor", query: { name: "hello" } };
    const wrapper = mount(VEntityField, {
      props: {
        title: "Post title",
        route,
      },
      global: {
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    });

    expect(wrapper.findComponent(RouterLinkStub).props("to")).toEqual(route);
    expect(wrapper.get(".entity-field-title").text()).toBe("Post title");
  });

  it("renders the extra slot next to the default title", () => {
    const wrapper = mount(VEntityField, {
      props: { title: "Post title" },
      slots: {
        extra: () => h("span", { class: "extra" }, "Extra"),
      },
    });

    const body = wrapper.get(".entity-field-title-body");

    expect(body.get(".entity-field-title").text()).toBe("Post title");
    expect(body.get(".extra").text()).toBe("Extra");
  });

  it("renders the extra slot next to a custom title", () => {
    const wrapper = mount(VEntityField, {
      slots: {
        title: () => h("span", { class: "custom-title" }, "Custom title"),
        extra: () => h("span", { class: "extra" }, "Extra"),
      },
    });

    const body = wrapper.get(".entity-field-title-body");

    expect(body.get(".custom-title").text()).toBe("Custom title");
    expect(body.get(".extra").text()).toBe("Extra");
  });

  it("converts numeric widths to pixels", () => {
    const wrapper = mount(VEntityField, {
      props: { width: 120, maxWidth: 320 },
    });
    const element = wrapper.get(".entity-field-wrapper").element as HTMLElement;

    expect(element.style.width).toBe("120px");
    expect(element.style.maxWidth).toBe("320px");
  });

  it("keeps string widths and accepts zero", () => {
    const stringWidth = mount(VEntityField, {
      props: { width: "100%", maxWidth: "30rem" },
    });
    const stringElement = stringWidth.get(".entity-field-wrapper")
      .element as HTMLElement;

    expect(stringElement.style.width).toBe("100%");
    expect(stringElement.style.maxWidth).toBe("30rem");

    const zeroWidth = mount(VEntityField, { props: { width: 0 } });
    const zeroElement = zeroWidth.get(".entity-field-wrapper")
      .element as HTMLElement;

    expect(zeroElement.style.width).toBe("0px");
  });

  it("emits click and is keyboard operable when clickable", async () => {
    const onClick = () => {};
    const wrapper = mount(VEntityField, {
      props: { title: "Open detail" },
      attrs: { onClick },
    });
    const title = wrapper.get(".entity-field-title");

    expect(title.attributes("role")).toBe("button");
    expect(title.attributes("tabindex")).toBe("0");
    expect(title.classes()).toContain("entity-field-title-clickable");

    await title.trigger("click");
    await title.trigger("keydown", { key: "Enter" });
    await title.trigger("keydown", { key: " " });

    expect(wrapper.emitted("click")).toHaveLength(3);
  });

  it("does not add button semantics to a routed title", () => {
    const wrapper = mount(VEntityField, {
      props: {
        title: "Post title",
        route: { name: "PostEditor" },
      },
      attrs: { onClick: () => {} },
      global: {
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    });
    const title = wrapper.get(".entity-field-title");

    expect(title.attributes("role")).toBeUndefined();
    expect(title.attributes("tabindex")).toBeUndefined();
  });

  it("is not keyboard operable without a click listener", async () => {
    const wrapper = mount(VEntityField, {
      props: { title: "Static title" },
    });
    const title = wrapper.get(".entity-field-title");

    expect(title.attributes("role")).toBeUndefined();
    expect(title.attributes("tabindex")).toBeUndefined();

    await title.trigger("keydown", { key: "Enter" });

    expect(wrapper.emitted("click")).toBeUndefined();
  });
});
