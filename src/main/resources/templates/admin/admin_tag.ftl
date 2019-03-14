<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.tags.title' /></@head>
<div class="content-wrapper">
    <style>
        .tags{zoom:1;margin:0;padding:0}.tags:after,.tags:before{content:'';display:table}.tags:after{clear:both}.tags li{position:relative;float:left;margin:0 0 8px 12px;list-style:none}.tags li:active{margin-top:1px;margin-bottom:7px}.tags li:after{content:'';z-index:2;position:absolute;top:10px;right:-2px;width:5px;height:6px;opacity:.95;background:#eb6b22;border-radius:3px 0 0 3px;-webkit-box-shadow:inset 1px 0 #99400e;box-shadow:inset 1px 0 #99400e}.tags a,.tags span{display:block;-webkit-box-sizing:border-box;-moz-box-sizing:border-box;box-sizing:border-box}.tags a{height:26px;line-height:23px;padding:0 9px 0 8px;font-size:12px;color:#555;text-decoration:none;text-shadow:0 1px #fff;background:#fafafa;border-width:1px 0 1px 1px;border-style:solid;border-color:#dadada #d2d2d2 #c5c5c5;border-radius:3px 0 0 3px;background-image:-webkit-linear-gradient(top,#fcfcfc,#f0f0f0);background-image:-moz-linear-gradient(top,#fcfcfc,#f0f0f0);background-image:-o-linear-gradient(top,#fcfcfc,#f0f0f0);background-image:linear-gradient(to bottom,#fcfcfc,#f0f0f0);-webkit-box-shadow:inset 0 0 0 1px rgba(255,255,255,.7),0 1px 2px rgba(0,0,0,.05);box-shadow:inset 0 0 0 1px rgba(255,255,255,.7),0 1px 2px rgba(0,0,0,.05)}.tags a:hover span{padding:0 7px 0 6px;max-width:40px;-webkit-box-shadow:inset 0 0 0 1px rgba(255,255,255,.15),1px 1px 2px rgba(0,0,0,.2);box-shadow:inset 0 0 0 1px rgba(255,255,255,.15),1px 1px 2px rgba(0,0,0,.2)}.tags span{position:absolute;top:1px;left:100%;z-index:2;overflow:hidden;max-width:0;height:24px;line-height:21px;padding:0 0 0 2px;color:#fff;text-shadow:0 -1px rgba(0,0,0,.3);background:#eb6b22;border:1px solid;border-color:#d15813 #c85412 #bf5011;border-radius:0 2px 2px 0;opacity:.95;background-image:-webkit-linear-gradient(top,#ed7b39,#df5e14);background-image:-moz-linear-gradient(top,#ed7b39,#df5e14);background-image:-o-linear-gradient(top,#ed7b39,#df5e14);background-image:linear-gradient(to bottom,#ed7b39,#df5e14);-webkit-transition:.3s ease-out;-moz-transition:.3s ease-out;-o-transition:.3s ease-out;transition:.3s ease-out;-webkit-transition-property:padding,max-width;-moz-transition-property:padding,max-width;-o-transition-property:padding,max-width;transition-property:padding,max-width}.green li:after{background:#65bb34;-webkit-box-shadow:inset 1px 0 #3a6b1e;box-shadow:inset 1px 0 #3a6b1e}.green span{background:#65bb34;border-color:#549b2b #4f9329 #4b8b27;background-image:-webkit-linear-gradient(top,#71ca3f,#5aa72e);background-image:-moz-linear-gradient(top,#71ca3f,#5aa72e);background-image:-o-linear-gradient(top,#71ca3f,#5aa72e);background-image:linear-gradient(to bottom,#71ca3f,#5aa72e)}.blue li:after{background:#56a3d5;-webkit-box-shadow:inset 1px 0 #276f9e;box-shadow:inset 1px 0 #276f9e}.blue span{background:#56a3d5;border-color:#3591cd #318cc7 #2f86be;background-image:-webkit-linear-gradient(top,#6aaeda,#4298d0);background-image:-moz-linear-gradient(top,#6aaeda,#4298d0);background-image:-o-linear-gradient(top,#6aaeda,#4298d0);background-image:linear-gradient(to bottom,#6aaeda,#4298d0)}
    </style>
    <section class="content-header" id="animated-header">
        <h1>
            <@spring.message code='admin.tags.title' />
            <small></small>
        </h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="javascript:void(0)">
                    <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.posts.title' /></a></li>
            <li class="active"><@spring.message code='admin.tags.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-md-5">
                <div class="box box-primary">
                    <#if updateTag??>
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.tags.text.edit-tag' /><#if updateTag??>[${updateTag.tagName}]</#if></h3>
                        </div>
                        <form role="form" id="tagSaveForm">
                            <input type="hidden" name="tagId" value="${updateTag.tagId?c}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="tagName"><@spring.message code='admin.tags.form.tag-name' /></label>
                                    <input type="text" class="form-control" id="tagName" name="tagName" value="${updateTag.tagName!}">
                                    <small><@spring.message code='admin.tags.form.tag-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="tagUrl"><@spring.message code='admin.tags.form.tag-url' /></label>
                                    <input type="text" class="form-control" id="tagUrl" name="tagUrl" value="${updateTag.tagUrl!}">
                                    <small><@spring.message code='admin.tags.form.tag-url-tips' /></small>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm" onclick="save()"><@spring.message code='common.btn.define-edit' /></button>
                                <a data-pjax="true" href="/admin/tag" class="btn btn-info btn-sm"><@spring.message code='common.btn.back-to-add' /></a>
                                <#if updateTag.posts?size = 0>
                                <a data-pjax="true" href="/admin/tag/remove?tagId=${updateTag.tagId?c}" class="btn btn-danger btn-sm  pull-right"><@spring.message code='common.btn.delete' /></a>
                                </#if>
                            </div>
                        </form>
                    <#else >
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.tags.text.add-tag' /></h3>
                        </div>
                        <form role="form" id="tagSaveForm">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="tagName"><@spring.message code='admin.tags.form.tag-name' /></label>
                                    <input type="text" class="form-control" id="tagName" name="tagName">
                                    <small><@spring.message code='admin.tags.form.tag-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="tagUrl"><@spring.message code='admin.tags.form.tag-url' /></label>
                                    <input type="text" class="form-control" id="tagUrl" name="tagUrl">
                                    <small><@spring.message code='admin.tags.form.tag-url-tips' /></small>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm" onclick="save()"><@spring.message code='common.btn.define-add' /></button>
                            </div>
                        </form>
                    </#if>
                </div>
            </div>
            <div class="col-md-7">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.tags.text.all-tags' /></h3>
                    </div>
                    <div class="box-body table-responsive">
                        <ul class="tags blue">
                            <@commonTag method="tags">
                                <#if tags?? && tags?size gt 0>
                                    <#list tags as tag>
                                        <li>
                                            <a data-pjax="true" href="/admin/tag/edit?tagId=${tag.tagId?c}">
                                                ${tag.tagName}
                                                <span>${tag.posts?size}</span>
                                            </a>
                                        </li>
                                    </#list>
                                </#if>
                            </@commonTag>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <!-- 删除确认弹出层 -->
    <div class="modal fade label-primary" id="removeCateModal">
        <div class="modal-dialog">
            <div class="modal-content message_align">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title"><@spring.message code='common.text.tips' /></h4>
                </div>
                <div class="modal-body">
                    <p><@spring.message code='common.text.define-delete' /></p>
                </div>
                <div class="modal-footer">
                    <input type="hidden" id="url"/>
                    <button type="button" class="btn btn-default " data-dismiss="modal"><@spring.message code='common.btn.cancel' /></button>
                    <a onclick="removeIt()" class="btn btn-danger " data-dismiss="modal"><@spring.message code='common.btn.define' /></a>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    function modelShow(url) {
        $('#url').val(url);
        $('#removeCateModal').modal();
    }
    function removeIt(){
        var url=$.trim($("#url").val());
        <#if (options.admin_pjax!'true') == 'true'>
            pjax.loadUrl(url);
        <#else>
            window.location.href = url;
        </#if>
    }
    function save() {
        var param = $("#tagSaveForm").serialize();
        $.post("/admin/tag/save",param,function (data) {
            if (data.code === 1) {
                halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/tag',"${options.admin_pjax!'true'}");
            } else {
                halo.showMsg(data.msg,'error',2000);
            }
        })
    }
</script>
</@footer>
</#compress>
