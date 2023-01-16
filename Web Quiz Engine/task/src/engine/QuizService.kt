package engine

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class QuizService(@Autowired var repository: QuizRepository) {
    fun getAllQuizzes(): List<QuizItem> = repository.findAll().toList()
    fun getQuizById(id: Long): QuizItem? = repository.findQuizById(id)
    fun saveQuiz(quizItem: QuizItem): QuizItem = repository.save(quizItem)
    fun deleteQuizById(id: Long) {
        repository.deleteById(id)
    }
}


@Repository
interface QuizRepository : CrudRepository<QuizItem, Long> {
    fun findQuizById(id: Long): QuizItem?
}