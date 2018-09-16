<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.posts.edit.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/editor.md/css/editormd.min.css">
        <link rel="stylesheet" href="/static/plugins/jquery-tageditor/jquery.tag-editor.css">
        <style type="text/css">
            #post_title{
                font-weight: 400;
            }
            #btnOpenAttach{margin-left:4px;padding:3px 6px;position:relative;top:-4px;border:1px solid #ccc;border-radius:2px;background:#fff;text-shadow:none;font-weight:600;font-size:12px;line-height:normal;color:#3c8dbc;cursor:pointer;transition:all .2s ease-in-out}
            #btnOpenAttach:hover{background:#3c8dbc;color:#fff}
            .form-horizontal .control-label{
                text-align: left;
            }
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.posts.edit.title' /></h1>
            <a id="btnOpenAttach" href="#" onclick="openAttachCopy()">
                <@spring.message code='admin.editor.btn.attachs' />
            </a>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="#"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li>
                    <a data-pjax="true" href="/admin/posts"><@spring.message code='admin.posts.title' /></a>
                </li>
                <li class="active"><@spring.message code='admin.posts.edit.title' /></li>
            </ol>
        </section>
        <section class="content">
            <div class="row">
                <div class="col-md-9">
                    <#if post??>
                        <input type="hidden" id="postId" name="postId" value="${post.postId?c}">
                    <#else>
                        <input type="hidden" id="postId" name="postId" value="">
                    </#if>
                    <div style="margin-bottom: 10px;">
                        <input type="text" class="form-control input-lg" id="post_title" name="post_title" placeholder="<@spring.message code='admin.posts.edit.form.title.placeholder' />" onblur="autoComplateUrl();" value="<#if post??>${post.postTitle}</#if>">
                    </div>
                    <div style="display: block;margin-bottom: 10px;">
                        <span>
                            <@spring.message code='admin.editor.form.url' />
                            <a href="#">${options.blog_url}/archives/<span id="postUrl"><#if post??>${post.postUrl}</#if></span>/</a>
                            <button class="btn btn-default btn-sm " id="btn_input_postUrl"><@spring.message code='common.btn.edit' /></button>
                            <button class="btn btn-default btn-sm " id="btn_change_postUrl" onclick="urlOnBlurAuto()" style="display: none;"><@spring.message code='common.btn.define' /></button>
                        </span>
                    </div>
</#compress>
                    <div class="box box-primary">
                        <!-- Editor.md编辑器 -->
                        <div class="box-body pad">
                            <div id="markdown-editor">
                                <textarea style="display:none;"><#if post??>${post.postContentMd?if_exists}</#if></textarea>
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
                            <h3 class="box-title"><@spring.message code='admin.editor.text.category' /></h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body" style="display: block">
                            <div class="form-group">
                                <ul style="list-style: none;padding: 0px;margin: 0px;">
                                    <@commonTag method="categories">
                                        <#if post??>
                                            <#list categories as cate>
                                                <li style="padding: 0;margin: 0px;list-style: none">
                                                    <label>
                                                        <input name="categories" id="categories" type="checkbox" class="minimal" value="${cate.cateId?c}" <#list post.categories as postCate><#if postCate.cateId = cate.cateId>checked="checked"</#if></#list>> ${cate.cateName}
                                                    </label>
                                                </li>
                                            </#list>
                                        <#else>
                                            <#list categories as cate>
                                                <li style="padding: 0;margin: 0px;list-style: none">
                                                    <label>
                                                        <input name="categories" id="categories" type="checkbox" class="minimal" value="${cate.cateId?c}"> ${cate.cateName}
                                                    </label>
                                                </li>
                                            </#list>
                                        </#if>
                                    </@commonTag>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.editor.text.tag' /></h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <input type="text" class="form-control input-lg" id="tagList" name=""/><br>
                            <select class="form-control" id="chooseTag" name="chooseTag">
                                <@commonTag method="tags">
                                    <#if tags??>
                                        <option value=""><@spring.message code='admin.editor.select.tag.default' /></option>
                                        <#list tags as tag>
                                            <option value="${tag.tagName}">${tag.tagName}(${tag.posts?size})</option>
                                        </#list>
                                    <#else>
                                        <option><@spring.message code='common.text.no-data' /></option>
                                    </#if>
                                </@commonTag>
                            </select>
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
        <script src="/static/plugins/editor.md/editormd.min.js"></script>
        <script src="/static/plugins/jquery-tageditor/jquery.tag-editor.min.js"></script>
        <script src="/static/plugins/jquery-tageditor/jquery.caret.min.js"></script>
        <script src="/static/plugins/hz2py/jQuery.Hz2Py-min.js"></script>
        <script>
            $('#tagList').tagEditor({
                //initialTags: ['Hello', 'World', 'Example', 'Tags'],
                delimiter: ',',
                placeholder: '<@spring.message code="admin.posts.edit.form.tag.placeholder" />',
                forceLowercase: false
            });

            /**
             * 加载该文章已有的标签
             */
            <#if post??>
                <#if post.tags?size gt 0>
                    <#list post.tags as tag>
                    $('#tagList').tagEditor('addTag','${tag.tagName}');
                    </#list>
                </#if>
            </#if>

            $('#chooseTag').change(function () {
                $('#tagList').tagEditor('addTag',$(this).val());
            });

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

            var editor;
            /**
             * 加载编辑器
             */
            function loadEditor() {
                editor = editormd("markdown-editor", {
                    width: "100%",
                    height: 620,
                    syncScrolling: "single",
                    path: "/static/plugins/editor.md/lib/",
                    saveHTMLToTextarea: true,
                    imageUpload : true,
                    imageFormats : ["jpg", "jpeg", "gif", "png", "bmp", "webp"],
                    imageUploadURL : "/admin/attachments/upload/editor",
                    htmlDecode: "script",
                    tocStartLevel : 1,
                    onfullscreen : function() {
                        $("#markdown-editor").css("z-index","9999");
                    },
                    onfullscreenExit : function() {
                        $("#markdown-editor").css("z-index","");
                    }
                    // toolbarIcons : function () {
                    //     return editormd.toolbarModes["simple"];
                    // }
                });
            }
            $(document).ready(function () {
                loadEditor();
            });

            /**
             * 自动填充路径，并且将汉字转化成拼音以-隔开
             */
            function autoComplateUrl() {
                var titleVal = $("#post_title").val();
                if(titleVal!="" && titleVal!=null && $("#postUrl").html()==''){
                    var result = $("#post_title").toPinyin().toLowerCase();
                    $("#postUrl").html(result.substring(0,result.length-1));
                }
            }

            /**
             * 检测是否已经存在该链接
             * @constructor
             */
            function urlOnBlurAuto() {
                if($('#newPostUrl').val()===""){
                    showMsg("<@spring.message code='admin.editor.js.no-url' />","info",2000);
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
                        if(data.code==0){
                            showMsg(data.msg,"error",2000);
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
                var postUrl = $("#postUrl").html();
                $('#postUrl').html("<input type='text' id='newPostUrl' onblur='urlOnBlurAuto()' value='"+postUrl+"'>");
                $(this).hide();
                $('#btn_change_postUrl').show();
            });
            var postTitle = $("#post_title");
            var cateList = new Array();

            /**
             * 提交文章
             * @param status 文章状态
             */
            function push(status) {
                var Title = "";
                if(postTitle.val()){
                    Title = postTitle.val();
                }else{
                    showMsg("<@spring.message code='admin.editor.js.no-title' />","info",2000);
                    return;
                }
                $('input[name="categories"]:checked').each(function(){
                    cateList.push($(this).val());
                });
                if($('#postUrl').html()===""){
                    showMsg("<@spring.message code='admin.editor.js.no-url' />","info",2000);
                    return;
                }
                $.ajax({
                    type: 'POST',
                    url: '/admin/posts/new/push',
                    async: false,
                    data: {
                        'postId': $('#postId').val(),
                        'postStatus': status,
                        'postTitle': Title,
                        'postUrl' : $('#postUrl').html().toString(),
                        'postContentMd': editor.getMarkdown(),
                        'postContent': editor.getHTML(),
                        'postThumbnail': $('#selectImg').attr('src'),
                        'cateList' : cateList.toString(),
                        'tagList' : $('#tagList').tagEditor('getTags')[0].tags.toString(),
                        'allowComment' : $('#allowComment').val()
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
                                loaderBg: '#ffffff',
                                afterHidden: function () {
                                    window.location.href="/admin/posts";
                                }
                            });
                        }else{
                            $.toast({
                                text: data.msg,
                                heading: '<@spring.message code="common.text.tips" />',
                                icon: 'error',
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: 1000,
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
                        'postContentMd': editor.getMarkdown()
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
