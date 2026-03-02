package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.be_do_an_tot_nghiep.model.AiPrompt;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<AiPrompt,Long> {
    AiPrompt findPromptByCode(String code);

    Optional<AiPrompt> findByCode(String code);

}
