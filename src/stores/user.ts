import { apiClient } from "@/utils/api-client";
import type { User } from "@halo-dev/api-client";
import { defineStore } from "pinia";

interface UserStoreState {
  currentUser?: User;
  isAnonymous: boolean;
  loginModalVisible: boolean;
}

export const useUserStore = defineStore("user", {
  state: (): UserStoreState => ({
    currentUser: undefined,
    isAnonymous: true,
    loginModalVisible: false,
  }),
  actions: {
    async fetchCurrentUser() {
      try {
        const { data } = await apiClient.user.getCurrentUserDetail();
        this.currentUser = data;
        this.isAnonymous = data.metadata.name === "anonymousUser";
      } catch (e) {
        console.error("Failed to fetch current user", e);
      }
    },
  },
});
