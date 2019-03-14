<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.index.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.index.title' /></h1>
        <a class="btn-header" id="btnWidgetsOption" href="javascript:void(0)">
            <@spring.message code='admin.index.btn.widgets' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li class="active"><@spring.message code='admin.index.bread.active' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-lg-12 col-xs-12" id="widgetOptionsPanel" style="display: none">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.index.widgets.title' /></h3>
                    </div>
                    <form class="form-horizontal" id="widgetsOption">
                        <div class="box-body">
                            <div class="col-sm-6 col-xs-6">
                                <div class="form-group">
                                    <label for="widgetPostCount" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.post-count' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_postcount" id="widgetPostCount" value="true" ${((options.widget_postcount!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_postcount" id="widgetPostCount" value="false" ${((options.widget_postcount!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="widgetCommentCount" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.comment-count' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_commentcount" id="widgetCommentCount" value="true" ${((options.widget_commentcount!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_commentcount" id="widgetCommentCount" value="false" ${((options.widget_commentcount!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="widgetAttachmentCount" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.attachment-count' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_attachmentcount" id="widgetAttachmentCount" value="true" ${((options.widget_attachmentcount!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_attachmentcount" id="widgetAttachmentCount" value="false" ${((options.widget_attachmentcount!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="widgetDayCount" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.day-count' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_daycount" id="widgetDayCount" value="true" ${((options.widget_daycount!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_daycount" id="widgetDayCount" value="false" ${((options.widget_daycount!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-6 col-xs-6">
                                <div class="form-group">
                                    <label for="widgetPostLastest" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.post-lastest' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_postlastest" id="widgetPostLastest" value="true" ${((options.widget_postlastest!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_postlastest" id="widgetPostLastest" value="false" ${((options.widget_postlastest!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="widgetCommentLastest" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.comment-lastest' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_commentlastest" id="widgetCommentLastest" value="true" ${((options.widget_commentlastest!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_commentlastest" id="widgetCommentLastest" value="false" ${((options.widget_commentlastest!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="widgetLogsLastest" class="col-sm-4 control-label"><@spring.message code='admin.index.widgets.logs-lastest' />：</label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_logslastest" id="widgetLogsLastest" value="true" ${((options.widget_logslastest!'true')=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.display' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="widget_logslastest" id="widgetLogsLastest" value="false" ${((options.widget_logslastest!'true')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.hide' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button type="button" class="btn btn-primary pull-right" onclick="saveOptions('widgetsOption')"><@spring.message code='common.btn.save' /></button>
                        </div>
                    </form>
                </div>
            </div>
            <#if (options.widget_postcount!'true')=='true'>
            <div class="col-lg-3 col-xs-6" id="widgetPostCountBody">
                <div class="small-box bg-aqua">
                    <div class="inner"><h3><@articleTag method="postsCount">${postsCount!0}</@articleTag></h3><p><@spring.message code='admin.index.widgets.posts' /></p></div>
                    <div class="icon"><i class="ion ion-bag"></i></div>
                    <a data-pjax="true" href="/admin/posts" class="small-box-footer"><@spring.message code='common.btn.view-all' /> <i class="fa fa-arrow-circle-right"></i></a>
                </div>
            </div>
            </#if>
            <#if (options.widget_commentcount!'true')=='true'>
            <div class="col-lg-3 col-xs-6" id="widgetCommentCountBody">
                <div class="small-box bg-green">
                    <div class="inner"><h3>${commentCount!0}</h3><p><@spring.message code='admin.index.widgets.comments' /></p></div>
                    <div class="icon"><i class="ion ion-stats-bars"></i></div>
                    <a data-pjax="true" href="/admin/comments" class="small-box-footer"><@spring.message code='common.btn.view-all' /> <i class="fa fa-arrow-circle-right"></i></a>
                </div>
            </div>
            </#if>
            <#if (options.widget_attachmentcount!'true')=='true'>
            <div class="col-lg-3 col-xs-6" id="widgetAttachmentCountBody">
                <div class="small-box bg-yellow">
                    <div class="inner"><h3>${mediaCount!0}</h3><p><@spring.message code='admin.index.widgets.attachments' /></p></div>
                    <div class="icon"><i class="ion ion-person-add"></i></div>
                    <a data-pjax="true" href="/admin/attachments" class="small-box-footer"><@spring.message code='common.btn.upload-image' /> <i class="fa fa-arrow-circle-right"></i></a>
                </div>
            </div>
            </#if>
            <#if (options.widget_daycount!'true')=='true'>
            <div class="col-lg-3 col-xs-6" id="widgetDayCountBody">
                <div class="small-box bg-red">
                    <div class="inner"><h3 id="blogStart">${hadDays!}</h3><p><@spring.message code='admin.index.widgets.day-count' /></p></div>
                    <div class="icon"><i class="ion ion-pie-graph"></i></div>
                    <a href="javascript:void(0)" class="small-box-footer" data-toggle="modal" data-target="#blogInfo">${options.blog_start!'0000-00-00'} <i class="fa fa-star"></i></a>
                </div>
            </div>
            </#if>
        </div>

        <div class="row">
            <#if (options.widget_postlastest!'true')=='true'>
            <div class="col-lg-6 col-xs-12" id="widgetPostLastestBody">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.index.widgets.post-lastest' /></h3>
                        <div class="box-tools">
                            <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                <i class="fa fa-minus"></i>
                            </button>
                        </div>
                    </div>
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover text-center">
                            <tbody>
                                <tr>
                                    <th width="50%"><@spring.message code='common.th.title' /></th>
                                    <th><@spring.message code='common.th.status' /></th>
                                    <th><@spring.message code='common.th.date' /></th>
                                </tr>
                                <#if postTopFive??>
                                    <#list postTopFive as post>
                                        <tr>
                                            <#if post.postStatus == 0>
                                                <td><a target="_blank" href="/archives/${post.postUrl}">${post.postTitle}</a></td>
                                            <#else >
                                                <td>${post.postTitle}</td>
                                            </#if>
                                            <td class="text-center">
                                                <#if post.postStatus==0>
                                                    <span class="label bg-green"><@spring.message code='common.status.published' /></span>
                                                <#elseif post.postStatus==1>
                                                    <span class="label bg-yellow"><@spring.message code='common.status.draft' /></span>
                                                <#else>
                                                    <span class="label bg-red"><@spring.message code='common.status.recycle-bin' /></span>
                                                </#if>
                                            </td>
                                            <td><@common.timeline datetime="${post.postDate!}"?datetime /></td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr><td><@spring.message code='common.text.no-data' /></td></tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            </#if>
            <#if (options.widget_commentlastest!'true')=='true'>
            <div class="col-lg-6 col-xs-12" id="widgetCommentLastestBody">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.index.widgets.comment-lastest' /></h3>
                        <div class="box-tools">
                            <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                <i class="fa fa-minus"></i>
                            </button>
                        </div>
                    </div>
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover text-center">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.comment-author' /></th>
                                    <th width="20%"><@spring.message code='common.th.comment-page' /></th>
                                    <th width="30%"><@spring.message code='common.th.content' /></th>
                                    <th><@spring.message code='common.th.status' /></th>
                                    <th><@spring.message code='common.th.date' /></th>
                                </tr>
                                <#if comments??>
                                <#list comments as comment>
                                    <tr>
                                        <td>${comment.commentAuthor}</td>
                                        <td>
                                            <#if comment.post.postType=="post">
                                                <a target="_blank" href="/archives/${comment.post.getPostUrl()}">${comment.post.postTitle}</a>
                                            <#else>
                                                <a target="_blank" href="/p/${comment.post.getPostUrl()}">${comment.post.postTitle}</a>
                                            </#if>
                                        </td>
                                        <td>
                                            <#switch comment.commentStatus>
                                                <#case 0>
                                                ${comment.commentContent}
                                                <#break>
                                                <#case 1>
                                                ${comment.commentContent}
                                                <#break>
                                                <#case 2>
                                                ${comment.commentContent}
                                                <#break>
                                            </#switch>
                                        </td>
                                        <td>
                                            <#switch comment.commentStatus>
                                            <#case 0>
                                            <span class="label bg-green"><@spring.message code='common.status.published' /></span>
                                            <#break >
                                            <#case 1>
                                            <span class="label bg-yellow"><@spring.message code='common.status.checking' /></span>
                                            <#break >
                                            <#case 2>
                                            <span class="label bg-red"><@spring.message code='common.status.recycle-bin' /></span>
                                            <#break >
                                            </#switch>
                                        </td>
                                        <td><@common.timeline datetime="${comment.commentDate}"?datetime /></td>
                                    </tr>
                                </#list>
                                <#else>
                                    <tr><td><@spring.message code='common.text.no-data' /></td></tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            </#if>
            <#if (options.widget_logslastest!'true')=='true'>
            <div class="col-lg-6 col-xs-12" id="widgetLogsLastestBody">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.index.widgets.logs-lastest' /></h3>
                        <div class="box-tools">
                            <div class="btn-group">
                                <button type="button" class="btn btn-box-tool dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                    <i class="fa fa-bars"></i></button>
                                <ul class="dropdown-menu pull-right" role="menu">
                                    <li><a href="javascript:void(0)" onclick="halo.layerModal('/admin/logs','<@spring.message code="admin.index.widgets.text.all-logs" />')"><@spring.message code='common.btn.view-all' /></a></li>
                                    <li><a href="/admin/logs/clear"><@spring.message code='admin.index.widgets.btn.clear-logs' /></a></li>
                                </ul>
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover text-center">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.action' /></th>
                                    <th><@spring.message code='common.th.result' /></th>
                                    <th>IP</th>
                                    <th><@spring.message code='common.th.date' /></th>
                                </tr>
                                <#if logs??>
                                <#list logs as log>
                                    <tr>
                                        <td>${log.logTitle}</td>
                                        <td>${log.logContent}</td>
                                        <td>${log.logIp}</td>
                                        <td>${log.logCreated?string("yyyy-MM-dd HH:mm")}</td>
                                    </tr>
                                </#list>
                                <#else>
                                    <tr><td><@spring.message code='common.text.no-data' /></td></tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            </#if>
        </div>
    </section>
    <div class="modal fade" id="blogInfo" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="blog-data"><@spring.message code='admin.index.blog-data.title' /></h4>
                </div>
                <div class="modal-body">
                    <p>「${options.blog_title!}」<@spring.message code='admin.index.blog-data.days-count-before' /><span id="blogStartDay">${hadDays!}</span><@spring.message code='admin.index.blog-data.days-count-after' /></p>
                    <p><@spring.message code='admin.index.blog-data.during' /></p>
                    <p><@spring.message code='admin.index.blog-data.posts-count-before' />&nbsp;<@articleTag method="postsCount">${postsCount!0}</@articleTag>&nbsp;<@spring.message code='admin.index.blog-data.posts-count-after' /></p>
                    <p><@spring.message code='admin.index.blog-data.tags-count-before' />&nbsp;<@commonTag method="tags">${tags?size}</@commonTag>&nbsp;<@spring.message code='admin.index.blog-data.tags-count-after' /></p>
                    <p><@spring.message code='admin.index.blog-data.comments-count-before' />&nbsp;${commentCount}&nbsp;<@spring.message code='admin.index.blog-data.comments-count-after' /></p>
                    <p><@spring.message code='admin.index.blog-data.links-count-before' />&nbsp;<@commonTag method="links">${links?size}</@commonTag>&nbsp;<@spring.message code='admin.index.blog-data.links-count-after' /></p>
                    <p><@spring.message code='admin.index.blog-data.views-count-before' />&nbsp;${postViewsSum!0}&nbsp;<@spring.message code='admin.index.blog-data.views-count-after' /></p>
                    <p><@spring.message code='admin.index.blog-data.motto' /></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-dismiss="modal"><@spring.message code='common.btn.define' /></button>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    $('#btnWidgetsOption').click(function () {
        $('#widgetOptionsPanel').slideToggle(400);
    });
</script>
</@footer>
</#compress>
