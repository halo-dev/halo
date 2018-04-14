<#include "module/macro.ftl">
<@head title="${post.postTitle} · ${options.site_title?default('Anatole')}" keywords="${post.postTitle},${options.seo_keywords?default('Anatole')}" description="${options.seo_desc?default('Anatole')}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
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
                        ${post.postContent}
                    </div>
                    <div class="post-footer">
                        <div class="meta">
                            <div class="info">
                                <i class="fa fa-sun-o"></i>
                                <span class="date">${post.postDate?string("yyyy-MM-dd")}</span>
                                <i class="fa fa-comment-o"></i>
                                <a href="/post/${post.postUrl}#comment_widget">Comments</a>
                                <if post.tags??>
                                    <i class="fa fa-tag"></i>
                                    <#list post.tags as tag>
                                        <a href="/tags/${tag.tagUrl}" class="tag">&nbsp;${tag.tagName}</a>
                                    </#list>
                                </if>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="share">
                    <div class="evernote">
                        <a href="javascript:(function(){EN_CLIP_HOST='http://www.evernote.com';try{var%20x=document.createElement('SCRIPT');x.type='text/javascript';x.src=EN_CLIP_HOST+'/public/bookmarkClipper.js?'+(new%20Date().getTime()/100000);document.getElementsByTagName('head')[0].appendChild(x);}catch(e){location.href=EN_CLIP_HOST+'/clip.action?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}})();"
                           ref="nofollow" target="_blank" class="fa fa-bookmark"></a>
                    </div>
                    <div class="weibo">
                        <a href="javascript:void((function(s,d,e){try{}catch(e){}var f='http://service.weibo.com/share/share.php?',u=d.location.href,p=['url=',e(u),'&title=',e(d.title),'&appkey=2924220432'].join('');function a(){if(!window.open([f,p].join(''),'mb',['toolbar=0,status=0,resizable=1,width=620,height=450,left=',(s.width-620)/2,',top=',(s.height-450)/2].join('')))u.href=[f,p].join('');};if(/Firefox/.test(navigator.userAgent)){setTimeout(a,0)}else{a()}})(screen,document,encodeURIComponent));"
                           class="fa fa-weibo"></a>
                    </div>
                    <div class="twitter">
                        <a href="http://twitter.com/home?status=http://anatole.cai-cai.me/post/2015-05-22 ,Anatole,更轻量的Evernote第三方客户端－Alternote！,;"
                           class="fa fa-twitter"></a>
                    </div>
                </div>
                <div class="pagination">
                    <ul class="clearfix">
                        <#if afterPost??>
                        <li class="pre pagbuttons">
                            <a class="btn" role="navigation" href="/post/${afterPost.postUrl}" title="${afterPost.postTitle}">上一篇</a>
                        </li>
                        </#if>
                        <#if beforePost??>
                        <li class="next pagbuttons">
                            <a class="btn" role="navigation" href="/post/${beforePost.postUrl}" title="${beforePost.postTitle}">下一篇</a>
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
<@footer></@footer>