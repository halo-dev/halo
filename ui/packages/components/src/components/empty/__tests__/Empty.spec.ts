import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { h } from "vue";
import { VButton } from "../../button";
import { VEmpty } from "../index";

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

  it("should render only the regions that have content", () => {
    const wrapper = mount(VEmpty, { props: { title: "Not found" } });

    expect(wrapper.find(".empty-title").exists()).toBe(true);
    expect(wrapper.find(".empty-message").exists()).toBe(false);
    expect(wrapper.find(".empty-actions").exists()).toBe(false);
  });

  it("should work with message prop", () => {
    const wrapper = mount(VEmpty, { props: { message: "No posts found" } });
    expect(wrapper.find(".empty-message").text()).toEqual("No posts found");
  });

  it("should work with title slot", () => {
    const wrapper = mount(VEmpty, {
      slots: { title: h("span", "Custom title") },
    });

    expect(wrapper.find(".empty-title").text()).toEqual("Custom title");
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

  it("should render a decorative image by default", () => {
    const wrapper = mount(VEmpty);

    expect(wrapper.find(".empty-image > img").attributes().alt).toEqual("");
  });

  it("should work with imageAlt prop", () => {
    const wrapper = mount(VEmpty, {
      props: { imageAlt: "No posts found" },
    });

    expect(wrapper.find(".empty-image > img").attributes().alt).toEqual(
      "No posts found"
    );
  });

  it("should hide the image with showImage prop", () => {
    const wrapper = mount(VEmpty, {
      props: { showImage: false, title: "No posts found" },
    });

    expect(wrapper.find(".empty-image").exists()).toBe(false);
  });

  it("should apply the size class", () => {
    const wrapper = mount(VEmpty, { props: { size: "sm" } });

    expect(wrapper.find(".empty-size-sm").exists()).toBe(true);
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
