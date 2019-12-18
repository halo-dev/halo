(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-2d21a35c"],{bb17:function(t,e,a){"use strict";a.r(e);var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("a-row",{attrs:{gutter:12}},[a("a-col",{style:{"padding-bottom":"12px"},attrs:{xl:10,lg:10,md:10,sm:24,xs:24}},[a("a-card",{attrs:{title:t.title,bodyStyle:{padding:"16px"}}},[a("a-form",{attrs:{layout:"horizontal"}},[a("a-form-item",{attrs:{label:"网站名称："}},[a("a-input",{model:{value:t.link.name,callback:function(e){t.$set(t.link,"name",e)},expression:"link.name"}})],1),a("a-form-item",{attrs:{label:"网站地址：",help:"* 需要加上 http://"}},[a("a-input",{model:{value:t.link.url,callback:function(e){t.$set(t.link,"url",e)},expression:"link.url"}},[a("a",{attrs:{slot:"addonAfter",href:"javascript:void(0);"},on:{click:t.handleParseUrl},slot:"addonAfter"},[a("a-icon",{attrs:{type:"sync"}})],1)])],1),a("a-form-item",{attrs:{label:"Logo："}},[a("a-input",{model:{value:t.link.logo,callback:function(e){t.$set(t.link,"logo",e)},expression:"link.logo"}})],1),a("a-form-item",{attrs:{label:"分组："}},[a("a-auto-complete",{attrs:{dataSource:t.teams,allowClear:""},model:{value:t.link.team,callback:function(e){t.$set(t.link,"team",e)},expression:"link.team"}})],1),a("a-form-item",{attrs:{label:"排序编号："}},[a("a-input",{attrs:{type:"number"},model:{value:t.link.priority,callback:function(e){t.$set(t.link,"priority",e)},expression:"link.priority"}})],1),a("a-form-item",{attrs:{label:"描述："}},[a("a-input",{attrs:{type:"textarea",autosize:{minRows:5}},model:{value:t.link.description,callback:function(e){t.$set(t.link,"description",e)},expression:"link.description"}})],1),a("a-form-item",["create"===t.formType?a("a-button",{attrs:{type:"primary"},on:{click:t.handleSaveClick}},[t._v("保存")]):a("a-button-group",[a("a-button",{attrs:{type:"primary"},on:{click:t.handleSaveClick}},[t._v("更新")]),"update"===t.formType?a("a-button",{attrs:{type:"dashed"},on:{click:t.handleAddLink}},[t._v("返回添加")]):t._e()],1)],1)],1)],1)],1),a("a-col",{style:{"padding-bottom":"12px"},attrs:{xl:14,lg:14,md:14,sm:24,xs:24}},[a("a-card",{attrs:{title:"所有友情链接",bodyStyle:{padding:"16px"}}},[t.isMobile()?a("a-list",{attrs:{itemLayout:"vertical",size:"large",dataSource:t.links,loading:t.loading},scopedSlots:t._u([{key:"renderItem",fn:function(e,n){return a("a-list-item",{key:n},[a("template",{slot:"actions"},[a("a-dropdown",{attrs:{placement:"topLeft",trigger:["click"]}},[a("span",[a("a-icon",{attrs:{type:"bars"}})],1),a("a-menu",{attrs:{slot:"overlay"},slot:"overlay"},[a("a-menu-item",[a("a",{attrs:{href:"javascript:;"},on:{click:function(a){return t.handleEditLink(e.id)}}},[t._v("编辑")])]),a("a-menu-item",[a("a-popconfirm",{attrs:{title:"你确定要删除【"+e.name+"】链接？",okText:"确定",cancelText:"取消"},on:{confirm:function(a){return t.handleDeleteLink(e.id)}}},[a("a",{attrs:{href:"javascript:;"}},[t._v("删除")])])],1)],1)],1)],1),a("template",{slot:"extra"},[a("span",[t._v("\n                "+t._s(e.team)+"\n              ")])]),a("a-list-item-meta",[a("template",{slot:"description"},[t._v("\n                "+t._s(e.description)+"\n              ")]),a("span",{staticStyle:{"max-width":"300px",display:"block","white-space":"nowrap",overflow:"hidden","text-overflow":"ellipsis"},attrs:{slot:"title"},slot:"title"},[t._v("\n                "+t._s(e.name)+"\n              ")])],2),a("a",{attrs:{href:e.url,target:"_blank"}},[t._v(t._s(e.url))])],2)}}],null,!1,365927485)}):a("a-table",{attrs:{columns:t.columns,dataSource:t.links,loading:t.loading,rowKey:function(t){return t.id}},scopedSlots:t._u([{key:"url",fn:function(e){return[a("a",{attrs:{target:"_blank",href:e}},[t._v(t._s(e))])]}},{key:"name",fn:function(e){return a("ellipsis",{attrs:{length:15,tooltip:""}},[t._v(t._s(e))])}},{key:"action",fn:function(e,n){return a("span",{},[a("a",{attrs:{href:"javascript:;"},on:{click:function(e){return t.handleEditLink(n.id)}}},[t._v("编辑")]),a("a-divider",{attrs:{type:"vertical"}}),a("a-popconfirm",{attrs:{title:"你确定要删除【"+n.name+"】链接？",okText:"确定",cancelText:"取消"},on:{confirm:function(e){return t.handleDeleteLink(n.id)}}},[a("a",{attrs:{href:"javascript:;"}},[t._v("删除")])])],1)}}])})],1)],1)],1)],1)},i=[],l=(a("7f7f"),a("b54a"),a("ac0d")),o=a("9efd"),r="/api/admin/links",s={listAll:function(){return Object(o["a"])({url:"".concat(r),method:"get"})},create:function(t){return Object(o["a"])({url:r,data:t,method:"post"})},get:function(t){return Object(o["a"])({url:"".concat(r,"/").concat(t),method:"get"})},getByParse:function(t){return Object(o["a"])({url:"".concat(r,"/parse"),params:{url:t},method:"get"})},update:function(t,e){return Object(o["a"])({url:"".concat(r,"/").concat(t),data:e,method:"put"})},delete:function(t){return Object(o["a"])({url:"".concat(r,"/").concat(t),method:"delete"})},listTeams:function(){return Object(o["a"])({url:"".concat(r,"/teams"),method:"get"})}},c=s,d=[{title:"名称",dataIndex:"name",scopedSlots:{customRender:"name"}},{title:"网址",dataIndex:"url",scopedSlots:{customRender:"url"}},{title:"分组",dataIndex:"team"},{title:"排序",dataIndex:"priority"},{title:"操作",key:"action",scopedSlots:{customRender:"action"}}],u={mixins:[l["a"],l["b"]],data:function(){return{formType:"create",data:[],loading:!1,columns:d,links:[],link:{},teams:[]}},computed:{title:function(){return this.link.id?"修改友情链接":"添加友情链接"}},created:function(){this.loadLinks(),this.loadTeams()},methods:{loadLinks:function(){var t=this;this.loading=!0,c.listAll().then((function(e){t.links=e.data.data,t.loading=!1}))},loadTeams:function(){var t=this;c.listTeams().then((function(e){t.teams=e.data.data}))},handleSaveClick:function(){this.createOrUpdateLink()},handleAddLink:function(){this.formType="create",this.link={}},handleEditLink:function(t){var e=this;c.get(t).then((function(t){e.link=t.data.data,e.formType="update"}))},handleDeleteLink:function(t){var e=this;c.delete(t).then((function(t){e.$message.success("删除成功！"),e.loadLinks(),e.loadTeams()}))},handleParseUrl:function(){var t=this;c.getByParse(this.link.url).then((function(e){t.link=e.data.data}))},createOrUpdateLink:function(){var t=this;this.link.name?this.link.url?(this.link.id?c.update(this.link.id,this.link).then((function(e){t.$message.success("更新成功！"),t.loadLinks(),t.loadTeams()})):c.create(this.link).then((function(e){t.$message.success("保存成功！"),t.loadLinks(),t.loadTeams()})),this.handleAddLink()):this.$notification["error"]({message:"提示",description:"网站地址不能为空！"}):this.$notification["error"]({message:"提示",description:"网站名称不能为空！"})}}},m=u,p=a("2877"),f=Object(p["a"])(m,n,i,!1,null,null,null);e["default"]=f.exports}}]);