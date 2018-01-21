<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="utf-8">
    <meta name="X-UA-Compatible" content="IE=edge">
    <meta name="author" content="Robert Yang">
    <title>${post.postTitle} · ${options.site_title}</title>
    <meta name="description" content="写这个主要是因为昨天B站视频评论，里面有个人在自己也不懂“动态范围”“宽容度”的情况下强行给别人科普。他认为照片能拉回多少高光和暗部信息叫宽容度，而在视频中这个概念叫动态范围，在视频中使用 Log 模式拍摄能提高动态范围。
    动态范围先说动态范围和宽容度这两个概念，实际上动态范围经常用在音响设备中,它">
    <meta name="keywords" content="可乐没气,RobertYang,博客,吐槽,思想,想法,RobertYang's blog,Blog,技术,Wordpress,互联网,hexo">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="mobile-web-app-capable">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta name="renderer" content="webkit">
    <link rel="short icon" href="/Anatole/source/images/favicon.png" type="image/x-icon">
    <link rel="stylesheet" href="/Anatole/source/css/style.css">
    <link rel="stylesheet" href="/Anatole/source/css/blog_basic.css">
    <link rel="stylesheet" href="/Anatole/source/css/font-awesome.min.css">
    <link rel="alternate" type="application/atom+xml" title="ATOM 1.0" href="/atom.xml">
</head>

<body>
<div class="sidebar animated fadeInDown">
    <div class="logo-title">
        <div class="title">
            <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAWIAAAFiCAMAAAD7giJIAAADAFBMVEVHcEwmjczx9/z5/Prq8e+oy9Yqj836+/tFkb3///8dicuNw+Gs1Ow5Ojr7/f76/f0NerpkvE8/l6rE29QKeroJerkJermenp6Ki4okjMz5/f23t7d0sVUrkM4ii8zumSkIeblPt079/v55uuELe7sJerktkc4GdrdUo9He7ekYqk37/Pbg7/Mii8wKe7oVf75TuFFNodWx1+vZ7PVxtd5gu0vstB4aqk4/m9Ltpxxeu04sLCui26cYqk2u1N4IeLhERER2yYCYzZzuoBzO6M0NfbtkvE9gu0v63p9bXFxAQUGop6caq07y9PW02fD7/Pz8+Ovtrxk/QEAvMC/3xH7q8fH8/P2n26DW4db41It5ueA/t2aTxuV/uZFZWVlUtj30s1hQvXL///9BQkIJerkqj81ERUU0NDP///8FeLnsth4MfLorkM7vlSE3NzYukc4Vgb0gissmjcwAcbUAdbdGR0f+/v4fiMf8/f7x9/08PT0cq1AlJiQylNAciMrstyFhu0wtLSxBQkIOfbzstRifn582lMg4OTlBm9MRf7zrsQ8AbrJNpttHn9VnZ2cpKSgVqUygzecxMTAXhchcuUap0+2YyukdhcDHx8c7mNHukx1BottUVFRJSkqHv+Birdvj8vt3uN5OT0729/be3t7W6/mIwuZSqt7//fbv8e0hrlSy2PCRx+cLpUO53PHypTuzsrJ9veT4+/3ukBZtstuqqqkerFHujA9tbm5RtUDB3u/q9Pr21XthqtPz+f399uAOgMXN5fNcXVzzylj72Z52d3aAgH/G4vLh5und7vjzzmiDynP87MLR5/Wn2ptCuWhuwVsiIiHuuyvV09JZqNn98c1HmshVo9DU7tHn7+rtqR5rtuR4zZKWlpYxh7hMn8/2v3H4zZKlpKSS1qf53IvAwMCh0vDvvjQxslwXfLUpi8Pxw0O8u7qy4r2zzt375K8xkMWQkJDj3tjOzczE5Pz0s1eTvdRYwHiT0YV0rcy64rBnxoEAaK/H6MDHwb0ZGRkVgfIkAAAApHRSTlMAa2sLAQRrBARra2pra2sdH/4MCUXMuGn+uCZqHuyE/nxCSGxY7cxraxXEOmfNjzJkhEREQ9dnhFe/gmtq6ivotGtDq3CBvahAakK8rH+4tpxB6L2yusOrvGTAxoufvb1/q4HP/////////////////////////////////////////////////////////////////////////////////////ur7JCMAACAASURBVHja7JptbFPnFcdLbWM3lCV2AiNZjEhIoQnNYmAIxAJVECVCo+xT12lt1S924shWjF/Im5llgmhaoZKbcCGLBEtSKyi+wQgQWE0lkIY0NmTJNuq0OZInpPpLVDUSZUiJEgXtPM+143ffe83QRHV+EhEv4uT65/9zznkMr72GIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAiCIAjyEpEr5SjhZfotL6+vrfr/OJbL+R8v5+/Ly2Wy8v9NdbnqReqUlzXVHX9DKf37y5UZyCXbUmlUoKHop5drNCqVrDzPnyrKmpoOlr5YdQVfXd5UUVb0uwWGK97bqP2ZVMVKmaz+UHMLoNPpWlqam5sP15bKZBLLaMr21f2qaAtyDaSj7qAs3/tXsrly8+4Xq15RRaur1lbWNRVbCQxv2dAx8rpSQnoVCkV5dU2N7tMZlmVnGMDn8zHOBt2hXVUyKVmWl+2r3LZ5V2l50enYsmHjutI830+1xmxp3Lm72OoaWv0dWl21Vlu5udgsaJre2/BWxwnRiiG91TU63f7W1tYje9TAjNMPcJzfeaDh2LG9LYckZFlRUanVNm6ukhVr+Jcb3nr3eK0yn2KDQdu4c4esWMOk+m9rFVSxwQBZqCrGsaqsbqNeL16xsh7Se8StVrvjqGcYZwI/52d8kOUdpSKrKUosBoOlcl+pstjzpzc5ht4ooBiqryumOn9CQEwvrU4Ua7cV1XUUZds3vitaMewe1TqSXneSVMVUM8lyS6m42UAVG7SVW4uxQM+foGKo/qb06vETkqbYYC6m68g1FZBhsYrl0IAhwelkKgY46MriJjmv2GCu3FUvL+78CSs22yp3S67OZ1ifrriYrgP9pk68YmJ4f3qCaS8mfdifmeS9NWIcryreJ/kExs+fsGLaiKRW509IpmLpXYefmWIVyzVgeE/SrttN1gnAR9YJhnGSibcKOD4o/LriimGSSJ3WifMnQjGp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"
                 style="width:127px;">
            <h3 title="">
                <a href="/">${options.site_title}</a>
            </h3>
            <div class="description">
                <p>这个冬天的太阳晒起来也是冷的，冰凉</p>
            </div>
        </div>
    </div>
    <ul class="social-links">
        <li>
            <a href="https://twitter.com/yangxuran">
                <i class="fa fa-twitter"></i>
            </a>
        </li>
        <li>
            <a href="/atom.xml">
                <i class="fa fa-rss"></i>
            </a>
        </li>
        <li>
            <a href="http://weibo.com/5272687222">
                <i class="fa fa-weibo"></i>
            </a>
        </li>
    </ul>
    <div class="footer">
        <a target="_blank" href="/">
            <span>Theme by </span>
        </a>
        <a href="https://www.caicai.me"> CaiCai </a>
        <span>&</span>
        <a href="https://github.com/Ben02/hexo-theme-Anatole"> Ben</a>
        <div class="by_farbox">
            <a href="https://hexo.io/zh-cn/" target="_blank">Proudly published with Hexo&#65281;</a>
        </div>
        <div class="by_farbox">
            <a href="http://www.miitbeian.gov.cn" target="_blank">蜀ICP备16017640号-1</a>
        </div>
        <div class="by_farbox">
            <a href="https://creativecommons.org/licenses/by-nc-sa/3.0/deed.zh" target="_blank">本站所有作品均使用 CC BY-NC-SA 3.0 授权</a>
        </div>
    </div>
</div>
<div class="main">
    <div class="page-top animated fadeInDown">
        <div class="nav">
            <li>
                <a href="/">首页</a>
            </li>
            <li>
                <a href="/about">关于</a>
            </li>
            <li>
                <a href="/archives">归档</a>
            </li>
            <li>
                <a href="/links">友链</a>
            </li>
        </div>
        <div class="information">
            <div class="back_btn">
                <li>
                    <a class="fa fa-chevron-left" onclick="window.history.go(-1)"> </a>
                </li>
            </div>
            <div class="avatar">
                <img src="data:image/png;base64,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">
            </div>
        </div>
    </div>
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
                                <span class="date">2018-01-08</span>
                                <i class="fa fa-comment-o"></i>
                                <a href="/dynamic-range-latitude-raw-log/#comments">评论</a>
                                <i class="fa fa-tag"></i>
                                <a class="tag" href="/categories/技术6/" title="技术6">技术6 </a>
                                <a class="tag" href="/tags/动态范围/" title="动态范围">动态范围 </a>
                                <a class="tag" href="/tags/宽容度/" title="宽容度">宽容度 </a>
                                <a class="tag" href="/tags/RAW/" title="RAW">RAW </a>
                                <a class="tag" href="/tags/Log/" title="Log">Log </a>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="share">
                    <div class="evernote">
                        <a class="fa fa-bookmark" href="javascript:(function(){EN_CLIP_HOST='http://www.evernote.com';try{var%20x=document.createElement('SCRIPT');x.type='text/javascript';x.src=EN_CLIP_HOST+'/public/bookmarkClipper.js?'+(new%20Date().getTime()/100000);document.getElementsByTagName('head')[0].appendChild(x);}catch(e){location.href=EN_CLIP_HOST+'/clip.action?url='+encodeURIComponent(location.href)+'&amp;title='+encodeURIComponent(document.title);}})();"
                           ref="nofollow" target="_blank"></a>
                    </div>
                    <div class="weibo">
                        <a class="fa fa-weibo" href="javascript:void((function(s,d,e){try{}catch(e){}var f='http://service.weibo.com/share/share.php?',u=d.location.href,p=['url=',e(u),'&amp;title=',e(d.title),'&amp;appkey=2924220432'].join('');function a(){if(!window.open([f,p].join(''),'mb',['toolbar=0,status=0,resizable=1,width=620,height=450,left=',(s.width-620)/2,',top=',(s.height-450)/2].join('')))u.href=[f,p].join('');};if(/Firefox/.test(navigator.userAgent)){setTimeout(a,0)}else{a()}})(screen,document,encodeURIComponent));"></a>
                    </div>
                    <div class="twitter">
                        <a class="fa fa-twitter" href="http://twitter.com/home?status=,https://www.isthnew.com/dynamic-range-latitude-raw-log/,可乐没气的猫,动态范围 宽容度 RAW Log 都是什么,;"></a>
                    </div>
                </div>
                <div class="pagination">
                    <ul class="clearfix">
                        <li class="pre pagbuttons">
                            <a class="btn" role="navigation" href="/choose-comment-system/" title="选择一个评论系统">上一篇</a>
                        </li>
                        <li class="next pagbuttons">
                            <a class="btn" role="navigation" href="/introduction-to-timelapse-photography/" title="延时摄影入门">下一篇</a>
                        </li>
                    </ul>
                </div>
                <a id="comments"></a>
                <div id="comment"></div>
            </div>
        </div>
    </div>
</div>
<script src="/Anatole/source/js/jquery.js"></script>
<script src="/Anatole/source/js/jquery-migrate-1.2.1.min.js"></script>
<script src="/Anatole/source/js/jquery.appear.js"></script>
<script>
    "serviceWorker" in navigator && window.addEventListener("load", function () {
        navigator.serviceWorker.register("/sw.js").then(function (e) {
            console.log("ServiceWorker registration successful with scope: ", e.scope)
        }).catch(function (e) {
            console.log("ServiceWorker registration failed: ", e)
        })
    });
</script>
<script>
    ! function (e, n, o) {
        var t = e.screen,
                a = encodeURIComponent,
                r = ["dt=" + a(n.title), "dr=" + a(n.referrer), "ul=" + (o.language || o.browserLanguage), "sd=" + t.colorDepth +
                "-bit", "sr=" + t.width + "x" + t.height, "vp=" + e.innerWidth + "x" + e.innerHeight, "z=" + +new Date
                ],
                i = "?" + r.join("&");
        e.__beacon_img = new Image, e.__beacon_img.src = "https://tools.isthnew.com/ga.php" + i
    }(window, document, navigator, location);
</script>
</body>

</html>