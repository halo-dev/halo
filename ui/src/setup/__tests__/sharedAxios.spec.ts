import { axiosInstance } from "@halo-dev/api-client";
import axios from "axios";
import { describe, expect, it } from "vite-plus/test";

describe("shared Axios isolation", () => {
  it("keeps axios.create clients isolated from Halo's API instance", () => {
    const customClient = axios.create({ baseURL: "https://example.test" });
    customClient.defaults.headers.common["X-Custom-Client"] = "custom";

    expect(customClient).not.toBe(axiosInstance);
    expect(customClient.defaults.baseURL).toBe("https://example.test");
    expect(axiosInstance.defaults.baseURL).toBe("");
    expect(axiosInstance.defaults.headers.common["X-Custom-Client"]).toBe(
      undefined
    );
    expect(axiosInstance.defaults.headers.common["X-Requested-With"]).toBe(
      "XMLHttpRequest"
    );
  });
});
