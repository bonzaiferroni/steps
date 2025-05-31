package ponder.steps.model

import kabinet.api.*
import kabinet.clients.GeminiMessage
import ponder.steps.model.data.Example
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewExample
import ponder.steps.model.data.NewIntent
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
        object Search : GetEndpoint<List<Step>>(this, "/search") {
            val query = EndpointParam("query", { it }, { it })
            val includeChildren = EndpointParam("includeChildren", { it.toBoolean() }, { it.toString()})
        }
        object GenerateImage : GetByIdEndpoint<String>(this, "/generate-image")
        object Create: PostEndpoint<NewStep, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Step>(this)
    }

    object Intents: GetByIdEndpoint<Intent>(this, "/intent") {
        object User : GetEndpoint<List<Intent>>(this, "/user")
        object Create: PostEndpoint<NewIntent, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Intent>(this)
    }
}

val apiPrefix = "/api/v1"
