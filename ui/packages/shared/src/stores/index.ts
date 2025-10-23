import {
  axiosInstance,
  consoleApiClient,
  type DetailedUser,
} from "@halo-dev/api-client";
import { defineStore } from "pinia";
import { ref } from "vue";
import type { GlobalInfo } from "./types";

export const stores = {
  currentUser: defineStore("currentUser", () => {
    const currentUser = ref<DetailedUser>();
    const isAnonymous = ref(false);

    async function fetchCurrentUser() {
      const { data } = await consoleApiClient.user.getCurrentUserDetail();
      currentUser.value = data;
      isAnonymous.value = data.user.metadata.name === "anonymousUser";
    }

    return { currentUser, isAnonymous, fetchCurrentUser };
  }),
  globalInfo: defineStore("global-info", () => {
    const globalInfo = ref<GlobalInfo>();

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
