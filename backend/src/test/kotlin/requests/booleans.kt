package requests

import us.kesslern.ascient.util.QueryParamBase

class BooleanCreateParams(
    val name: String? = null,
    val value: String? = null
) : QueryParamBase()

class BooleanUpdateParams(
    val value: String? = null
) : QueryParamBase()
