<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：系统设置"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            .form-horizontal .control-label{
                text-align: left;
            }
            .nav-tabs-custom > .nav-tabs > li.active {
                border-top-color: #d2d6de;
            }
        </style>
        <section class="content-header">
            <h1>
                博客设置
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin">
                        <i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a data-pjax="true" href="#">设置</a></li>
                <li class="active">博客设置</li>
            </ol>
        </section>
        <!-- tab选项卡 -->
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-12">
                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs">
                            <li class="active">
                                <a href="#general" data-toggle="tab">常规设置</a>
                            </li>
                            <li>
                                <a href="#seo" data-toggle="tab">SEO设置</a>
                            </li>
                            <li>
                                <a href="#post" data-toggle="tab">文章设置</a>
                            </li>
                            <li>
                                <a href="#comment" data-toggle="tab">评论设置</a>
                            </li>
                            <li>
                                <a href="#attach" data-toggle="tab">附件设置</a>
                            </li>
                            <li>
                                <a href="#admin" data-toggle="tab">后台设置</a>
                            </li>
                            <li>
                                <a href="#email" data-toggle="tab">邮箱设置</a>
                            </li>
                            <li>
                                <a href="#other" data-toggle="tab">其他设置</a>
                            </li>
                        </ul>
                        <!-- 基础设置 -->
                        <div class="tab-content">
                            <div class="tab-pane active" id="general">
                                <form method="post" class="form-horizontal" id="commonOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="blogTitle" class="col-lg-2 col-sm-4 control-label">博客标题：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="blogTitle" name="blog_title" value="${options.blog_title?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogUrl" class="col-lg-2 col-sm-4 control-label">博客地址：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="url" class="form-control" id="blogUrl" name="blog_url" value="${options.blog_url?default('http://localhost:8080')}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogLogo" class="col-lg-2 col-sm-4 control-label">LOGO：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="text" class="form-control selectData" id="blogLogo" name="blog_logo" value="${options.blog_logo?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " type="button" onclick="openAttach('blogLogo')">选择</button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogFooterInfo" class="col-lg-2 col-sm-4 control-label">页脚信息：
                                                <span data-toggle="tooltip" data-placement="top" title="支持HTML" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <textarea class="form-control" rows="5" id="blogFooterInfo" name="blog_footer_info" style="resize: none">${options.blog_footer_info?if_exists}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('commonOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                            <!-- seo设置 -->
                            <div class="tab-pane" id="seo">
                                <form method="post" class="form-horizontal" id="seoOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="keywords" class="col-lg-2 col-sm-4 control-label">关键词：
                                                <span data-toggle="tooltip" data-placement="top" title="多个关键词使用,隔开" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="keywords" name="seo_keywords" value="${options.seo_keywords?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="desc" class="col-lg-2 col-sm-4 control-label">博客描述：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="desc" name="seo_desc" value="${options.seo_desc?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="baiduToken" class="col-lg-2 col-sm-4 control-label">百度推送token：
                                                <span data-toggle="tooltip" data-placement="top" title="百度站长平台获取" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="text" class="form-control" id="baiduToken" name="seo_baidu_token" value="${options.seo_baidu_token?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " id="btn_push_baidu" onclick="pushAllToBaidu()" type="button">推送</button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('seoOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                            <!-- 文章设置 -->
                            <div class="tab-pane" id="post">
                                <form method="post" class="form-horizontal" id="postOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="indexPosts" class="col-lg-2 col-sm-4 control-label">首页显示条数：
                                                <span data-toggle="tooltip" data-placement="top" title="默认为10条" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="number" class="form-control" id="indexPosts" name="index_posts" value="${options.index_posts?default('10')}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="rssPosts" class="col-lg-2 col-sm-4 control-label">RSS显示条数：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="number" class="form-control" id="rssPosts" name="rss_posts" value="${options.rss_posts?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="postSummary" class="col-lg-2 col-sm-4 control-label">文章摘要字数：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="number" class="form-control" id="postSummary" name="post_summary" value="${options.post_summary?default('50')}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " id="btn_update_summary" onclick="updateAllSummary()" type="button">更新</button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('postOptions')">保存</button>
                                    </div>
                                </form>
                            </div>

                            <!-- 评论设置 -->
                            <div class="tab-pane" id="comment">
                                <form method="post" class="form-horizontal" id="commentOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">评论系统：
                                                <span data-toggle="tooltip" data-placement="top" title="其他第三方评论系统在后台无法管理" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="native" ${((options.comment_system?default('native'))=='native')?string('checked','')}> 原生
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="valine" ${((options.comment_system?default('native'))=='valine')?string('checked','')}> Valine
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="disqus" ${((options.comment_system?default('native'))=='disqus')?string('checked','')}> Disqus
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="livere" ${((options.comment_system?default('native'))=='livere')?string('checked','')}> Livere
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="changyan" ${((options.comment_system?default('native'))=='changyan')?string('checked','')}> 畅言
                                                </label>
                                            </div>
                                        </div>

                                        <!-- 原生设置 -->
                                        <div class="native-options" style="display: none">
                                            <div class="form-group">
                                                <label for="nativeCommentAvatar" class="col-lg-2 col-sm-4 control-label">评论者头像：
                                                    <span data-toggle="tooltip" data-placement="top" title="为用户评论者默认设置一个邮箱" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <select class="form-control" id="nativeCommentAvatar" name="native_comment_avatar">
                                                        <option value="mm" ${((options.native_comment_avatar?default('default'))=='mm')?string('selected','')}>默认</option>
                                                        <option value="identicon" ${((options.native_comment_avatar?default('default'))=='identicon')?string('selected','')}>抽象几何图形</option>
                                                        <option value="monsterid" ${((options.native_comment_avatar?default('default'))=='monsterid')?string('selected','')}>小怪物</option>
                                                        <option value="wavatar" ${((options.native_comment_avatar?default('default'))=='wavatar')?string('selected','')}>Wavatar</option>
                                                        <option value="retro" ${((options.native_comment_avatar?default('default'))=='retro')?string('selected','')}>复古</option>
                                                        <option value="robohash" ${((options.native_comment_avatar?default('default'))=='robohash')?string('selected','')}>机器人</option>
                                                        <option value="blank" ${((options.native_comment_avatar?default('default'))=='blank')?string('selected','')}>不显示头像</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label">评论审核后才显示：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_need_check" value="true" ${((options.new_comment_need_check?default("true"))=='true')?string('checked','')}> 开启
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_need_check" value="false" ${((options.new_comment_need_check?if_exists)=='false')?string('checked','')}> 关闭
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label">新评论通知：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_notice" value="true" ${((options.new_comment_notice?if_exists)=='true')?string('checked','')}> 启用
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_notice" value="false" ${((options.new_comment_notice?if_exists)=='false')?string('checked','')}> 禁用
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label">评论审核通过通知对方：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_pass_notice" value="true" ${((options.comment_pass_notice?if_exists)=='true')?string('checked','')}> 启用
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_pass_notice" value="false" ${((options.comment_pass_notice?if_exists)=='false')?string('checked','')}> 禁用
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label">评论回复通知对方：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_reply_notice" value="true" ${((options.comment_reply_notice?if_exists)=='true')?string('checked','')}> 启用
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_reply_notice" value="false" ${((options.comment_reply_notice?if_exists)=='false')?string('checked','')}> 禁用
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="nativeCommentPlaceholder" class="col-lg-2 col-sm-4 control-label">占位提示：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="url" class="form-control" id="nativeCommentPlaceholder" name="native_comment_placeholder" value="${options.native_comment_placeholder?default('赶快评论一个吧！')}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="nativeCss" class="col-lg-2 col-sm-4 control-label">自定义CSS：
                                                    <span data-toggle="tooltip" data-placement="top" title="对评论框自定义样式，如边距等" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="nativeCss" name="native_css" style="resize: none">${options.native_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- valine选项 -->
                                        <div class="valine-options" style="display: none">
                                            <div class="form-group">
                                                <label for="valineAppId" class="col-lg-2 col-sm-4 control-label">APP ID：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valineAppId" name="valine_appid" value="${options.valine_appid?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valineAppKey" class="col-lg-2 col-sm-4 control-label">APP KEY：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valineAppKey" name="valine_appkey" value="${options.valine_appkey?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valineAvatar" class="col-lg-2 col-sm-4 control-label">头像类型：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valineAvatar" name="valine_avatar" value="${options.valine_avatar?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valinePlaceholder" class="col-lg-2 col-sm-4 control-label">占位提示：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valinePlaceholder" name="valine_placeholder" value="${options.valine_placeholder?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valineCss" class="col-lg-2 col-sm-4 control-label">自定义CSS：
                                                    <span data-toggle="tooltip" data-placement="top" title="对评论框自定义样式，如边距等" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="valineCss" name="valine_css" style="resize: none">${options.valine_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- disqus选项 -->
                                        <div class="disqus-options" style="display: none">
                                            <div class="form-group">
                                                <label for="disqusShortname" class="col-lg-2 col-sm-4 control-label">Disqus ShortName：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="disqusShortname" name="disqus_shortname" value="${options.disqus_shortname?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="disqusCss" class="col-lg-2 col-sm-4 control-label">自定义CSS：
                                                    <span data-toggle="tooltip" data-placement="top" title="对评论框自定义样式，如边距等" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="disqusCss" name="disqus_css" style="resize: none">${options.disqus_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- livere选项 -->
                                        <div class="livere-options" style="display: none">
                                            <div class="form-group">
                                                <label for="livereDataUid" class="col-lg-2 col-sm-4 control-label">livere data-uid：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="livereDataUid" name="livere_data_uid" value="${options.livere_data_uid?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="livereCss" class="col-lg-2 col-sm-4 control-label">自定义CSS：
                                                    <span data-toggle="tooltip" data-placement="top" title="对评论框自定义样式，如边距等" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="livereCss" name="livere_css" style="resize: none">${options.livere_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 畅言选项 -->
                                        <div class="changyan-options" style="display: none">
                                            <div class="form-group">
                                                <label for="changyanAppId" class="col-lg-2 col-sm-4 control-label">APP ID：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="changyanAppId" name="changyan_appid" value="${options.changyan_appid?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="changyanConf" class="col-lg-2 col-sm-4 control-label">CONF：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="changyanConf" name="changyan_conf" value="${options.changyan_conf?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="changyanCss" class="col-lg-2 col-sm-4 control-label">自定义CSS：
                                                    <span data-toggle="tooltip" data-placement="top" title="对评论框自定义样式，如边距等" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="changyanCss" name="changyan_css" style="resize: none">${options.changyan_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('commentOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                            <!-- 附件设置 -->
                            <div class="tab-pane" id="attach">
                                <form method="post" class="form-horizontal" id="attachOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">存储位置：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="attach_loc" value="server" ${((options.attach_loc?default('server'))=='server')?string('checked','')}> 本地服务器
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="attach_loc" value="upyun" ${((options.attach_loc?if_exists)=='upyun')?string('checked','')} disabled="disabled"> 又拍云
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="attach_loc" value="qiniu" ${((options.attach_loc?if_exists)=='qiniu')?string('checked','')} disabled="disabled"> 七牛云
                                                </label>
                                            </div>
                                        </div>

                                        <!-- 原生设置 -->
                                        <div class="server-options" style="display: none">

                                        </div>

                                        <!-- 又拍云选项 -->
                                        <div class="upyun-options" style="display: none">
                                            <div class="form-group">
                                                <label for="upyunOssDomain" class="col-lg-2 col-sm-4 control-label">域名绑定：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssDomain" name="upyun_oss_domain" value="${options.upyun_oss_domain?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssBucket" class="col-lg-2 col-sm-4 control-label">空间名称：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssBucket" name="upyun_oss_bucket" value="${options.upyun_oss_bucket?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssOperator" class="col-lg-2 col-sm-4 control-label">操作员名称：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssOperator" name="upyun_oss_operator" value="${options.upyun_oss_operator?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssPwd" class="col-lg-2 col-sm-4 control-label">操作员密码：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssPwd" name="upyun_oss_pwd" value="${options.upyun_oss_pwd?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssSrc" class="col-lg-2 col-sm-4 control-label">文件目录：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssSrc" name="upyun_oss_src" value="${options.upyun_oss_src?if_exists}">
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 七牛云 -->
                                        <div class="qiniu-options" style="display: none">
                                            <div class="form-group">
                                                <label for="qiniuDomain" class="col-lg-2 col-sm-4 control-label">域名：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuDomain" name="qiniu_domain" value="${options.qiniu_domain?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="qiniuAccessKey" class="col-lg-2 col-sm-4 control-label">Access Key：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuAccessKey" name="qiniu_access_key" value="${options.qiniu_access_key?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="qiniuSecretKey" class="col-lg-2 col-sm-4 control-label">Secret Key：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuSecretKey" name="qiniu_secret_key" value="${options.qiniu_secret_key?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="qiniuBucket" class="col-lg-2 col-sm-4 control-label">Bucket：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuBucket" name="qiniu_bucket" value="${options.qiniu_bucket?if_exists}">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('attachOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                            <!-- 后台设置 -->
                            <div class="tab-pane" id="admin">
                                <form method="post" class="form-horizontal" id="adminOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">启用pjax：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_pjax" value="true" ${((options.admin_pjax?default('true'))=='true')?string('checked','')}> 启用
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_pjax" value="false" ${((options.admin_pjax?if_exists)=='false')?string('checked','')}> 禁用
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">后台加载动画：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_loading" value="true" ${((options.admin_loading?if_exists)=='true')?string('checked','')}> 启用
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_loading" value="false" ${((options.admin_loading?default('false'))=='false')?string('checked','')}> 禁用
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">后台布局：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_layout" value="" ${((options.admin_layout?default(''))=='')?string('checked','')}> 正常布局
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_layout" value="layout-boxed" ${((options.admin_layout?default(''))=='layout-boxed')?string('checked','')}> 盒子布局
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="adminTheme" class="col-lg-2 col-sm-4 control-label">后台主题：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <select class="form-control" id="adminTheme" name="admin_theme">
                                                    <option value="skin-blue" ${((options.admin_theme?default('skin-blue'))=='skin-blue')?string('selected','')}>默认主题</option>
                                                    <option value="skin-blue-light" ${((options.admin_theme?if_exists)=='skin-blue-light')?string('selected','')}>上蓝左白</option>
                                                    <option value="skin-black" ${((options.admin_theme?if_exists)=='skin-black')?string('selected','')}>上白左黑</option>
                                                    <option value="skin-black-light" ${((options.admin_theme?if_exists)=='skin-black-light')?string('selected','')}>上白左白</option>
                                                    <option value="skin-green" ${((options.admin_theme?if_exists)=='skin-green')?string('selected','')}>上绿左黑</option>
                                                    <option value="skin-green-light" ${((options.admin_theme?if_exists)=='skin-green-light')?string('selected','')}>上绿左白</option>
                                                    <option value="skin-purple" ${((options.admin_theme?if_exists)=='skin-purple')?string('selected','')}>上紫左黑</option>
                                                    <option value="skin-purple-light" ${((options.admin_theme?if_exists)=='skin-purple-light')?string('selected','')}>上紫左白</option>
                                                    <option value="skin-red" ${((options.admin_theme?if_exists)=='skin-red')?string('selected','')}>上红左黑</option>
                                                    <option value="skin-red-light" ${((options.admin_theme?if_exists)=='skin-red-light')?string('selected','')}>上红左白</option>
                                                    <option value="skin-yellow" ${((options.admin_theme?if_exists)=='skin-yellow')?string('selected','')}>上黄左黑</option>
                                                    <option value="skin-yellow-light" ${((options.admin_theme?if_exists)=='skin-yellow-light')?string('selected','')}>上黄左白</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">侧边栏样式：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="sidebar_style" value="" ${((options.sidebar_style?default(''))=='')?string('checked','')}> 展开
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="sidebar_style" value="sidebar-collapse" ${((options.sidebar_style?default(''))=='sidebar-collapse')?string('checked','')}> 收拢
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('adminOptions')">保存</button>
                                    </div>
                                </form>
                            </div>

                            <!-- 邮箱设置 -->
                            <div class="tab-pane" id="email">
                                <form method="post" class="form-horizontal" id="emailOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">是否启用：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="smtp_email_enable" value="true" ${((options.smtp_email_enable?default('false'))=='true')?string('checked','')}> 启用
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="smtp_email_enable" value="false" ${((options.smtp_email_enable?if_exists)=='false')?string('checked','')}> 禁用
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emSmtpHost" class="col-lg-2 col-sm-4 control-label">SMTP地址：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="emailSmtpHost" name="mail_smtp_host" value="${options.mail_smtp_host?if_exists}" autocomplete='address-line1'>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailSmtpUserName" class="col-lg-2 col-sm-4 control-label">邮箱账号：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="email" class="form-control" id="emailSmtpUserName" name="mail_smtp_username" value="${options.mail_smtp_username?if_exists}" autocomplete="email">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailSmtpPassword" class="col-lg-2 col-sm-4 control-label">邮箱密码：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="password" class="form-control" id="emailSmtpPassword" name="mail_smtp_password" value="${options.mail_smtp_password?if_exists}" current-password>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailFromName" class="col-lg-2 col-sm-4 control-label">发件姓名：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="emailFromName" name="mail_from_name" value="${options.mail_from_name?if_exists}" autocomplete="name">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('emailOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                            <!-- 其他设置 -->
                            <div class="tab-pane" id="other">
                                <form method="post" class="form-horizontal" id="otherOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label">API服务：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="api_status" value="true" ${((options.api_status?if_exists)=='true')?string('checked','')}> 启用
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="api_status" value="false" ${((options.api_status?default('false'))=='false')?string('checked','')}> 禁用
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="statisticsCode" class="col-lg-2 col-sm-4 control-label">统计代码：
                                                <span data-toggle="tooltip" data-placement="top" title="可以使用cnzz，百度，google等" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <textarea class="form-control" rows="6" id="statisticsCode" name="statistics_code" style="resize: none">${options.statistics_code?if_exists}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('otherOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <@compress single_line=true>
        <script>
            $(function () {
                $('[data-toggle="tooltip"]').tooltip();
                checkCommentOption();
                checkAttachOption();
            });

            /**
             * 打开附件
             */
            function openAttach(id) {
                layer.open({
                    type: 2,
                    title: '所有附件',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/select?id='+id,
                    scrollbar: false
                });
            }

            /**
             * 更新所有文章的摘要
             */
            function updateAllSummary() {
                $.ajax({
                    type: 'GET',
                    url: '/admin/posts/updateSummary',
                    data: {
                        postSummary : $('#postSummary').val()
                    },
                    success: function (data) {
                        if(data.code==1){
                            showMsg("所有文章摘要更新成功！","success",1000);
                        }else{
                            showMsg("更新失败！","success",2000);
                        }
                    }
                });
            }

            /**
             * 主动提交文章到百度
             */
            function pushAllToBaidu() {
                $.ajax({
                    type: 'GET',
                    url: '/admin/posts/pushAllToBaidu',
                    data: {
                        baiduToken : $('#baiduToken').val()
                    },
                    success: function (data) {
                        if(data.code==1){
                            showMsg("推送所有文章成功！","success",1000);
                        }else{
                            $.toast({
                                text: data.msg,
                                heading: '提示',
                                icon: icon,
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: hideAfter,
                                stack: 1,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff'
                            });
                        }
                    }
                });
            }

            /**
             * 评论选项切换
             */
            function checkCommentOption() {
                var native = $('input:radio[value=native]:checked').val();
                var valine = $('input:radio[value=valine]:checked').val();
                var disqus = $('input:radio[value=disqus]:checked').val();
                var livere = $('input:radio[value=livere]:checked').val();
                var changyan = $('input:radio[value=changyan]:checked').val();
                if(native!=null){
                    $('.native-options').show();
                }else{
                    $('.native-options').hide();
                }
                if(valine!=null){
                    $('.valine-options').show();
                }else{
                    $('.valine-options').hide();
                }
                if(disqus!=null){
                    $('.disqus-options').show();
                }else{
                    $('.disqus-options').hide();
                }
                if(livere!=null){
                    $('.livere-options').show();
                }else{
                    $('.livere-options').hide();
                }
                if(changyan!=null){
                    $('.changyan-options').show();
                }else{
                    $('.changyan-options').hide();
                }
            }

            /**
             * 附件选项切换
             */
            function checkAttachOption() {
                var server = $('input:radio[value=server]:checked').val();
                var upyun = $('input:radio[value=upyun]:checked').val();
                var qiniu = $('input:radio[value=qiniu]:checked').val();
                if(server!=null){
                    $('.server-options').show();
                }else{
                    $('.server-options').hide();
                }
                if(upyun!=null){
                    $('.upyun-options').show();
                }else{
                    $('.upyun-options').hide();
                }
                if(qiniu!=null){
                    $('.qiniu-options').show();
                }else{
                    $('.qiniu-options').hide();
                }
            }

            /**
             * 后台布局切换
             */
            function viewLayout() {
                var layout = $('input:radio[value=layout-boxed]:checked').val();
                if(layout!=null){
                    $('body').addClass('layout-boxed');
                }else{
                    $('body').removeClass('layout-boxed');
                }
            }

            /**
             * 预览侧边栏
             */
            function viewSideBar() {
                var layout = $('input:radio[value=sidebar-collapse]:checked').val();
                if(layout!=null){
                    $('body').addClass('sidebar-collapse');
                }else{
                    $('body').removeClass('sidebar-collapse');
                }
            }
            $('input[name=comment_system]').click(function () {
                checkCommentOption();
            });
            $('input[name=attach_loc]').click(function () {
                checkAttachOption();
            });
            $('input[name=admin_layout]').click(function () {
                viewLayout();
            });
            $('input[name=sidebar_style]').click(function () {
                viewSideBar();
            });

            /**
             * 预览后台样式切换
             */
            $(function () {
                var beforeTheme;
                $('#adminTheme').change(function () {
                    if($('body').hasClass("${options.admin_theme?default('skin-blue')}")){
                        $('body').removeClass("${options.admin_theme?default('skin-blue')}");
                    }
                    if(beforeTheme!=null){
                        $('body').removeClass(beforeTheme);
                    }
                    $('body').addClass($(this).val());
                    beforeTheme = $(this).val();
                })
            })
        </script>
        </@compress>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>
