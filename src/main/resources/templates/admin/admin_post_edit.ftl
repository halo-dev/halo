<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.posts.edit.title' /></@head>
<div class="content-wrapper">
    <link rel="stylesheet" href="/static/halo-backend/plugins/simplemde/simplemde.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/jquery-tageditor/jquery.tag-editor.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/datetimepicker/css/bootstrap-datetimepicker.min.css">
    <style type="text/css">
        #postTitle{font-weight: 400;}
    </style>
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.posts.edit.title' /></h1>
        <a class="btn-header" id="btnOpenAttach" href="javascript:void(0)" onclick="halo.layerModal('/admin/attachments/select?type=post','<@spring.message code="common.js.all-attachment" />')">
            <@spring.message code='admin.editor.btn.attachs' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="javascript:void(0)"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li>
                <a data-pjax="true" href="/admin/posts"><@spring.message code='admin.posts.title' /></a>
            </li>
            <li class="active"><@spring.message code='admin.posts.edit.title' /></li>
        </ol>
    </section>
    <section class="content" id="animated-content">
        <div class="row">
            <div class="col-md-9">
                <input type="hidden" id="postId" name="postId" value="${post.postId?c}">
                <div style="margin-bottom: 10px;">
                    <input type="text" class="form-control input-lg" id="postTitle" name="postTitle" placeholder="<@spring.message code='admin.posts.edit.form.title.placeholder' />" onblur="autoComplateUrl();" value="${post.postTitle!}">
                </div>
                <div style="display: block;margin-bottom: 10px;">
                    <span>
                        <@spring.message code='admin.editor.form.url' />
                        <a href="javascript:void(0)">${options.blog_url!}/archives/<span id="postUrl">${post.postUrl!}</span>/</a>
                        <button class="btn btn-default btn-sm " id="btn_input_postUrl"><@spring.message code='common.btn.edit' /></button>
                        <button class="btn btn-default btn-sm " id="btn_change_postUrl" onclick="urlOnBlurAuto()" style="display: none;"><@spring.message code='common.btn.define' /></button>
                    </span>
                </div>
</#compress>
                <div class="box box-primary">
                    <!-- Editor.md编辑器 -->
                    <div class="box-body pad">
                        <div id="markdown-editor">
                            <textarea id="editorarea" style="display:none;">${post.postContentMd!}</textarea>
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
                                <option value="1" <#if (post.allowComment!1)==1>selected</#if>><@spring.message code='common.select.yes' /></option>
                                <option value="0" <#if (post.allowComment!)==0>selected</#if>><@spring.message code='common.select.no' /></option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="postDate" class="control-label">发布时间：</label>
                            <input type="text" class="form-control" id="postDate" name="postDate" value="${post.postDate!?string('yyyy-MM-dd HH:mm')}">
                        </div>
                    </div>
                    <div class="box-footer">
                        <button onclick="push(1)" class="btn btn-default btn-sm "><@spring.message code='admin.editor.save-draft' /></button>
                        <button onclick="push(0)" class="btn btn-primary btn-sm pull-right " data-loading-text="<@spring.message code='admin.editor.btn.pushing' />">
                            <@spring.message code='admin.editor.btn.update' />
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
                                    <#list categories as cate>
                                            <li style="padding: 0;margin: 0px;list-style: none">
                                                <div class="pretty p-default">
                                                    <input name="categories" id="categories" type="checkbox" class="minimal" value="${cate.cateId?c}" <#list post.categories as postCate><#if postCate.cateId = cate.cateId>checked="checked"</#if></#list>>
                                                    <div class="state p-primary">
                                                        <label>${cate.cateName}</label>
                                                    </div>
                                                </div>
                                            </li>
                                    </#list>
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
                            <img src="${post.postThumbnail!'/static/halo-frontend/images/thumbnail/thumbnail.png'}" class="img-responsive img-thumbnail" id="selectImg" onclick="halo.layerModal('/admin/attachments/select?id=selectImg','<@spring.message code="common.js.all-attachment" />')" style="cursor: pointer;">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
</div>
<@footer>
<script type="application/javascript" id="footer_script">

    $('#postDate').datetimepicker({
        format: 'yyyy-mm-dd hh:ii',
        language: 'zh-CN',
        weekStart: 1,
        todayBtn: 1,
        autoclose: 1
    });

    MathJax.Hub.Config({
        tex2jax: {inlineMath: [["$","$"],["\\(","\\)"]]}
    });

    var QUEUE = MathJax.Hub.queue;

    /**
     * 加载编辑器
     */
    var simplemde = new SimpleMDE({
        element: document.getElementById("editorarea"),
        autoDownloadFontAwesome: false,
        autofocus: true,
        autosave: {
            enabled: true,
            uniqueId: "editor-temp-${post.postId?c}",
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
        inlineAttachment.editors.codemirror4.attach(simplemde.codemirror, {
            uploadUrl: "/admin/attachments/upload"
        });
    });

    var tagList = $('#tagList');

    /**
     * 初始化标签
     */
    tagList.tagEditor({
        delimiter: ',',
        placeholder: '<@spring.message code="admin.posts.edit.form.tag.placeholder" />',
        forceLowercase: false
    });

    /**
     * 加载该文章已有的标签
     */
    <#if post.tags?size gt 0>
        <#list post.tags as tag>
            tagList.tagEditor('addTag','${tag.tagName}');
        </#list>
    </#if>

    $('#chooseTag').change(function () {
        tagList.tagEditor('addTag',$(this).val());
    });

    /**
     * 自动填充路径，并且将汉字转化成拼音以-隔开
     */
    function autoComplateUrl() {
        var titleVal = $("#postTitle");
        var postUrl = $("#postUrl");
        if(titleVal.val()!=="" && titleVal.val() !== null && postUrl.html()===''){
            var result = titleVal.toPinyin().toLowerCase();
            postUrl.html(result.substring(0,result.length-1));
        }
    }

    /**
     * 检测是否已经存在该链接
     * @constructor
     */
    function urlOnBlurAuto() {
        var newPostUrl = $('#newPostUrl');
        if(newPostUrl.val()===""){
            halo.showMsg("<@spring.message code='admin.editor.js.no-url' />",'info',2000);
            return;
        }
        $.get('/admin/posts/checkUrl',{'postUrl': newPostUrl.val()},function (data) {
            if(data.code===0){
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
        var postUrl = $("#postUrl");
        postUrl.html("<input type='text' id='newPostUrl' onblur='urlOnBlurAuto()' value='"+postUrl.html()+"'>");
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
        var cateList = new Array();
        if(!postTitle.val()){
            halo.showMsg("<@spring.message code='admin.editor.js.no-title' />",'info',2000);
            return;
        }
        if(!postUrl.html()){
            halo.showMsg("<@spring.message code='admin.editor.js.no-url' />",'info',2000);
            return;
        }
        $('input[name="categories"]:checked').each(function(){
            cateList.push($(this).val());
        });
        $.post('/admin/posts/update',{
            'postId': $('#postId').val(),
            'postStatus': status,
            'postTitle': postTitle.val(),
            'postUrl' : postUrl.html().toString(),
            'postContentMd': simplemde.value(),
            'postThumbnail': $('#selectImg').attr('src'),
            'cateList' : cateList.toString(),
            'tagList' : $('#tagList').tagEditor('getTags')[0].tags.toString(),
            'allowComment' : $('#allowComment').val(),
            'postDate' : $("#postDate").val()
        },function (data) {
            if(data.code === 1){
                //清除自动保存的内容
                simplemde.clearAutosavedValue();
                halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/posts',"${options.admin_pjax!'true'}");
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON')
    }
</script>
</@footer>
</#compress>
