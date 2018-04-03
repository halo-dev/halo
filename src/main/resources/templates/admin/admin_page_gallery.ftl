<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-图库">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            #btnNewPicture{margin-left:4px;padding:3px 6px;position:relative;top:-4px;border:1px solid #ccc;border-radius:2px;background:#fff;text-shadow:none;font-weight:600;font-size:12px;line-height:normal;color:#3c8dbc;cursor:pointer;transition:all .2s ease-in-out}
            #btnNewPicture:hover{background:#3c8dbc;color:#fff}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;">图库<small></small></h1>
            <a id="btnNewPicture" href="#">
                添加图片
            </a>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a data-pjax="true" href="/admin/page">页面</a></li>
                <li class="active">图库</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-lg-12 col-xs-12" id="newPicturePanel" style="display: none">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">添加图片</h3>
                        </div>
                        <form class="form-horizontal" id="widgetsOption">
                            <div class="box-body">
                                <div class="col-sm-6 col-xs-6">
                                    <div class="form-group">
                                        <label for="widgetPostCount" class="col-sm-4 control-label">文章总数：</label>
                                        <div class="col-sm-8">
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_postcount" id="widgetPostCount" value="true" ${((options.widget_postcount?default('true'))=='true')?string('checked','')}> 显示
                                            </label>
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_postcount" id="widgetPostCount" value="false" ${((options.widget_postcount?default('true'))=='false')?string('checked','')}> 隐藏
                                            </label>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="widgetCommentCount" class="col-sm-4 control-label">评论总数：</label>
                                        <div class="col-sm-8">
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_commentcount" id="widgetCommentCount" value="true" ${((options.widget_commentcount?default('true'))=='true')?string('checked','')}> 显示
                                            </label>
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_commentcount" id="widgetCommentCount" value="false" ${((options.widget_commentcount?default('true'))=='false')?string('checked','')}> 隐藏
                                            </label>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="widgetAttachmentCount" class="col-sm-4 control-label">附件总数：</label>
                                        <div class="col-sm-8">
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_attachmentcount" id="widgetAttachmentCount" value="true" ${((options.widget_attachmentcount?default('true'))=='true')?string('checked','')}> 显示
                                            </label>
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_attachmentcount" id="widgetAttachmentCount" value="false" ${((options.widget_attachmentcount?default('true'))=='false')?string('checked','')}> 隐藏
                                            </label>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="widgetDayCount" class="col-sm-4 control-label">成立天数：</label>
                                        <div class="col-sm-8">
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_daycount" id="widgetDayCount" value="true" ${((options.widget_daycount?default('true'))=='true')?string('checked','')}> 显示
                                            </label>
                                            <label class="radio-inline">
                                                <input type="radio" name="widget_daycount" id="widgetDayCount" value="false" ${((options.widget_daycount?default('true'))=='false')?string('checked','')}> 隐藏
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-info pull-right" onclick="saveOptions('widgetsOption')">保存</button>
                            </div>
                        </form>
                    </div>
                </div>
                <#if options.widget_postcount?default("true")=="true">
                <div class="col-lg-3 col-xs-6" id="widgetPostCountBody">
                    <!-- small box -->
                    <div class="small-box bg-aqua">
                        <div class="inner"><h3>${postCount?default(0)}</h3><p>文章</p></div>
                        <div class="icon"><i class="ion ion-bag"></i></div>
                        <a href="/admin/posts" class="small-box-footer">查看所有 <i class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>
                </#if>
                <#if options.widget_commentcount?default("true")=="true">
                <div class="col-lg-3 col-xs-6" id="widgetCommentCountBody">
                    <!-- small box -->
                    <div class="small-box bg-green">
                        <div class="inner"><h3>${commentCount?default(0)}</h3><p>评论</p></div>
                        <div class="icon"><i class="ion ion-stats-bars"></i></div>
                        <a href="/admin/comments" class="small-box-footer">查看所有 <i class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>
                </#if>
                <!-- ./col -->
                <#if options.widget_attachmentcount?default("true")=="true">
                <div class="col-lg-3 col-xs-6" id="widgetAttachmentCountBody">
                    <!-- small box -->
                    <div class="small-box bg-yellow">
                        <div class="inner"><h3>${mediaCount?default(0)}</h3><p>媒体库</p></div>
                        <div class="icon"><i class="ion ion-person-add"></i></div>
                        <a href="/admin/attachments" class="small-box-footer">上传图片 <i class="fa fa-arrow-circle-right"></i></a>
                    </div>
                </div>
                </#if>
                <!-- ./col -->
                <#if options.widget_daycount?default("true")=="true">
                <div class="col-lg-3 col-xs-6" id="widgetDayCountBody">
                    <!-- small box -->
                    <div class="small-box bg-red">
                        <div class="inner"><h3 id="siteStart">1</h3><p>成立天数</p></div>
                        <div class="icon"><i class="ion ion-pie-graph"></i></div>
                        <a href="#" class="small-box-footer">${options.site_start?default('0000-00-00')} <i class="fa fa-star"></i></a>
                    </div>
                </div>
                </#if>
                <!-- ./col -->
            </div>
        </section>
        <script type="application/javascript">
            $('#btnNewPicture').click(function () {
                $('#newPicturePanel').slideToggle(400);
            });
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>