import { usePluginModuleStore } from "@/stores/plugin";
import type {
  Extension,
  ListedComment,
  Post,
  SinglePage,
} from "@halo-dev/api-client";
import type {
  CommentSubjectRefProvider,
  CommentSubjectRefResult,
} from "@halo-dev/console-shared";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

export function useSubjectRef(comment: ListedComment) {
  const { t } = useI18n();

  const SubjectRefProviders = ref<CommentSubjectRefProvider[]>([
    {
      kind: "Post",
      group: "content.halo.run",
      resolve: (subject: Extension): CommentSubjectRefResult => {
        const post = subject as Post;
        return {
          label: t("core.comment.subject_refs.post"),
          title: post.spec.title,
          externalUrl: post.status?.permalink,
          route: {
            name: "PostEditor",
            query: {
              name: post.metadata.name,
            },
          },
        };
      },
    },
    {
      kind: "SinglePage",
      group: "content.halo.run",
      resolve: (subject: Extension): CommentSubjectRefResult => {
        const singlePage = subject as SinglePage;
        return {
          label: t("core.comment.subject_refs.page"),
          title: singlePage.spec.title,
          externalUrl: singlePage.status?.permalink,
          route: {
            name: "SinglePageEditor",
            query: {
              name: singlePage.metadata.name,
            },
          },
        };
      },
    },
  ]);

  const { pluginModules } = usePluginModuleStore();

  onMounted(() => {
    for (const pluginModule of pluginModules) {
      const callbackFunction =
        pluginModule?.extensionPoints?.["comment:subject-ref:create"];

      if (typeof callbackFunction !== "function") {
        continue;
      }

      const providers = callbackFunction();

      SubjectRefProviders.value.push(...providers);
    }
  });

  const subjectRefResult = computed(() => {
    const { subject } = comment;
    if (!subject) {
      return {
        label: t("core.comment.subject_refs.unknown"),
        title: t("core.comment.subject_refs.unknown"),
      };
    }
    const subjectRef = SubjectRefProviders.value.find(
      (provider) =>
        provider.kind === subject.kind &&
        subject.apiVersion.startsWith(provider.group)
    );
    if (!subjectRef) {
      return {
        label: t("core.comment.subject_refs.unknown"),
        title: t("core.comment.subject_refs.unknown"),
      };
    }
    return subjectRef.resolve(subject);
  });

  return {
    subjectRefResult,
  };
}
