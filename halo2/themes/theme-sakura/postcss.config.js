/* 
* Postcss 插件说明。
* postcss-mixins (https://github.com/postcss/postcss-mixins)  -- 用于书写通用的 css mixins，使响应式等操作更加通用化。
* postcss-nesting  (https://github.com/csstools/postcss-plugins/tree/main/plugins/postcss-nesting) -- 基于 CSS Nesting 语法规范的嵌套语法。
* postcss-custom-properties (https://github.com/csstools/postcss-plugins/tree/main/plugins/postcss-custom-properties) -- 支持自定义 css 变量。
* colorguard (https://github.com/SlexAxton/css-colorguard) -- 检查是否具有相似的颜色
* autoprefixer (https://github.com/postcss/autoprefixer) -- 添加浏览器前缀。
* 
*/
module.exports = {
  plugins: [
    require('postcss-mixins'),
    require('postcss-nesting'),
    require('postcss-custom-properties'),
    // colorguard 对于透明度无法识别，所以在处理时需要注意。
    // require('colorguard'),
    require('autoprefixer')
  ]
}
