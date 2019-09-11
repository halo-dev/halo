import { message } from 'ant-design-vue/es'
// import defaultSettings from '../defaultSettings';

let lessNodesAppended

const colorList = [
  {
    key: '红色',
    color: '#F5222D'
  },
  {
    key: '浅红色',
    color: '#FA541C'
  },
  {
    key: '日暮',
    color: '#FAAD14'
  },
  {
    key: '青色',
    color: '#13C2C2'
  },
  {
    key: '绿色',
    color: '#52C41A'
  },
  {
    key: '默认',
    color: '#1890FF'
  },
  {
    key: '蓝色',
    color: '#2F54EB'
  },
  {
    key: '紫色',
    color: '#722ED1'
  }
]

const updateTheme = primaryColor => {
  // Don't compile less in production!
  /* if (process.env.NODE_ENV === 'production') {
    return;
  } */
  // Determine if the component is remounted
  if (!primaryColor) {
    return
  }
  const hideMessage = message.loading('正在编译主题！', 0)
  function buildIt() {
    if (!window.less) {
      return
    }
    setTimeout(() => {
      window.less
        .modifyVars({
          '@primary-color': primaryColor
        })
        .then(() => {
          hideMessage()
        })
        .catch(() => {
          message.error('Failed to update theme')
          hideMessage()
        })
    }, 200)
  }
  if (!lessNodesAppended) {
    // insert less.js and color.less
    const lessStyleNode = document.createElement('link')
    const lessConfigNode = document.createElement('script')
    const lessScriptNode = document.createElement('script')
    lessStyleNode.setAttribute('rel', 'stylesheet/less')
    lessStyleNode.setAttribute('href', '/color.less')
    lessConfigNode.innerHTML = `
      window.less = {
        async: true,
        env: 'production',
        javascriptEnabled: true
      };
    `
    lessScriptNode.src = 'https://cdnjs.loli.net/ajax/libs/less.js/3.8.1/less.min.js'
    lessScriptNode.async = true
    lessScriptNode.onload = () => {
      buildIt()
      lessScriptNode.onload = null
    }
    document.body.appendChild(lessStyleNode)
    document.body.appendChild(lessConfigNode)
    document.body.appendChild(lessScriptNode)
    lessNodesAppended = true
  } else {
    buildIt()
  }
}

export { updateTheme, colorList }
