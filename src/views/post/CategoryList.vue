<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="10" :lg="10" :md="10" :sm="24" :xs="24">
        <a-card title="添加分类目录">
          <a-form layout="horizontal">
            <a-form-item label="名称：" help="*页面上所显示的名称">
              <a-input />
            </a-form-item>
            <a-form-item label="路径名称：" help="*这是文章路径上显示的名称，最好为英文">
              <a-input />
            </a-form-item>
            <a-form-item label="上级目录：">
              <a-select>
                <a-select-option value="1">上级目录</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="描述：" help="*分类描述，部分主题可显示">
              <a-input type="textarea" :autosize="{ minRows: 3 }" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col :xl="14" :lg="14" :md="14" :sm="24" :xs="24">
        <a-card title="所有分类">
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
    dataIndex: 'slugName'
  },
  {
    title: '描述',
    dataIndex: 'description'
  },
  {
    title: '文章数',
    dataIndex: 'posts.count'
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
