package com.boris.ai.incident.assistant.agent.stage;

import org.springframework.stereotype.Component;

import com.boris.ai.incident.assistant.agent.model.EnrichedIncidentContext;
import com.boris.ai.incident.assistant.agent.model.ParsedIncidentInput;
import com.boris.ai.incident.assistant.knowledge.IncidentKnowledgeProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultIncidentKnowledgeAssemblyStage implements IncidentKnowledgeAssemblyStage {

	private final IncidentKnowledgeProvider knowledgeProvider;

	@Override
	public EnrichedIncidentContext assemble(ParsedIncidentInput parsedInput) {
		return new EnrichedIncidentContext(parsedInput, knowledgeProvider.knowledgeText());
	}
}
