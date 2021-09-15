<template>
  <div ref="codemirrorRef"></div>
</template>
<script>
import { basicSetup } from '@codemirror/basic-setup'
import { EditorView, keymap } from '@codemirror/view'
import { EditorState } from '@codemirror/state'
import { indentWithTab } from '@codemirror/commands'

export default {
  name: 'Codemirror',
  model: {
    prop: 'value',
    event: 'update:value'
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    extensions: {
      type: Array,
      default: () => []
    },
    height: {
      type: String,
      default: '500px'
    }
  },
  data() {
    return {
      codemirrorState: null,
      codemirrorView: null
    }
  },
  mounted() {
    this.handleInitCodemirror()
  },
  beforeDestroy() {
    if (this.codemirrorView) {
      this.codemirrorView.destroy()
    }
  },
  methods: {
    handleInitCodemirror() {
      if (this.codemirrorView) {
        this.codemirrorView.destroy()
      }

      const codemirrorRef = this.$refs.codemirrorRef

      const onUpdateExtension = EditorView.updateListener.of(vu => {
        if (vu.docChanged) {
          const doc = vu.state.doc
          this.$emit('update:value', doc.toString())
        }
      })

      const defaultTheme = EditorView.theme({
        '&': {
          height: this.height
        }
      })

      this.codemirrorState = EditorState.create({
        doc: this.value,
        extensions: [basicSetup, onUpdateExtension, keymap.of([indentWithTab]), defaultTheme, ...this.extensions]
      })

      this.codemirrorView = new EditorView({
        state: this.codemirrorState,
        parent: codemirrorRef
      })
    }
  }
}
</script>
