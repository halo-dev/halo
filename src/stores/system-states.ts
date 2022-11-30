import { defineStore } from "pinia";
import axios from "axios";
import type { ConfigMap } from "@halo-dev/api-client";

interface SystemState {
  isSetup: boolean;
}

interface SystemStatesState {
  states: SystemState;
}

export const useSystemStatesStore = defineStore({
  id: "system-states",
  state: (): SystemStatesState => ({
    states: {
      isSetup: false,
    },
  }),
  actions: {
    async fetchSystemStates() {
      try {
        const { data } = await axios.get<ConfigMap>(
          `${
            import.meta.env.VITE_API_URL
          }/api/v1alpha1/configmaps/system-states`,
          {
            withCredentials: true,
          }
        );

        if (data.data) {
          this.states = JSON.parse(data.data["states"]);
          return;
        }
        this.states.isSetup = false;
      } catch (error) {
        this.states.isSetup = false;
      }
    },
  },
});
