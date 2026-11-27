import { mount, type VueWrapper } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { nextTick } from "vue";
import { VPagination } from "../index";

function getSelects(wrapper: VueWrapper) {
  return wrapper.findAll("select");
}

function getPageSelect(wrapper: VueWrapper) {
  return getSelects(wrapper)[0];
}

function getSizeSelect(wrapper: VueWrapper) {
  return getSelects(wrapper)[1];
}

function isDisabled(wrapper: VueWrapper, selector: string) {
  return (wrapper.get(selector).element as HTMLButtonElement).disabled;
}

describe("Pagination", () => {
  it("emits page changes from the buttons and the page select", async () => {
    const wrapper = mount(VPagination, {
      props: { page: 2, size: 10, total: 100 },
    });

    await wrapper.get(".pagination__btn--next").trigger("click");
    expect(wrapper.emitted("update:page")?.[0]).toEqual([3]);
    expect(wrapper.emitted("change")?.[0]).toEqual([{ page: 3, size: 10 }]);

    await wrapper.setProps({ page: 3 });
    await wrapper.get(".pagination__btn--prev").trigger("click");
    expect(wrapper.emitted("update:page")?.[1]).toEqual([2]);
    expect(wrapper.emitted("change")?.[1]).toEqual([{ page: 2, size: 10 }]);

    await getPageSelect(wrapper).setValue("5");
    expect(wrapper.emitted("update:page")?.[2]).toEqual([5]);
    expect(wrapper.emitted("change")?.[2]).toEqual([{ page: 5, size: 10 }]);
  });

  it("resets to the first page when the size changes", async () => {
    const wrapper = mount(VPagination, {
      props: { page: 3, size: 20, total: 100, sizeOptions: [20, 50] },
    });

    await getSizeSelect(wrapper).setValue("50");

    expect(wrapper.emitted("update:size")?.[0]).toEqual([50]);
    expect(wrapper.emitted("update:page")?.[0]).toEqual([1]);
    expect(wrapper.emitted("change")?.[0]).toEqual([{ page: 1, size: 50 }]);
  });

  it("clamps an out-of-range page and reports the correction", async () => {
    const wrapper = mount(VPagination, {
      props: { page: 99, size: 10, total: 100 },
    });

    await nextTick();

    expect(wrapper.emitted("update:page")?.[0]).toEqual([10]);
    expect(getPageSelect(wrapper).element.value).toBe("10");
    expect(isDisabled(wrapper, ".pagination__btn--prev")).toBe(false);
    expect(isDisabled(wrapper, ".pagination__btn--next")).toBe(true);
  });

  it("clamps the page when the total shrinks", async () => {
    const wrapper = mount(VPagination, {
      props: { page: 3, size: 10, total: 21 },
    });

    await wrapper.setProps({ total: 20 });

    expect(wrapper.emitted("update:page")?.[0]).toEqual([2]);
    expect(getPageSelect(wrapper).element.value).toBe("2");
  });

  it("does not reset a valid page before the total is loaded", async () => {
    const wrapper = mount(VPagination, {
      props: { page: 3, size: 10, total: 0 },
    });

    await nextTick();

    expect(wrapper.emitted("update:page")).toBeUndefined();
    expect((getPageSelect(wrapper).element as HTMLSelectElement).disabled).toBe(
      true
    );

    await wrapper.setProps({ total: 100 });

    expect(wrapper.emitted("update:page")).toBeUndefined();
    expect(getPageSelect(wrapper).element.value).toBe("3");
  });

  it("normalizes a non-numeric page to the first page", async () => {
    const wrapper = mount(VPagination, {
      props: { page: Number.NaN, size: 10, total: 100 },
    });

    await nextTick();

    expect(wrapper.emitted("update:page")?.[0]).toEqual([1]);
    expect(getPageSelect(wrapper).element.value).toBe("1");
    expect(isDisabled(wrapper, ".pagination__btn--prev")).toBe(true);
    expect(isDisabled(wrapper, ".pagination__btn--next")).toBe(false);
  });

  it("normalizes invalid sizes and keeps the pager usable", async () => {
    for (const size of [0, -1, Number.NaN]) {
      const wrapper = mount(VPagination, {
        props: { page: 1, size, sizeOptions: [20, 30], total: 100 },
      });

      await nextTick();

      expect(wrapper.emitted("update:size")?.[0]).toEqual([20]);
      expect(getSizeSelect(wrapper).element.value).toBe("20");
      expect(getPageSelect(wrapper).findAll("option")).toHaveLength(5);
      expect(isDisabled(wrapper, ".pagination__btn--next")).toBe(false);

      wrapper.unmount();
    }
  });

  it("always includes the current size in the size options", () => {
    const wrapper = mount(VPagination, {
      props: { page: 1, size: 25, total: 100, sizeOptions: [20, 30] },
    });

    expect(
      getSizeSelect(wrapper)
        .findAll("option")
        .map((option) => option.text())
    ).toEqual(["20", "30", "25"]);
    expect(getSizeSelect(wrapper).element.value).toBe("25");
  });

  it("renders a single empty page option when total is zero", () => {
    const wrapper = mount(VPagination, {
      props: { page: 1, size: 10, total: 0 },
    });

    const pageSelect = getPageSelect(wrapper);

    expect(pageSelect.findAll("option")).toHaveLength(1);
    expect(pageSelect.find("option").text()).toBe("0 / 0");
    expect((pageSelect.element as HTMLSelectElement).disabled).toBe(true);
    expect(isDisabled(wrapper, ".pagination__btn--prev")).toBe(true);
    expect(isDisabled(wrapper, ".pagination__btn--next")).toBe(true);
  });

  it("treats an invalid total as zero", () => {
    const wrapper = mount(VPagination, {
      props: { page: 1, size: 10, total: Number.NaN },
    });

    expect(wrapper.get(".pagination__total").text()).toBe("共 0 项数据");
    expect(getPageSelect(wrapper).findAll("option")).toHaveLength(1);
  });
});
