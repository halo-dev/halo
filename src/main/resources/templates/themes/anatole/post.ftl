<#include "module/macro.ftl">
<@head title="${post.postTitle!} · ${options.blog_title!'Anatole'}" keywords="${post.postTitle!},${options.seo_keywords!'Anatole'},${tagWords!}" description="${post.postSummary!}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
    <link href="/anatole/source/plugins/prism/prism.css" type="text/css" rel="stylesheet" />
    <style>
        code, tt {
            font-size: 1.2em;
        }
        table {
            border-spacing: 0;
            border-collapse: collapse;
            margin-top: 0;
            margin-bottom: 16px;
            display: block;
            width: 100%;
            overflow: auto;

        }
        table th {
            font-weight: 600;
        }
        table th,
        table td {
            padding: 6px 13px;
            border: 1px solid #dfe2e5;
        }
        table tr {
            background-color: #fff;
            border-top: 1px solid #c6cbd1;
        }
        table tr:nth-child(2n) {
            background-color: #f6f8fa;
        }
    </style>
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <div class="post-page">
                <div class="post animated fadeInDown">
                    <div class="post-title">
                        <h3>
                            <a>${post.postTitle}</a>
                        </h3>
                    </div>
                    <div class="post-content">
                        ${post.postContent!}
                    </div>
                    <div class="post-footer">
                        <div class="meta">
                            <div class="info">
                                <i class="fa fa-sun-o"></i>
                                <span class="date">${post.postDate?string("yyyy-MM-dd")}</span>
                                <i class="fa fa-comment-o"></i>
                                <a href="${options.blog_url!}/archives/${post.postUrl}#comment_widget">Comments</a>
                                <#if post.tags?size gt 0>
                                    <i class="fa fa-tag"></i>
                                    <#list post.tags as tag>
                                        <a href="${options.blog_url!}/tags/${tag.tagUrl}" class="tag">&nbsp;${tag.tagName}</a>
                                    </#list>
                                </#if>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="share" style="display: inline-flex">
                    <div class="evernote">
                        <a href="javascript:(function(){EN_CLIP_HOST='http://www.evernote.com';try{var%20x=document.createElement('SCRIPT');x.type='text/javascript';x.src=EN_CLIP_HOST+'/public/bookmarkClipper.js?'+(new%20Date().getTime()/100000);document.getElementsByTagName('head')[0].appendChild(x);}catch(e){location.href=EN_CLIP_HOST+'/clip.action?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}})();"
                           ref="nofollow" target="_blank" class="fa fa-bookmark"></a>
                    </div>
                    <div class="weibo">
                        <a href="javascript:void((function(s,d,e){try{}catch(e){}var f='http://service.weibo.com/share/share.php?',u=d.location.href,p=['url=',e(u),'&title=',e(d.title),'&appkey=2924220432'].join('');function a(){if(!window.open([f,p].join(''),'mb',['toolbar=0,status=0,resizable=1,width=620,height=450,left=',(s.width-620)/2,',top=',(s.height-450)/2].join('')))u.href=[f,p].join('');};if(/Firefox/.test(navigator.userAgent)){setTimeout(a,0)}else{a()}})(screen,document,encodeURIComponent));"
                           class="fa fa-weibo"></a>
                    </div>
                    <div class="twitter">
                        <a href="http://twitter.com/home?status=${options.blog_url}/archives/${post.postUrl} ,${options.blog_title!},${post.postTitle},;"
                           class="fa fa-twitter"></a>
                    </div>
                </div>
                <div class="pagination">
                    <ul class="clearfix">
                        <#if nextPost??>
                        <li class="pre pagbuttons">
                            <a class="btn" role="navigation" href="${options.blog_url!}/archives/${nextPost.postUrl}" title="${nextPost.postTitle}">上一篇</a>
                        </li>
                        </#if>
                        <#if prePost??>
                        <li class="next pagbuttons">
                            <a class="btn" role="navigation" href="${options.blog_url!}/archives/${prePost.postUrl}" title="${prePost.postTitle}">下一篇</a>
                        </li>
                        </#if>
                    </ul>
                </div>
                <div id="comment_widget">
                    <#include "module/comment.ftl">
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript" src="/anatole/source/plugins/prism/prism.js"></script>
<@footer></@footer>
