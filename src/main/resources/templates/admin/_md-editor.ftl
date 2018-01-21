<#include "module/_macro.ftl">
<@head title="Halo后台管理-文章编辑"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <link rel="stylesheet" href="/static/plugins/editor.md/css/editormd.min.css">
        <style type="text/css">
            #post_title{
                font-weight: 400;
            }
        </style>
        <section class="content-header">
            <h1>
                新建文章
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a href="#"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li>
                    <a href="/admin/posts">文章</a>
                </li>
                <li class="active">新建文章</li>
            </ol>
        </section>
        <section class="content">
            <div class="row">
                <div class="col-md-9">
                    <#if post??>
                        <input type="hidden" id="postId" name="postId" value="${post.postId?if_exists}">
                    </#if>
                    <div style="margin-bottom: 10px;">
                        <input type="text" class="form-control input-lg" id="post_title" name="post_title" placeholder="请输入文章标题" value="<#if post??>${post.postTitle}</#if>" onblur="TitleOnBlurAuto()">
                    </div>
                    <div style="display: block;margin-bottom: 10px;">
                        <span>
                            永久链接：
                            <a href="#">https://ryanc.cc/archives/<span id="postUrl"></span>/</a>
                            <button class="btn btn-default btn-sm" id="btn_input_postUrl">编辑</button>
                            <button class="btn btn-default btn-sm" id="btn_change_postUrl" onclick="UrlOnBlurAuto()" style="display: none;">确定</button>
                        </span>
                    </div>
                    <div class="box box-primary">
                        <!-- Editor.md编辑器 -->
                        <div class="box-body pad">
                            <div id="markdown-editor" style="z-index: 9999;">
                                <textarea style="display:none;"><#if post??>${post.postContentMd?if_exists}</#if></textarea>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">发布</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button onclick="push(1)" class="btn btn-default btn-sm">保存草稿</button>
                            <button onclick="push(0)" class="btn btn-primary btn-sm pull-right" data-loading-text="发布中...">${btnPush}</button>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">分类目录</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body" style="display: block">
                            <div class="form-group">
                                <ul style="list-style: none;padding: 0px;margin: 0px;">
                                    <#list categories as cate>
                                        <li style="padding: 0;margin: 0px;list-style: none">
                                            <label>
                                                <input name="categories" id="categories" type="checkbox" class="minimal" value="${cate.cateId}"> ${cate.cateName}
                                            </label>
                                        </li>
                                    </#list>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">标签</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <div>标签设置</div>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">缩略图</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <div>
                                <img class="img-responsive selectData">
                                <button class="btn btn-primary btn-sm" onclick="openAttach()">选择</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
        <script src="/static/plugins/layer/layer.js"></script>
        <script>
            function openAttach() {
                layer.open({
                    type: 2,
                    title: '所有附件',
                    shadeClose: true,
                    shade: 0.5,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/select',
                    scrollbar: false
                });
            }
            var editor;
            function loadEditor() {
                $.getScript("/static/plugins/editor.md/editormd.min.js",function () {
                    editor = editormd("markdown-editor", {
                        width: "100%",
                        height: 620,
                        syncScrolling: "single",
                        path: "/static/plugins/editor.md/lib/",
                        saveHTMLToTextarea: true,
                        toolbarIcons : function () {
                            return editormd.toolbarModes["simple"];
                        }
                    });
                });
            }
            $(document).ready(function () {
                loadEditor();
            });
            <#if options.admin_pjax?default("true") == "true">
            $(document).on('pjax:complete',function () {
                loadEditor();
            });
            </#if>
            function TitleOnBlurAuto() {
                $('#postUrl').html($('#post_title').val());
            }
            function UrlOnBlurAuto() {
                if($('#newPostUrl').val()===""){
                    showMsg("固定链接不能为空！","info",2000);
                    return;
                }
                $.ajax({
                    type: 'GET',
                    url: '/admin/posts/checkUrl',
                    async: false,
                    data: {
                        'postUrl': $('#newPostUrl').val()
                    },
                    success: function (data) {
                        if(data=="exists"){
                            showMsg("该路径已经存在！","info",2000);
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
            var cateList = new Array();
            function push(status) {
                var Title = "";
                if(postTitle.val()){
                    Title = postTitle.val();
                }else{
                    showMsg("标题不能为空！","info",2000);
                    return;
                }
                $('input[name="categories"]:checked').each(function(){
                    cateList.push($(this).val());
                });
                if($('#postUrl').html()===""){
                    showMsg("固定链接不能为空！","info",2000);
                    return;
                }
                $.ajax({
                    type: 'POST',
                    url: '/admin/posts/new/push',
                    async: false,
                    data: {
                        <#if post??>
                        'postId': $('#postId').val(),
                        </#if>
                        'postStatus': status,
                        'postTitle': Title,
                        'postUrl' : $('#postUrl').html(),
                        'postContentMd': editor.getMarkdown(),
                        'postContent': editor.getTextareaSavedHTML(),
                        'cateList' : cateList.toString()
                    },
                    success: function (data) {
                        $.toast({
                            text: "发布成功！",
                            heading: '提示',
                            icon: 'success',
                            showHideTransition: 'fade',
                            allowToastClose: true,
                            hideAfter: 1000,
                            stack: 5,
                            position: 'top-center',
                            textAlign: 'left',
                            loader: true,
                            loaderBg: '#ffffff',
                            afterHidden: function () {
                                window.location.href="/admin/posts";
                            }
                        });
                    }
                });
            }
            $(document).keydown(function (event) {
                if(event.ctrlKey&&event.keyCode === 83){
                    push(1);
                }
            });
        </script>
    </div>
    <#include "module/_footer.ftl">
    <div class="control-sidebar-bg"></div>
</div>
<@footer></@footer>