<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.pages.edit.title' /></@head>
<div class="content-wrapper">
    <link rel="stylesheet" href="/static/halo-backend/plugins/easymde/easymde.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/datetimepicker/css/bootstrap-datetimepicker.min.css">
    <style type="text/css">
        #postTitle{font-weight: 400;}
        .CodeMirror .cm-spell-error:not(.cm-url):not(.cm-comment):not(.cm-tag):not(.cm-word) {background: none;}
        .CodeMirror-fullscreen,.editor-toolbar.fullscreen{z-index: 1030;}
        .CodeMirror, .CodeMirror-scroll {min-height: 480px;}
        .editor-preview-active img,.editor-preview-active-side img{width: 100%;}
    </style>
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;">
            <@spring.message code='admin.pages.edit.title' />
        </h1>
        <a class="btn-header" id="btnOpenAttach" href="javascript:void(0)" onclick="halo.layerModal('/admin/attachments/select?type=post','<@spring.message code="common.js.all-attachment" />')">
            <@spring.message code='admin.editor.btn.attachs' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="javascript:void(0)"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li>
                <a data-pjax="true" href="/admin/page"><@spring.message code='admin.pages.title' /></a>
            </li>
            <li class="active"><@spring.message code='admin.pages.edit.title' /></li>
        </ol>
    </section>
    <section class="content" id="animated-content">
        <div class="row">
            <div class="col-md-9">
                <#if post??>
                    <input type="hidden" id="postId" name="postId" value="${post.postId?c}">
                <#else >
                    <input type="hidden" id="postId" name="postId" value="">
                </#if>
                <div style="margin-bottom: 10px;">
                    <input type="text" class="form-control input-lg" id="postTitle" name="postTitle" placeholder="<@spring.message code='admin.pages.edit.form.title.placeholder' />" value="<#if post??>${post.postTitle}</#if>">
                </div>
                <div style="display: block;margin-bottom: 10px;">
                    <span>
                        <@spring.message code='admin.editor.form.url' />
                        <a href="javascript:void(0)">${options.blog_url}/p/<span id="postUrl"><#if post??>${post.postUrl}</#if></span>/</a>
                        <button class="btn btn-default btn-sm " id="btn_input_postUrl"><@spring.message code='common.btn.edit' /></button>
                        <button class="btn btn-default btn-sm " id="btn_change_postUrl" onclick="UrlOnBlurAuto()" style="display: none;"><@spring.message code='common.btn.define' /></button>
                    </span>
                </div>
</#compress>
                <div class="box box-primary">
                    <!-- Editor.md编辑器 -->
                    <div class="box-body pad">
                        <div id="markdown-editor">
                            <textarea id="editorarea" style="display:none;"><#if post??>${post.postContentMd!}</#if></textarea>
                        </div>
                    </div>
                </div>
<#compress >
            </div>
            <div class="col-md-3">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.editor.text.push' /></h3>
                        <div class="box-tools">
                            <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                <i class="fa fa-minus"></i>
                            </button>
                        </div>
                    </div>
                    <div class="box-body">
                        <div class="form-group">
                            <label for="allowComment" class="control-label"><@spring.message code='admin.editor.allow-comment' /></label>
                            <select class="form-control" id="allowComment" name="allowComment">
                                <option value="1" <#if post?? && (post.allowComment!1)==1>selected</#if>><@spring.message code='common.select.yes' /></option>
                                <option value="0" <#if post?? && (post.allowComment!)==0>selected</#if>><@spring.message code='common.select.no' /></option>
                            </select>
                        </div>
                        <#if post??>
                            <div class="form-group">
                                <label for="postDate" class="control-label"><@spring.message code='admin.editor.post.date' /></label>
                                <input type="text" class="form-control" id="postDate" name="postDate" value="${post.postDate!?string('yyyy-MM-dd HH:mm')}">
                            </div>
                        <#else>
                            <input type="hidden" class="form-control" id="postDate" name="postDate">
                        </#if>
                        <div class="form-group">
                            <label for="customTpl" class="control-label"><@spring.message code='admin.editor.page.custom.tpl' /></label>
                            <select class="form-control" id="customTpl" name="customTpl">
                                <#if customTpls?? && customTpls?size gt 0>
                                    <option value=""><@spring.message code='admin.editor.page.choose.tpl' /></option>
                                    <#list customTpls as tpl>
                                    <option value="${tpl}" <#if post?? && (post.customTpl!) == '${tpl}'>selected</#if>>${tpl}</option>
                                    </#list>
                                <#else>
                                    <option value=""><@spring.message code='admin.editor.page.no.tpl' /></option>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="box-footer">
                        <button onclick="push(1)" class="btn btn-default btn-sm "><@spring.message code='admin.editor.save-draft' /></button>
                        <button onclick="push(0)" class="btn btn-primary btn-sm pull-right " data-loading-text="<@spring.message code='admin.editor.btn.pushing' />">
                        <#if post??>
                            <@spring.message code='admin.editor.btn.update' />
                        <#else>
                            <@spring.message code='admin.editor.text.push' />
                        </#if>
                        </button>
                    </div>
                </div>
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.editor.text.thumbnail' /></h3>
                        <div class="box-tools">
                            <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                <i class="fa fa-minus"></i>
                            </button>
                        </div>
                    </div>
                    <div class="box-body">
                        <div>
                            <img src="<#if post??>${post.postThumbnail!'/static/halo-frontend/images/thumbnail/thumbnail.png'}<#else>/static/halo-frontend/images/thumbnail/thumbnail.png</#if>" class="img-responsive img-thumbnail" id="selectImg" onclick="halo.layerModal('/admin/attachments/select?id=selectImg','<@spring.message code="common.js.all-attachment" />')" style="cursor: pointer;">
                        </div>
                    </div>
                    <div class="box-footer">
                        <button onclick="removeThumbnail()" class="btn btn-default btn-sm "><@spring.message code='common.btn.remove' /></button>
                    </div>
                </div>
            </div>
        </div>
    </section>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    <#if post??>
        $('#postDate').datetimepicker({
            format: 'yyyy-mm-dd hh:ii',
            language: 'zh-CN',
            weekStart: 1,
            todayBtn: 1,
            autoclose: 1
        });
    </#if>

    MathJax.Hub.Config({
        showProcessingMessages: false,
        messageStyle: "none",
        tex2jax: {
            inlineMath: [ ['$','$'], ["\\(","\\)"] ],
            displayMath: [ ['$$','$$'], ["\\[","\\]"] ],
            skipTags: ['script', 'noscript', 'style', 'textarea', 'pre','code','a']
        }
    });

    var QUEUE = MathJax.Hub.queue;

    /**
     * 加载编辑器
     */
    var easyMDE = new EasyMDE({
        element: document.getElementById("editorarea"),
        autoDownloadFontAwesome: false,
        autofocus: true,
        autosave: {
            enabled: true,
            uniqueId: "editor-temp-page-<#if post??>${post.postId?c}<#else>1</#if>",
            delay: 10000
        },
        renderingConfig: {
            codeSyntaxHighlighting: true
        },
        previewRender: function(plainText) {
            var preview = document.getElementsByClassName("editor-preview-side")[0];
            preview.innerHTML = this.parent.markdown(plainText);
            preview.setAttribute('id','editor-preview');
            MathJax.Hub.Queue(["Typeset",MathJax.Hub,"editor-preview"]);
            return preview.innerHTML;
        },
        showIcons: ["code", "table"],
        status: ["autosave", "lines", "words"],
        tabSize: 4
    });

    /**
     * 方法来自https://gitee.com/supperzh/zb-blog/blob/master/src/main/resources/templates/article/publish.html#L255
     */
    $(function () {
        inlineAttachment.editors.codemirror4.attach(easyMDE.codemirror, {
            uploadUrl: "/admin/attachments/upload"
        });
    });

    /**
     * 检测是否已经存在该链接
     * @constructor
     */
    function UrlOnBlurAuto() {
        var newPostUrl = $('#newPostUrl');
        if(newPostUrl.val()===""){
            halo.showMsg("<@spring.message code='admin.editor.js.no-url' />",'info',2000);
            return;
        }
        $.get('/admin/page/checkUrl',{'postUrl': newPostUrl.val()},function (data) {
            if(data.code === 0){
                halo.showMsg(data.msg,'error',2000);
                return;
            }else{
                $('#postUrl').html(newPostUrl.val());
                $('#btn_change_postUrl').hide();
                $('#btn_input_postUrl').show();
            }
        },'JSON');
    }
    $('#btn_input_postUrl').click(function () {
        $('#postUrl').html("<input type='text' id='newPostUrl' onblur='UrlOnBlurAuto()' value=''>");
        $(this).hide();
        $('#btn_change_postUrl').show();
    });


    /**
     * 提交文章
     * @param status 文章状态
     */
    function push(status) {
        var postTitle = $("#postTitle");
        var postUrl = $("#postUrl");
        if(!postTitle.val()){
            halo.showMsg("<@spring.message code='admin.editor.js.no-title' />",'info',2000);
            return;
        }
        if(!postUrl.html()){
            halo.showMsg("<@spring.message code='admin.editor.js.no-url' />",'info',2000);
            return;
        }

        $.post('/admin/page/new/push',{
            'postId': $('#postId').val(),
            'postStatus': status,
            'postTitle': postTitle.val(),
            'postUrl' : postUrl.html().toString(),
            'postContentMd': easyMDE.value(),
            'postThumbnail': $('#selectImg').attr('src'),
            'allowComment' : $('#allowComment').val(),
            'customTpl' : $("#customTpl").val(),
            'postDate' : $("#postDate").val()
        },function (data) {
            if(data.code===1){
                //清除自动保存的内容
                easyMDE.toTextArea();
                easyMDE = null;
                halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/page',"${options.admin_pjax!'true'}");
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON');
    }

    function removeThumbnail(){
        $("#selectImg").attr("src","/static/halo-frontend/images/thumbnail/thumbnail.png");
    }
</script>
</@footer>
</#compress>
