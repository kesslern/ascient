function websocketStuff(sessionId) {
  window.socket = new WebSocket(`ws://${ window.location.host }/api/websocket`)
  window.socket.addEventListener('open', () => {
    window.socket.send(sessionId)
  })
}

class Api {
  sessionId = null

  async authenticate(username, password) {
    const response = await fetch(
      `/api/users/authenticate?username=${ username }&password=${ password }`,
      { method: 'POST' }
    )
    if (response.ok) {
      const body = JSON.parse(await response.text())
      this.sessionId = body.sessionId
      websocketStuff(body.sessionId)
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
