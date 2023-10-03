package io.gilah.sbb.question;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import io.gilah.sbb.answer.Answer;
import io.gilah.sbb.user.SiteUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answerList;

	public void addAnswer(Answer answer){
		answerList.add(answer);
		answer.setQuestion(this);
	}
	
	@ManyToOne
	private SiteUser author;  			// 한명의 유저(One)가 여러 개의 질문(Many)을 할 수 있다.
	
	private LocalDateTime modifyDate;  // 질문 수정 일시
	
	@ManyToMany
	Set<SiteUser> voter;				// 추천기능 (추천인은 중복되면 안됨) - ManyToMany는 테이블이 생성된다.
	
}