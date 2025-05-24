package ponder.contemplate.model

import kabinet.api.*
import ponder.contemplate.model.data.Example
import ponder.contemplate.model.data.NewExample
import ponder.contemplate.model.data.NewStep
import ponder.contemplate.model.data.Step

object Api: ParentEndpoint(null, apiPrefix) {
    object Examples : GetByIdEndpoint<Example>(this, "/example") {
        object User : GetEndpoint<List<Example>>(this, "/user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }

    object Steps: GetByIdEndpoint<Step>(this, "/step") {
        object Parent : GetEndpoint<List<Step>>(this, "/parent/{id}")
        object Root : GetEndpoint<List<Step>>(this, "/root")
        object Create: PostEndpoint<NewStep, Int>(this)
        object Delete: DeleteEndpoint<Int>(this)
        object Update: UpdateEndpoint<Step>(this)
    }
}

val apiPrefix = "/api/v1"
