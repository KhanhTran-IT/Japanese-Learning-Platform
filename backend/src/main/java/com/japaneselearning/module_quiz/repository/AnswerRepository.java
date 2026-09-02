package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_quiz.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdOrderBySortOrderAsc(Long questionId);
}
