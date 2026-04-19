package com.boris.ai.incident.assistant.agent.prompt;

import com.boris.ai.incident.assistant.agent.model.EnrichedIncidentContext;
import com.boris.ai.incident.assistant.agent.model.RecoveryHint;

public interface IncidentTriagePromptComposer {

	String systemPrompt();

	String userPrompt(EnrichedIncidentContext context, RecoveryHint recoveryHint);
}
