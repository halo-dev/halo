<!DOCTYPE HTML>
<!--
	Lens by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>
	<head>
		<title>图库 | ${options.site_title}</title>
		<meta charset="utf-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1" />
		<!--[if lte IE 8]><script src="/halo/source/plugins/gallery/js/ie/html5shiv.js"></script><![endif]-->
		<link rel="stylesheet" href="/halo/source/plugins/gallery/css/main.css" />
		<!--[if lte IE 8]><link rel="stylesheet" href="/halo/source/plugins/gallery/css/ie8.css" /><![endif]-->
		<!--[if lte IE 9]><link rel="stylesheet" href="/halo/source/plugins/gallery/css/ie9.css" /><![endif]-->
		<noscript><link rel="stylesheet" href="/halo/source/plugins/gallery/css/noscript.css" /></noscript>
	</head>
	<body class="is-loading-0 is-loading-1 is-loading-2">

		<!-- Main -->
			<div id="main">

				<!-- Header -->
					<header id="header">
						<h1>${options.site_title}</h1>
						<p>${user.userDesc}</p>
						<ul class="icons">
							<li><a href="#" class="icon fa-twitter"><span class="label">Twitter</span></a></li>
							<li><a href="#" class="icon fa-instagram"><span class="label">Instagram</span></a></li>
							<li><a href="#" class="icon fa-github"><span class="label">Github</span></a></li>
							<li><a href="#" class="icon fa-envelope-o"><span class="label">Email</span></a></li>
						</ul>
					</header>

				<!-- Thumbnail -->
					<section id="thumbnails">
						<#list galleries as gallery>
							<article>
								<a class="thumbnail" href="${gallery.galleryUrl}" data-position="left center"><img src="${gallery.galleryThumbnailUrl}" alt="" /></a>
								<h2>${gallery.galleryName}</h2>
								<p>${gallery.galleryDesc}</p>
							</article>
						</#list>
					</section>

				<!-- Footer -->
					<footer id="footer">
						<ul class="copyright">
							<li>&copy; Untitled.</li><li>Design: <a href="http://html5up.net">HTML5 UP</a>.</li>
						</ul>
					</footer>
			</div>

		<!-- Scripts -->
			<script src="/halo/source/plugins/gallery/js/jquery.min.js"></script>
			<script src="/halo/source/plugins/gallery/js/skel.min.js"></script>
			<!--[if lte IE 8]><script src="/halo/source/plugins/gallery/js/ie/respond.min.js"></script><![endif]-->
			<script src="/halo/source/plugins/gallery/js/main.js"></script>
	</body>
</html>