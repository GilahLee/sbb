package io.gilah.sbb.answer;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import io.gilah.sbb.question.Question;
import io.gilah.sbb.question.QuestionForm;
import io.gilah.sbb.question.QuestionService;
import io.gilah.sbb.user.SiteUser;
import io.gilah.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
public class AnswerController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    // Answer 등록
    // @Valid를 통해 폼으로 입력받은 데이터를 바인딩하여 오류를 전송한다.
    // Security에서 제공하는 Principal 객체를 통해 현재 로그인한 사용자의 정보를 읽어올 수 있다.
    // @PreAuthorize("isAuthenticated()"): 로그인을 하지 않은 상태에서 이 메소드를 호출하면 로그인 화면으로 이동
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createAnswer(Model model, 
    		@PathVariable("id") Integer id, 
    		@Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal) {
    	
    	// Answer를 등록하려면 부모레코드인 Question이 있는지 먼저 확인
        Question question = this.questionService.getQuestion(id);
        
        // 사용자 정보를 DB
        SiteUser siteUser = this.userService.getUser(principal.getName());
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_detail";
        }
        
        // 부모인 Question과 자식데이터인 Content를 Service 단으로 전달
        // this.answerService.create(question, answerForm.getContent(), siteUser);
        // return String.format("redirect:/question/detail/%s", id);
        
        // 앵커 기능이 동작하도록 수정
        Answer answer = this.answerService.create(question, 
                answerForm.getContent(), siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s", 
                answer.getQuestion().getId(), answer.getId());
    }
    
    // 답변 수정화면 요청
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(
    		AnswerForm answerForm, 
    		@PathVariable("id") Integer id, 
    		Principal principal) {
    	
        Answer answer = this.answerService.getAnswer(id);
        
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        answerForm.setContent(answer.getContent());
        return "answer_form";
    }
    
    // 답변 수정 처리 요청
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
            @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
        	System.out.println("답변 오류");
            return "answer_form";
        }
        Answer answer = this.answerService.getAnswer(id);
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        
        this.answerService.modify(answer, answerForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s", 
        		answer.getQuestion().getId(), answer.getId());
    }
    
    // 답변 삭제
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String answerDelete(Principal principal, @PathVariable("id") Integer id) {
        Answer answer = this.answerService.getAnswer(id);
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.answerService.delete(answer);
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }
    
    // 답변 추천기능
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String answerVote(Principal principal, @PathVariable("id") Integer id) {
        Answer answer = this.answerService.getAnswer(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.answerService.vote(answer, siteUser);
        
        
        // return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
        // 앵커 기능이 동작하도록 수정
        return String.format("redirect:/question/detail/%s#answer_%s", 
                answer.getQuestion().getId(), answer.getId());
    }
    
}