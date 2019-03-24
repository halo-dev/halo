<template>
  <div>
    <a-row :gutter="12">
      <a-col :xl="10" :lg="10" :md="10" :sm="24" :xs="24">
        <a-card hoverable title="添加标签">
          <a-form :layout="horizontal">
            <a-form-item label="名称：" help="*页面上所显示的名称">
              <a-input/>
            </a-form-item>
            <a-form-item label="路径名称：" help="*这是文章路径上显示的名称，最好为英文">
              <a-input/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col :xl="14" :lg="14" :md="14" :sm="24" :xs="24">
        <a-card hoverable title="所有标签">
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>10 篇文章</span>
            </template>
            <a-tag color="pink">Java</a-tag>
          </a-tooltip>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>10 篇文章</span>
            </template>
            <a-tag color="red">PHP</a-tag>
          </a-tooltip>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>10 篇文章</span>
            </template>
            <a-tag color="orange">.NET</a-tag>
          </a-tooltip>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>10 篇文章</span>
            </template>
            <a-tag color="green">Python</a-tag>
          </a-tooltip>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>10 篇文章</span>
            </template>
            <a-tag color="pink">Golang</a-tag>
          </a-tooltip>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
export default {}
</script>

<style scoped>
</style>
