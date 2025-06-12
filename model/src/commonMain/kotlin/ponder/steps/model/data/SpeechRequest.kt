package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SpeechRequest(
    val text: String,
    val theme: String? = null,
    val voice: SpeechVoice? = null
)

enum class SpeechVoice(
    val apiName: String
) {
    Bright("Zephyr"),
    Upbeat("Puck"),
    Informative("Charon"),
    Firm("Kore"),
    Excitable("Fenrir"),
    Youthful("Leda"),
    Breezy("Aoede"),
    EasyGoing("Callirrhoe"),
    Smooth("Algieba"),
    Breathy("Enceladus"),
    Clear("Iapetus"),
    Gravelly("Algenib"),
    Informative2("Rasalgethi"),
    Upbeat2("Laomedeia"),
    Soft("Achernar"),
    Firm2("Alnilam"),
    Even("Schedar"),
    Mature("Gacrux"),
    Forward("Pulcherrima"),
    Friendly("Achird"),
    Casual("Zubenelgenubi"),
    Gentle("Vindemiatrix"),
    Lively("Sadachbia"),
    Knowledgeable("Sadaltager"),
    Warm("Sulafat")
}