<template>
  <a-card
    :loading="loading"
    :body-style="{ padding: '24px' }"
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
          <countTo
            :startVal="startNumber"
            :endVal="typeof number === 'function' && number() || number"
            :duration="3000"
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
      required: false,
      default: ''
    },
    number: {
      type: Number,
      required: false,
      default: 0
    },
    loading: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      startNumber: 0
    }
  },
  watch: {
    number: function(newValue, oldValue) {
      this.startNumber = oldValue
    }
  }
}
</script>
