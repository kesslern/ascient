import { createAction, createReducer } from 'redux-starter-kit'
import api from '../api'

const namespace = 'ascient-ui/booleans'

export const actions = {
  delete: createAction(`${ namespace }/delet`),
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
})

export function doRetrieveBooleans() {
  return async dispatch => {
    const booleans = JSON.parse(await api.request('/api/booleans'))
    const normalizedBooleans = booleans.reduce((acc, current) => {
      acc[current.id] = current
      return acc
    }, {})
    dispatch(actions.set(normalizedBooleans))
  }
}

export function doSetBoolean(id, value) {
  return async dispatch => {
    const result = await api.request(`/api/booleans/${ id }?value=${ value }`, { method: 'PUT' })
    const updatedBoolean = JSON.parse(result)
    dispatch(actions.update(updatedBoolean))
  }
}

export function doCreateBoolean(name) {
  return async dispatch => {
    const result = await api.request(`/api/booleans?name=${ name }`, { method: 'POST' })
    const updatedBoolean = JSON.parse(result)
    dispatch(actions.update(updatedBoolean))
  }
}

export function doDeleteBoolean(id) {
  return dispatch => {
    api.request(`/api/booleans/${ id }`, { method: 'DELETE' })
    dispatch(actions.delete(id))
  }
}
