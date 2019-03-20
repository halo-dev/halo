<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="12" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card :loading="loading" :bordered="false" title="最新文章" :bodyStyle="{ padding: '0px' }">
          <a-table :columns="postColumns" :dataSource="postData" :pagination="false">

          </a-table>
        </a-card>
      </a-col>
      <a-col :xl="12" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card :loading="loading" :bordered="false" title="最新评论" :bodyStyle="{ padding: '0px' }">
          <a-table :columns="commentColumns" :dataSource="commentData" :pagination="false">

          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>

const postColumns = [{
  title: '标题',
  dataIndex: 'title',
  scopedSlots: { customRender: 'name' }
}, {
  title: '状态',
  className: 'status',
  dataIndex: 'status'
}, {
  title: '发布时间',
  dataIndex: 'date'
}]

const commentColumns = [{
  title: '评论者',
  dataIndex: 'author',
  scopedSlots: { customRender: 'name' }
}, {
  title: '状态',
  className: 'status',
  dataIndex: 'status'
}, {
  title: '内容',
  className: 'content',
  dataIndex: 'content'
}, {
  title: '发布时间',
  dataIndex: 'date'
}]

const postData = [{
  key: '1',
  title: '测试文章1',
  status: '已发布',
  date: '2019-03-20'
}, {
  key: '2',
  title: '测试文章3',
  status: '草稿',
  date: '2019-03-20'
}, {
  key: '3',
  title: '测试文章3',
  status: '回收站',
  date: '2019-03-20'
}]

const commentData = [{
  key: '1',
  author: 'test1',
  status: '已发布',
  content: '测试评论1',
  date: '2019-03-20'
}, {
  key: '2',
  author: 'test1',
  status: '草稿',
  content: '测试评论1',
  date: '2019-03-20'
}, {
  key: '3',
  author: 'test1',
  status: '回收站',
  content: '测试评论1',
  date: '2019-03-20'
}]

export default {
  name: 'Dashboard',
  components: {
  },
  data () {
    return {
      loading: true,
      postColumns,
      postData,
      commentColumns,
      commentData
    }
  },
  created () {
    setTimeout(() => {
      this.loading = !this.loading
    }, 1000)
  }
}
</script>

<style lang="less" scoped>

</style>
