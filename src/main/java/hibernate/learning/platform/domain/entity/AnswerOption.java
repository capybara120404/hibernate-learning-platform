package hibernate.learning.platform.domain.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import hibernate.learning.platform.domain.DomainPreconditions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer_options")
@Access(AccessType.FIELD)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerOption extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    private AnswerOption(Question question, String text, boolean correct) {
        this.question = DomainPreconditions.requireNonNull(question, "Answer option question must not be null");
        this.text = DomainPreconditions.requireNotBlank(text, "Answer option text must not be blank");
        this.correct = correct;
    }

    static AnswerOption create(Question question, String text, boolean correct) {
        return new AnswerOption(question, text, correct);
    }

    public void updateText(String text) {
        this.text = DomainPreconditions.requireNotBlank(text, "Answer option text must not be blank");
    }

    public void markCorrect(boolean correct) {
        this.correct = correct;
    }
}
