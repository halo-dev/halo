<#include "module/macro.ftl">
<@head title="${post.title!} · ${options.blog_title!}" keywords="${post.title!},${options.seo_keywords!},${tagWords!}" description="${post.summary!}" />
<#include "module/sidebar.ftl">
<div class="main">
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <div class="post-page">
                <div class="post animated fadeInDown">
                    <div class="post-title">
                        <h3>
                            <a>${post.title}</a>
                        </h3>
                    </div>
                    <div class="post-content">
                        ${post.formatContent!}
                    </div>
                    <div class="post-footer">
                        <div class="meta">
                            <div class="info">
                                <i class="fa fa-sun-o"></i>
                                <span class="date">${post.createTime?string("yyyy-MM-dd")}</span>
                                <i class="fa fa-comment-o"></i>
                                <a href="${context!}/archives/${post.url}#comment_widget">Comments</a>
                                <#if tags?size gt 0>
                                    <i class="fa fa-tag"></i>
                                    <#list tags as tag>
                                        <a href="${context!}/tags/${tag.slugName}" class="tag">&nbsp;${tag.name}</a>
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
                        <a href="http://twitter.com/home?status=${context!}/archives/${post.url} ,${options.blog_title!},${post.title},;"
                           class="fa fa-twitter"></a>
                    </div>
                </div>
                <div class="pagination">
                    <ul class="clearfix">
                        <#if nextPost??>
                            <li class="next pagbuttons">
                                <a class="btn" role="navigation" href="${context!}/archives/${nextPost.url}" title="${nextPost.title}">下一篇</a>
                            </li>
                        </#if>
                        <#if prePost??>
                            <li class="pre pagbuttons">
                                <a class="btn" role="navigation" href="${context!}/archives/${prePost.url}" title="${prePost.title}">上一篇</a>
                            </li>
                        </#if>
                    </ul>
                </div>
                <div id="comment_widget" style="margin: 30px;">
                    <#include "module/comment.ftl">
                    <@comment post=post type="post" />
                </div>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>
