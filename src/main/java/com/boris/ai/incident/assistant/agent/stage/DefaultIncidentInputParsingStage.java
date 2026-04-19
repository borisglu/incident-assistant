package com.boris.ai.incident.assistant.agent.stage;

import org.springframework.stereotype.Component;

import com.boris.ai.incident.assistant.agent.model.ParsedIncidentInput;
import com.boris.ai.incident.assistant.config.IncidentAssistantProperties;
import com.boris.ai.incident.assistant.exception.IncidentAssistantException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultIncidentInputParsingStage implements IncidentInputParsingStage {

	private final IncidentAssistantProperties properties;

	@Override
	public ParsedIncidentInput parse(String rawDescription) {
		if (rawDescription == null || rawDescription.isBlank()) {
			log.warn("Rejected incident input: blank description");
			throw new IncidentAssistantException("Incident description must not be blank");
		}
		String normalized = rawDescription.strip();
		int maxInputLength = properties.getMaxInputLength();
		if (normalized.length() > maxInputLength) {
			log.warn("Rejected incident input: length {} exceeds max {}", normalized.length(), maxInputLength);
			throw new IncidentAssistantException(
					"Incident description is too long (max " + maxInputLength + " characters)");
		}
		return new ParsedIncidentInput(normalized);
	}
}
