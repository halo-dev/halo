<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }">
        <a-card title="添加菜单">
          <a-form layout="horizontal">
            <a-form-item label="名称：" help="*页面上所显示的名称">
              <a-input />
            </a-form-item>
            <a-form-item label="路径：" help="*菜单的路径">
              <a-input />
            </a-form-item>
            <a-form-item label="上级菜单：">
              <a-select>
                <a-select-option value="1">上级菜单</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="排序编号：">
              <a-input type="number" />
            </a-form-item>
            <a-form-item label="图标：" help="*请根据主题的支持选填">
              <a-input />
            </a-form-item>
            <a-form-item label="打开方式：">
              <a-select defaultValue="_self">
                <a-select-option value="_self">当前窗口</a-select-option>
                <a-select-option value="_blank">新窗口</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-button type="primary">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col
        :xl="14"
        :lg="14"
        :md="14"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }">
        <a-card title="所有菜单">
          <a-table :columns="columns" :dataSource="data" :loading="loading"> </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
const columns = [
  {
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '路径',
    dataIndex: 'url'
  },
  {
    title: '排序',
    dataIndex: 'sort'
  },
  {
    title: '图标',
    dataIndex: 'icon'
  }
]
export default {
  data() {
    return {
      data: [],
      loading: false,
      columns
    }
  }
}
</script>

<style scoped>
</style>
