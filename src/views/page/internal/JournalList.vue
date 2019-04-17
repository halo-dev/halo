<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col :md="6" :sm="24">
                  <a-form-item label="关键词">
                    <a-input/>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="年月份">
                    <a-select placeholder="请选择年月">
                      <a-select-option value="2019-01">2019-01</a-select-option>
                      <a-select-option value="2019-02">2019-02</a-select-option>
                      <a-select-option value="2019-03">2019-03</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="状态">
                    <a-select placeholder="请选择状态">
                      <a-select-option value="1">公开</a-select-option>
                      <a-select-option value="0">私密</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <span class="table-page-search-submitButtons">
                    <a-button type="primary">查询</a-button>
                    <a-button style="margin-left: 8px;">重置</a-button>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div class="table-operator">
            <a-button type="primary" icon="plus" @click="handleNew">写日志</a-button>
          </div>
          <a-divider/>
          <div style="margin-top:15px">
            <a-list itemLayout="vertical" :pagination="pagination" :dataSource="listData">
              <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
                <a slot="actions" @click="handleEdit(item)">编辑</a>
                <a slot="actions">删除</a>
                <a-list-item-meta :description="item.description">
                  <a slot="title" :href="item.href">{{ item.title }}</a>
                  <a-avatar slot="avatar" :src="item.avatar" size="large"/>
                </a-list-item-meta>
                {{ item.content }}
              </a-list-item>
            </a-list>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-modal title="新建" v-model="visible">
      <a-form layout="vertical">
        <a-form-item label="标题：">
          <a-input v-model="journal.title"/>
        </a-form-item>
        <a-form-item label="内容：">
          <a-input
            type="textarea"
            :autosize="{ minRows: 5 }"
            v-model="journal.content"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
const listData = []
for (let i = 0; i < 50; i++) {
  listData.push({
    href: '#',
    title: `Title ${i}`,
    avatar: 'https://gravatar.loli.net/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm',
    description: '2019-04-12 09:00:00',
    content:
      '这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志,这是一条日志'
  })
}

export default {
  data() {
    return {
      listData,
      visible: false,
      pagination: {
        onChange: page => {
          console.log(page)
        },
        pageSize: 10
      },
      actions: [{ type: 'star-o', text: '156' }, { type: 'like-o', text: '156' }, { type: 'message', text: '2' }],
      journal: {}
    }
  },
  methods: {
    handleNew() {
      this.visible = true
    },
    handleEdit(item) {
      this.journal = item
      this.visible = true
    }
  }
}
</script>
<style>
</style>
