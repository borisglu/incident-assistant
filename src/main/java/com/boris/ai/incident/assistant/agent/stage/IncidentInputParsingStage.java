package com.boris.ai.incident.assistant.agent.stage;

import com.boris.ai.incident.assistant.agent.model.ParsedIncidentInput;

/**
 * Stage 1: validate and normalize raw user text before any LLM work.
 */
public interface IncidentInputParsingStage {

	ParsedIncidentInput parse(String rawDescription);
}
