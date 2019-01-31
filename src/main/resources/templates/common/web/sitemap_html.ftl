<#--
see https://gitee.com/yadong.zhang/DBlog/blob/master/blog-web/src/main/java/com/zyd/blog/controller/RestWebSiteController.java
-->
<#compress >
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>${options.blog_title!} 网站地图</title>
    <meta name="robots" content="index,follow"/>
    <style type="text/css">
        body {
            color: #000000;
            background: #ffffff;
            margin: 20px;
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 14px;
        }

        #myTable {
            list-style: none;
            margin: 10px 0px 10px 0px;
            padding: 0px;
            width: 100%;
            min-width: 804px;
        }

        #myTable li {
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

        #myTable li .T1-h {
            font-weight: bold;
            min-width: 300px;
        }

        #myTable li .T2-h {
            width: 200px;
            font-weight: bold;
        }

        #myTable li .T3-h {
            width: 200px;
            font-weight: bold;
        }

        #myTable li .T4-h {
            width: 100px;
            font-weight: bold;
        }

        #myTable li .T1 {
            min-width: 300px;
        }

        #myTable li .T2 {
            width: 200px;
        }

        #myTable li .T3 {
            width: 200px;
        }

        #myTable li .T4 {
            width: 100px;
        }

        #footer {
            padding: 2px;
            margin: 0px;
            font-size: 8pt;
            color: gray;
            min-width: 900px;
        }

        #footer a {
            color: gray;
        }

        .myClear {
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

        /* 窗口缩小到768px以下时 */
        @media (max-width: 768px) {
            .T2-h, .T3-h, .T4-h, .T2, .T3, .T4 {
                display: none;
            }
            #myTable, #footer,  #myTable li .T1, #myTable li, #myTable li .T1-h, #myTable li .T1 {
                max-width: 100%;
                min-width: auto;
                white-space: nowrap;
                word-wrap: normal;
                text-overflow: ellipsis;
                overflow: hidden;
            }
        }

        /* 窗口放大到768px以上时 */
        @media (min-width: 768px) {

        }
    </style>
</head>
<body>
<h2 style="text-align: center; margin-top: 20px">${options.blog_title!} 网站地图 </h2>
<div id="nav"><a href="${options.blog_url!}"><strong>${options.blog_title!}</strong></a> &raquo; <a href="${options.blog_url!}/sitemap.html">站点地图</a></div>
<div id="content">
    <h3>最新文章</h3>
    <ul id="myTable">
        <li>
            <div class="T1-h pull-left">URL</div>
            <div class="T2-h pull-right">Last Change</div>
            <div class="T3-h pull-right">Change Frequency</div>
            <div class="T4-h pull-right">Priority</div>
        </li>
        <div class="myClear"></div>
        <li>
            <div class="T1 pull-left"><a href="${options.blog_url!}" title="${options.blog_title!}">${options.blog_title!}</a></div>
            <div class="T2 pull-right">${options.blog_start!}</div>
            <div class="T3 pull-right">daily</div>
            <div class="T4 pull-right">1</div>
        </li>
        <div class="myClear"></div>
        <#if posts?? && posts?size gt 0>
            <#list posts as post>
                <li>
                    <div class="T1 pull-left"><a href="${options.blog_url!}/archives/${post.postUrl!}" title="${post.postTitle!}">${post.postTitle!} | ${options.blog_title!}</a></div>
                    <div class="T2 pull-right">${post.postDate?string('yyyy-MM-dd')}</div>
                    <div class="T3 pull-right">daily</div>
                    <div class="T4 pull-right">0.6</div>
                </li>
                <div class="myClear"></div>
            </#list>
        </#if>
    </ul>
</div>
<div id="content">
    <h3>分类目录</h3>
    <ul id="myTable">
        <@commonTag method="categories">
            <#if categories?? && categories?size gt 0>
                <#list categories as cate>
                    <li>
                        <div class="T1 pull-left"><a href="${options.blog_url}/categories/${cate.cateUrl!}" title="前端编程">${cate.cateName} | ${options.blog_title!}</a></div>
                        <div class="T2 pull-right">${options.blog_start!}</div>
                        <div class="T3 pull-right">daily</div>
                        <div class="T4 pull-right">0.6</div>
                    </li>
                    <div class="myClear"></div>
                </#list>
            </#if>
        </@commonTag>
    </ul>
</div>
<div id="content">
    <h3>标签目录</h3>
    <ul id="myTable">
        <@commonTag method="tags">
            <#if tags?? && tags?size gt 0>
                <#list tags as tag>
                    <li>
                        <div class="T1 pull-left"><a href="${options.blog_url}/tags/${tag.tagUrl!}" title="前端编程">${tag.tagName} | ${options.blog_title!}</a></div>
                        <div class="T2 pull-right">${options.blog_start!}</div>
                        <div class="T3 pull-right">daily</div>
                        <div class="T4 pull-right">0.6</div>
                    </li>
                    <div class="myClear"></div>
                </#list>
            </#if>
        </@commonTag>
    </ul>
</div>
<div id="footer">
    该文件由<a href="${options.blog_url!}" title="${options.blog_title!}">${options.blog_title!}</a>网站自动生成。
</div>
</body>
</html>
</#compress>