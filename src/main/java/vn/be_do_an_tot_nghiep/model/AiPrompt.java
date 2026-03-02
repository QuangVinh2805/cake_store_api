package vn.be_do_an_tot_nghiep.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ai_prompt")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiPrompt {
    @Id
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String content;
}
