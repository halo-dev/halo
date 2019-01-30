<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.attachments.title' /></@head>
<div class="content-wrapper">
    <link rel="stylesheet" href="/static/halo-backend/plugins/fileinput/fileinput.min.css">
    <style type="text/css" rel="stylesheet">
        .div-thumbnail{transition:all .5s ease-in-out;padding:10px}
        .thumbnail{margin-bottom:0}
    </style>
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.attachments.title' /></h1>
        <a class="btn-header" id="showForm" href="javascript:void(0)">
            <i class="fa fa-cloud-upload" aria-hidden="true"></i><@spring.message code='admin.attachments.btn.upload' />
        </a>
        <ol class="breadcrumb">
            <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a></li>
            <li class="active"><@spring.message code='admin.attachments.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
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
                <div class="col-lg-2 col-md-3 col-sm-6 col-xs-6 div-thumbnail" onclick="halo.layerModal('/admin/attachments/attachment?attachId=${attachment.attachId?c}','<@spring.message code="admin.attachments.modal.detail-title" />')">
                    <a href="javascript:void(0)" class="thumbnail">
                        <img src="${attachment.attachSmallPath!}" class="img-responsive">
                    </a>
                </div>
            </#list>
            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                <div class="no-margin pull-left">
                    <@spring.message code='admin.pageinfo.text.no' />${attachments.number+1}/${attachments.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                </div>
                <div class="btn-group pull-right btn-group-sm" role="group">
                    <a data-pjax="true" class="btn btn-default <#if !attachments.hasPrevious()>disabled</#if>" href="/admin/attachments" >
                        <@spring.message code='admin.pageinfo.btn.first' />
                    </a>
                    <a data-pjax="true" class="btn btn-default <#if !attachments.hasPrevious()>disabled</#if>" href="/admin/attachments?page=${attachments.number-1}" >
                        <@spring.message code='admin.pageinfo.btn.pre' />
                    </a>
                    <a data-pjax="true" class="btn btn-default <#if !attachments.hasNext()>disabled</#if>" href="/admin/attachments?page=${attachments.number+1}">
                        <@spring.message code='admin.pageinfo.btn.next' />
                    </a>
                    <a data-pjax="true" class="btn btn-default <#if !attachments.hasNext()>disabled</#if>" href="/admin/attachments?page=${attachments.totalPages-1}">
                        <@spring.message code='admin.pageinfo.btn.last' />
                    </a>
                </div>
            </div>
        </div>
    </section>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    $('#uploadImg').fileinput({
        language: 'zh',
        uploadUrl: '/admin/attachments/upload',
        uploadAsync: true,
        allowedFileExtensions: ['jpg','gif','png','jpeg','svg','psd'],
        maxFileCount: 100,
        enctype : 'multipart/form-data',
        showClose: false
    }).on("filebatchuploadcomplete",function (event, files, extra) {
        $("#uploadForm").hide(400);
        halo.showMsgAndRedirect('上传成功！','success',1000,'/admin/attachments',"${options.admin_pjax!'true'}");
    });
    $("#showForm").click(function(){
        $("#uploadForm").slideToggle(400);
    });
</script>
</@footer>
</#compress>
