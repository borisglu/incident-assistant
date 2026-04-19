package com.boris.ai.incident.assistant.api;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boris.ai.incident.assistant.IncidentAssistantApplication;

@SpringBootTest(classes = IncidentAssistantApplication.class)
@AutoConfigureMockMvc
class IncidentAnalysisControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChatModel chatModel;

	@Test
	void analyzeEndpointReturnsStructuredJson() throws Exception {
		Mockito.when(chatModel.call(any(Prompt.class)))
				.thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(validJson())))));

		mockMvc.perform(post("/api/v1/incidents/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"description\":\"PayGate timeouts in payment-service logs.\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.category").isString())
				.andExpect(jsonPath("$.summary").isString())
				.andExpect(jsonPath("$.severity").value("high"))
				.andExpect(jsonPath("$.hypotheses[0].title").isString())
				.andExpect(jsonPath("$.hypotheses[0].next_steps[0]").isString())
				.andExpect(jsonPath("$.hypotheses[0].next_steps[1]").isString());
	}

	private static String validJson() {
		return """
				{
				  "category": "External payment provider issue",
				  "summary": "Timeouts calling PayGate cause payment failures.",
				  "severity": "high",
				  "hypotheses": [
				    {
				      "title": "Provider incident",
				      "reasoning": "Timeouts cluster on PayGate integration.",
				      "next_steps": [
				        "Check PayGate status page",
				        "Review payment-service PayGate latency metrics"
				      ]
				    }
				  ]
				}
				""";
	}
}
