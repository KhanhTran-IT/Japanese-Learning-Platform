package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderBySortOrderAsc(Long quizId);
    
    int countByQuizId(Long quizId);

    @Query("SELECT q.quiz.id, COUNT(q) FROM Question q WHERE q.quiz.id IN :quizIds GROUP BY q.quiz.id")
    List<Object[]> countQuestionsByQuizIds(@Param("quizIds") Collection<Long> quizIds);
}
