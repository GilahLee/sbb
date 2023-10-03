package io.gilah.sbb.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.gilah.sbb.DataNotFoundException;
import io.gilah.sbb.question.Question;
import io.gilah.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);
        answer.setAuthor(author);
        
        this.answerRepository.save(answer);  // JPA가 제공하는 메소드
        
        return answer;
    }
    
    // 답변 수정을 위한 데이터 요청 
    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
        	System.out.println("답변 찾음");
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer);
    }
    
    public void delete(Answer answer) {
        this.answerRepository.delete(answer);
    }
    
    // 답변 추천 기능
    public void vote(Answer answer, SiteUser siteUser) {
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }
    
    
    
}