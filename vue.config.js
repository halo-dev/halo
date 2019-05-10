const path = require('path')
const webpack = require('webpack')
// const GenerateAssetPlugin = require('generate-asset-webpack-plugin')

function resolve(dir) {
  return path.join(__dirname, dir)
}

// var createServerConfig = function(compilation) {
//   const configJson = {
//     apiUrl: 'http://localhost:8090'
//   }
//   return JSON.stringify(configJson)
// }

// vue.config.js
module.exports = {
  configureWebpack: {
    plugins: [
      new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/)
      // new GenerateAssetPlugin({
      //   filename: 'config.json',
      //   fn: (compilation, cb) => {
      //     cb(null, createServerConfig(compilation))
      //   },
      //   extraFiles: []
      // })
    ]
  },

  chainWebpack: (config) => {
    config.resolve.alias
      .set('@$', resolve('src'))
      .set('@api', resolve('src/api'))
      .set('@assets', resolve('src/assets'))
      .set('@comp', resolve('src/components'))
      .set('@views', resolve('src/views'))
      .set('@layout', resolve('src/layout'))
      .set('@static', resolve('src/static'))

    const svgRule = config.module.rule('svg')
    svgRule.uses.clear()
    svgRule
      .oneOf('inline')
      .resourceQuery(/inline/)
      .use('vue-svg-icon-loader')
      .loader('vue-svg-icon-loader')
      .end()
      .end()
      .oneOf('external')
      .use('file-loader')
      .loader('file-loader')
      .options({
        name: 'assets/[name].[hash:8].[ext]'
      })
  },

  css: {
    loaderOptions: {
      less: {
        modifyVars: {
          /*
          'primary-color': '#F5222D',
          'link-color': '#F5222D',
          'border-radius-base': '4px',
          */
        },
        javascriptEnabled: true
      }
    }
  },

  lintOnSave: undefined,
  // babel-loader no-ignore node_modules/*
  transpileDependencies: [],
  productionSourceMap: false
}
