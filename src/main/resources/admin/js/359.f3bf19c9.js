"use strict";(self["webpackChunkhalo_admin"]=self["webpackChunkhalo_admin"]||[]).push([[359],{79359:function(t,e,a){a.r(e),a.d(e,{default:function(){return h}});var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("page-view",[a("a-card",{attrs:{bodyStyle:{padding:"16px"},bordered:!1}},[a("div",{staticClass:"table-operator"},[a("a-button",{attrs:{type:"danger"},on:{click:t.handleClearActionLogs}},[t._v("清空操作日志")])],1),a("div",{staticClass:"mt-4"},[a("a-table",{attrs:{columns:t.list.columns,dataSource:t.list.data,loading:t.list.loading,pagination:!1,rowKey:function(t){return t.id},scrollToFirstRowOnChange:!0},scopedSlots:t._u([{key:"type",fn:function(e){return[t._v(" "+t._s(t._f("typeConvert")(e))+" ")]}},{key:"ipAddress",fn:function(e){return[a("div",{staticClass:"blur hover:blur-none transition-all"},[t._v(t._s(e))])]}},{key:"createTime",fn:function(e){return[a("a-tooltip",{attrs:{placement:"top"}},[a("template",{slot:"title"},[t._v(" "+t._s(t._f("moment")(e))+" ")]),t._v(" "+t._s(t._f("timeAgo")(e))+" ")],2)]}}])}),a("div",{staticClass:"page-wrapper"},[a("a-pagination",{staticClass:"pagination",attrs:{current:t.pagination.page,defaultPageSize:t.pagination.size,pageSizeOptions:["10","20","50","100"],total:t.pagination.total,showLessItems:"",showSizeChanger:""},on:{change:t.handlePageChange,showSizeChange:t.handlePageSizeChange}})],1)],1)])],1)},i=[],s=a(46519),r=(a(41479),a(70315),a(71101)),o=a(18608),l=a(84707),c=[{title:"ID",dataIndex:"id"},{title:"类型",dataIndex:"type",scopedSlots:{customRender:"type"}},{title:"关键值",dataIndex:"logKey"},{title:"内容",dataIndex:"content"},{title:"IP",dataIndex:"ipAddress",scopedSlots:{customRender:"ipAddress"}},{title:"操作时间",dataIndex:"createTime",scopedSlots:{customRender:"createTime"}}],d={name:"ActionLog",components:{PageView:r.B4},data:function(){return{list:{columns:c,data:[],total:0,loading:!1,params:{page:0,size:50}}}},computed:{pagination:function(){return{page:this.list.params.page+1,size:this.list.params.size,total:this.list.total}}},created:function(){this.handleListActionLogs()},methods:{handleListActionLogs:function(){var t=this;return(0,s.Z)(regeneratorRuntime.mark((function e(){var a;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.prev=0,t.list.loading=!0,e.next=4,o.Z.log.list(t.list.params);case 4:a=e.sent,t.list.data=a.data.content,t.list.total=a.data.total,e.next=12;break;case 9:e.prev=9,e.t0=e["catch"](0),t.$log.error(e.t0);case 12:return e.prev=12,t.list.loading=!1,e.finish(12);case 15:case"end":return e.stop()}}),e,null,[[0,9,12,15]])})))()},handlePageChange:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:1;this.list.params.page=t-1,this.handleListActionLogs()},handlePageSizeChange:function(t,e){this.$log.debug("Current: ".concat(t,", PageSize: ").concat(e)),this.list.params.page=0,this.list.params.size=e,this.handleListActionLogs()},handleClearActionLogs:function(){var t=this;t.$confirm({title:"提示",maskClosable:!0,content:"是否确定要清空所有操作日志？",onOk:function(){return(0,s.Z)(regeneratorRuntime.mark((function e(){return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,o.Z.log.clear();case 3:e.next=8;break;case 5:e.prev=5,e.t0=e["catch"](0),t.$log.error("Failed to clear action logs.",e.t0);case 8:return e.prev=8,e.next=11,t.handleListActionLogs();case 11:return e.finish(8);case 12:case"end":return e.stop()}}),e,null,[[0,5,8,12]])})))()}})}},filters:{typeConvert:function(t){var e=l.Js[t];return e?e.text:t}}},u=d,p=a(42177),g=(0,p.Z)(u,n,i,!1,null,null,null),h=g.exports}}]);