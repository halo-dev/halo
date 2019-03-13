<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.tools.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.tools.title' /></h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin">
                    <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.setting.bread.setting' /></a></li>
            <li class="active"><@spring.message code='admin.tools.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-sm-6 col-md-4">
                <div class="box box-solid">
                    <div class="box-body">
                        <h4 style="background-color:#f7f7f7; font-size: 18px; text-align: center; padding: 7px 10px; margin-top: 0;">
                            <@spring.message code='admin.tools.markdown.name' />
                        </h4>
                        <div class="media">
                            <div class="media-body">
                                <div class="clearfix">
                                    <p class="pull-right">
                                        <a href="#" class="btn btn-success btn-sm ad-click-event"  onclick="halo.layerModal('/admin/tools/markdownImport','<@spring.message code="admin.tools.markdown.name" />')">
                                            <@spring.message code='admin.tools.btn.import' />
                                        </a>
                                    </p>
                                    <h4 style="margin-top: 0"><@spring.message code='admin.tools.markdown.full.name' /></h4>
                                    <p><@spring.message code='admin.tools.markdown.desc' /></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4">
                <div class="box box-solid">
                    <div class="box-body">
                        <h4 style="background-color:#f7f7f7; font-size: 18px; text-align: center; padding: 7px 10px; margin-top: 0;">
                            JSON 数据导出
                        </h4>
                        <div class="media">
                            <div class="media-body">
                                <div class="clearfix">
                                    <p class="pull-right">
                                        <a href="/admin/tools/dataExport" class="btn btn-success btn-sm ad-click-event">
                                            导出
                                        </a>
                                    </p>
                                    <h4 style="margin-top: 0">导出 JSON 格式数据</h4>
                                    <p>此工具可以将博客设置，文章，评论等数据导出</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4">
                <div class="box box-solid">
                    <div class="box-body">
                        <h4 style="background-color:#f7f7f7; font-size: 18px; text-align: center; padding: 7px 10px; margin-top: 0;">
                            <@spring.message code='admin.tools.wordpress.name' />
                        </h4>
                        <div class="media">
                            <div class="media-body">
                                <div class="clearfix">
                                    <p class="pull-right">
                                        <a href="javascript:void(0)" class="btn btn-success btn-sm ad-click-event">
                                            <@spring.message code='admin.tools.btn.import' />
                                        </a>
                                    </p>
                                    <h4 style="margin-top: 0"><@spring.message code='admin.tools.wordpress.full.name' /></h4>
                                    <p><@spring.message code='admin.tools.wordpress.desc' /></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
</div>
<@footer>
<script type="application/javascript" id="footer_script"></script>
</@footer>
</#compress>
