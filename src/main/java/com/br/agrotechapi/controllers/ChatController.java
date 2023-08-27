package com.br.agrotechapi.controllers;

import com.br.agrotechapi.models.Response;
import com.br.agrotechapi.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private static final String API_KEY = "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";
    private static final String INTRO_TEXT = "Você é um especialista em agricultura. Responda às seguintes perguntas sobre o assunto:";

    private final RestTemplate restTemplate;
    private final ResponseService responseService;

    @Autowired
    public ChatController(RestTemplate restTemplate, ResponseService responseService) {
        this.restTemplate = restTemplate;
        this.responseService = responseService;
    }

    @PostMapping("/ask")
    public String askQuestion(@RequestBody Map<String, String> requestData) {
        String pergunta = requestData.get("quest");
        String quest = INTRO_TEXT + " " + pergunta;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", quest);
        messages.add(message);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                String.class
        );

        String responseBody = response.getBody();

        // Salvar a pergunta e a resposta no banco de dados
        Response savedResponse = responseService.saveResponse(new Response(null, pergunta, responseBody));

        return responseBody;
    }

    @GetMapping("/responses/{id}")
    public ResponseEntity<String> getResponseById(@PathVariable Long id) {
        Response response = responseService.getResponseById(id);

        if (response != null) {
            return ResponseEntity.ok(response.getAnswer());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
