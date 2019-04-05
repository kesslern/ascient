import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Route, Switch, Redirect } from 'react-router'
import PropTypes from 'prop-types'

import AppContainer from './AppContainer'
import BooleansContainer from './BooleansContainer'

const PrivateRoute = ({ authenticated, component: Component, ...rest }) => (
  <Route {...rest} render={props => (
    authenticated ?
      <Component {...props} /> :
      <Redirect to="/" />
  )} />
)

class Routes extends Component {
  static propTypes = {
    authenticated: PropTypes.bool.isRequired
  }

  render() {
    const { authenticated } = this.props

    return (
      <Switch>"
        <Route exact path="/" component={AppContainer} />
        <PrivateRoute
          authenticated={authenticated}
          exact path="/booleans"
          component={BooleansContainer} /> :
        <Redirect to="/"/>
      </Switch>
    )
  }
}

const mapStateToProps = state => ({
  authenticated: state.authentication.authenticated
})

export default connect(mapStateToProps)(Routes)
