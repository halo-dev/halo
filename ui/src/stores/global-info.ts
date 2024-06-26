import type { GlobalInfo } from "@/types";
import axios from "axios";
import { defineStore } from "pinia";
import { ref } from "vue";

export const useGlobalInfoStore = defineStore("global-info", () => {
  const globalInfo = ref<GlobalInfo>();

  async function fetchGlobalInfo() {
    const { data } = await axios.get<GlobalInfo>(`/actuator/globalinfo`, {
      withCredentials: true,
    });
    globalInfo.value = data;
  }

  return { globalInfo, fetchGlobalInfo };
});
