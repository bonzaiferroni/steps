package ponder.steps.model

import kabinet.api.*
import kabinet.clients.GeminiMessage
import ponder.steps.model.data.Example
import ponder.steps.model.data.NewExample
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step

object Api: ParentEndpoint(null, apiPrefix) {
    object Gemini : ParentEndpoint(this, "/gemini") {
        object Chat : PostEndpoint<List<GeminiMessage>, String>(this, "/chat")
        object Image : PostEndpoint<String, String>(this, "/image")
    }
    object Examples : GetByIdEndpoint<Example>(this, "/example") {
        object User : GetEndpoint<List<Example>>(this, "/user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }

    object Steps: GetByIdEndpoint<Step>(this, "/step") {
        val includeChildren = EndpointParam("includeChildren", { it.toBoolean() }, { it.toString()})

        object Parent : GetByIdEndpoint<Step>(this, "/parent") {
            val includeChildren = EndpointParam("includeChildren", { it.toBoolean() }, { it.toString()})
        }
        object Children : GetByIdEndpoint<List<Step>>(this, "/children") {
            val includeChildren = EndpointParam("includeChildren", { it.toBoolean() }, { it.toString()})
        }
        object Root : GetEndpoint<List<Step>>(this, "/root") {
            val includeChildren = EndpointParam("includeChildren", { it.toBoolean() }, { it.toString()})
        }
        object GenerateImage : GetByIdEndpoint<String>(this, "/generate-image")
        object Create: PostEndpoint<NewStep, String>(this)
        object Delete: DeleteEndpoint<String>(this)
        object Update: UpdateEndpoint<Step>(this)
    }
}

val apiPrefix = "/api/v1"
