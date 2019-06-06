<template>
  <a-card
    :loading="loading"
    :body-style="{ padding: '18px 24px 18px' }"
    :bordered="false"
  >
    <div class="analysis-card-container">
      <div class="meta">
        <span class="analysis-card-title">
          <slot name="title">{{ title }}</slot>
        </span>
        <span class="analysis-card-action">
          <slot name="action"></slot>
        </span>
      </div>
      <div class="number">
        <slot name="number">
          <!-- <span>{{ typeof number === 'function' && number() || number }}</span> -->
          <countTo
            :startVal="0"
            :endVal="typeof number === 'function' && number() || number"
            :duration="4000"
            :autoplay="true"
          ></countTo>
        </slot>
      </div>
    </div>
  </a-card>
</template>

<script>
import countTo from 'vue-count-to'
export default {
  name: 'AnalysisCard',
  components: {
    countTo
  },
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
.analysis-card-container {
  position: relative;
  overflow: hidden;
  width: 100%;
  .meta {
    position: relative;
    overflow: hidden;
    width: 100%;
    color: rgba(0, 0, 0, 0.45);
    font-size: 14px;
    line-height: 22px;
    .analysis-card-action {
      cursor: pointer;
      position: absolute;
      top: 0;
      right: 0;
    }
  }
}
.number {
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-all;
  white-space: nowrap;
  color: #000;
  margin-top: 4px;
  margin-bottom: 0;
  font-size: 32px;
  line-height: 38px;
  height: 38px;
}
</style>
