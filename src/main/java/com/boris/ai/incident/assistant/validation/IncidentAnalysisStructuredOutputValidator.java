package com.boris.ai.incident.assistant.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.boris.ai.incident.assistant.domain.Hypothesis;
import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;
import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IncidentAnalysisStructuredOutputValidator {

	private final ObjectMapper objectMapper;

	public IncidentAnalysisResult validateAndParse(String jsonPayload) {
		IncidentAnalysisResult parsed;
		try {
			parsed = objectMapper.readValue(jsonPayload, IncidentAnalysisResult.class);
		}
		catch (Exception ex) {
			throw new StructuredOutputValidationException("Invalid JSON for incident analysis", ex);
		}

		if (parsed.getCategory() == null || parsed.getCategory().isBlank()) {
			throw new StructuredOutputValidationException("Field 'category' must be a non-blank string");
		}
		if (parsed.getSummary() == null || parsed.getSummary().isBlank()) {
			throw new StructuredOutputValidationException("Field 'summary' must be a non-blank string");
		}
		if (parsed.getSeverity() == null) {
			throw new StructuredOutputValidationException("Field 'severity' is required");
		}
		if (parsed.getHypotheses() == null || parsed.getHypotheses().isEmpty()) {
			throw new StructuredOutputValidationException("Field 'hypotheses' must contain at least 1 item");
		}
		if (parsed.getHypotheses().size() > 3) {
			throw new StructuredOutputValidationException("Field 'hypotheses' must contain at most 3 items");
		}

		for (int i = 0; i < parsed.getHypotheses().size(); i++) {
			validateHypothesis(i, parsed.getHypotheses().get(i));
		}
		return parsed;
	}

	private static void validateHypothesis(int index, Hypothesis hypothesis) {
		if (hypothesis == null) {
			throw new StructuredOutputValidationException("hypotheses[" + index + "] is null");
		}
		if (hypothesis.getTitle() == null || hypothesis.getTitle().isBlank()) {
			throw new StructuredOutputValidationException("hypotheses[" + index + "].title must be non-blank");
		}
		if (hypothesis.getReasoning() == null || hypothesis.getReasoning().isBlank()) {
			throw new StructuredOutputValidationException("hypotheses[" + index + "].reasoning must be non-blank");
		}
		List<String> steps = hypothesis.getNextSteps();
		if (steps == null || steps.size() < 2 || steps.size() > 3) {
			throw new StructuredOutputValidationException(
					"hypotheses[" + index + "].next_steps must contain 2 to 3 non-empty strings");
		}
		for (int s = 0; s < steps.size(); s++) {
			if (steps.get(s) == null || steps.get(s).isBlank()) {
				throw new StructuredOutputValidationException(
						"hypotheses[" + index + "].next_steps[" + s + "] must be non-blank");
			}
		}
	}
}
