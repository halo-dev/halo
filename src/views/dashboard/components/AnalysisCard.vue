<template>
  <a-card
    :loading="loading"
    :body-style="{ padding: '8px 24px 8px' }"
    :bordered="false"
  >
    <div class="card-main">
      <div class="number">
        <slot name="number">
          <span>{{ number }}</span>
        </slot>
      </div>
      <div class="title">
        <slot name="title">
          <span>{{ title }}</span>
        </slot>
      </div>
    </div>
  </a-card>
</template>

<script>
export default {
  name: 'AnalysisCard',
  props: {
    title: {
      type: String,
      default: ''
    },
    number: {
      type: [Function, Number, String],
      required: false,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style lang="less" scoped>
.card-main {
  .number {
    color: #000;
    margin-bottom: 0;
    font-size: 38px;
    line-height: 38px;
    height: 38px;
  }
}
</style>