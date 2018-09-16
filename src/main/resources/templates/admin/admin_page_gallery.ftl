<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.pages.galleries.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            #btnNewPicture{margin-left:4px;padding:3px 6px;position:relative;top:-4px;border:1px solid #ccc;border-radius:2px;background:#fff;text-shadow:none;font-weight:600;font-size:12px;line-height:normal;color:#3c8dbc;cursor:pointer;transition:all .2s ease-in-out}
            #btnNewPicture:hover{background:#3c8dbc;color:#fff}
            .form-horizontal .control-label{
                text-align: left;
            }
            .div-thumbnail{transition:all .5s ease-in-out;padding:10px}
            .thumbnail{margin-bottom:0}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.pages.galleries.title' /><small></small></h1>
            <a id="btnNewPicture" href="#">
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
        <section class="content container-fluid">
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
                                                <button class="btn btn-default " type="button" onclick="openAttach('galleryUrl')"><@spring.message code='common.btn.choose' /></button>
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
                    <div class="col-lg-2 col-md-3 col-sm-6 col-xs-6 div-thumbnail" onclick="openDetail(${gallery.galleryId?c})">
                        <a href="#" class="thumbnail">
                            <img src="${gallery.galleryThumbnailUrl?if_exists}" class="img-responsive">
                        </a>
                    </div>
                </#list>
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <div class="no-margin pull-left">
                        <@spring.message code='admin.pageinfo.text.no' />${galleries.number+1}/${galleries.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                    </div>
                    <ul class="pagination no-margin pull-right">
                        <li><a data-pjax="true" class="btn btn-sm <#if !galleries.hasPrevious()>disabled</#if>" href="/admin/page/galleries" ><@spring.message code='admin.pageinfo.btn.first' /></a> </li>
                        <li><a data-pjax="true" class="btn btn-sm <#if !galleries.hasPrevious()>disabled</#if>" href="/admin/page/galleries?page=${galleries.number-1}" ><@spring.message code='admin.pageinfo.btn.pre' /></a></li>
                        <li><a data-pjax="true" class="btn btn-sm <#if !galleries.hasNext()>disabled</#if>" href="/admin/page/galleries?page=${galleries.number+1}"><@spring.message code='admin.pageinfo.btn.next' /></a></li>
                        <li><a data-pjax="true" class="btn btn-sm <#if !galleries.hasNext()>disabled</#if>" href="/admin/page/galleries?page=${galleries.totalPages-1}"><@spring.message code='admin.pageinfo.btn.last' /></a> </li>
                    </ul>
                </div>
            </div>
        </section>
        <script type="application/javascript">
            $('#btnNewPicture').click(function () {
                $('#newPicturePanel').slideToggle(400);
            });
            function openAttach(id) {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="common.js.all-attachment" />',
                    shadeClose: true,
                    shade: 0.5,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/select?id='+id,
                    scrollbar: false
                });
            }
            function openDetail(id) {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="admin.pages.galleries.modal.title" />',
                    shadeClose: true,
                    shade: 0.5,
                    area: ['90%', '90%'],
                    content: '/admin/page/gallery?galleryId='+id,
                    scrollbar: false
                });
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>
