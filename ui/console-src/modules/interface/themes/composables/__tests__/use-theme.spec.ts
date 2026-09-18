import { consoleApiClient, type Theme } from "@halo-dev/api-client";
import { Dialog } from "@halo-dev/components";
import { afterEach, expect, it, vi } from "vite-plus/test";
import { ref } from "vue";
import { useThemeLifeCycle } from "../use-theme";

vi.mock("@console/composables/use-activated-theme", () => ({
  useActivatedTheme: () => ({ data: ref(null) }),
  invalidateThemeQueries: vi.fn(),
}));
vi.mock("@tanstack/vue-query", () => ({ useQueryClient: vi.fn() }));
vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("@halo-dev/components", () => ({
  Dialog: { info: vi.fn() },
  Toast: { success: vi.fn() },
}));
vi.mock("@halo-dev/api-client", () => ({
  consoleApiClient: { theme: { theme: { activateTheme: vi.fn() } } },
}));

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  vi.clearAllMocks();
});

it.each([true, false])(
  "updates the selected theme before reloading only after successful activation: %s",
  async (success) => {
    const url = new URL(
      "http://localhost/console/theme/settings/style?theme=old&other=value#field"
    );
    const historyState = { position: 1 };
    const replaceState = vi.fn((_state, _title, value) => {
      url.href = String(value);
    });
    const reload = vi.fn(() => {
      expect(url.searchParams.get("theme")).toBe("new");
    });
    vi.stubGlobal("window", {
      location: { href: url.href, reload },
      history: { state: historyState, replaceState },
    });
    const error = vi.spyOn(console, "error").mockImplementation(() => {});
    const activate = vi.mocked(consoleApiClient.theme.theme.activateTheme);
    if (success) activate.mockResolvedValue({} as never);
    else activate.mockRejectedValue(new Error("activation failed"));

    const { handleActiveTheme } = useThemeLifeCycle(
      ref({ metadata: { name: "new" }, spec: {} } as Theme)
    );
    handleActiveTheme(true);
    expect(reload).not.toHaveBeenCalled();
    await vi.mocked(Dialog.info).mock.calls[0][0].onConfirm?.();

    expect(activate).toHaveBeenCalledWith({ name: "new" });
    expect(reload).toHaveBeenCalledTimes(success ? 1 : 0);
    expect(replaceState).toHaveBeenCalledTimes(success ? 1 : 0);
    expect(url.pathname).toBe("/console/theme/settings/style");
    expect(url.searchParams.get("other")).toBe("value");
    expect(url.hash).toBe("#field");
    expect(error).toHaveBeenCalledTimes(success ? 0 : 1);
  }
);
