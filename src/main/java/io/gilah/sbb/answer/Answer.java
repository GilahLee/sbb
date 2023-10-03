package io.gilah.sbb.answer;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.gilah.sbb.question.Question;
import io.gilah.sbb.user.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    private LocalDateTime createDate;

    @JsonIgnore // JSON 순환반복이 일어났을때 제어를 위해 https://1sangcoder.tistory.com/160
    @ManyToOne 							// n:1 관계 (Answer(다) : Question(일))
    private Question question; 			// Answer(자식)가 Many, Question(부모)이 One
    
    @ManyToOne
    private SiteUser author;   			// 한 명(SiteUser)의 유저가 여러 대답을 할 수 있으므로
    
    private LocalDateTime modifyDate;  // 답변 수정 일시
    
    @ManyToMany
    Set<SiteUser> voter;				// 추천 기능 - ManyToMany는 테이블이 생성된다.
}