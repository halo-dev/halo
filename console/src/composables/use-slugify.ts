import { slugify } from "transliteration";
import { watch, type Ref } from "vue";
import ShortUniqueId from "short-unique-id";
import type { ModeType } from "@/types/slug";
import { useGlobalInfoStore } from "@/stores/global-info";
const uid = new ShortUniqueId();

const globalInfoStore = useGlobalInfoStore();

const Strategy = {
  generateByTitle: (value: string) => {
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
    return uid.stamp(32);
  },
};

const onceList = ["shortUUID", "UUID", "timestamp"];
export default function useSlugify(
  source: Ref<string>,
  target: Ref<string>,
  auto: Ref<boolean>
) {
  watch(
    () => source.value,
    () => {
      if (auto.value) {
        handleGenerateSlug();
      }
    }
  );

  const handleGenerateSlug = (forceUpdate = false) => {
    const mode: ModeType =
      globalInfoStore.globalInfo.postSlugGenerationStrategy;
    if (forceUpdate) {
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
