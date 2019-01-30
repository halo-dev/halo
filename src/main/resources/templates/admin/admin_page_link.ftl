<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.pages.links.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1><@spring.message code='admin.pages.links.title' /><small></small></h1>
        <ol class="breadcrumb">
            <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i><@spring.message code='admin.index.bread.index' /></a></li>
            <li><a data-pjax="true" href="/admin/page"><@spring.message code='admin.pages.title' /></a></li>
            <li class="active"><@spring.message code='admin.pages.links.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-md-5">
                <div class="box box-primary">
                    <#if updateLink??>
                        <div class="box-header with-border"><h3 class="box-title"><@spring.message code='admin.pages.links.text.edit-link' /></h3></div>
                        <form role="form" id="linkSaveForm">
                            <input type="hidden" name="linkId" value="${updateLink.linkId?c}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="linkName"><@spring.message code='admin.pages.links.form.link-name' /></label>
                                    <input type="text" class="form-control" id="linkName" name="linkName" value="${updateLink.linkName!}">
                                    <small><@spring.message code='admin.pages.links.form.link-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="linkUrl"><@spring.message code='admin.pages.links.form.link-url' /></label>
                                    <input type="url" class="form-control" id="linkUrl" name="linkUrl" value="${updateLink.linkUrl!}">
                                    <small><@spring.message code='admin.pages.links.form.link-url-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="linkPic">LOGO</label>
                                    <input type="text" class="form-control" id="linkPic" name="linkPic" value="${updateLink.linkPic!}">
                                    <small><@spring.message code='admin.pages.links.form.link-pic-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="linkDesc"><@spring.message code='admin.pages.links.form.link-desc' /></label>
                                    <textarea class="form-control" rows="3" id="linkDesc" name="linkDesc" style="resize: none">${updateLink.linkDesc!}</textarea>
                                    <small><@spring.message code='admin.pages.links.form.link-desc-tips' /></small>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm" onclick="save()"><@spring.message code='common.btn.define-edit' /></button>
                                <a data-pjax="true" href="/admin/page/links" class="btn btn-info btn-sm"><@spring.message code='common.btn.back-to-add' /></a>
                            </div>
                        </form>
                    <#else>
                        <div class="box-header with-border"><h3 class="box-title"><@spring.message code='admin.pages.links.text.add-link' /></h3></div>
                        <form role="form" id="linkSaveForm">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="linkName"><@spring.message code='admin.pages.links.form.link-name' /></label>
                                    <input type="text" class="form-control" id="linkName" name="linkName" >
                                    <small><@spring.message code='admin.pages.links.form.link-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="linkUrl"><@spring.message code='admin.pages.links.form.link-url' /></label>
                                    <input type="text" class="form-control" id="linkUrl" name="linkUrl" >
                                    <small><@spring.message code='admin.pages.links.form.link-url-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="linkPic">LOGO</label>
                                    <input type="text" class="form-control" id="linkPic" name="linkPic" >
                                    <small><@spring.message code='admin.pages.links.form.link-pic-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="linkDesc"><@spring.message code='admin.pages.links.form.link-desc' /></label>
                                    <textarea class="form-control" rows="3" id="linkDesc" name="linkDesc" style="resize: none"></textarea>
                                    <small><@spring.message code='admin.pages.links.form.link-desc-tips' /></small>
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
                    <div class="box-header with-border"><h3 class="box-title"><@spring.message code='admin.pages.links.text.all-links' /></h3></div>
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.name' /></th>
                                    <th><@spring.message code='common.th.site' /></th>
                                    <th><@spring.message code='common.th.desc' /></th>
                                    <th><@spring.message code='common.th.control' /></th>
                                </tr>
                                <@commonTag method="links">
                                    <#if links?? && links?size gt 0>
                                        <#list links as link>
                                            <tr>
                                                <td>${link.linkName}</td>
                                                <td>${link.linkUrl}</td>
                                                <td width="30%">${link.linkDesc}</td>
                                                <td>
                                                    <#if updateLink?? && updateLink.linkId?c==link.linkId?c>
                                                        <a class="btn btn-primary btn-xs" href="javascript:void(0)" disabled><@spring.message code='common.btn.editing' /></a>
                                                    <#else >
                                                    <a data-pjax="true" class="btn btn-primary btn-xs" href="/admin/page/links/edit?linkId=${link.linkId?c}"><@spring.message code='common.btn.modify' /></a>
                                                    </#if>
                                                    <button class="btn btn-danger btn-xs" onclick="modelShow('/admin/page/links/remove?linkId=${link.linkId?c}')"><@spring.message code='common.btn.delete' /></>
                                                </td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </@commonTag>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <!-- 删除确认弹出层 -->
    <div class="modal fade" id="removeLinkModal">
        <div class="modal-dialog">
            <div class="modal-content message_align">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title"><@spring.message code='common.text.tips' /></h4>
                </div>
                <div class="modal-body"><p><@spring.message code='common.text.define-delete' /></p></div>
                <div class="modal-footer">
                    <input type="hidden" id="url"/>
                    <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message code='common.btn.cancel' /></button>
                    <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal"><@spring.message code='common.btn.define' /></a>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    function modelShow(url) {
        $('#url').val(url);
        $('#removeLinkModal').modal();
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
        var param = $("#linkSaveForm").serialize();
        $.post("/admin/page/links/save",param,function (data) {
            if (data.code === 1) {
                halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/page/links',"${options.admin_pjax!'true'}");
            } else {
                halo.showMsg(data.msg,'error',2000);
            }
        })
    }
</script>
</@footer>
</#compress>
