import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Card, CardContent, Typography } from '@material-ui/core'
import { withStyles } from '@material-ui/core/styles'
import red from '@material-ui/core/colors/red'
import green from '@material-ui/core/colors/green'
import FormControl from '@material-ui/core/FormControl'
import InputLabel from '@material-ui/core/InputLabel'
import Input from '@material-ui/core/Input'
import CardActions from '@material-ui/core/CardActions'
import Button from '@material-ui/core/Button'

const styles = theme => ({
  main: {
    width: 'auto',
    display: 'block',
    marginLeft: theme.spacing.unit * 3,
    marginRight: theme.spacing.unit * 3,
    marginTop: theme.spacing.unit * 8,
    [theme.breakpoints.up(400 + theme.spacing.unit * 3 * 2)]: {
      width: 400,
      marginLeft: 'auto',
      marginRight: 'auto',
    },
  },
  cardContent: {
    paddingBottom: '0 !important'
  },
  actions: {
    flexDirection: 'row-reverse'
  },
  false: {
    background: red[200]
  },
  true: {
    background: green[200]
  }
})

class Booleans extends Component {
  static propTypes = {
    doDeleteBoolean: PropTypes.func.isRequired,
    doCreateBoolean: PropTypes.func.isRequired,
    doRetrieveBooleans: PropTypes.func.isRequired,
    doSetBoolean: PropTypes.func.isRequired,
    booleans: PropTypes.array.isRequired,
    classes: PropTypes.object.isRequired,
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
    const { doDeleteBoolean, classes } = this.props

    return (
      <div className={classes.main}>
        {this.props.booleans.map(bool => (
          <Card
            className={bool.value ? classes.true : classes.false}
            key={bool.id}
            onClick={() => {
              this.toggleBoolean(bool)
            }}>
            <CardContent className={classes.cardContent}>
              <Typography variant="h5" component="h2">
                {bool.name}
              </Typography>
              <Typography className={classes.pos} color="textSecondary">
          Created at: {bool.creationTime}
              </Typography>
              <Typography className={classes.pos} color="textSecondary">
          Updated: {bool.updatedAt}
              </Typography>
              <CardActions className={classes.actions}>
                <Button size="small" onClick={e => {
                  e.stopPropagation()
                  doDeleteBoolean(bool.id)
                }}>Delete</Button>
              </CardActions>
            </CardContent>
          </Card>
        ))}
        <FormControl margin="normal" fullWidth>
          <InputLabel htmlFor="newBoolean">New Boolean</InputLabel>
          <Input
            id="newBoolean"
            name="newBoolean"
            value={newBoolean}
            onKeyPress={this.onKeyPress}
            onChange={this.onChange}
            autoComplete="off"
            autoFocus
          />
        </FormControl>
      </div>
    )
  }
}

export default withStyles(styles)(Booleans)
