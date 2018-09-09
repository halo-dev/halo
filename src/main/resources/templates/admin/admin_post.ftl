<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.posts.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            .draft,.publish,.trash{list-style:none;float:left;margin:0;padding-bottom:10px}
            #btnNewPost{margin-left:4px;padding:3px 6px;position:relative;top:-4px;border:1px solid #ccc;border-radius:2px;background:#fff;text-shadow:none;font-weight:600;font-size:12px;line-height:normal;color:#3c8dbc;cursor:pointer;transition:all .2s ease-in-out}
            #btnNewPost:hover{background:#3c8dbc;color:#fff}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.posts.title' /></h1>
            <a data-pjax="false" id="btnNewPost" href="/admin/posts/new">
                <@spring.message code='admin.posts.btn.new-post' />
            </a>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin">
                        <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li><a data-pjax="true" href="#"><@spring.message code='admin.posts.title' /></a></li>
                <li class="active"><@spring.message code='admin.posts.bread.all-posts' /></li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-xs-12">
                    <ul style="list-style: none;padding-left: 0">
                        <li class="publish">
                            <a data-pjax="true" href="/admin/posts" <#if status==0>style="color: #000" </#if>><@spring.message code='common.status.published' /><span class="count">(${publishCount})</span></a>&nbsp;|&nbsp;
                        </li>
                        <li class="draft">
                            <a data-pjax="true" href="/admin/posts?status=1" <#if status==1>style="color: #000" </#if>><@spring.message code='common.status.draft' /><span class="count">(${draftCount})</span></a>&nbsp;|&nbsp;
                        </li>
                        <li class="trash">
                            <a data-pjax="true" href="/admin/posts?status=2" <#if status==2>style="color: #000" </#if>><@spring.message code='common.status.recycle-bin' /><span class="count">(${trashCount})</span></a>
                        </li>
                    </ul>
                </div>
                <div class="col-xs-12">
                    <div class="box box-primary">
                        <div class="box-body table-responsive">
                            <table class="table table-bordered table-hover">
                                <thead>
                                <tr>
                                    <th><@spring.message code='common.th.title' /></th>
                                    <th><@spring.message code='common.th.categories' /></th>
                                    <th><@spring.message code='common.th.tags' /></th>
                                    <th><@spring.message code='common.th.comments' /></th>
                                    <th><@spring.message code='common.th.views' /></th>
                                    <th><@spring.message code='common.th.date' /></th>
                                    <th><@spring.message code='common.th.control' /></th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if posts.content?size gt 0>
                                        <#list posts.content as post>
                                            <tr>
                                                <#if post.postTitle?length gt 20>
                                                    <td>${post.postTitle?substring(0,20)}...</td>
                                                <#else >
                                                    <td>${post.postTitle}</td>
                                                </#if>
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
                                                    <span class="label" style="background-color: #d6cdcd;">${post.postViews}</span>
                                                </td>
                                                <td>${post.postDate?if_exists?string("yyyy-MM-dd HH:mm")}</td>
                                                <td>
                                                    <#switch post.postStatus>
                                                        <#case 0>
                                                            <a href="/archives/${post.postUrl}" class="btn btn-primary btn-xs " target="_blank"><@spring.message code='common.btn.view' /></a>
                                                            <a href="/admin/posts/edit?postId=${post.postId?c}" class="btn btn-info btn-xs "><@spring.message code='common.btn.edit' /></a>
                                                            <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/posts/throw?postId=${post.postId?c}&status=0','<@spring.message code="common.text.tips.to-recycle-bin" />')"><@spring.message code='common.btn.recycling' /></button>
                                                            <#break >
                                                        <#case 1>
                                                            <a href="/admin/posts/edit?postId=${post.postId?c}"
                                                               class="btn btn-info btn-xs "><@spring.message code="common.btn.edit" /></a>
                                                            <button class="btn btn-primary btn-xs " onclick="modelShow('/admin/posts/revert?postId=${post.postId?c}&status=1','<@spring.message code="common.text.tips.to-release-post" />')"><@spring.message code='common.btn.release' /></button>
                                                            <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/posts/throw?postId=${post.postId?c}&status=1','<@spring.message code="common.text.tips.to-recycle-bin" />')"><@spring.message code='common.btn.recycling' /></button>
                                                            <#break >
                                                        <#case 2>
                                                            <a href="/admin/posts/revert?postId=${post.postId?c}&status=2" class="btn btn-primary btn-xs "><@spring.message code='common.btn.reduction' /></a>
                                                            <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/posts/remove?postId=${post.postId?c}&postType=${post.postType}','<@spring.message code="common.text.tips.to-delete" />')"><@spring.message code='common.btn.delete' /></button>
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
                            <ul class="pagination no-margin pull-right">
                                <li><a data-pjax="true" class="btn btn-sm <#if !posts.hasPrevious()>disabled</#if>" href="/admin/posts?status=${status}"><@spring.message code='admin.pageinfo.btn.first' /></a> </li>
                                <li><a data-pjax="true" class="btn btn-sm <#if !posts.hasPrevious()>disabled</#if>" href="/admin/posts?status=${status}&page=${posts.number-1}"><@spring.message code='admin.pageinfo.btn.pre' /></a></li>
                                <li><a data-pjax="true" class="btn btn-sm <#if !posts.hasNext()>disabled</#if>" href="/admin/posts?status=${status}&page=${posts.number+1}"><@spring.message code='admin.pageinfo.btn.next' /></a></li>
                                <li><a data-pjax="true" class="btn btn-sm <#if !posts.hasNext()>disabled</#if>" href="/admin/posts?page=${posts.totalPages-1}&status=${status}"><@spring.message code='admin.pageinfo.btn.last' /></a> </li>
                            </ul>
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
        <script>
            function modelShow(url,message) {
                $('#url').val(url);
                $('#message').html(message);
                $('#removePostModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>
