package io.gilah.sbb.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import io.gilah.sbb.DataNotFoundException;
import io.gilah.sbb.answer.Answer;
import io.gilah.sbb.user.SiteUser;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {
	private final QuestionRepository questionRepository;

	// 1) JPA를 이용해 전체 데이터를 조회
	//    Pageable 객체를 통해 페이징이 가능. kw:검색 키워드
	public Page<Question> getList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Specification<Question> spec = search(kw);
        return this.questionRepository.findAll(spec, pageable);
	}

	// 2) Question detail을 조회
	public Question getQuestion(Integer id) {
		// 1개의 question과 + Question의 Answer를 전부 질의. 즉 2번의 select 실행됨
		Optional<Question> question = this.questionRepository.findById(id); 

		// id에 해당하는 데이터가 존재하면 반환, 아니면 익셉션 처리
		if(question.isPresent()) {
			return question.get();
		} else {
			throw new DataNotFoundException("question not found");
		}
	}

	// 3) 질문 저장
	public void create(String subject, String content, SiteUser user) {
		Question q = new Question();
		q.setSubject(subject);
		q.setContent(content);
		q.setCreateDate(LocalDateTime.now());
		q.setAuthor(user);

		this.questionRepository.save(q);
	}

	// 4) 질문 수정
	public void modify(Question question, String subject, String content) {
		question.setSubject(subject);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		this.questionRepository.save(question);
	}

	// 5) 질문 삭제
	public void delete(Question question) {
		// TODO Auto-generated method stub

	}

	// 6) 추천 저장
	public void vote(Question question, SiteUser siteUser) {
		question.getVoter().add(siteUser);
		this.questionRepository.save(question);
	}

	// 7) 검색 기능 (kw : 검색어)
	private Specification<Question> search(String kw) {
		return new Specification<>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true);  // 중복을 제거 
				Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
				Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
				Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
				return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목 
						cb.like(q.get("content"), "%" + kw + "%"),      // 내용 
						cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자 
						cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용 
						cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자 
			}
		};
	}
	

}
