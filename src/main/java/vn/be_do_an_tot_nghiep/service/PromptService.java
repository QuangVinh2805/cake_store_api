package vn.be_do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.be_do_an_tot_nghiep.repository.PromptRepository;

@Service
public class PromptService {
    @Autowired
    PromptRepository promptRepository;

    public String getSystemPrompt(String code) {
        return promptRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Prompt not found"))
                .getContent();
    }
}
