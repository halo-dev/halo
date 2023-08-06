import { defineStore } from "pinia";
import type { GlobalInfo } from "@/types";
import { ref } from "vue";
import axios from "axios";

export const useGlobalInfoStore = defineStore("global-info", () => {
  const globalInfo = ref<GlobalInfo>();

  async function fetchGlobalInfo() {
    const { data } = await axios.get<GlobalInfo>(
      `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
      {
        withCredentials: true,
      }
    );
    globalInfo.value = data;
  }

  return { globalInfo, fetchGlobalInfo };
});
