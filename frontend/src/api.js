import { store } from './index.js'
import { actions } from './state/websocket'
import axios from 'axios'

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
    const response = await axios.post(
      `/api/users/authenticate?username=${ username }&password=${ password }`
    )
    this.sessionId = response.data.sessionId
    axios.defaults.headers.common['X-AscientSession'] = this.sessionId
    this.initWebSocket()
    return response.data
  }
}

export default new Api()
