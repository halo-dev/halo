<#-- livere_comment -->
<style type="text/css" rel="stylesheet">
    ${options.livere_css?if_exists}
</style>
<div id="livere-comment">
    <div id="lv-container" data-id="city" data-uid="${options.livere_data_uid?if_exists}">
        <script type="text/javascript">
            (function(d, s) {
                var j, e = d.getElementsByTagName(s)[0];
                if (typeof LivereTower === 'function') { return; }
                j = d.createElement(s);
                j.src = 'https://cdn-city.livere.com/js/embed.dist.js';
                j.async = true;
                e.parentNode.insertBefore(j, e);
            })(document, 'script');
        </script>
        <noscript> 为正常使用来必力评论功能请激活JavaScript</noscript>
    </div>
</div>
