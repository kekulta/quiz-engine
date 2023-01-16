package engine

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.context.request.WebRequest
import java.util.*
import javax.validation.Valid
import kotlin.random.Random

const val Quiz =
    "{\"title\":\"The Java Logo\",\"text\":\"What is depicted on the Java logo?\",\"options\":[\"Robot\",\"Tea leaf\",\"Cup of coffee\",\"Bug\"]}"
const val success = "{\"success\":true,\"feedback\":\"Congratulations, you're right!\"}"
const val failure = "{\"success\":false,\"feedback\":\"Wrong answer! Please, try again.\"}"

@SpringBootApplication
open class WebQuizEngine {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<WebQuizEngine>(*args)
        }
    }
}

@RestController
class Controller(@Autowired val quizService: QuizService) {

    //private val quizzes: MutableMap<Long, QuizItem> = Collections.synchronizedMap(mutableMapOf())

    @GetMapping("/api/quizzes")
    fun getAllQuizzes(): ResponseEntity<List<QuizItem>> {
        return ResponseEntity.ok(quizService.getAllQuizzes())
    }

    @PostMapping("/api/quizzes")
    fun createQuiz(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody @Valid quizCreateRequest: QuizItemRequest
    ): ResponseEntity<QuizItem> {
        println(quizCreateRequest)
        return ResponseEntity.ok(
            quizService.saveQuiz(quizCreateRequest.build(user.username))
        )
    }

    @GetMapping("/api/quizzes/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<QuizItem> {
        //println("$id ${quizzes[id]}")
        quizService.getQuizById(id)?.let {
            return ResponseEntity.ok(it)
        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/api/quizzes/{id}/solve")
    fun solveQuiz(@RequestBody answer: Answer, @PathVariable id: Long): ResponseEntity<String> {
        quizService.getQuizById(id)?.let {
            println(
                "${it.answer} ${answer.answer} ${it.answer?.containsAll(answer.answer)} ${
                    answer.answer.containsAll(
                        it.answer ?: emptyList()
                    )
                } ${answer.answer == it.answer} ${it.answer == answer.answer}"
            )
            return if (answer.answer == it.answer) {
                ResponseEntity.ok(success)
            } else {
                ResponseEntity.ok(failure)
            }
        }
        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/api/quizzes/{id}")
    fun deleteQuiz(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<String> {
        quizService.getQuizById(id)?.let {
            if (it.author == user.username) {
                quizService.deleteQuizById(id)
                return ResponseEntity.noContent().build()
            } else {
                return ResponseEntity(HttpStatus.FORBIDDEN)
            }
        }
        return ResponseEntity.notFound().build()
    }


}

