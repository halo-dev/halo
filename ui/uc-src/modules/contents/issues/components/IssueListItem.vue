<script lang="ts" setup>
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import {
  VEntity,
  VEntityField,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { computed } from "vue";

interface Issue {
  metadata: {
    name: string;
    creationTimestamp: string;
  };
  spec: {
    title: string;
    description: string;
    status: string;
    priority: string;
    type: string;
  };
}

interface ListedIssue {
  issue: Issue;
  stats: {
    commentsCount: number;
    upvotesCount: number;
  };
}

const props = withDefaults(
  defineProps<{
    issue: ListedIssue;
  }>(),
  {}
);

const statusColor = computed(() => {
  switch (props.issue.issue.spec.status) {
    case "OPEN":
      return "green";
    case "IN_PROGRESS":
      return "yellow";
    case "RESOLVED":
      return "blue";
    case "CLOSED":
      return "gray";
    default:
      return "gray";
  }
});

const priorityColor = computed(() => {
  switch (props.issue.issue.spec.priority) {
    case "CRITICAL":
      return "red";
    case "HIGH":
      return "orange";
    case "MEDIUM":
      return "yellow";
    case "LOW":
      return "green";
    default:
      return "gray";
  }
});

const typeColor = computed(() => {
  switch (props.issue.issue.spec.type) {
    case "BUG":
      return "red";
    case "FEATURE_REQUEST":
      return "blue";
    case "IMPROVEMENT":
      return "green";
    case "QUESTION":
      return "purple";
    default:
      return "gray";
  }
});
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField
        :title="issue.issue.spec.title"
        :description="issue.issue.spec.description"
        :route="{
          name: 'IssueDetail',
          params: { name: issue.issue.metadata.name },
        }"
      >
        <template #extra>
          <VSpace class="mt-1 sm:mt-0">
            <VTag :color="statusColor" variant="outline" size="sm">
              {{ $t(`core.issue.status.${issue.issue.spec.status.toLowerCase()}`) }}
            </VTag>
            <VTag :color="priorityColor" variant="outline" size="sm">
              {{ $t(`core.issue.priority.${issue.issue.spec.priority.toLowerCase()}`) }}
            </VTag>
            <VTag :color="typeColor" variant="outline" size="sm">
              {{ $t(`core.issue.type.${issue.issue.spec.type.toLowerCase()}`) }}
            </VTag>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField>
        <template #description>
          <div class="flex items-center gap-2 text-xs text-gray-500">
            <span>{{ formatDatetime(issue.issue.metadata.creationTimestamp) }}</span>
            <span>•</span>
            <span>{{ relativeTimeTo(issue.issue.metadata.creationTimestamp) }}</span>
          </div>
        </template>
      </VEntityField>
    </template>
  </VEntity>
</template>