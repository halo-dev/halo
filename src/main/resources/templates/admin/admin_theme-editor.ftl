<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.themes.edit.title' /></@head>
<div class="content-wrapper">
    <link rel="stylesheet" href="/static/halo-backend/plugins/easymde/easymde.min.css">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.themes.edit.title' /></h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)">外观</a></li>
            <li class="active">主题编辑</li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
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
                        <h3 class="box-title">${options.theme!'halo'}主题</h3>
                    </div>
                    <div class="box-body table-responsive no-padding">
                        <div class="table-responsive mailbox-messages">
                            <table class="table table-hover table-striped">
                                <tbody>
                                    <#list tpls as tpl>
                                        <tr style="cursor: pointer">
                                            <td class="mailbox-name" onclick="loadContent('${tpl}')"><a href="javascript:void(0)">${tpl}</a></td>
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
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    /**
     * 加载编辑器
     */
    var easyMDE = new EasyMDE({
        element: document.getElementById("tplContent"),
        autoDownloadFontAwesome: false,
        autofocus: true,
        renderingConfig: {
            codeSyntaxHighlighting: true
        },
        showIcons: ["code", "table"],
        status: false,
        tabSize: 4,
        toolbar: false,
        toolbarTips: false
    });
    function loadContent(tplName) {
        var tplNameTitle = $('#tplNameTitle');
        if (tplName && tplName !== '') {
            $.get('/admin/themes/getTpl',{'tplName': tplName},function (data) {
                easyMDE.value(data);
                tplNameTitle.html(tplName);
            })
        } else {
            easyMDE.value('');
            tplNameTitle.html('');
        }
    }

    function saveTpl() {
        $.post('/admin/themes/editor/save',{
            'tplName': $('#tplNameTitle').html(),
            'tplContent': easyMDE.value()
        },function (data) {
            if(data.code === 1){
                halo.showMsg(data.msg,'success',1000);
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON');
    }
</script>
</@footer>
