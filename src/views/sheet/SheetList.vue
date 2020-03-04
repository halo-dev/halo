<template>
  <div>
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="internal">
              <span slot="tab">
                <a-icon type="paper-clip" />内置页面
              </span>
              <InternalSheetList />
            </a-tab-pane>
            <a-tab-pane key="custom">
              <span slot="tab">
                <a-icon type="fork" />自定义页面
              </span>
              <CustomSheetList />
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import InternalSheetList from './components/InternalSheetList'
import CustomSheetList from './components/CustomSheetList'

export default {
  components: {
    InternalSheetList,
    CustomSheetList
  }
}
</script>
