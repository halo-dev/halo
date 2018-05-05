<!-- Import Roboto -->
<link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500" rel="stylesheet">
<!--
<% if(theme.fonts.use === "google") { %>
    <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500" rel="stylesheet">
<% } else if(theme.fonts.use === "ustc") { %>
    <link href="https://fonts.proxy.ustclug.org/css?family=Roboto:300,400,500" rel="stylesheet">
<% } else if(theme.fonts.use === "custom") { %>
    <link href="<%= theme.fonts.custom_font_host %>/css?family=Roboto:300,400,500" rel="stylesheet">
<% } else if(theme.fonts.use === "baomitu") { %>
    <style>
        @font-face {
            font-family: Roboto;
            font-style: normal;
            font-weight: 300;
            src: url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-300.eot);
            src: local('Roboto'),local('Roboto-Normal'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-300.eot?#iefix) format('embedded-opentype'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-300.woff2) format('woff2'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-300.woff) format('woff'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-300.ttf) format('truetype'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-300.svg#Roboto) format('svg')
        }

        @font-face {
            font-family: Roboto;
            font-style: normal;
            font-weight: regular;
            src: url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-regular.eot);
            src: local('Roboto'),local('Roboto-Normal'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-regular.eot?#iefix) format('embedded-opentype'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-regular.woff2) format('woff2'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-regular.woff) format('woff'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-regular.ttf) format('truetype'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-regular.svg#Roboto) format('svg')
        }

        @font-face {
            font-family: Roboto;
            font-style: normal;
            font-weight: 500;
            src: url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-500.eot);
            src: local('Roboto'),local('Roboto-Normal'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-500.eot?#iefix) format('embedded-opentype'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-500.woff2) format('woff2'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-500.woff) format('woff'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-500.ttf) format('truetype'),url(https://lib.baomitu.com/fonts/roboto/roboto-v15-latin-500.svg#Roboto) format('svg')
        }
    </style>
<% } else if(theme.fonts.use === "catnet") { %>
    <style>
        @font-face {
            font-family: 'Roboto';
            font-style: normal;
            font-weight: 300;
            src: local('Roboto Light'), local('Roboto-Light'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-Light.woff2') format('woff2'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-Light.woff') format('woff');
        }
        @font-face {
            font-family: 'Roboto';
            font-style: italic;
            font-weight: 300;
            src: local('Roboto LightItalic'), local('Roboto-LightItalic'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-LightItalic.woff2') format('woff2'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-LightItalic.woff') format('woff');
        }
        @font-face {
            font-family: 'Roboto';
            font-style: normal;
            font-weight: 400;
            src: local('Roboto Regular'), local('Roboto-Regular'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-Regular.woff2') format('woff2'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-Regular.woff') format('woff');
        }
        @font-face {
            font-family: 'Roboto';
            font-style: italic;
            font-weight: 400;
            src: local('Roboto RegularItalic'), local('Roboto-RegularItalic'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-RegularItalic.woff2') format('woff2'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-RegularItalic.woff') format('woff');
        }
        @font-face {
            font-family: 'Roboto';
            font-style: normal;
            font-weight: 500;
            src: local('Roboto Medium'), local('Roboto-Medium'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-Medium.woff2') format('woff2'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-Medium.woff') format('woff');
        }
        @font-face {
            font-family: 'Roboto';
            font-style: italic;
            font-weight: 500;
            src: local('Roboto MediumItalic'), local('Roboto-MediumItalic'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-MediumItalic.woff2') format('woff2'), url('https://cdnjs.cat.net/ajax/libs/mdui/0.2.1/fonts/roboto/Roboto-MediumItalic.woff') format('woff');
        }
    </style>
<% } %>
-->
<!-- Import Material Icons -->
<!--
<% if(theme.vendors.materialcdn) { %>
    <%- cssLsload({path:(theme.vendors.materialcdn + '/css/material-icons.css'),key:'material_icons'}) %>
<% } else { %>
    <%- cssLsload({path:('css/material-icons.css'),key:'material_icons'}) %>
<% } %>
-->
<style id="material_icons"></style><script>if(typeof window.lsLoadCSSMaxNums === "undefined")window.lsLoadCSSMaxNums = 0;window.lsLoadCSSMaxNums++;lsloader.load("material_icons","/material/source/css/material-icons.css?pqhB/Rd/ab0H2+kZp0RDmw==",function(){if(typeof window.lsLoadCSSNums === "undefined")window.lsLoadCSSNums = 0;window.lsLoadCSSNums++;if(window.lsLoadCSSNums == window.lsLoadCSSMaxNums)document.documentElement.style.display="";}, false)</script>
