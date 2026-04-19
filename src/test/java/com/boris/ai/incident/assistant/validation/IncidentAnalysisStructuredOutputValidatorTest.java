package com.boris.ai.incident.assistant.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;
import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;

class IncidentAnalysisStructuredOutputValidatorTest {

	private final IncidentAnalysisStructuredOutputValidator validator =
			new IncidentAnalysisStructuredOutputValidator(new ObjectMapper());

	@Test
	void acceptsValidPayload() {
		String json = """
				{
				  "category": "External payment provider issue",
				  "summary": "PayGate timeouts cause card payment failures.",
				  "severity": "high",
				  "hypotheses": [
				    {
				      "title": "Provider degradation",
				      "reasoning": "Timeouts cluster on PayGate calls only.",
				      "next_steps": [
				        "Check PayGate status page",
				        "Compare PayGate latency vs other providers"
				      ]
				    }
				  ]
				}
				""";
		IncidentAnalysisResult parsed = validator.validateAndParse(json);
		assertThat(parsed.getCategory()).contains("payment");
		assertThat(parsed.getHypotheses()).hasSize(1);
		assertThat(parsed.getHypotheses().get(0).getNextSteps()).hasSize(2);
	}

	@Test
	void rejectsTooManyHypotheses() {
		String json = """
				{
				  "category": "c",
				  "summary": "s",
				  "severity": "low",
				  "hypotheses": [
				    {"title":"t1","reasoning":"r","next_steps":["a","b"]},
				    {"title":"t2","reasoning":"r","next_steps":["a","b"]},
				    {"title":"t3","reasoning":"r","next_steps":["a","b"]},
				    {"title":"t4","reasoning":"r","next_steps":["a","b"]}
				  ]
				}
				""";
		assertThatThrownBy(() -> validator.validateAndParse(json))
				.isInstanceOf(StructuredOutputValidationException.class);
	}
}
