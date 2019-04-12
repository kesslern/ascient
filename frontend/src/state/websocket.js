import { createAction } from 'redux-starter-kit'

const namespace = 'ascient/websocket'

const actions = {
  set: createAction(`${ namespace }/set`),
  delete: createAction(`${ namespace }/delete`)
}

export { actions }
