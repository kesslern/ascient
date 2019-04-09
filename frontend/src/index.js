import React from 'react'
import ReactDOM from 'react-dom'
import { configureStore, combineReducers, getDefaultMiddleware } from 'redux-starter-kit'
import { Provider } from 'react-redux'
import { createBrowserHistory } from 'history'
import { connectRouter, routerMiddleware, ConnectedRouter } from 'connected-react-router'

import './index.css'
import Routes from './Routes'
import defaultState from './state/default-state'
import { authenticationReducer } from './state/authentication'
import { booleanEntities } from './state/booleans'

const history = createBrowserHistory()

const store = configureStore({
  reducer: {
    router: connectRouter(history),
    authentication: authenticationReducer,
    entities: combineReducers({
      booleans: booleanEntities
    })
  },
  middleware: [ ...getDefaultMiddleware(), routerMiddleware(history) ],
  preloadedState: defaultState,
})

window.websocket = new WebSocket(`ws://${ window.location.host }/api/websocket`)

function render(Component) {
  ReactDOM.render(
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <Component />
      </ConnectedRouter>
    </Provider>, document.getElementById('root'))
}

render(Routes)

if (module.hot) {
  module.hot.accept('./Routes', () => {
    // eslint-disable-next-line global-require
    const NextApp = require('./Routes').default
    render(NextApp)
  })
}
