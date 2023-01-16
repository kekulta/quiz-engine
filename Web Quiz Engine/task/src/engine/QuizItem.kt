package engine

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.random.Random

@Entity
@Table(name = "quizzes")
data class QuizItem(
    @NotBlank
    var title: String = "undef",
    @NotBlank
    var text: String = "undef",
    @Size(min = 2)
    @ElementCollection
    var options: MutableList<String> = mutableListOf(),
    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    var answer: MutableList<Int>? = mutableListOf(),
    @JsonIgnore
    var author: String = "default",
    @Id
    @GeneratedValue
    var id: Long = -1L,
)


data class QuizItemRequest(
    @NotBlank
    val title: String,
    @NotBlank
    val text: String,
    @Size(min = 2)
    val options: List<String>,
    val answer: List<Int>?
) {
    fun build(author: String) =
        QuizItem(
            title = title,
            text = text,
            options = options.toMutableList(),
            answer = answer?.toMutableList() ?: mutableListOf(),
            author = author
        )
}

data class Answer(@JsonProperty("answer") val answer: MutableList<Int>)