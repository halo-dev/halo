(self["webpackChunkhalo_admin"]=self["webpackChunkhalo_admin"]||[]).push([[395],{86763:function(t,e,n){var o="Expected a function",r=NaN,a="[object Symbol]",i=/^\s+|\s+$/g,s=/^[-+]0x[0-9a-f]+$/i,u=/^0b[01]+$/i,c=/^0o[0-7]+$/i,l=parseInt,p="object"==typeof n.g&&n.g&&n.g.Object===Object&&n.g,f="object"==typeof self&&self&&self.Object===Object&&self,d=p||f||Function("return this")(),g=Object.prototype,h=g.toString,v=Math.max,S=Math.min,m=function(){return d.Date.now()};function w(t,e,n){var r,a,i,s,u,c,l=0,p=!1,f=!1,d=!0;if("function"!=typeof t)throw new TypeError(o);function g(e){var n=r,o=a;return r=a=void 0,l=e,s=t.apply(o,n),s}function h(t){return l=t,u=setTimeout(k,e),p?g(t):s}function w(t){var n=t-c,o=t-l,r=e-n;return f?S(r,i-o):r}function T(t){var n=t-c,o=t-l;return void 0===c||n>=e||n<0||f&&o>=i}function k(){var t=m();if(T(t))return x(t);u=setTimeout(k,w(t))}function x(t){return u=void 0,d&&r?g(t):(r=a=void 0,s)}function C(){void 0!==u&&clearTimeout(u),l=0,r=c=a=u=void 0}function P(){return void 0===u?s:x(m())}function R(){var t=m(),n=T(t);if(r=arguments,a=this,c=t,n){if(void 0===u)return h(c);if(f)return u=setTimeout(k,e),g(c)}return void 0===u&&(u=setTimeout(k,e)),s}return e=y(e)||0,b(n)&&(p=!!n.leading,f="maxWait"in n,i=f?v(y(n.maxWait)||0,e):i,d="trailing"in n?!!n.trailing:d),R.cancel=C,R.flush=P,R}function b(t){var e=typeof t;return!!t&&("object"==e||"function"==e)}function T(t){return!!t&&"object"==typeof t}function k(t){return"symbol"==typeof t||T(t)&&h.call(t)==a}function y(t){if("number"==typeof t)return t;if(k(t))return r;if(b(t)){var e="function"==typeof t.valueOf?t.valueOf():t;t=b(e)?e+"":e}if("string"!=typeof t)return 0===t?t:+t;t=t.replace(i,"");var n=u.test(t);return n||c.test(t)?l(t.slice(2),n?2:8):s.test(t)?r:+t}t.exports=w},79395:function(t,e,n){"use strict";n.r(e),n.d(e,{default:function(){return m}});var o=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("page-view",{attrs:{"sub-title":t.postToStage.inProgress?"当前内容已保存，但还未发布。":"",title:t.postToStage.title?t.postToStage.title:"新文章",affix:""}},[n("template",{slot:"extra"},[n("a-space",[n("a-button",{attrs:{loading:t.previewSaving},on:{click:t.handlePreviewClick}},[t._v("预览")]),n("a-button",{attrs:{type:"primary"},on:{click:function(e){t.postSettingVisible=!0}}},[t._v("发布")])],1)],1),n("a-row",{attrs:{gutter:12}},[n("a-col",{attrs:{span:24}},[n("div",{staticClass:"mb-4"},[n("a-input",{attrs:{placeholder:"请输入文章标题",size:"large"},model:{value:t.postToStage.title,callback:function(e){t.$set(t.postToStage,"title",e)},expression:"postToStage.title"}})],1),n("div",{style:{height:t.editorHeight},attrs:{id:"editor"}},[n("MarkdownEditor",{attrs:{originalContent:t.postToStage.originalContent},on:{"update:originalContent":function(e){return t.$set(t.postToStage,"originalContent",e)},"update:original-content":function(e){return t.$set(t.postToStage,"originalContent",e)},change:t.onContentChange,save:function(e){return t.handleSaveDraft()}}})],1)])],1),n("PostSettingModal",{attrs:{post:t.postToStage,savedCallback:t.onPostSavedCallback,visible:t.postSettingVisible},on:{"update:visible":function(e){t.postSettingVisible=e},onUpdate:t.onUpdateFromSetting}})],2)},r=[],a=n(29230),i=(n(70315),n(12566),n(95493)),s=n(11940),u=n(22401),c=n(64647),l=n(69716),p=n(62210),f=n(86763),d=n.n(f),g={mixins:[c.jB,c.KT,c.g3],components:{PostSettingModal:i.Z,MarkdownEditor:s.Z,PageView:u.B4},data:function(){return{postSettingVisible:!1,postToStage:{},contentChanges:0,previewSaving:!1}},beforeRouteEnter:function(t,e,n){var o=t.query.postId;n(function(){var t=(0,a.Z)(regeneratorRuntime.mark((function t(e){var n,r;return regeneratorRuntime.wrap((function(t){while(1)switch(t.prev=t.next){case 0:if(!o){t.next=6;break}return t.next=3,p.Z.post.get(Number(o));case 3:n=t.sent,r=n.data,e.postToStage=r;case 6:case"end":return t.stop()}}),t)})));return function(e){return t.apply(this,arguments)}}())},destroyed:function(){window.onbeforeunload&&(window.onbeforeunload=null)},beforeRouteLeave:function(t,e,n){var o=this.$createElement;this.contentChanges<=1?n():this.$confirm({title:"当前页面数据未保存，确定要离开吗？",content:function(){return o("div",{style:"color:red;"},["如果离开当面页面，你的数据很可能会丢失！"])},onOk:function(){n()},onCancel:function(){n(!1)}})},mounted:function(){window.onbeforeunload=function(t){return t=t||window.event,t&&(t.returnValue="当前页面数据未保存，确定要离开吗？"),"当前页面数据未保存，确定要离开吗？"}},beforeMount:function(){document.addEventListener("keydown",this.onRegisterSaveShortcut)},beforeDestroy:function(){document.removeEventListener("keydown",this.onRegisterSaveShortcut)},methods:{onRegisterSaveShortcut:function(t){!t.ctrlKey&&!t.metaKey||t.altKey||t.shiftKey||83!==t.keyCode||(t.preventDefault(),t.stopPropagation(),this.handleSaveDraft())},handleSaveDraft:d()((0,a.Z)(regeneratorRuntime.mark((function t(){var e,n;return regeneratorRuntime.wrap((function(t){while(1)switch(t.prev=t.next){case 0:if(!this.postToStage.id){t.next=16;break}return t.prev=1,t.next=4,p.Z.post.updateDraftById(this.postToStage.id,this.postToStage.originalContent,this.postToStage.content,!0);case 4:e=t.sent,n=e.data,this.postToStage.inProgress=n.inProgress,this.handleRestoreSavedStatus(),this.$message.success({content:"内容已保存",duration:.5}),t.next=14;break;case 11:t.prev=11,t.t0=t["catch"](1),this.$log.error("Failed to update post content",t.t0);case 14:t.next=18;break;case 16:return t.next=18,this.handleCreatePost();case 18:case"end":return t.stop()}}),t,this,[[1,11]])}))),300),handleCreatePost:function(){var t=this;return(0,a.Z)(regeneratorRuntime.mark((function e(){var n,o,r;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return t.postToStage.title||(t.postToStage.title=(0,l._)(new Date,"YYYY-MM-DD-HH-mm-ss")),e.prev=1,t.postToStage.keepRaw=!0,e.next=5,p.Z.post.create(t.postToStage);case 5:n=e.sent,o=n.data,t.postToStage=o,t.handleRestoreSavedStatus(),r=t.$router.history.current.path,t.$router.push({path:r,query:{postId:t.postToStage.id}}).catch((function(t){return t})),t.$message.success({content:"文章已创建",duration:.5}),e.next=17;break;case 14:e.prev=14,e.t0=e["catch"](1),t.$log.error("Failed to create post",e.t0);case 17:case"end":return e.stop()}}),e,null,[[1,14]])})))()},handlePreviewClick:function(){var t=this;return(0,a.Z)(regeneratorRuntime.mark((function e(){var n,o;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:if(t.previewSaving=!0,!t.postToStage.id){e.next=9;break}return e.next=4,p.Z.post.updateDraftById(t.postToStage.id,t.postToStage.originalContent,t.postToStage.content,!0);case 4:n=e.sent,o=n.data,t.postToStage.inProgress=o.inProgress,e.next=11;break;case 9:return e.next=11,t.handleCreatePost();case 11:return e.next=13,t.handleOpenPreview();case 13:case"end":return e.stop()}}),e)})))()},handleOpenPreview:function(){var t=this;return(0,a.Z)(regeneratorRuntime.mark((function e(){var n;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,p.Z.post.getPreviewLinkById(t.postToStage.id);case 3:n=e.sent,window.open(n,"_blank"),t.handleRestoreSavedStatus(),e.next=11;break;case 8:e.prev=8,e.t0=e["catch"](0),t.$log.error("Failed to get preview link",e.t0);case 11:return e.prev=11,setTimeout((function(){t.previewSaving=!1}),400),e.finish(11);case 14:case"end":return e.stop()}}),e,null,[[0,8,11,14]])})))()},handleRestoreSavedStatus:function(){this.contentChanges=0},onContentChange:function(t){var e=t.originalContent,n=t.renderContent;this.contentChanges++,this.postToStage.originalContent=e,this.postToStage.content=n},onPostSavedCallback:function(){this.contentChanges=0,this.$router.push({name:"PostList"})},onUpdateFromSetting:function(t){this.postToStage=t}}},h=g,v=n(42177),S=(0,v.Z)(h,o,r,!1,null,null,null),m=S.exports}}]);