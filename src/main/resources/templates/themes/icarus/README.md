<p align="center" class="has-mb-6">
<img class="not-gallery-item" height="48" src="http://ppoffice.github.io/hexo-theme-icarus/images/logo.svg">
<br> A simple, delicate, and modern theme for the static site generator Hexo.
<br>
<a href="http://ppoffice.github.io/hexo-theme-icarus/">Preview</a> | 
<a href="http://ppoffice.github.io/hexo-theme-icarus/categories/">Documentation</a> |
<a href="https://github.com/ppoffice/hexo-theme-icarus/archive/master.zip">Download</a>
<br>
</p>

![Icarus](http://ppoffice.github.io/hexo-theme-icarus/gallery/preview.png?1 "Icarus Preview")

### :cd: Installation

Download & extract or `git clone` Icarus from GitHub to your blog's theme folder, and that's it!

```shell
git clone https://github.com/ppoffice/hexo-theme-icarus.git themes/icarus
```

Once started, Icarus will remind you of any missing dependencies and configuration files.

### :gift: Features

**Extensive Plugin Support**

Icarus includes plentiful search, comment, sharing and other plugins out of the box. You can choose any of them to enrich your
blog experience, or build your own plugin easily referring to the existing Icarus plugins.

Comment plugins

- [Changyan](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/changyan-comment-plugin/)
- [Disqus](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/disqus-comment-plugin/)
- [Facebook](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/facebook-comment-plugin/)
- [Gitment](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/gitment-comment-plugin/)
- [Isso](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/isso-comment-plugin/)
- [LiveRe](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/livere-comment-plugin/)
- [Valine](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Comment/valine-comment-plugin/)

Search plugins

- [Insight Search](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Search/insight-search-plugin/)
- [Google Custom Search Engine](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Search/google-cse-plugin/)
- [Baidu Site Search](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Search/baidu-search-plugin/)

Share plugins

- [AddThis](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Share/addthis-share-plugin/)
- [AddToAny](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Share/addtoany-share-plugin/)
- [Baidu Share](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Share/baidu-share-plugin/)
- [Share.js](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Share/share-js-share-plugin/)
- [ShareThis](http://ppoffice.github.io/hexo-theme-icarus/Plugins/Share/sharethis-share-plugin/)

Other plugins

- [Hexo Tag Plugin](http://ppoffice.github.io/hexo-theme-icarus/Configuration/Posts/hexo-built-in-tag-helpers/)
- [lightGallery & Justified Gallery](http://ppoffice.github.io/hexo-theme-icarus/Plugins/General/gallery-plugin/)
- [MathJax](http://ppoffice.github.io/hexo-theme-icarus/Plugins/General/mathjax-plugin/)
- [Site Analytics](http://ppoffice.github.io/hexo-theme-icarus/Plugins/General/site-analytics-plugin/)

**Rich Code Highlight Theme Choices**

Icarus directly import code highlight themes from the [highlight.js](https://highlightjs.org/) package, and makes more than 
70 highlight themes available to you.

<table>
    <tr>
        <td><img src="http://ppoffice.github.io/hexo-theme-icarus/gallery/code-highlight/atom-one-light.png"></td>
        <td><img src="http://ppoffice.github.io/hexo-theme-icarus/gallery/code-highlight/monokai.png"></td>
        <td><img src="http://ppoffice.github.io/hexo-theme-icarus/gallery/code-highlight/androidstudio.png"></td>
    </tr>
</table>

**Elastic Theme Configuration**

In addition to the minimalistic and easy-to-understand configuration design, Icarus allows you to set configurations on a 
per-page basis with the ability to merge and override partial configurations.

<div>
<table>
    <tr>
        <th>_config.yml</th>
        <th>post.md</th>
    </tr>
    <tr>
        <td>
            <pre>menu:
    Archives: /archives
    Categories: /categories
    Tags: /tags
    About: /about</pre>
        </td>
        <td>
            <pre>title: A Simple Post
menu:
    Go Home: /index.html
---
# Here is some simple markdown.</pre>
        </td>
    </tr>
    <tr>
        <td><img height="40" src="http://ppoffice.github.io/hexo-theme-icarus/gallery/navbar/main-config.png"></td>
        <td><img height="40" src="http://ppoffice.github.io/hexo-theme-icarus/gallery/navbar/post-config.png"></td>
    </tr>
</table>
</div>

**Responsive Layout**

No matter what modern browsering device your audiences are using, they can always get the best experience because Icarus's responsive
layout across multiple viewpoints.

![Responsive Layout](http://ppoffice.github.io/hexo-theme-icarus/gallery/responsive.png)

### :hammer: Development

This project is built with

- Hexo 3.7.1
- Ejs
- Stylus
- Bulma 0.7.2

Please refer to the documentation for Icarus implementation details.

### :tada: Contribute

If you feel like to help us build a better Icarus, you can

:electric_plug: Write a plugin |
:black_nib: <a href="https://github.com/ppoffice/hexo-theme-icarus/new/site/source/_posts">Submit a tutorial</a> |
:triangular_flag_on_post: <a href="https://github.com/ppoffice/hexo-theme-icarus/issues/new">Report a bug</a> |
:earth_asia: <a href="https://github.com/ppoffice/hexo-theme-icarus/tree/master/languages">Add a translation</a>

### :memo: License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/ppoffice/hexo-theme-icarus/blob/master/LICENSE) file for details.
