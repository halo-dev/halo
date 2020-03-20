<template>
  <div>
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="independent">
              <span slot="tab">
                <a-icon type="paper-clip" />独立页面
              </span>
              <IndependentSheetList />
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
import IndependentSheetList from './components/IndependentSheetList'
import CustomSheetList from './components/CustomSheetList'

export default {
  components: {
    IndependentSheetList,
    CustomSheetList
  }
}
</script>
