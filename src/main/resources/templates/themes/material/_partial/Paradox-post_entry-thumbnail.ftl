<script type="text/ls-javascript" id="thumbnail-script">
    var randomNum;

    var locatePost = $('.locate-thumbnail-symbol').next();
    for(var i = 0; i < ${options.index_posts!10}; i++) {
        randomNum = Math.floor(Math.random() * 19 + 1);

        locatePost.children('.post_thumbnail-random').attr('id', 'random_thumbnail-'+randomNum);
        locatePost.children('.post_thumbnail-random').attr('data-original', '/material/source/img/random/material-' + randomNum + '.png');
    $('.post_thumbnail-random').addClass('lazy');

        locatePost = locatePost.next();
    }
</script>
