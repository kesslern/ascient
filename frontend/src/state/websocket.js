import { createAction } from 'redux-starter-kit'

const namespace = 'ascient/websocket'

const actions = {
  update: createAction(`${ namespace }/update`)
}

export { actions }
