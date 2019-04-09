const proxy = require('http-proxy-middleware')

module.exports = app => {
  app.use(proxy('/api/websocket', { target: 'http://localhost:8080', ws: true }))
}
