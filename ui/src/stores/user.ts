import type { Role, User } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { defineStore } from "pinia";

interface UserStoreState {
  currentUser?: User;
  currentRoles?: Role[];
  isAnonymous: boolean;
}

export const useUserStore = defineStore("user", {
  state: (): UserStoreState => ({
    currentUser: undefined,
    currentRoles: [],
    isAnonymous: true,
  }),
  actions: {
    async fetchCurrentUser() {
      try {
        const { data } = await consoleApiClient.user.getCurrentUserDetail();
        this.currentUser = data.user;
        this.currentRoles = data.roles;
        this.isAnonymous = data.user.metadata.name === "anonymousUser";
      } catch (e) {
        console.error("Failed to fetch current user", e);
      }
    },
  },
});
