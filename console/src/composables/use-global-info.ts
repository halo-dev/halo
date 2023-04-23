import type { GlobalInfo } from "@/modules/system/actuator/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export function useGlobalInfoFetch() {
  const { data } = useQuery<GlobalInfo>({
    queryKey: ["globalinfo"],
    queryFn: async () => {
      const { data } = await axios.get<GlobalInfo>(
        `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
        {
          withCredentials: true,
        }
      );

      return data;
    },
  });

  return {
    globalInfo: data,
  };
}
