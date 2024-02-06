import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { VEmpty } from "../index";
import { VButton } from "../../button";
import { h } from "vue";

describe("Empty", () => {
  it("should render", () => {
    expect(mount(VEmpty)).toBeDefined();
  });

  it("should match snapshot", () => {
    expect(
      mount(VEmpty, {
        props: {
          title: "Not found",
          message: "No posts found",
          image: "./Empty.svg",
        },
        slots: {
          actions: h(VButton, { type: "primary" }, "New Post"),
        },
      }).html()
    ).toMatchSnapshot();
  });

  it("should work with title prop", () => {
    const wrapper = mount(VEmpty, { props: { title: "Not found" } });
    expect(wrapper.find(".empty-title").text()).toEqual("Not found");
  });

  it("should work with message prop", () => {
    const wrapper = mount(VEmpty, { props: { message: "No posts found" } });
    expect(wrapper.find(".empty-message").text()).toEqual("No posts found");
  });

  it("should work with message slot", () => {
    const wrapper = mount(VEmpty, {
      props: { message: "empty" },
      slots: { message: h("span", h("storage", "No posts found")) },
    });

    expect(wrapper.find(".empty-message")).not.toEqual("Empty");
    expect(wrapper.find(".empty-message > span > storage").text()).toEqual(
      "No posts found"
    );
  });

  it("should work with actions slot", () => {
    const wrapper = mount(VEmpty, {
      slots: {
        actions: h(VButton, { type: "primary" }, "New Post"),
      },
    });

    expect(wrapper.findComponent(VButton)).toBeDefined();
    expect(wrapper.findComponent(VButton).find(".btn-content").text()).toEqual(
      "New Post"
    );
  });

  it("should work with image prop", async () => {
    const wrapper = mount({
      data() {
        return {
          image: "",
        };
      },
      render() {
        return h(VEmpty, {
          image: this.image,
        });
      },
    });

    expect(
      wrapper.find(".empty-image > img").attributes().src.endsWith("/Empty.svg")
    ).toBe(true);

    await wrapper.setData({ image: "./empty.png" });

    expect(wrapper.find(".empty-image > img").attributes().src).toEqual(
      "./empty.png"
    );
  });

  it("should work with image slot", () => {
    const wrapper = mount(VEmpty, {
      slots: { image: h("img", { src: "./empty", alt: "Empty Status" }) },
    });

    const attributes = wrapper.find(".empty-image > img").attributes();

    expect(attributes.src).not.toEqual("/src/components/empty/Empty.svg");

    expect(attributes.src).toEqual("./empty");
    expect(attributes.alt).toEqual("Empty Status");
  });
});
