package io.gilah.sbb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.gilah.sbb.answer.Answer;
import io.gilah.sbb.answer.AnswerRepository;
import io.gilah.sbb.question.Question;
import io.gilah.sbb.question.QuestionRepository;
import io.gilah.sbb.question.QuestionService;
import jakarta.transaction.Transactional;


@SpringBootTest
class SbbApplicationTests {

    @Autowired
    private QuestionRepository questionRepository;  // 질문관련
    
    @Autowired
    private AnswerRepository answerRepository;		// 답변관련
    
    @Autowired
    private QuestionService questionService;

    @Test
    void testJpa1() {        
        Question q1 = new Question();
        q1.setSubject("sbb가 무엇인가요?");
        q1.setContent("sbb에 대해서 알고 싶습니다.");
        q1.setCreateDate(LocalDateTime.now());
        this.questionRepository.save(q1);  // 첫번째 질문 저장

        Question q2 = new Question();
        q2.setSubject("스프링부트 모델 질문입니다.");
        q2.setContent("id는 자동으로 생성되나요?");
        q2.setCreateDate(LocalDateTime.now());
        this.questionRepository.save(q2);  // 두번째 질문 저장
    }
    
    @Test
    void testJpa2() {
        List<Question> all = this.questionRepository.findAll();
        assertEquals(2, all.size());

        Question q = all.get(0);
        assertEquals("sbb가 무엇인가요?", q.getSubject());
    }
    
    @Test
    void testJpa3() {
        Optional<Question> oq = this.questionRepository.findById(1);
        if(oq.isPresent()) {
            Question q = oq.get();
            assertEquals("sbb가 무엇인가요?", q.getSubject());
        }
    }
    
    @Test
    void testJpa4() {
        Question q = this.questionRepository.findBySubject("sbb가 무엇인가요?");
        assertEquals(1, q.getId());
    }
    
    @Test
    void testJpa5() {
        Question q = this.questionRepository.findBySubjectAndContent(
                "sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
        assertEquals(1, q.getId());
    }
    
    @Test
    void testJpa6() {
        List<Question> qList = this.questionRepository.findBySubjectLike("sbb%");
        System.out.println(qList);
        Question q = qList.get(0);
        assertEquals("sbb가 무엇인가요?", q.getSubject());
    }
    
    // 수정
    @Test
    void testJpa7() {
        Optional<Question> oq = this.questionRepository.findById(1);
        assertTrue(oq.isPresent());
        Question q = oq.get();
        q.setSubject("수정된 제목");
        this.questionRepository.save(q);
    }
    
    // 삭제
    @Test
    void testJpa8() {
        assertEquals(2, this.questionRepository.count()); // 전체 데이터 건수 반환
        Optional<Question> oq = this.questionRepository.findById(1);
        assertTrue(oq.isPresent());
        Question q = oq.get();
        this.questionRepository.delete(q);
        assertEquals(1, this.questionRepository.count());
    }
    
    //---------- 답변관련
    // 답변 생성
    @Test
    void testJpa11() {
        Optional<Question> oq = this.questionRepository.findById(2);
        assertTrue(oq.isPresent());
        Question q = oq.get();
        System.out.println("ddd===========>" + q.getClass());
        Answer a = new Answer();
        a.setContent("네 자동으로 생성됩니다.");
        
        a.setQuestion(q);  // 어떤 질문의 답변인지 알기위해서 Question 객체가 필요하다.
        a.setCreateDate(LocalDateTime.now());
        this.answerRepository.save(a);
    }
    
    // 답변조회 (1번 답변 조회)
    @Test
    void testJpa12() {
        Optional<Answer> oa = this.answerRepository.findById(1);
        assertTrue(oa.isPresent());
        Answer a = oa.get();
        assertEquals(2, a.getQuestion().getId());
    }
    
    // 질문 2에 대한 답변 조회
    @Test
    @Transactional
    void testJpa13() {
        Optional<Question> oq = this.questionRepository.findById(2);
        assertTrue(oq.isPresent());
        Question q = oq.get();
        
        // 위에서 조회 후에 세션이 종료되어 아래의 조회는 에러발생
        // 테스트코드에서 세션을 종료하지 않으려면 상단에 @Transactional을 선언하면 메소드 종료될 때까지 세션이 유지됨
        // 실행시에는 상관없으며 test 시에만 @Transactional이 필요
        List<Answer> answerList = q.getAnswerList();

        assertEquals(2, answerList.size());
        assertEquals("네 자동으로 생성됩니다.", answerList.get(0).getContent());
    } 
    
    // 페이징을 위해 여러개의 데이터 삽입하기
    @Test
    void testJpa14() {
        for (int i = 1; i <= 300; i++) {
            String subject = String.format("테스트 데이터입니다:[%03d]", i);
            String content = "내용무";
            this.questionService.create(subject, content, null); // 오류나지 않도록
        }
    }
    
}