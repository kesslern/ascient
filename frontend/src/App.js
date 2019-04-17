import React, { Component } from 'react'
import Button from '@material-ui/core/Button'
import FormControl from '@material-ui/core/FormControl'
import Input from '@material-ui/core/Input'
import InputLabel from '@material-ui/core/InputLabel'
import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import PropTypes from 'prop-types'
import withStyles from '@material-ui/core/styles/withStyles'

import './App.css'

const styles = theme => ({
  main: {
    width: 'auto',
    display: 'block',
    marginLeft: theme.spacing.unit * 3,
    marginRight: theme.spacing.unit * 3,
    [theme.breakpoints.up(400 + theme.spacing.unit * 3 * 2)]: {
      width: 400,
      marginLeft: 'auto',
      marginRight: 'auto',
    },
  },
  paper: {
    marginTop: theme.spacing.unit * 8,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: `${ theme.spacing.unit * 2 }px ${ theme.spacing.unit * 3 }px ${ theme.spacing.unit * 3 }px`,
  },
  form: {
    width: '100%',
    marginTop: theme.spacing.unit,
  },
  submit: {
    marginTop: theme.spacing.unit * 3,
  },
})

class App extends Component {
  static propTypes = {
    authenticated: PropTypes.bool.isRequired,
    authenticate: PropTypes.func.isRequired,
    navigateToBooleans: PropTypes.func.isRequired,
    classes: PropTypes.object.isRequired,
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
    const { classes } = this.props

    return (
      <main className={classes.main}>
        <Paper className={classes.paper}>
          <Typography component="h1" variant="h5">
          Sign in
          </Typography>
          <form className={classes.form} onSubmit={this.handleSubmit}>
            <FormControl margin="normal" required fullWidth>
              <InputLabel htmlFor="username">Username</InputLabel>
              <Input id="username" name="username" autoFocus />
            </FormControl>
            <FormControl margin="normal" required fullWidth>
              <InputLabel htmlFor="password">Password</InputLabel>
              <Input name="password" type="password" id="password" autoComplete="current-password" />
            </FormControl>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              color="primary"
              className={classes.submit}
            >
            Sign in
            </Button>
          </form>
        </Paper>
      </main>
    )
  }
}

export default withStyles(styles)(App)
