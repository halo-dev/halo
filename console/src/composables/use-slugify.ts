import { slugify } from "transliteration";
import { watch, type Ref } from "vue";
import ShortUniqueId from "short-unique-id";
import type { ModeType } from "@/types/slug";
import { useGlobalInfoStore } from "@/stores/global-info";
const uid = new ShortUniqueId();
const Strategy = {
  generateByTitle: (value: string) => {
    if (!value) return "";
    return slugify(value, { trim: true });
  },
  shortUUID: (value: string) => {
    if (!value) return "";
    return uid.randomUUID(8);
  },
  UUID: (value: string) => {
    if (!value) return "";
    return uid.randomUUID(16);
  },
  timestamp: (value: string) => {
    if (!value) return "";
    return new Date().getTime().toString();
  },
};
type FormType = "TAGS" | "CATEGORIES" | "POSTS";
const onceList = ["shortUUID", "UUID", "timestamp"];
export default function useSlugify(
  source: Ref<string>,
  target: Ref<string>,
  auto: Ref<boolean>,
  formType: FormType
) {
  watch(
    () => source.value,
    () => {
      if (auto.value) {
        if (formType != "POSTS") {
          handleGenerateSlug(false, true);
        } else {
          handleGenerateSlug();
        }
      }
    }
  );

  const handleGenerateSlug = (
    forceUpdate = false,
    onlyGenerateBytitle = false
  ) => {
    const globalInfoStore = useGlobalInfoStore();
    const mode: ModeType = globalInfoStore.globalInfo
      ?.postSlugGenerationStrategy as ModeType;
    if (onlyGenerateBytitle) {
      target.value = Strategy["generateByTitle"](source.value);
    }
    if (forceUpdate && !onlyGenerateBytitle) {
      target.value = Strategy[mode](source.value);
      return;
    }
    if (onceList.includes(mode) && target.value) return;
    target.value = Strategy[mode](source.value);
  };

  return {
    handleGenerateSlug,
  };
}
