import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { VDatePickerPanel } from "../../components/date-picker";
import { VPagination } from "../../components/pagination";
import {
  resolveComponentsLocale,
  setComponentsLocale,
  translate,
} from "../index";

afterEach(() => {
  setComponentsLocale("en");
});

describe("components locales", () => {
  it("resolves locale aliases", () => {
    expect(resolveComponentsLocale("zh")).toBe("zh-CN");
    expect(resolveComponentsLocale("zh-Hans")).toBe("zh-CN");
    expect(resolveComponentsLocale("zh-Hant")).toBe("zh-TW");
    expect(resolveComponentsLocale("zh-HK")).toBe("zh-TW");
    expect(resolveComponentsLocale("es-ES")).toBe("es");
    expect(resolveComponentsLocale("fr")).toBe("en");
    expect(resolveComponentsLocale(undefined)).toBe("en");
  });

  it("interpolates params and falls back to the key", () => {
    setComponentsLocale("zh-CN");
    expect(translate("pagination.total", { total: 12 })).toBe("共 12 项数据");
    expect(translate("unknown.key")).toBe("unknown.key");
  });

  it("updates built-in copy when the locale changes", async () => {
    const wrapper = mount(VPagination, {
      props: { page: 1, total: 10 },
    });

    expect(wrapper.get(".pagination__btn--prev").attributes("aria-label")).toBe(
      "Previous page"
    );

    setComponentsLocale("zh-CN");
    await wrapper.vm.$nextTick();

    expect(wrapper.get(".pagination__btn--prev").attributes("aria-label")).toBe(
      "上一页"
    );
    expect(wrapper.get(".pagination__total").text()).toBe("共 10 项数据");
  });

  it("formats calendar text with the components locale", async () => {
    setComponentsLocale("zh-CN");
    const wrapper = mount(VDatePickerPanel, {
      props: { modelValue: "2026-11-23" },
    });

    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain("2026年11月");
  });
});
