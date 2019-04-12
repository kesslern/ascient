import { store } from './index.js'
import { actions } from './state/websocket'

class Api {
  sessionId = null
  webSocket = null

  initWebSocket() {
    this.socket = new WebSocket(`ws://${ window.location.host }/api/websocket`)
    this.socket.onmessage = this.verifyWebSocketAuth
    this.socket.onopen = () => {
      this.socket.send(this.sessionId)
    }
  }

  verifyWebSocketAuth = event => {
    if (event.data === 'Authenticated') {
      this.socket.onmessage = this.processWebSocketMessage
    } else {
      throw Error('Websocket auth failed')
    }
  }

  processWebSocketMessage = event => {
    const data = JSON.parse(event.data)
    if (data.action === 'SET') {
      store.dispatch(actions.set(data))
    } else {
      store.dispatch(actions.delete(data))
    }
  }

  async authenticate(username, password) {
    const response = await fetch(
      `/api/users/authenticate?username=${ username }&password=${ password }`,
      { method: 'POST' }
    )
    if (response.ok) {
      const body = JSON.parse(await response.text())
      this.sessionId = body.sessionId
      this.initWebSocket()
      return body
    }
    throw Error('unauthenticated')
  }

  async request(path, config) {
    config = config ? config : {}
    config.headers = { 'X-AscientSession': this.sessionId }
    const response = await fetch(path, config)
    return response.text()
  }
}

export default new Api()
