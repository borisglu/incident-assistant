package com.boris.ai.incident.assistant.agent.prompt;

import org.springframework.stereotype.Component;

import com.boris.ai.incident.assistant.agent.model.EnrichedIncidentContext;
import com.boris.ai.incident.assistant.agent.model.RecoveryHint;
import com.boris.ai.incident.assistant.config.IncidentAssistantProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultIncidentTriagePromptComposer implements IncidentTriagePromptComposer {

	private static final String DEFAULT_SYSTEM_PROMPT = """
			You are an on-call incident triage assistant for a payment platform.

			Your job is to classify the incident, summarize impact, assign severity, and propose a small set of testable hypotheses with concrete next diagnostic steps.

			Hard requirements:
			- Output MUST be a single JSON object and nothing else (no markdown, no code fences, no commentary).
			- All string fields MUST be written in English.
			- JSON keys MUST match exactly:
			  category (string)
			  summary (string)
			  severity (one of: low, medium, high)
			  hypotheses (array, 1 to 3 items)
			    - title (string)
			    - reasoning (string)
			    - next_steps (array of 2 to 3 strings; each item non-empty)

			Severity guidance:
			- high: broad customer impact, payment failures, auth outage, major data correctness risk, cascading timeouts.
			- medium: partial impact, degraded latency with clear blast radius, provider issues with workarounds.
			- low: limited scope, mostly internal noise, cosmetic reporting issues, no money movement impact.

			Be concise and operational. Prefer hypotheses grounded in the provided platform context and past incidents.
			""".strip();

	private final IncidentAssistantProperties properties;

	@Override
	public String systemPrompt() {
		String configured = properties.getPrompt().getSystem();
		if (configured == null || configured.isBlank()) {
			return DEFAULT_SYSTEM_PROMPT;
		}
		return configured.strip();
	}

	@Override
	public String userPrompt(EnrichedIncidentContext context, RecoveryHint recoveryHint) {
		StringBuilder sb = new StringBuilder();
		sb.append("## Platform context and historical incidents\n");
		sb.append(context.getKnowledgeText().strip()).append("\n\n");

		sb.append("## New incident description\n");
		sb.append(context.getParsedInput().getNormalizedDescription().strip()).append("\n\n");

		if (recoveryHint != null && recoveryHint.getHints() != null && !recoveryHint.getHints().isEmpty()) {
			sb.append("## Recovery instructions (previous attempt failed validation)\n");
			for (String hint : recoveryHint.getHints()) {
				sb.append("- ").append(hint.strip()).append("\n");
			}
			sb.append("\n");
		}

		sb.append("""
				Return ONLY the JSON object described in the system message.
				""".strip());
		return sb.toString();
	}
}
