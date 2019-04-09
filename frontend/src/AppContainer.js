import { connect } from 'react-redux'
import { push } from 'connected-react-router'
import { doAuthentication } from './state/authentication'
import App from './App'

const mapStateToProps = state => ({
  authenticated: state.authentication.authenticated,
  sessionId: state.authentication.sessionId,
})

const mapDispatchToProps = dispatch => ({
  authenticate: (username, password) => dispatch(doAuthentication(username, password)),
  navigateToBooleans: () => dispatch(push('/booleans'))
})

export default connect(mapStateToProps, mapDispatchToProps)(App)
