import { PluginStatusPhaseEnum } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import { getPluginStatusRefetchInterval } from "../use-plugin";

describe("getPluginStatusRefetchInterval", () => {
  it.each([
    [1000, true, PluginStatusPhaseEnum.Pending],
    [1000, true, PluginStatusPhaseEnum.Starting],
    [false, true, PluginStatusPhaseEnum.Started],
    [false, true, PluginStatusPhaseEnum.Failed],
    [false, false, PluginStatusPhaseEnum.Starting],
  ])(
    "returns %s when enabled is %s and phase is %s",
    (expected, enabled, phase) => {
      expect(getPluginStatusRefetchInterval(enabled, phase)).toBe(expected);
    }
  );
});
