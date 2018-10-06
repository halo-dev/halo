<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.pages.edit.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/simplemde/simplemde.min.css">
        <style type="text/css">
            #post_title{font-weight: 400;}
            .form-horizontal .control-label{text-align: left;}
            .CodeMirror .cm-spell-error:not(.cm-url):not(.cm-comment):not(.cm-tag):not(.cm-word) {background: none;}
            .CodeMirror-fullscreen,.editor-toolbar.fullscreen{z-index: 1030;}
            .CodeMirror, .CodeMirror-scroll {min-height: 480px;}
            .editor-preview-active img,.editor-preview-active-side img{width: 100%;}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;">
                <@spring.message code='admin.pages.edit.title' />
            </h1>
            <a class="btn-header" id="btnOpenAttach" href="#" onclick="openAttachCopy()">
                <@spring.message code='admin.editor.btn.attachs' />
            </a>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="#"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li>
                    <a data-pjax="true" href="/admin/page"><@spring.message code='admin.pages.title' /></a>
                </li>
                <li class="active"><@spring.message code='admin.pages.edit.title' /></li>
            </ol>
        </section>
        <section class="content">
            <div class="row">
                <div class="col-md-9">
                    <#if post??>
                        <input type="hidden" id="postId" name="postId" value="${post.postId?c}">
                    <#else >
                        <input type="hidden" id="postId" name="postId" value="">
                    </#if>
                    <div style="margin-bottom: 10px;">
                        <input type="text" class="form-control input-lg" id="post_title" name="post_title" placeholder="<@spring.message code='admin.pages.edit.form.title.placeholder' />" value="<#if post??>${post.postTitle}</#if>">
                    </div>
                    <div style="display: block;margin-bottom: 10px;">
                        <span>
                            <@spring.message code='admin.editor.form.url' />
                            <a href="#">${options.blog_url}/p/<span id="postUrl"><#if post??>${post.postUrl}</#if></span>/</a>
                            <button class="btn btn-default btn-sm " id="btn_input_postUrl"><@spring.message code='common.btn.edit' /></button>
                            <button class="btn btn-default btn-sm " id="btn_change_postUrl" onclick="UrlOnBlurAuto()" style="display: none;"><@spring.message code='common.btn.define' /></button>
                        </span>
                    </div>
</#compress>
                    <div class="box box-primary">
                        <!-- Editor.md编辑器 -->
                        <div class="box-body pad">
                            <div id="markdown-editor">
                                <textarea id="editorarea" style="display:none;"><#if post??>${post.postContentMd?if_exists}</#if></textarea>
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
                            <label for="allowComment" class="control-label"><@spring.message code='admin.editor.allow-comment' /></label>
                            <select class="form-control" id="allowComment" name="allowComment">
                                <option value="1" <#if post?? && post.allowComment?default(1)==1>selected</#if>><@spring.message code='common.select.yes' /></option>
                                <option value="0" <#if post?? && post.allowComment?default(1)==0>selected</#if>><@spring.message code='common.select.no' /></option>
                            </select>
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
                                <#if post??>
                                    <img src="${post.postThumbnail?default("/static/images/thumbnail/thumbnail.png")}" class="img-responsive img-thumbnail" id="selectImg" onclick="openAttach('selectImg')" style="cursor: pointer;">
                                <#else >
                                    <img src="/static/images/thumbnail/thumbnail.png" class="img-responsive img-thumbnail" id="selectImg" onclick="openAttach('selectImg')" style="cursor: pointer;">
                                </#if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/simplemde/simplemde.min.js"></script>
        <script src="/static/plugins/inline-attachment/codemirror-4.inline-attachment.min.js"></script>
        <script>
            /**
             * 加载编辑器
             */
            var simplemde = new SimpleMDE({
                element: document.getElementById("editorarea"),
                autoDownloadFontAwesome: false,
                autofocus: true,
                autosave: {
                    enabled: true,
                    uniqueId: "editor-temp-page-<#if post??>${post.postId}<#else>1</#if>",
                    delay: 10000
                },
                renderingConfig: {
                    codeSyntaxHighlighting: true
                },
                showIcons: ["code", "table"],
                status: true,
                status: ["autosave", "lines", "words"],
                tabSize: 4
            });

            /**
             * 方法来自https://gitee.com/supperzh/zb-blog/blob/master/src/main/resources/templates/article/publish.html#L255
             */
            $(function () {
                inlineAttachment.editors.codemirror4.attach(simplemde.codemirror, {
                    uploadUrl: "/admin/attachments/upload"
                });
            })

            /**
             * 打开附件
             */
            function openAttach(e) {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="common.js.all-attachment" />',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/select?id='+e,
                    scrollbar: false
                });
            }
            function openAttachCopy() {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="common.js.all-attachment" />',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/select?type=post',
                    scrollbar: false
                });
            }
            /**
             * 检测是否已经存在该链接
             * @constructor
             */
            function UrlOnBlurAuto() {
                if($('#newPostUrl').val()===""){
                    halo.showMsg("<@spring.message code='admin.editor.js.no-url' />",'info',2000);
                    return;
                }
                $.ajax({
                    type: 'GET',
                    url: '/admin/page/checkUrl',
                    async: false,
                    data: {
                        'postUrl': $('#newPostUrl').val()
                    },
                    success: function (data) {
                        if(data.code==0){
                            halo.showMsg(data.msg,'error',2000);
                            return;
                        }else{
                            $('#postUrl').html($('#newPostUrl').val());
                            $('#btn_change_postUrl').hide();
                            $('#btn_input_postUrl').show();
                        }
                    }
                });
            }
            $('#btn_input_postUrl').click(function () {
                $('#postUrl').html("<input type='text' id='newPostUrl' onblur='UrlOnBlurAuto()' value=''>");
                $(this).hide();
                $('#btn_change_postUrl').show();
            });
            var postTitle = $("#post_title");

            /**
             * 提交文章
             * @param status 文章状态
             */
            function push(status) {
                var Title = "";
                if(postTitle.val()){
                    Title = postTitle.val();
                }else{
                    halo.showMsg("<@spring.message code='admin.editor.js.no-title' />",'info',2000);
                    return;
                }
                $('input[name="categories"]:checked').each(function(){
                    cateList.push($(this).val());
                });
                if($('#postUrl').html()===""){
                    halo.showMsg("<@spring.message code='admin.editor.js.no-url' />",'info',2000);
                    return;
                }
                $.ajax({
                    type: 'POST',
                    url: '/admin/page/new/push',
                    async: false,
                    data: {
                        'postId': $('#postId').val(),
                        'postStatus': status,
                        'postTitle': Title,
                        'postUrl' : $('#postUrl').html().toString(),
                        'postContentMd': simplemde.value(),
                        'postContent': simplemde.markdown(simplemde.value()),
                        'postThumbnail': $('#selectImg').attr('src'),
                        'allowComment' : $('#allowComment').val()
                    },
                    success: function (data) {
                        if(data.code==1){
                            //清除自动保存的内容
                            simplemde.clearAutosavedValue();
                            halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/page');
                        }else{
                            halo.showMsg(data.msg,'error',2000);
                        }
                    }
                });
            }

            // setInterval("autoPush()","60000");
            /**
             * 自动保存文章
             */
            function autoPush() {
                var Title = "";
                if(postTitle.val()){
                    Title = postTitle.val();
                }
                $.ajax({
                    type: 'POST',
                    url: '/admin/posts/new/autoPush',
                    async: false,
                    data: {
                        'postId': $('#postId').val(),
                        'postTitle': Title,
                        'postUrl' : $('#postUrl').html().toString(),
                        'postContentMd': simplemde.value(),
                        'postType' : "page"
                    },
                    success: function (data) {
                        if(!$("#post_title").val()){
                            $("#post_title").val(data.result.postTitle);
                        }
                        if(!$("#postId").val()){
                            $("#postId").val(data.result.postId);
                        }
                        if($("#postUrl").html()==''){
                            $("#postUrl").html(data.result.postUrl);
                        }
                    }
                });
            }

            /**
             * Ctrl+C保存
             */
            $(document).keydown(function (event) {
                if(event.ctrlKey&&event.keyCode === 83){
                    autoPush();
                }
            });
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>
