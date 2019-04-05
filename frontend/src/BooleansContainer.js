import { connect } from 'react-redux'
import Booleans from './Booleans'
import { doDeleteBoolean, doCreateBoolean, doRetrieveBooleans, doSetBoolean } from './state/booleans'

const mapStateToProps = state => ({
  booleans: Object.values(state.entities.booleans)
})

const mapDispatchToProps = dispatch => ({
  doDeleteBoolean: id => dispatch(doDeleteBoolean(id)),
  doCreateBoolean: name => dispatch(doCreateBoolean(name)),
  doRetrieveBooleans: () => dispatch(doRetrieveBooleans()),
  doSetBoolean: (id, value) => dispatch(doSetBoolean(id, value)),
})

export default connect(mapStateToProps, mapDispatchToProps)(Booleans)
