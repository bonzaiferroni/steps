package ponder.contemplate.model

import kabinet.api.*
import ponder.contemplate.model.data.Example
import ponder.contemplate.model.data.NewExample

object Api: ParentEndpoint(null, apiPrefix) {
    object Examples : GetByIdEndpoint<Example>(this, "/example") {
        object User : GetEndpoint<List<Example>>(this, "/user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }
}

val apiPrefix = "/api/v1"
