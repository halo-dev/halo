<script>lsloader.load("np_js","/material/source/js/nprogress.js?pl3Qhb9lvqR1FlyLUna1Yw==", true)</script>

<script type="text/ls-javascript" id="NProgress-script">
    NProgress.configure({
        showSpinner: true
    });
    NProgress.start();
    $('#nprogress .bar').css({
        'background': '${options.theme_material_uiux_nprogress_color!"#29d"}'
    });
    $('#nprogress .peg').css({
        'box-shadow': '0 0 10px ${options.theme_material_uiux_nprogress_color!'#29d'}, 0 0 15px ${options.theme_material_uiux_nprogress_color!'#29d'}'
    });
    $('#nprogress .spinner-icon').css({
        'border-top-color': '${options.theme_material_uiux_nprogress_color!"#29d"}',
        'border-left-color': '${options.theme_material_uiux_nprogress_color!"#29d"}'
    });
    setTimeout(function() {
        NProgress.done();
        $('.fade').removeClass('out');
    }, ${options.theme_material_uiux_nprogress_buffer!'800'});
</script>
