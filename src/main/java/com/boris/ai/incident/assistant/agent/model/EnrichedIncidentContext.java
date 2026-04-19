package com.boris.ai.incident.assistant.agent.model;

import lombok.Value;

@Value
public class EnrichedIncidentContext {

	ParsedIncidentInput parsedInput;
	String knowledgeText;
}
