<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.pages.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.pages.title' /><small></small></h1>
        <a class="btn-header" id="btnNewPage" href="/admin/page/new">
            <@spring.message code='admin.pages.btn.new-page' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.pages.title' /></a></li>
            <li class="active"><@spring.message code='admin.pages.bread.all-pages' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-md-12">
                <div class="nav-tabs-custom">
                    <ul class="nav nav-tabs">
                        <li class="active">
                            <a href="#internal" data-toggle="tab"><@spring.message code='admin.pages.tab.internal-page' /></a>
                        </li>
                        <li>
                            <a href="#pages" data-toggle="tab"><@spring.message code='admin.pages.tab.custom-page' /></a>
                        </li>
                    </ul>
                    <div class="tab-content" style="padding: 0;">
                        <div class="tab-pane active" id="internal">
                            <div class="box-body table-responsive no-padding">
                                <table class="table table-hover">
                                    <tbody>
                                        <tr>
                                            <th><@spring.message code='common.th.title' /></th>
                                            <th><@spring.message code='common.th.url' /></th>
                                            <th><@spring.message code='common.th.control' /></th>
                                        </tr>
                                        <tr>
                                            <td><@spring.message code='admin.pages.links' /></td>
                                            <td>/links</td>
                                            <td>
                                                <a href="/links" class="btn btn-info btn-xs " target="_blank"><@spring.message code='common.btn.view' /></a>
                                                <a data-pjax="true" href="/admin/page/links" class="btn btn-primary btn-xs "><@spring.message code='common.btn.edit' /></a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><@spring.message code='admin.pages.gallery' /></td>
                                            <td>/gallery</td>
                                            <td>
                                                <a href="/gallery" class="btn btn-info btn-xs " target="_blank"><@spring.message code='common.btn.view' /></a>
                                                <a data-pjax="true" href="/admin/page/galleries" class="btn btn-primary btn-xs "><@spring.message code='common.btn.edit' /></a>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="tab-pane" id="pages">
                            <div class="box-body table-responsive no-padding">
                                <table class="table table-hover">
                                    <tbody>
                                        <tr>
                                            <th><@spring.message code='common.th.title' /></th>
                                            <th><@spring.message code='common.th.url' /></th>
                                            <th><@spring.message code='common.th.comments' /></th>
                                            <td><@spring.message code='common.th.views' /></td>
                                            <th><@spring.message code='common.th.status' /></th>
                                            <th><@spring.message code='common.th.date' /></th>
                                            <th><@spring.message code='common.th.control' /></th>
                                        </tr>
                                        <#if pages?size gt 0>
                                            <#list pages as page>
                                                <tr>
                                                    <td>${page.postTitle}</td>
                                                    <td>/p/${page.postUrl}</td>
                                                    <td>
                                                        <span class="label" style="background-color: #d6cdcd;">${page.comments?size}</span>
                                                    </td>
                                                    <td>
                                                        <span class="label" style="background-color: #d6cdcd;">${page.postViews}</span>
                                                    </td>
                                                    <td>
                                                        <#if page.postStatus==0>
                                                            <span class="label bg-green"><@spring.message code='common.status.published' /></span>
                                                        <#elseif page.postStatus==1>
                                                            <span class="label bg-yellow"><@spring.message code='common.status.draft' /></span>
                                                        <#else>
                                                            <span class="label bg-red"><@spring.message code='common.status.recycle-bin' /></span>
                                                        </#if>
                                                    </td>
                                                    <td>${page.postDate?string("yyyy-MM-dd HH:mm")}</td>
                                                    <td>
                                                        <a href="/p/${page.postUrl}" class="btn btn-info btn-xs " target="_blank"><@spring.message code='common.btn.view' /></a>
                                                        <a data-pjax="true" href="/admin/page/edit?pageId=${page.postId?c}" class="btn btn-primary btn-xs "><@spring.message code='common.btn.edit' /></a>
                                                        <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/posts/remove?postId=${page.postId?c}&postType=${page.postType}','<@spring.message code="common.text.tips.to-delete" />')"><@spring.message code='common.btn.delete' /></button>
                                                    </td>
                                                </tr>
                                            </#list>
                                            <#else>
                                            <tr>
                                                <td colspan="7" style="text-align: center;"><@spring.message code='common.text.no-data' /></td>
                                            </tr>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <!-- 删除确认弹出层 -->
    <div class="modal fade" id="removePostModal">
        <div class="modal-dialog">
            <div class="modal-content message_align">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title"><@spring.message code='common.text.tips' /></h4>
                </div>
                <div class="modal-body">
                    <p id="message"></p>
                </div>
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
    function modelShow(url,message) {
        $('#url').val(url);
        $('#message').html(message);
        $('#removePostModal').modal();
    }
    function removeIt(){
        var url=$.trim($("#url").val());
        <#if (options.admin_pjax!'true') == 'true'>
            pjax.loadUrl(url);
        <#else>
            window.location.href = url;
        </#if>
    }
</script>
</@footer>
</#compress>
