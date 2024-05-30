import type { GlobalInfo } from "@/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export function useGlobalInfoFetch() {
  const { data } = useQuery<GlobalInfo>({
    queryKey: ["globalinfo"],
    queryFn: async () => {
      const { data } = await axios.get<GlobalInfo>(`/actuator/globalinfo`, {
        withCredentials: true,
      });

      return data;
    },
  });

  return {
    globalInfo: data,
  };
}
