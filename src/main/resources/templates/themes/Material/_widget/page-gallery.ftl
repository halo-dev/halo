<link rel="stylesheet" href="/material/source/css/gallery.min.css">
<body>
<!-- Wrapper -->
<div id="wrapper">

    <!-- Header -->
    <header id="header">
        <h1>
            <a href="/"><strong>Ryan0up'S Blog</strong></a> - 图库
        </h1>
        <nav>
            <ul>
                <li>
                    <a href="#footer" class="icon fa-info-circle">
                        ©&nbsp;RYAN0UP
                    </a>
                </li>
            </ul>
        </nav>
    </header>

    <!-- Main -->
    <div id="main">
        <#list galleries as gallery>
            <article class="thumb">
                <a href="${gallery.galleryThumbnailUrl}" class="image lazy" data-original="${gallery.galleryThumbnailUrl}">
                    <img class="lazy" data-original="${gallery.galleryUrl}" alt="${gallery.galleryDesc}" />
                </a>
                <h2>${gallery.galleryName}</h2>
                <p>${gallery.galleryDate?string("yyyy-MM-dd")}</p>
            </article>
        </#list>

        <!--
        <article class="thumb">
            <a href="https://cdn.ryanc.cc/img/blog/gallery/Photo_2.jpeg" class="image lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_2.jpeg"><img class="lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_2.jpeg" alt="2016日落" /></a>
            <h2>2016日落</h2>
            <p>2016-04-04</p>
        </article>

        <article class="thumb">
            <a href="https://cdn.ryanc.cc/img/blog/gallery/Photo_3.jpeg" class="image lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_3.jpeg"><img class="lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_3.jpeg" alt="2015黄昏" /></a>
            <h2>2015黄昏</h2>
            <p>2015-7-7</p>
        </article>

        <article class="thumb">
            <a href="https://cdn.ryanc.cc/img/blog/gallery/Photo_4.jpeg" class="image lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_4.jpeg"><img class="lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_4.jpeg" alt="2016南山" /></a>
            <h2>2016南山</h2>
            <p>南山</p>
        </article>

        <article class="thumb">
            <a href="https://cdn.ryanc.cc/img/blog/gallery/Photo_5.jpeg" class="image lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_5.jpeg"><img class="lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_5.jpeg" alt="2016成都" /></a>
            <h2>2016成都</h2>
            <p>成都动物园</p>
        </article>

        <article class="thumb">
            <a href="https://cdn.ryanc.cc/img/blog/gallery/Photo_6.jpeg" class="image lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_6.jpeg"><img class="lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_6.jpeg" alt="2017重庆A" /></a>
            <h2>2017重庆A</h2>
            <p>2017-5-4</p>
        </article>

        <article class="thumb">
            <a href="https://cdn.ryanc.cc/img/blog/gallery/Photo_7.jpeg" class="image lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_7.jpeg"><img class="lazy" data-original="https://cdn.ryanc.cc/img/blog/gallery/Photo_7.jpeg" alt="2017重庆B" /></a>
            <h2>2017重庆B</h2>
            <p>2017-5-4</p>
        </article>
        -->
    </div>
    <!-- Footer -->
    <!--
    <footer id="footer" class="panel">
      <div class="inner split">
        <div>
          <section>
            <h2>Magna feugiat sed adipiscing</h2>
            <p>Nulla consequat, ex ut suscipit rutrum, mi dolor tincidunt erat, et scelerisque turpis ipsum eget quis orci mattis aliquet. Maecenas fringilla et ante at lorem et ipsum. Dolor nulla eu bibendum sapien. Donec non pharetra dui. Nulla consequat, ex ut suscipit rutrum, mi dolor tincidunt erat, et scelerisque turpis ipsum.</p>
          </section>
          <section>
            <h2>Follow me on ...</h2>
            <ul class="icons">
              <li><a href="#" class="icon fa-twitter"><span class="label">Twitter</span></a></li>
              <li><a href="#" class="icon fa-facebook"><span class="label">Facebook</span></a></li>
              <li><a href="#" class="icon fa-instagram"><span class="label">Instagram</span></a></li>
              <li><a href="#" class="icon fa-github"><span class="label">GitHub</span></a></li>
              <li><a href="#" class="icon fa-dribbble"><span class="label">Dribbble</span></a></li>
              <li><a href="#" class="icon fa-linkedin"><span class="label">LinkedIn</span></a></li>
              <li><a href="#" class="icon fa-telegram"><span class="label">Telegram</span></a></li>
            </ul>
          </section>
          <p class="copyright">
            &copy; Design: <a href="#">Gallery</a>.
          </p>
        </div>
        <div>
          <section>
            <h2>Get in touch</h2>
            <form method="post" action="#">
              <div class="field half first">
                <input type="text" name="name" id="name" placeholder="Name" />
              </div>
              <div class="field half">
                <input type="text" name="email" id="email" placeholder="Email" />
              </div>
              <div class="field">
                <textarea name="message" id="message" rows="4" placeholder="Message"></textarea>
              </div>
              <ul class="actions">
                <li><input type="submit" value="Send" class="special" /></li>
                <li><input type="reset" value="Reset" /></li>
              </ul>
            </form>
          </section>
        </div>
      </div>
    </footer>
    -->

</div>

<!-- Scripts -->
<script src="/material/source/js/gallery/gallery.js"></script>
<script>lsloader.load("lazyload_js","/material/source/js/lazyload.min.js?1BcfzuNXqV+ntF6gq+5X3Q==", true)</script>
<script>
    queue.offer(function(){
        $('.lazy').lazyload({
            effect: 'fadeIn',
            event: 'scrollstop'
        });
    })
    <!-- Start Queue -->
    $(document).ready(function(){
        setInterval(function(){
            queue.execNext();
        },200);
    });
</script>
</body>
