package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.clients.promptTemplate
import klutch.server.*
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.model.data.StepImageRequest
import ponder.steps.model.data.toAiPrompt
import ponder.steps.server.clients.GeminiService
import ponder.steps.server.db.services.PathService

fun Routing.serveSteps(
    service: PathService = PathService(),
    gemini: GeminiService = GeminiService()
) {
    // Ahoy! This route be for fetchin' a single step by its id!
    get(Api.Steps, { it }) { stepId, endpoint ->
        val includeChildren = endpoint.includeChildren.readParam(call)
        service.readStep(stepId, includeChildren)
    }

    authenticateJwt {
        get(Api.Steps.Parent, { it }) { id, endpoint ->
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.readParent(id, includeChildren)
        }

        get(Api.Steps.Children, { it }) { id, endpoint ->
            val parentId = call.getIdOrThrow { it }
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.readChildren(parentId, includeChildren)
        }

        get(Api.Steps.Root) { endpoint ->
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.readRootSteps(includeChildren)
        }

        get(Api.Steps.Search) { endpoint ->
            val query = endpoint.query.readParam(call)
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.searchSteps(query, includeChildren)
        }

        get(Api.Steps.Search) { endpoint ->
            val query = endpoint.query.readParam(call)
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.searchSteps(query, includeChildren)
        }

        get(Api.Steps.GenerateImageV1, { it }) { id, endpoint ->
            val step = service.readStep(id, false) ?: error("step missing: $id")
            val url = gemini.generateImage(step.label)
            val isSuccess = service.updateStep(step.copy(imgUrl = url))
            if (!isSuccess) error("unable to generate image")
            url
        }

        post(Api.Steps.GenerateImageV2) { request, endpoint ->
            val prompt = request.toAiPrompt()
            gemini.generateImage(prompt, request.stepLabel)
        }

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
    }
}

//val prompt = promptTemplate(
//            "../docs/article_reader-read_article.md",
//            "document_types" to documentTypes,
//            "news_categories" to newsCategories,
//            "news_type" to newsTypes,
//            "article_content" to articleContent,
//            "unclear_text" to PERSON_UNCLEAR
//        )


// Provide the document type that best fits, exactly as it appears in the list:
//<|document_types|>