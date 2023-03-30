import type {
  GlobalInfo,
  SocialAuthProvider,
} from "@/modules/system/actuator/types";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";

export function useAuthProvidersFetch() {
  const { data: socialAuthProviders } = useQuery<SocialAuthProvider[]>({
    queryKey: ["social-auth-providers"],
    queryFn: async () => {
      const { data } = await axios.get<GlobalInfo>(
        `${import.meta.env.VITE_API_URL}/actuator/globalinfo`,
        {
          withCredentials: true,
        }
      );

      return data.socialAuthProviders;
    },
    refetchOnWindowFocus: false,
  });

  return {
    socialAuthProviders,
  };
}
