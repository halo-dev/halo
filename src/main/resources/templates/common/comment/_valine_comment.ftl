<#-- Valine comment -->
<style type="text/css" rel="stylesheet">
    ${options.valine_css?if_exists}
</style>
<div id="valine-comment"></div>
<script src="//cdn1.lncld.net/static/js/3.0.4/av-min.js"></script>
<script src="//unpkg.com/valine/dist/Valine.min.js"></script>
<script>
    var GUEST_INFO = ['nick','mail','link'];
    var guest_info = 'nick,mail,link'.split(',').filter(function(item){
        return GUEST_INFO.indexOf(item) > -1
    });
    var notify = 'true' == true;
    var verify = 'true' == true;
    new Valine({
        el: '#valine-comment',
        notify: notify,
        verify: verify,
        appId: "${options.valine_appid?if_exists}",
        appKey: "${options.valine_appkey?if_exists}",
        placeholder: "${options.valine_placeholder?if_exists}",
        pageSize:'10',
        avatar:'${options.valine_avatar?if_exists}',
        lang:'zh-cn'
    });
</script>
