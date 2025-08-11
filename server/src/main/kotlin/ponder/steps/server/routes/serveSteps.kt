package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.environment.Environment
import klutch.environment.readEnvFromPath
import klutch.server.*
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.model.data.StepSuggestGeminiResponse
import ponder.steps.model.data.StepSuggestResponse
import ponder.steps.model.data.StepWithDescription
import ponder.steps.model.data.toAiPrompt
import klutch.gemini.GeminiService
import ponder.steps.server.db.services.PathService
import java.io.File

fun Routing.serveSteps(
    service: PathService = PathService(),
    gemini: GeminiService = GeminiService()
) {
    // Ahoy! This route be for fetchin' a single step by its id!
    get(Api.Steps, { it }) { stepId, endpoint ->
        service.readStep(stepId)
    }

    authenticateJwt {
        post(Api.Steps.Create) { newStep, endpoint ->
            val userId = call.getUserId()
            service.createStep(newStep, userId)
        }

        update(Api.Steps.Update) { step, endpoint ->
            service.updateStep(step)
        }

        delete(Api.Steps.Delete) { stepId, endpoint ->
            service.deleteStep(stepId)
        }

        get(Api.Steps.Children, { it }) { id, endpoint ->
            val parentId = call.getIdOrThrow { it }
            service.readChildren(parentId)
        }

        get(Api.Steps.Root) { endpoint ->
            service.readRootSteps()
        }

        get(Api.Steps.Search) { endpoint ->
            val query = endpoint.query.readParam(call)
            service.searchSteps(query)
        }

        get(Api.Steps.GenerateImageV1, { it }) { id, endpoint ->
            val step = service.readStep(id) ?: error("step missing: $id")
            val urls = gemini.generateImage(step.label)
            val isSuccess = service.updateStep(step.copy(imgUrl = urls.url, thumbUrl = urls.thumbUrl))
            if (!isSuccess) error("unable to generate image")
            urls
        }

        post(Api.Steps.GenerateImageV2) { request, endpoint ->
            val prompt = request.toAiPrompt()
            gemini.generateImage(prompt, request.stepLabel)
        }

        post(Api.Steps.Suggest) { request, endpoint ->
            val template = File("../docs/step_suggest_request.md").readText()
            val prompt = request.toAiPrompt(template)
            val response: StepSuggestGeminiResponse? = gemini.requestJson(prompt)
            val suggestions = response?.suggestions?.map {
                val split = it.split(":")
                StepWithDescription(split[0], split.getOrNull(1)?.trim())
            } ?: error("suggestions is null")
            StepSuggestResponse(suggestions)
        }
    }
}

//val prompt = promptFromTemplate(
//            "../docs/article_reader-read_article.md",
//            "document_types" to documentTypes,
//            "news_categories" to newsCategories,
//            "news_type" to newsTypes,
//            "article_content" to articleContent,
//            "unclear_text" to PERSON_UNCLEAR
//        )


// Provide the document type that best fits, exactly as it appears in the list:
//<|document_types|>