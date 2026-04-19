package com.boris.ai.incident.assistant.agent.stage;

import com.boris.ai.incident.assistant.agent.model.EnrichedIncidentContext;
import com.boris.ai.incident.assistant.agent.model.ParsedIncidentInput;

/**
 * Stage 2: combine parsed user input with auxiliary knowledge (topology, past incidents).
 */
public interface IncidentKnowledgeAssemblyStage {

	EnrichedIncidentContext assemble(ParsedIncidentInput parsedInput);
}
