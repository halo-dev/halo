import {
  axiosInstance,
  consoleApiClient,
  type DetailedUser,
} from "@halo-dev/api-client";
import { defineStore } from "pinia";
import { ref } from "vue";
import type { GlobalInfo } from "./types";

/**
 * Collection of Pinia stores for shared application state.
 *
 * @remarks
 * These stores provide centralized state management for common data
 * that needs to be accessed across multiple components and plugins.
 */
export const stores = {
  /**
   * Store for managing the current authenticated user's information.
   *
   * @remarks
   * This store provides access to the current user's details and authentication state.
   * It includes helper methods to fetch the latest user information from the server.
   *
   * @example
   * ```typescript
   * import { stores } from "@halo-dev/console-shared";
   *
   * const userStore = stores.currentUser();
   *
   * // Fetch current user info
   * await userStore.fetchCurrentUser();
   *
   * // Access user data
   * console.log(userStore.currentUser?.user.metadata.name);
   * console.log(userStore.isAnonymous); // Check if user is anonymous
   * ```
   */
  currentUser: defineStore("currentUser", () => {
    /**
     * The current authenticated user's detailed information.
     * Will be `undefined` until `fetchCurrentUser` is called.
     */
    const currentUser = ref<DetailedUser>();

    /**
     * Indicates whether the current user is anonymous (not authenticated).
     * `true` if the user is "anonymousUser", `false` otherwise.
     */
    const isAnonymous = ref(false);

    /**
     * Fetches the current user's information from the server.
     * Updates both `currentUser` and `isAnonymous` reactive references.
     *
     * @throws Will throw an error if the API request fails or user is not authenticated.
     */
    async function fetchCurrentUser() {
      const { data } = await consoleApiClient.user.getCurrentUserDetail();
      currentUser.value = data;
      isAnonymous.value = data.user.metadata.name === "anonymousUser";
    }

    return { currentUser, isAnonymous, fetchCurrentUser };
  }),

  /**
   * Store for managing global system information and configuration.
   *
   * @remarks
   * This store provides access to global system settings, configuration,
   * and metadata that are shared across the entire application.
   *
   * @example
   * ```typescript
   * import { stores } from "@halo-dev/console-shared";
   *
   * const globalInfoStore = stores.globalInfo();
   *
   * // Fetch global info
   * await globalInfoStore.fetchGlobalInfo();
   *
   * // Access global settings
   * console.log(globalInfoStore.globalInfo?.externalUrl);
   * console.log(globalInfoStore.globalInfo?.siteTitle);
   * console.log(globalInfoStore.globalInfo?.allowRegistration);
   * ```
   */
  globalInfo: defineStore("global-info", () => {
    /**
     * The global system information and configuration.
     * Will be `undefined` until `fetchGlobalInfo` is called.
     */
    const globalInfo = ref<GlobalInfo>();

    /**
     * Fetches the global system information from the actuator endpoint.
     * Updates the `globalInfo` reactive reference with the latest data.
     *
     * @throws Will throw an error if the API request fails.
     */
    async function fetchGlobalInfo() {
      const { data } = await axiosInstance.get<GlobalInfo>(
        `/actuator/globalinfo`,
        {
          withCredentials: true,
        }
      );
      globalInfo.value = data;
    }

    return { globalInfo, fetchGlobalInfo };
  }),
};

export * from "./types";
