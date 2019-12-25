<template>
  <div>
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="environment">
              <span slot="tab">
                <a-icon type="folder" />文件列表
              </span>
            </a-tab-pane>
            <a-tab-pane key="runtimeLogs">
              <span slot="tab">
                <a-icon type="appstore" />部署平台
              </span>
            </a-tab-pane>
            <a-tab-pane key="optionsList">
              <span slot="tab">
                <a-icon type="setting" />配置
              </span>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script>
export default {
  name: 'StaticPagesManage'
}
</script>
