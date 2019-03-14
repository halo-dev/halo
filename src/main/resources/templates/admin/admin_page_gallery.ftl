<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.pages.galleries.title' /></@head>
<div class="content-wrapper">
    <style type="text/css" rel="stylesheet">
        .div-thumbnail{transition:all .5s ease-in-out;padding:10px}
        .thumbnail{margin-bottom:0}
    </style>
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.pages.galleries.title' /><small></small></h1>
        <a class="btn-header" id="btnNewPicture" href="javascript:void(0)">
            <@spring.message code='admin.pages.galleries.text.add-gallery' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="/admin/page"><@spring.message code='admin.pages.title' /></a></li>
            <li class="active"><@spring.message code='admin.pages.galleries.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-lg-12 col-xs-12" id="newPicturePanel" style="display: none">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.pages.galleries.text.add-gallery' /></h3>
                    </div>
                    <form action="/admin/page/gallery/save" method="post" role="form" class="form-horizontal">
                        <div class="box-body">
                            <div class="form-group">
                                <label for="galleryName" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-name' /></label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="galleryName" name="galleryName">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="galleryDesc" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-desc' /></label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="galleryDesc" name="galleryDesc">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="galleryDate" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-date' /></label>
                                <div class="col-sm-4">
                                    <input type="date" class="form-control" id="galleryDate" name="galleryDate">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="galleryLocation" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-location' /></label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="galleryLocation" name="galleryLocation">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="galleryUrl" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-url' /></label>
                                <div class="col-sm-4">
                                    <div class="input-group">
                                        <input type="text" class="form-control" id="galleryUrl" name="galleryUrl">
                                        <span class="input-group-btn">
                                            <button class="btn btn-default btn-flat" type="button" onclick="halo.layerModal('/admin/attachments/select?id=galleryUrl','<@spring.message code="common.js.all-attachment" />')"><@spring.message code='common.btn.choose' /></button>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="galleryThumbnailUrl" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-thumbnail-url' /></label>
                                <div class="col-sm-4">
                                    <input type="text" class="form-control" id="galleryThumbnailUrl" name="galleryThumbnailUrl">
                                </div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button type="submit" class="btn btn-primary pull-left"><@spring.message code='common.btn.save' /></button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <div class="row">
            <#list galleries.content as gallery>
                <div class="col-lg-2 col-md-3 col-sm-6 col-xs-6 div-thumbnail" onclick="halo.layerModal('/admin/page/gallery?galleryId=${gallery.galleryId?c}','<@spring.message code="admin.pages.galleries.modal.title" />')">
                    <a href="javascript:void(0)" class="thumbnail">
                        <img src="${gallery.galleryThumbnailUrl!}" class="img-responsive">
                    </a>
                </div>
            </#list>
            <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                <div class="no-margin pull-left">
                    <@spring.message code='admin.pageinfo.text.no' />${galleries.number+1}/${galleries.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                </div>
                <div class="btn-group pull-right btn-group-sm" role="group">
                    <a data-pjax="true" class="btn btn-default <#if !galleries.hasPrevious()>disabled</#if>" href="/admin/page/galleries" >
                        <@spring.message code='admin.pageinfo.btn.first' />
                    </a>
                    <a data-pjax="true" class="btn btn-default <#if !galleries.hasPrevious()>disabled</#if>" href="/admin/page/galleries?page=${galleries.number-1}" >
                        <@spring.message code='admin.pageinfo.btn.pre' />
                    </a>
                    <a data-pjax="true" class="btn btn-default <#if !galleries.hasNext()>disabled</#if>" href="/admin/page/galleries?page=${galleries.number+1}">
                        <@spring.message code='admin.pageinfo.btn.next' />
                    </a>
                    <a data-pjax="true" class="btn btn-default <#if !galleries.hasNext()>disabled</#if>" href="/admin/page/galleries?page=${galleries.totalPages-1}">
                        <@spring.message code='admin.pageinfo.btn.last' />
                    </a>
                </div>
            </div>
        </div>
    </section>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    $('#btnNewPicture').click(function () {
        $('#newPicturePanel').slideToggle(400);
    });
</script>
</@footer>
</#compress>
