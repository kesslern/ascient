import React, { Component } from 'react'
import { ListGroup, ListGroupItem, CloseButton } from 'react-bootstrap'
import './App.css'
import PropTypes from 'prop-types'

class Booleans extends Component {
  static propTypes = {
    doDeleteBoolean: PropTypes.func.isRequired,
    doCreateBoolean: PropTypes.func.isRequired,
    doRetrieveBooleans: PropTypes.func.isRequired,
    doSetBoolean: PropTypes.func.isRequired,
    booleans: PropTypes.array.isRequired
  }

  state = {
    newBoolean: ''
  }

  componentDidMount() {
    this.props.doRetrieveBooleans()
  }

  toggleBoolean = boolean => {
    this.props.doSetBoolean(boolean.id, !boolean.value)
  }

  onChange = event => {
    this.setState({ newBoolean: event.target.value })
  }

  onKeyPress = event => {
    if (event.key === 'Enter') {
      this.props.doCreateBoolean(this.state.newBoolean)
      this.setState({ newBoolean: '' })
    }
  }

  render() {
    const { newBoolean } = this.state
    const { doDeleteBoolean } = this.props

    return (
      <div className="booleans">
        <ListGroup>
          {this.props.booleans.map(bool => {
            const style = bool.value ? 'success' : 'danger'

            return (
              <ListGroupItem
                key={bool.id}
                variant={style}
                onClick={() => {
                  this.toggleBoolean(bool)
                }}>
                <div className="name">
                  {bool.name}
                  <CloseButton
                    onClick={event => {
                      event.stopPropagation()
                      doDeleteBoolean(bool.id)
                    }}/>
                </div>
                <div className="subtext">Created at: {bool.creationTime}</div>
                <div className="subtext">Updated: {bool.updatedAt}</div>
              </ListGroupItem>
            )
          })}
          <ListGroupItem>
            <input
              type="text"
              name="newBoolean"
              value={newBoolean}
              onChange={this.onChange}
              onKeyPress={this.onKeyPress} />
          </ListGroupItem>
        </ListGroup>
      </div>
    )
  }
}

export default Booleans
