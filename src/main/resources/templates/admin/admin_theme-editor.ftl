<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.themes.edit.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/editor.md/css/editormd.min.css">
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.themes.edit.title' /></h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li><a data-pjax="true" href="#">外观</a></li>
                <li class="active">主题编辑</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-9">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title" id="tplNameTitle"></h3>
                        </div>
                        <div class="box-body">
                            <div id="theme-editor">
                                <textarea style="display:none;" id="tplContent"></textarea>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button type="button" class="btn btn-primary " onclick="saveTpl()">确定修改</button>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">${options.theme?default('halo')}主题</h3>
                        </div>
                        <div class="box-body table-responsive">
                            <div class="table-responsive mailbox-messages">
                                <table class="table table-hover table-striped">
                                    <tbody>
                                        <#list tpls as tpl>
                                            <tr style="cursor: pointer">
                                                <td class="mailbox-name" onclick="loadContent('${tpl}')"><a href="#">${tpl}</a></td>
                                                <td class="mailbox-subject">
                                                    <#switch tpl>
                                                        <#case "index.ftl">
                                                        首页
                                                        <#break >
                                                        <#case "post.ftl">
                                                        文章页面
                                                        <#break >
                                                        <#case "archives.ftl">
                                                        文章归档页面
                                                        <#break >
                                                        <#case "links.ftl">
                                                        友情链接页面
                                                        <#break >
                                                        <#case "module/macro.ftl">
                                                        宏模板
                                                        <#break >
                                                        <#case "tag.ftl">
                                                        单个标签页面
                                                        <#break >
                                                        <#case "tags.ftl">
                                                        标签列表页面
                                                        <#break>
                                                        <#case "category.ftl">
                                                        单个分类页面
                                                        <#break >
                                                        <#case "page.ftl">
                                                        自定义页面
                                                        <#break>
                                                        <#case "gallery.ftl">
                                                        图库页面
                                                        <#break >
                                                        <#case "module/options.ftl">
                                                        设置选项
                                                        <#break >
                                                    </#switch>
                                                </td>
                                            </tr>
                                        </#list>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/editor.md/editormd.min.js"></script>
        <script>
            var editor;
            function loadEditor() {
                editor = editormd("theme-editor", {
                    width: "100%",
                    height: 620,
                    syncScrolling: "single",
                    path: "/static/plugins/editor.md/lib/",
                    watch            : false,
                    toolbar          : false,
                    codeFold         : true,
                    searchReplace    : true,
                    placeholder      : "Enjoy coding!",
                    value            : (localStorage.mode) ? $("#"+localStorage.mode.replace("text/", "")+"-code").val() : $("#html-code").val(),
                    theme            : (localStorage.theme) ? localStorage.theme : "default",
                    mode             : (localStorage.mode) ? localStorage.mode : "text/html"
                });
            }
            $(document).ready(function () {
                loadEditor();
            });
            function loadContent(tplName) {
                if (tplName && tplName != '') {
                    $.ajax({
                        type: 'GET',
                        url: '/admin/themes/getTpl',
                        async: false,
                        data: {
                            tplName: tplName
                        },
                        success: function (data) {
                            editor.setValue(data);
                            $('#tplNameTitle').html(tplName);
                        }
                    });
                } else {
                    editor.setValue('');
                    $('#tplNameTitle').html('');
                }
            }

            function saveTpl() {
                $.ajax({
                    type: 'POST',
                    url: '/admin/themes/editor/save',
                    async: false,
                    data:{
                        'tplName': $('#tplNameTitle').html(),
                        'tplContent': editor.getValue()
                    },
                    success: function (data) {
                        if(data.code==1){
                            $.toast({
                                text: data.msg,
                                heading: '<@spring.message code="common.text.tips" />',
                                icon: 'success',
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: 1000,
                                stack: 1,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff'
                            });
                        }else{
                            $.toast({
                                text: data.msg,
                                heading: '<@spring.message code="common.text.tips" />',
                                icon: 'error',
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: 2000,
                                stack: 1,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff'
                            });
                        }
                    }
                });
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>