<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.categories.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1>
            <@spring.message code='admin.categories.title' />
            <small></small>
        </h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin">
                    <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.categories.bread.posts' /></a></li>
            <li class="active"><@spring.message code='admin.categories.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-md-5">
                <div class="box box-primary">
                    <#if updateCategory??>
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.categories.text.edit-category' /> <#if updateCategory??>[${updateCategory.cateName}]</#if></h3>
                        </div>
                        <form role="form" id="cateSaveForm">
                            <input type="hidden" name="cateId" value="${updateCategory.cateId?c}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="cateName"><@spring.message code='admin.categories.form.cate-name' /></label>
                                    <input type="text" class="form-control" id="cateName" name="cateName" value="${updateCategory.cateName!}">
                                    <small><@spring.message code='admin.categories.form.cate-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="cateUrl"><@spring.message code='admin.categories.form.cate-url' /></label>
                                    <input type="text" class="form-control" id="cateUrl" name="cateUrl" value="${updateCategory.cateUrl!}">
                                    <small><@spring.message code='admin.categories.form.cate-url-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="cateDesc" class="control-label"><@spring.message code='admin.categories.form.cate-desc' /></label>
                                    <textarea class="form-control" rows="3" id="cateDesc" name="cateDesc" style="resize: none">${updateCategory.cateDesc!}</textarea>
                                    <small><@spring.message code='admin.categories.form.cate-desc-tips' /></small>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm" onclick="save()"><@spring.message code='common.btn.define-edit' /></button>
                                <a data-pjax="true" href="/admin/category" class="btn btn-info btn-sm "><@spring.message code='common.btn.back-to-add' /></a>
                            </div>
                        </form>
                    <#else >
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.categories.text.add-category' /></h3>
                        </div>
                        <form role="form" id="cateSaveForm">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="cateName"><@spring.message code='admin.categories.form.cate-name' /></label>
                                    <input type="text" class="form-control" id="cateName" name="cateName">
                                    <small><@spring.message code='admin.categories.form.cate-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="cateUrl"><@spring.message code='admin.categories.form.cate-url' /></label>
                                    <input type="text" class="form-control" id="cateUrl" name="cateUrl">
                                    <small><@spring.message code='admin.categories.form.cate-url-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="cateDesc" class="control-label"><@spring.message code='admin.categories.form.cate-desc' /></label>
                                    <textarea class="form-control" rows="3" id="cateDesc" name="cateDesc" style="resize: none"></textarea>
                                    <small><@spring.message code='admin.categories.form.cate-desc-tips' /></small>
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
                        <h3 class="box-title"><@spring.message code='admin.categories.text.all-categories' /></h3>
                    </div>
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.name' /></th>
                                    <th><@spring.message code='common.th.url' /></th>
                                    <th><@spring.message code='common.th.desc' /></th>
                                    <th><@spring.message code='common.th.posts-count' /></th>
                                    <th><@spring.message code='common.th.control' /></th>
                                </tr>
                                <@commonTag method="categories">
                                    <#if categories?? && categories?size gt 0>
                                        <#list categories as cate>
                                            <tr>
                                                <td>${cate.cateName!}</td>
                                                <td>${cate.cateUrl!}</td>
                                                <td>${cate.cateDesc!}</td>
                                                <td>
                                                    <span class="label" style="background-color: #d6cdcd;">${cate.posts?size}</span>
                                                </td>
                                                <td>
                                                    <#if updateCategory?? && updateCategory.cateId?c==cate.cateId?c>
                                                        <a href="javascript:void(0)" class="btn btn-primary btn-xs " disabled><@spring.message code='common.btn.editing' /></a>
                                                    <#else >
                                                        <a data-pjax="true" href="/admin/category/edit?cateId=${cate.cateId?c}" class="btn btn-primary btn-xs "><@spring.message code='common.btn.modify' /></a>
                                                    </#if>
                                                    <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/category/remove?cateId=${cate.cateId?c}')"><@spring.message code='common.btn.delete' /></button>
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
    <div class="modal fade" id="removeCateModal">
        <div class="modal-dialog">
            <div class="modal-content message_align">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title"><@spring.message code='common.text.tips' /></h4>
                </div>
                <div class="modal-body">
                    <p><@spring.message code='common.text.tips.to-delete' /></p>
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
        var param = $("#cateSaveForm").serialize();
        $.post('/admin/category/save',param,function (data) {
            if (data.code === 1) {
                halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/category',"${options.admin_pjax!'true'}");
            } else {
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON');
    }
</script>
</@footer>
</#compress>
