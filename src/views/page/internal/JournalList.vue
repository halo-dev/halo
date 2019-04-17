<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card>
          <a-list
            class="demo-loadmore-list"
            :loading="loading"
            itemLayout="horizontal"
            :dataSource="data"
          >
            <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
              <a slot="actions">edit</a>
              <a slot="actions">more</a>
              <a-list-item-meta :description="item.url">
                <a slot="title" href="https://vue.ant.design/">{{ item.url }}</a>
                <a-avatar
                  slot="avatar"
                  src="https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png"
                />
              </a-list-item-meta>
              <div>content</div>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import postApi from '@/api/post'

export default {
  data() {
    return {
      loading: true,
      data: [],
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        categoryId: null,
        status: null
      }
    }
  },
  created() {
    this.loadPosts()
  },
  methods: {
    loadPosts() {
      postApi.listLatest().then(response => {
        this.data = response.data.data
        this.loading = false
      })
    }
  }
}
</script>
<style>
.demo-loadmore-list {
  min-height: 350px;
}
</style>
