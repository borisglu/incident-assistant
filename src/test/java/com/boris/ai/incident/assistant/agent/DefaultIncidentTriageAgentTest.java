package com.boris.ai.incident.assistant.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boris.ai.incident.assistant.agent.prompt.DefaultIncidentTriagePromptComposer;
import com.boris.ai.incident.assistant.agent.recovery.DefaultIncidentAnalysisRecoveryPlanner;
import com.boris.ai.incident.assistant.agent.stage.DefaultIncidentInputParsingStage;
import com.boris.ai.incident.assistant.agent.stage.DefaultIncidentKnowledgeAssemblyStage;
import com.boris.ai.incident.assistant.config.IncidentAssistantProperties;
import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;
import com.boris.ai.incident.assistant.knowledge.IncidentKnowledgeProvider;
import com.boris.ai.incident.assistant.llm.IncidentLlmClient;
import com.boris.ai.incident.assistant.validation.IncidentAnalysisStructuredOutputValidator;
import com.boris.ai.incident.assistant.validation.OutputLanguageConsistencyChecker;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DefaultIncidentTriageAgentTest {

	@Mock
	private IncidentLlmClient llmClient;

	@Mock
	private IncidentKnowledgeProvider knowledgeProvider;

	@Test
	void retriesAfterInvalidJsonThenSucceeds() {
		when(knowledgeProvider.knowledgeText()).thenReturn("knowledge");
		when(llmClient.complete(anyString(), anyString()))
				.thenReturn("{}")
				.thenReturn(validJson());

		IncidentAssistantProperties properties = new IncidentAssistantProperties();
		properties.setMaxInputLength(8000);
		IncidentAssistantProperties.Llm llm = new IncidentAssistantProperties.Llm();
		llm.setMaxAttempts(3);
		properties.setLlm(llm);

		DefaultIncidentTriageAgent agent = new DefaultIncidentTriageAgent(
				new DefaultIncidentInputParsingStage(properties),
				new DefaultIncidentKnowledgeAssemblyStage(knowledgeProvider),
				new DefaultIncidentTriagePromptComposer(properties),
				llmClient,
				new IncidentAnalysisStructuredOutputValidator(new ObjectMapper()),
				new OutputLanguageConsistencyChecker(),
				new DefaultIncidentAnalysisRecoveryPlanner(),
				properties);

		IncidentAnalysisResult result = agent.analyze("Customers cannot pay by card; PayGate timeouts in payment-service logs.");
		assertThat(result.getSeverity().name()).isEqualTo("HIGH");
		assertThat(result.getHypotheses()).hasSize(1);

		verify(llmClient, times(2)).complete(anyString(), anyString());
	}

	private static String validJson() {
		return """
				{
				  "category": "External payment provider issue",
				  "summary": "Card payments fail due to PayGate timeouts; likely broad customer impact.",
				  "severity": "high",
				  "hypotheses": [
				    {
				      "title": "PayGate provider degradation",
				      "reasoning": "Logs show timeouts specifically on PayGate calls while other services look stable.",
				      "next_steps": [
				        "Check PayGate status page and incident notifications",
				        "Compare PayGate error rate vs other acquirers in payment-service metrics"
				      ]
				    }
				  ]
				}
				""";
	}
}
