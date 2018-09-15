<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.attachments.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            #showForm{margin-left:4px;padding:3px 6px;position:relative;top:-4px;border:1px solid #ccc;border-radius:2px;background:#fff;text-shadow:none;font-weight:600;font-size:12px;line-height:normal;color:#3c8dbc;cursor:pointer;transition:all .2s ease-in-out}
            #showForm:hover{background:#3c8dbc;color:#fff}
            .div-thumbnail{transition:all .5s ease-in-out;padding:10px}
            .thumbnail{margin-bottom:0}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.attachments.title' /></h1>
            <a id="showForm" href="#">
                <i class="fa fa-cloud-upload" aria-hidden="true"></i><@spring.message code='admin.attachments.btn.upload' />
            </a>
            <ol class="breadcrumb">
                <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a></li>
                <li class="active"><@spring.message code='admin.attachments.title' /></li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row" id="uploadForm" style="display: none;">
                <div class="col-md-12">
                    <div class="form-group">
                        <div class="file-loading">
                            <input id="uploadImg" class="file-loading" type="file" multiple name="file">
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <#list attachments.content as attachment>
                    <div class="col-lg-2 col-md-3 col-sm-6 col-xs-6 div-thumbnail" onclick="openDetail(${attachment.attachId?c})">
                        <a href="#" class="thumbnail">
                            <img src="${attachment.attachSmallPath?if_exists}" class="img-responsive">
                        </a>
                    </div>
                </#list>
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <div class="no-margin pull-left">
                        <@spring.message code='admin.pageinfo.text.no' />${attachments.number+1}/${attachments.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                    </div>
                    <ul class="pagination no-margin pull-right">
                        <li><a data-pjax="true" class="btn btn-sm <#if !attachments.hasPrevious()>disabled</#if>" href="/admin/attachments" ><@spring.message code='admin.pageinfo.btn.first' /></a> </li>
                        <li><a data-pjax="true" class="btn btn-sm <#if !attachments.hasPrevious()>disabled</#if>" href="/admin/attachments?page=${attachments.number-1}" ><@spring.message code='admin.pageinfo.btn.pre' /></a></li>
                        <li><a data-pjax="true" class="btn btn-sm <#if !attachments.hasNext()>disabled</#if>" href="/admin/attachments?page=${attachments.number+1}"><@spring.message code='admin.pageinfo.btn.next' /></a></li>
                        <li><a data-pjax="true" class="btn btn-sm <#if !attachments.hasNext()>disabled</#if>" href="/admin/attachments?page=${attachments.totalPages-1}"><@spring.message code='admin.pageinfo.btn.last' /></a> </li>
                    </ul>
                </div>
            </div>
        </section>
        <script type="application/javascript">
            function openDetail(id) {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="admin.attachments.js.modal.detail-title" />',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/attachment?attachId='+id,
                    scrollbar: false
                });
            }
            function loadFileInput() {
                $('#uploadImg').fileinput({
                    language: 'zh',
                    uploadUrl: '/admin/attachments/upload',
                    uploadAsync: true,
                    allowedFileExtensions: ['jpg','gif','png','jpeg','svg','psd'],
                    maxFileCount: 100,
                    enctype : 'multipart/form-data',
                    showClose: false
                }).on("fileuploaded",function (event,data,previewId,index) {
                    var data = data.jqXHR.responseJSON;
                    if(data.success=="1"){
                        $("#uploadForm").hide(400);
                        $.toast({
                            text: data.message,
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
                                window.location.reload();
                            }
                        });
                    }
                });
            }
            $(document).ready(function () {
                loadFileInput();
            });
            <#if options.admin_pjax?default("true") == "true">
            $(document).on('pjax:complete',function () {
                loadFileInput();
            });
            </#if>
            $("#showForm").click(function(){
                $("#uploadForm").slideToggle(400);
            });
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>
