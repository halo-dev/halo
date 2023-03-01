import { defineStore } from "pinia";
import { apiClient } from "@/utils/api-client";

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
        const { data } =
          await apiClient.extension.configMap.getv1alpha1ConfigMap(
            {
              name: "system-states",
            },
            { mute: true }
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
