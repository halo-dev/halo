import { slugify } from "transliteration";
import { watch, type Ref } from "vue";
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

  const handleGenerateSlug = () => {
    if (!source.value) {
      return;
    }
    target.value = slugify(source.value, {
      trim: true,
    });
  };

  return {
    handleGenerateSlug,
  };
}
