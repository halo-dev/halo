<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | 小工具</@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;">小工具</h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin">
                    <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.setting.bread.setting' /></a></li>
            <li class="active">小工具</li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-sm-6 col-md-4">
                <div class="box box-solid">
                    <div class="box-body">
                        <h4 style="background-color:#f7f7f7; font-size: 18px; text-align: center; padding: 7px 10px; margin-top: 0;">
                            Markdown 导入
                        </h4>
                        <div class="media">
                            <div class="media-body">
                                <div class="clearfix">
                                    <p class="pull-right">
                                        <a href="#" class="btn btn-success btn-sm ad-click-event"  onclick="halo.layerModal('/admin/tools/markdownImport','Markdown 导入')">
                                            导入
                                        </a>
                                    </p>
                                    <h4 style="margin-top: 0">Markdown 文档导入</h4>
                                    <p>支持 Hexo/Jekyll 导入并解析元数据</p>
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
                            WordPress 导入
                        </h4>
                        <div class="media">
                            <div class="media-body">
                                <div class="clearfix">
                                    <p class="pull-right">
                                        <a href="javascript:void(0)" class="btn btn-success btn-sm ad-click-event">
                                            导入
                                        </a>
                                    </p>
                                    <h4 style="margin-top: 0">WordPress 数据导入</h4>
                                    <p>尽请期待</p>
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
