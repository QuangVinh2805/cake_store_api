package vn.be_do_an_tot_nghiep.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private static final String API_KEY = "AIzaSyDY2TFDodWTWRFFd8lnW5lQuhJQOv4fgL4";
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=" + API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();
    private final PromptService promptService;
    private final ProductService productService;

    public AIService(PromptService promptService, ProductService productService) {
        this.promptService = promptService;
        this.productService = productService;
    }

    public String chat(String userMessage) {
        String promptTemplate = promptService.getSystemPrompt("CAKESHOP_SYSTEM");
        String productData = productService.buildProductText();

        String systemPrompt = promptTemplate.replace("{{DATA}}", productData);
        String finalPrompt = systemPrompt + "\n\nKhách: " + userMessage;

        try {
            return callGemini("gemini-2.5-flash", finalPrompt);
        } catch (HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                return callGemini("gemini-2.0-flash", finalPrompt);
            }
            throw e;
        }
    }

    private String callGemini(String model, String prompt) {
        String url = String.format(BASE_URL, model);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, body, Map.class);

        Map res = response.getBody();
        List candidates = (List) res.get("candidates");
        Map content = (Map) ((Map) candidates.get(0)).get("content");
        List parts = (List) content.get("parts");

        return (String) ((Map) parts.get(0)).get("text");
    }
}
