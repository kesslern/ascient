import { createAction, createReducer } from 'redux-starter-kit'
import axios from 'axios'
import { actions as webSocketActions } from './websocket'

const namespace = 'ascient/booleans'

export const actions = {
  delete: createAction(`${ namespace }/delete`),
  set: createAction(`${ namespace }/set`),
  update: createAction(`${ namespace }/change`),
}

export const booleanEntities = createReducer({}, {
  [actions.delete]: (state, action) => {
    const id = action.payload
    delete state[id]
  },
  [actions.set]: (state, action) => action.payload,
  [actions.update]: (state, action) => {
    const boolean = action.payload
    state[boolean.id] = boolean
  },
  [webSocketActions.set]: (state, action) => {
    const boolean = action.payload.entity
    state[boolean.id] = boolean
  },
  [webSocketActions.delete]: (state, action) => {
    const id = action.payload.entity
    delete state[id]
  }
})

export function doRetrieveBooleans() {
  return async dispatch => {
    const { data: booleans } = await axios.get('/api/booleans')
    const normalizedBooleans = booleans.reduce((acc, current) => {
      acc[current.id] = current
      return acc
    }, {})
    dispatch(actions.set(normalizedBooleans))
  }
}

export function doSetBoolean(id, value) {
  return async dispatch => {
    const { data: updatedBoolean } = await axios.put(`/api/booleans/${ id }`, { value })
    dispatch(actions.update(updatedBoolean))
  }
}

export function doCreateBoolean(name) {
  return async dispatch => {
    const { data: updatedBoolean } = await axios.post(`/api/booleans?name=${ name }`)
    dispatch(actions.update(updatedBoolean))
  }
}

export function doDeleteBoolean(id) {
  return dispatch => {
    axios.delete(`/api/booleans/${ id }`)
    dispatch(actions.delete(id))
  }
}
