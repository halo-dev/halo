import { Spin } from 'ant-design-vue'

export default {
  name: 'PageLoading',
  render() {
    return (<div style={{ paddingTop: 100, textAlign: 'center' }}>
      <Spin size="large" />
    </div>)
  }
}
