package ponder.steps.model

import kabinet.api.*
import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import ponder.steps.model.data.Example
import ponder.steps.model.data.Focus
import kabinet.model.ImageUrls
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewExample
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.NewStep
import kabinet.model.SpeechRequest
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepImageRequest
import ponder.steps.model.data.StepSuggestRequest
import ponder.steps.model.data.StepSuggestResponse
import ponder.steps.model.data.TrekItem

object Api: ParentEndpoint(null, apiPrefix) {
    object Gemini : ParentEndpoint(this, "/gemini") {
        object Chat : PostEndpoint<List<GeminiMessage>, String>(this, "/chat")
        object Image : PostEndpoint<String, ImageUrls>(this, "/image")
        object GenerateSpeech: PostEndpoint<SpeechRequest, String>(this, "/generate-speech")
    }

    object Examples : GetByIdEndpoint<Example>(this, "/example") {
        object User : GetEndpoint<List<Example>>(this, "/user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }

    object Steps: GetByIdEndpoint<Step>(this, "/step") {
        object Create: PostEndpoint<NewStep, String>(this)
        object Update: UpdateEndpoint<Step>(this)
        object Delete: DeleteEndpoint<String>(this)

        object Children : GetByIdEndpoint<List<Step>>(this, "/children")
        object Root : GetEndpoint<List<Step>>(this, "/root")
        object Search : GetEndpoint<List<Step>>(this, "/search") {
            val query = EndpointParam("query", { it }, { it })
        }
        object GenerateImageV1 : GetByIdEndpoint<ImageUrls>(this, "/generate-image-v1")
        object GenerateImageV2: PostEndpoint<StepImageRequest, ImageUrls>(this, "/generate-image-v2")
        object Suggest: PostEndpoint<StepSuggestRequest, StepSuggestResponse>(this, "/suggest")
    }

    object Sync: ParentEndpoint(this, "/sync") {
//        object Read: PostEndpoint<ReadSyncRequest, FullSync>(this, "/read")
//        object Write: PostEndpoint<FullSync, Boolean>(this, "/write")
    }

    object Intents: GetByIdEndpoint<Intent>(this, "/intent") {
        object User : GetEndpoint<List<Intent>>(this, "/user")
        object Create: PostEndpoint<NewIntent, String>(this)
        object Delete: DeleteEndpoint<String>(this)
        object Update: UpdateEndpoint<Intent>(this)
    }

    object Journey: ParentEndpoint(this, "/journey") {
        object UserTreks : GetEndpoint<List<TrekItem>>(this, "/active")
        object CompleteStep : PostEndpoint<String, Boolean>(this, "/complete")
        object StartTrek : PostEndpoint<String, Boolean>(this, "/start")
        object PauseTrek : PostEndpoint<String, Boolean>(this, "/pause")
        object StepIntoPath : PostEndpoint<String, Boolean>(this, "/step-into-path")
        object FocusTrek : GetEndpoint<Focus?>(this, "/focus")
    }
}

val apiPrefix = "/api/v1"

val geminiApi = object : GeminiApi {
    override val chat = Api.Gemini.Chat
    override val image = Api.Gemini.Image
    override val speech = Api.Gemini.GenerateSpeech
}