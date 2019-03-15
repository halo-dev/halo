<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.posts.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.posts.title' /></h1>
        <a data-pjax="true" class="btn-header" id="btnNewPost" href="/admin/posts/write">
            <@spring.message code='admin.posts.btn.new-post' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin">
                    <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' />
                </a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.posts.title' /></a></li>
            <li class="active"><@spring.message code='admin.posts.bread.all-posts' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-xs-12">
                <ul class="subbutton">
                    <li class="publish">
                        <a data-pjax="true" href="/admin/posts" <#if status==0>class="current"</#if>>
                            <@spring.message code='common.status.published' /><span class="count">(${publishCount})</span>
                        </a> |
                    </li>
                    <li class="draft">
                        <a data-pjax="true" href="/admin/posts?status=1" <#if status==1>class="current"</#if>>
                            <@spring.message code='common.status.draft' /><span class="count">(${draftCount})</span>
                        </a> |
                    </li>
                    <li class="trash">
                        <a data-pjax="true" href="/admin/posts?status=2" <#if status==2>class="current"</#if>>
                            <@spring.message code='common.status.recycle-bin' /><span class="count">(${trashCount})</span>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <div class="box box-primary">
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.title' /></th>
                                    <th><@spring.message code='common.th.categories' /></th>
                                    <th><@spring.message code='common.th.tags' /></th>
                                    <th><@spring.message code='common.th.comments' /></th>
                                    <th><@spring.message code='common.th.views' /></th>
                                    <th><@spring.message code='common.th.date' /></th>
                                    <th><@spring.message code='common.th.control' /></th>
                                </tr>
                                <#if posts.content?size gt 0>
                                    <#list posts.content as post>
                                        <tr>
                                            <td>
                                                <#if post.status==0>
                                                    <a target="_blank" href="/archives/${post.url!}">${post.title!}</a>
                                                <#else>
                                                    ${post.title!}
                                                </#if>
                                            </td>
                                            <td>
                                                <#if post.categories?size gt 0>
                                                    <#list post.categories as cate>
                                                        <label>${cate.cateName}</label>
                                                    </#list>
                                                <#else >
                                                    <label>无分类</label>
                                                </#if>
                                            </td>
                                            <td>
                                                <#if post.tags?size gt 0>
                                                    <#list post.tags as tag>
                                                        <label>${tag.tagName}</label>
                                                    </#list>
                                                <#else >
                                                    <label>无标签</label>
                                                </#if>
                                            </td>
                                            <td>
                                                <span class="label" style="background-color: #d6cdcd;">${post.getComments()?size}</span>
                                            </td>
                                            <td>
                                                <span class="label" style="background-color: #d6cdcd;">${post.visits!0}</span>
                                            </td>
                                            <td>${post.createTime!?string("yyyy-MM-dd HH:mm")}</td>
                                            <td>
                                                <#switch post.status>
                                                    <#case 0>
                                                        <#if post.topPriority == 0>
                                                            <button class="btn btn-primary btn-xs" onclick="modalShow('/admin/posts/topPost?postId=${post.id?c}&priority=1','是否置顶该文章？')">置顶</button>
                                                        <#else>
                                                            <button class="btn btn-primary btn-xs" onclick="modalShow('/admin/posts/topPost?postId=${post.id?c}&priority=0','是否取消置顶该文章？')">取消置顶</button>
                                                        </#if>
                                                        <a data-pjax="true" href="/admin/posts/edit?postId=${post.id?c}" class="btn btn-info btn-xs"><@spring.message code='common.btn.edit' /></a>
                                                        <button class="btn btn-danger btn-xs" onclick="modalShow('/admin/posts/throw?postId=${post.id?c}&status=0','<@spring.message code="common.text.tips.to-recycle-bin" />')"><@spring.message code='common.btn.recycling' /></button>
                                                        <#break >
                                                    <#case 1>
                                                        <a data-pjax="true" href="/admin/posts/edit?postId=${post.id?c}"
                                                           class="btn btn-info btn-xs"><@spring.message code="common.btn.edit" /></a>
                                                        <button class="btn btn-primary btn-xs" onclick="modalShow('/admin/posts/revert?postId=${post.id?c}&status=1','<@spring.message code="common.text.tips.to-release-post" />')"><@spring.message code='common.btn.release' /></button>
                                                        <button class="btn btn-danger btn-xs" onclick="modalShow('/admin/posts/throw?postId=${post.id?c}&status=1','<@spring.message code="common.text.tips.to-recycle-bin" />')"><@spring.message code='common.btn.recycling' /></button>
                                                        <#break >
                                                    <#case 2>
                                                        <a data-pjax="true" href="/admin/posts/revert?postId=${post.id?c}&status=2" class="btn btn-primary btn-xs "><@spring.message code='common.btn.reduction' /></a>
                                                        <button class="btn btn-danger btn-xs" onclick="modalShow('/admin/posts/remove?postId=${post.id?c}&postType=${post.type}','<@spring.message code="common.text.tips.to-delete" />')"><@spring.message code='common.btn.delete' /></button>
                                                        <#break >
                                                </#switch>
                                            </td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <th colspan="7" style="text-align: center"><@spring.message code='common.text.no-data' /></th>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="box-footer clearfix">
                        <div class="no-margin pull-left">
                            <@spring.message code='admin.pageinfo.text.no' />${posts.number+1}/${posts.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                        </div>
                        <div class="btn-group pull-right btn-group-sm" role="group">
                            <a data-pjax="true" class="btn btn-default <#if !posts.hasPrevious()>disabled</#if>" href="/admin/posts?status=${status}">
                                <@spring.message code='admin.pageinfo.btn.first' />
                            </a>
                            <a data-pjax="true" class="btn btn-default <#if !posts.hasPrevious()>disabled</#if>" href="/admin/posts?status=${status}&page=${posts.number-1}">
                                <@spring.message code='admin.pageinfo.btn.pre' />
                            </a>
                            <a data-pjax="true" class="btn btn-default <#if !posts.hasNext()>disabled</#if>" href="/admin/posts?status=${status}&page=${posts.number+1}">
                                <@spring.message code='admin.pageinfo.btn.next' />
                            </a>
                            <a data-pjax="true" class="btn btn-default <#if !posts.hasNext()>disabled</#if>" href="/admin/posts?page=${posts.totalPages-1}&status=${status}">
                                <@spring.message code='admin.pageinfo.btn.last' />
                            </a>
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
                    <a onclick="modalAction()" class="btn btn-danger" data-dismiss="modal"><@spring.message code='common.btn.define' /></a>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    function modalShow(url,message) {
        $('#url').val(url);
        $('#message').html(message);
        $('#removePostModal').modal();
    }
    function modalAction(){
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
