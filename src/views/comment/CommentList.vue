<template>
  <page-view>
    <div class="card-container">
      <a-tabs type="card">
        <a-tab-pane key="1" tab="文章">
          <comment-tab target="post"></comment-tab>
        </a-tab-pane>
        <a-tab-pane key="2" tab="页面">
          <comment-tab target="sheet"></comment-tab>
        </a-tab-pane>
      </a-tabs>
    </div>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import CommentTab from './components/CommentTab'

export default {
  components: {
    PageView,
    CommentTab
  }
}
</script>
