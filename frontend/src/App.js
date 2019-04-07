import React, { Component } from 'react'
import { Button, Form } from 'react-bootstrap'
import PropTypes from 'prop-types'
import './App.css'

class App extends Component {
  static propTypes = {
    authenticated: PropTypes.bool.isRequired,
    authenticate: PropTypes.func.isRequired,
    navigateToBooleans: PropTypes.func.isRequired,
  }

  handleSubmit = event => {
    event.preventDefault()
    event.stopPropagation()
    const { username, password } = event.currentTarget
    this.props.authenticate(username.value, password.value)
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.authenticated) {
      this.props.navigateToBooleans()
    }
  }

  render() {
    return (
      <div className="authPage">
        <Form className="loginForm" onSubmit={this.handleSubmit}>
          <Form.Group controlId="username">
            <Form.Label>Username</Form.Label>
            <Form.Control type="text" placeholder="Username" />
          </Form.Group>

          <Form.Group controlId="password">
            <Form.Label>Password</Form.Label>
            <Form.Control type="password" placeholder="Password" />
          </Form.Group>

          <Button variant="primary" type="submit">
            Submit
          </Button>
        </Form>
      </div>
    )
  }
}

export default App
