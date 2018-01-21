<!-- Valine comment -->
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
        appId: "${options.valine_appid}",
        appKey: "${options.valine_appkey}",
        placeholder: "${options.valine_placeholder}",
        pageSize:'10',
        avatar:'${options.valine_avatar}',
        lang:'zh-cn'
    });
</script>