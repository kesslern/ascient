import { createAction, createReducer } from 'redux-starter-kit'
import api from '../api'

const namespace = 'ascient-frontend/authentication'

export const actions = {
  authenticate: createAction(`${ namespace }/authenticate`)
}

export const authenticationReducer = createReducer({}, {
  [actions.authenticate]: (state, action) => {
    state.sessionId = action.payload.sessionId
    state.mustChangePassword = action.payload.mustChangePassword
    state.authenticated = true
  },
})

export function doAuthentication(username, password) {
  return async dispatch => {
    const response = await api.authenticate(username, password)
    dispatch(actions.authenticate(response))
  }
}
