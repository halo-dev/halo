<template>
  <a-button
    :type="computedType"
    @click="handleClick"
    :icon="computedIcon"
    :loading="loading"
    :size="size"
    :block="block"
    >{{ computedText }}</a-button
  >
</template>
<script>
export default {
  name: 'ReactiveButton',
  props: {
    type: {
      type: String,
      default: 'primary'
    },
    icon: {
      type: String,
      default: null
    },
    size: {
      type: String,
      default: 'default'
    },
    block: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    errored: {
      type: Boolean,
      default: false
    },
    text: {
      type: String,
      default: ''
    },
    loadedText: {
      type: String,
      default: ''
    },
    erroredText: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loaded: false,
      hasError: false
    }
  },
  watch: {
    loading(value) {
      if (!value) {
        this.loaded = true
        if (this.errored) {
          this.hasError = true
        }
        setTimeout(() => {
          this.loaded = false
          this.hasError = false
          this.$emit('callback')
        }, 400)
      }
    }
  },
  computed: {
    computedType() {
      if (this.loaded) {
        return this.hasError ? 'danger' : this.type
      }
      return this.type
    },
    computedIcon() {
      if (this.loaded) {
        return this.hasError ? 'close-circle' : 'check-circle'
      }
      return this.icon
    },
    computedText() {
      if (this.loaded) {
        return this.hasError ? this.erroredText : this.loadedText
      }
      return this.text
    }
  },
  methods: {
    handleClick() {
      this.$emit('click')
    }
  }
}
</script>
