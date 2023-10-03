package io.gilah.sbb.question;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import io.gilah.sbb.answer.AnswerForm;
import io.gilah.sbb.user.SiteUser;
import io.gilah.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping(value="/question")
public class QuestionController {
	
	// 생성자 주입방식
	private final QuestionService questionService;
	private final UserService userService;

	@GetMapping({"/", ""})
	public String root() {
		return "redirect:/question/list";
	}

	// 검색 키워드 추가 (kw: 검색키워드)
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue="0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {
		Page<Question> paging = this.questionService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "question_list";
	}

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Integer id, Model model, AnswerForm answerForm) {
		Question question = this.questionService.getQuestion(id);
		model.addAttribute("question", question);

		return "question_detail";
	}
	
	// 질문 등록 화면 요청
	// principal 객체를 사용하는 메서드에 사용. 로그인이 필요한 메서드라는 것을 의미
	// 이 애노테이션이 적용된 메소드를 로그아웃 상태에서 호출하면 로그인 페이지로 이동
	@PreAuthorize("isAuthenticated()") 
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }
    
	// 질문 등록 처리 요청
    // BindingResult 객체는 @Valid 애노테이션으로 인해 검증이 수행된 결과를 의미하는 객체
    // 매개변수 순서(@Valid 다음 BindingResult) 가 매우 중요하다. 
    // Principal : 로그인을 하면 생성되는 객체. 로그인을 안하면 null

	@PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(
    		@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {

    	if (bindingResult.hasErrors()) {
    		return "question_form";
    	}
    	
    	SiteUser siteUser = this.userService.getUser(principal.getName());
    	this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);
        return "redirect:/question/list"; // 질문 저장후 질문목록으로 이동
    }
	
	// 질문 수정화면 요청
	// 인증이 되었는지 여부를 @PreAuthorize로 먼저 확인
	// @PreAuthorize("hasRole('ROLE_ADMIN')") : ROLE_ADMIN인지 확인
    @PreAuthorize("isAuthenticated()") 	 
    @GetMapping("/modify/{id}")
    public String questionModify(
    		QuestionForm questionForm, 
    		@PathVariable("id") Integer id, 
    		Principal principal) {
    	
        Question question = this.questionService.getQuestion(id);
        
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        
        return "question_form";
    }

    
    // 4) 질문 수정 처리 요청
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, 
            Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        
        return String.format("redirect:/question/detail/%s#answer_%", id);
    }
    
    // 질문 삭제 요청
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
    
    // 추천
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
	
}