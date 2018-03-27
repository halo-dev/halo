<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-文章编辑">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <style type="text/css">
            #post_title{
                font-weight: 400;
            }
        </style>
        <section class="content-header">
            <h1>
                新建文章
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="#"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li>
                    <a data-pjax="true" href="/admin/posts">文章</a>
                </li>
                <li class="active">新建文章</li>
            </ol>
        </section>
        <section class="content">
            <div class="row">
                <div class="col-md-9">
                    <div style="margin-bottom: 10px;">
                        <input type="text" class="form-control input-lg" id="post_title" name="post_title" placeholder="请输入文章标题" value="<#if post??>${post.postTitle}</#if>">
                    </div>
                    <div style="display: block;margin-bottom: 10px;">
                        <span>
                            永久链接：
                            <a href="#">https://ryanc.cc/2017/12/11/study</a>
                        </span>
                    </div>
                    <div class="box box-primary">
                        <!-- Editor.md编辑器 -->
                        <div class="box-body pad">
                            <div id="ckeditor" style="z-index: 2;">
                                <textarea id="editor" name="editor" rows="20" cols="80"></textarea>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">发布</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button class="btn btn-default btn-sm">保存草稿</button>
                            <button onclick="push()" class="btn btn-primary btn-sm pull-right" data-loading-text="发布中...">${btnPush}</button>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">分类目录</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body" style="display: block">
                            <div class="form-group">
                                <ul style="list-style: none;padding: 0px;margin: 0px;">
                                    <#list categories as cate>
                                        <li style="padding: 0;margin: 0px;list-style: none">
                                            <label>
                                                <input name="categories" type="checkbox" class="minimal" value="${cate.cateId}"> ${cate.cateName}
                                            </label>
                                        </li>
                                    </#list>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">标签</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <div>标签设置</div>
                        </div>
                    </div>
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">缩略图</h3>
                            <div class="box-tools">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse" data-toggle="tooltip" title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>
                            </div>
                        </div>
                        <div class="box-body">
                            <div>缩略图</div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
        <script src="/static/plugins/ckeditor/ckeditor.js"></script>
        <script>
            $(function () {
                CKEDITOR.replace('editor');
            })
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>