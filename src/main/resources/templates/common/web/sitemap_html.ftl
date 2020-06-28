<#--
see https://gitee.com/yadong.zhang/DBlog/blob/master/blog-web/src/main/java/com/zyd/blog/controller/RestWebSiteController.java
-->
<#compress >
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>${blog_title!} 网站地图</title>
    <meta name="robots" content="index,follow"/>
    <style type="text/css">
        body {
            color: #000000;
            background: #ffffff;
            margin: 20px;
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 14px;
        }

        #contentTable {
            list-style: none;
            margin: 10px 0 10px 0;
            padding: 0;
            width: 100%;
            min-width: 804px;
        }

        #contentTable li {
            list-style-type: none;
            width: 100%;
            min-width: 404px;
            height: 20px;
            line-height: 20px;
            padding: 2px 0;
        }

        .pull-left{
            float: left!important;
        }
        .pull-right{
            float: right!important;
        }

        #contentTable li .T1-h {
            font-weight: bold;
            min-width: 300px;
        }

        #contentTable li .T2-h {
            width: 200px;
            font-weight: bold;
        }

        #contentTable li .T3-h {
            width: 200px;
            font-weight: bold;
        }

        #contentTable li .T4-h {
            width: 100px;
            font-weight: bold;
        }

        #contentTable li .T1 {
            min-width: 300px;
        }

        #contentTable li .T2 {
            width: 200px;
        }

        #contentTable li .T3 {
            width: 200px;
        }

        #contentTable li .T4 {
            width: 100px;
        }

        #footer {
            padding: 2px;
            margin: 0;
            font-size: 8pt;
            color: gray;
            min-width: 900px;
        }

        #footer a {
            color: gray;
        }

        .clear {
            clear: both;
        }

        #nav, #content, #footer {
            padding: 8px;
            border: 1px solid #EEEEEE;
            clear: both;
            width: 95%;
            margin: auto;
            margin-top: 10px;
        }

        @media (max-width: 768px) {
            .T2-h, .T3-h, .T4-h, .T2, .T3, .T4 {
                display: none;
            }
            #contentTable, #footer,  #contentTable li .T1, #contentTable li, #contentTable li .T1-h, #contentTable li .T1 {
                max-width: 100%;
                min-width: auto;
                white-space: nowrap;
                word-wrap: normal;
                text-overflow: ellipsis;
                overflow: hidden;
            }
        }
    </style>
</head>
<body>
<h2 style="text-align: center; margin-top: 20px">${blog_title!} 网站地图 </h2>
<div id="nav"><a href="${blog_url!}"><strong>${blog_title!}</strong></a> &raquo; <a href="${sitemap_html_url!}">站点地图</a></div>
<div id="content">
    <h3>最新文章</h3>
    <ul id="contentTable">
        <li>
            <div class="T1-h pull-left">URL</div>
            <div class="T2-h pull-right">Last Change</div>
            <div class="T3-h pull-right">Change Frequency</div>
            <div class="T4-h pull-right">Priority</div>
        </li>
        <div class="clear"></div>
        <li>
            <div class="T1 pull-left"><a href="${blog_url!}" title="${blog_title!}">${blog_title!}</a></div>
            <div class="T2 pull-right">${(options.birthday)?number_to_date?string("yyyy-MM-dd")} </div>
            <div class="T3 pull-right">daily</div>
            <div class="T4 pull-right">1</div>
        </li>
        <div class="clear"></div>
        <#if posts?? && posts?size gt 0>
            <#list posts as post>
                <li>
                    <div class="T1 pull-left"><a href="<#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${post.fullPath!}" title="${post.title!}">${post.title!} | ${blog_title!}</a></div>
                    <div class="T2 pull-right">${post.createTime?string('yyyy-MM-dd')}</div>
                    <div class="T3 pull-right">daily</div>
                    <div class="T4 pull-right">0.6</div>
                </li>
                <div class="clear"></div>
            </#list>
        </#if>
    </ul>
</div>
<div id="content">
    <h3>分类目录</h3>
    <ul id="contentTable">
        <@categoryTag method="list">
            <#if categories?? && categories?size gt 0>
                <#list categories as category>
                    <li>
                        <div class="T1 pull-left"><a href="<#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${category.fullPath!}" title="${category.name}">${category.name} | ${blog_title!}</a></div>
                        <div class="T2 pull-right">${category.createTime?string('yyyy-MM-dd')}</div>
                        <div class="T3 pull-right">daily</div>
                        <div class="T4 pull-right">0.6</div>
                    </li>
                    <div class="clear"></div>
                </#list>
            </#if>
        </@categoryTag>
    </ul>
</div>
<div id="content">
    <h3>标签目录</h3>
    <ul id="contentTable">
        <@tagTag method="list">
            <#if tags?? && tags?size gt 0>
                <#list tags as tag>
                    <li>
                        <div class="T1 pull-left"><a href="<#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${tag.fullPath!}" title="${tag.name}">${tag.name} | ${blog_title!}</a></div>
                        <div class="T2 pull-right">${tag.createTime?string('yyyy-MM-dd')}</div>
                        <div class="T3 pull-right">daily</div>
                        <div class="T4 pull-right">0.6</div>
                    </li>
                    <div class="clear"></div>
                </#list>
            </#if>
        </@tagTag>
    </ul>
</div>
<div id="footer">
    该文件由 <a href="${blog_url!}" title="${blog_title!}">${blog_title!}</a> 网站自动生成。
</div>
</body>
</html>
</#compress>