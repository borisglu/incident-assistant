package com.boris.ai.incident.assistant.agent.recovery;

import org.springframework.stereotype.Component;

import com.boris.ai.incident.assistant.agent.model.RecoveryHint;
import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;

@Component
public class DefaultIncidentAnalysisRecoveryPlanner implements IncidentAnalysisRecoveryPlanner {

	private static final int MAX_SNIPPET = 1200;

	@Override
	public RecoveryHint plan(StructuredOutputValidationException failure, String rawModelOutput) {
		String snippet = shorten(rawModelOutput == null ? "" : rawModelOutput, MAX_SNIPPET);
		return RecoveryHint.of(
				"Your previous answer failed validation: " + failure.getMessage(),
				"Regenerate the answer as a single raw JSON object (no markdown fences, no prose).",
				"Ensure hypotheses has 1-3 items; each next_steps has 2-3 non-empty strings; severity is low|medium|high.",
				"If you previously responded in the wrong language, rewrite ALL string fields in English.",
				"Here is the invalid output to learn from (may be truncated): " + snippet);
	}
	private static String shorten(String value, int max) {
		String v = value.strip();
		if (v.length() <= max) {
			return v;
		}
		return v.substring(0, max) + "…";
	}
}
