package vn.be_do_an_tot_nghiep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import vn.be_do_an_tot_nghiep.service.AIService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    @Autowired
    AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String reply = aiService.chat(message);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
