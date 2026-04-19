package com.boris.ai.incident.assistant.agent;

import org.springframework.stereotype.Service;

import com.boris.ai.incident.assistant.agent.model.EnrichedIncidentContext;
import com.boris.ai.incident.assistant.agent.model.ParsedIncidentInput;
import com.boris.ai.incident.assistant.agent.model.RecoveryHint;
import com.boris.ai.incident.assistant.agent.prompt.IncidentTriagePromptComposer;
import com.boris.ai.incident.assistant.agent.recovery.IncidentAnalysisRecoveryPlanner;
import com.boris.ai.incident.assistant.agent.stage.IncidentInputParsingStage;
import com.boris.ai.incident.assistant.agent.stage.IncidentKnowledgeAssemblyStage;
import com.boris.ai.incident.assistant.config.IncidentAssistantProperties;
import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;
import com.boris.ai.incident.assistant.json.JsonPayloadExtractor;
import com.boris.ai.incident.assistant.llm.IncidentLlmClient;
import com.boris.ai.incident.assistant.exception.IncidentAssistantException;
import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;
import com.boris.ai.incident.assistant.validation.IncidentAnalysisStructuredOutputValidator;
import com.boris.ai.incident.assistant.validation.OutputLanguageConsistencyChecker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultIncidentTriageAgent implements IncidentTriageAgent {

	private final IncidentInputParsingStage inputParsingStage;
	private final IncidentKnowledgeAssemblyStage knowledgeAssemblyStage;
	private final IncidentTriagePromptComposer promptComposer;
	private final IncidentLlmClient llmClient;
	private final IncidentAnalysisStructuredOutputValidator structuredOutputValidator;
	private final OutputLanguageConsistencyChecker languageConsistencyChecker;
	private final IncidentAnalysisRecoveryPlanner recoveryPlanner;
	private final IncidentAssistantProperties properties;

	@Override
	public IncidentAnalysisResult analyze(String rawIncidentDescription) {
		int maxAttempts = properties.getLlm().getMaxAttempts();

		ParsedIncidentInput parsed = inputParsingStage.parse(rawIncidentDescription);
		EnrichedIncidentContext enriched = knowledgeAssemblyStage.assemble(parsed);

		String systemPrompt = promptComposer.systemPrompt();
		RecoveryHint recoveryHint = RecoveryHint.empty();

		StructuredOutputValidationException lastFailure = null;
		String lastRaw = null;

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			String userPrompt = promptComposer.userPrompt(enriched, recoveryHint);
			try {
				lastRaw = llmClient.complete(systemPrompt, userPrompt);
				String json = JsonPayloadExtractor.extractJsonObject(lastRaw);
				IncidentAnalysisResult validated = structuredOutputValidator.validateAndParse(json);
				languageConsistencyChecker.assertConsistentLanguage(parsed.getNormalizedDescription(), validated);
				log.debug("Incident triage succeeded on attempt {}/{}", attempt, maxAttempts);
				return validated;
			}
			catch (StructuredOutputValidationException ex) {
				lastFailure = ex;
				log.warn(
						"Structured output validation failed (attempt {}/{}): {}",
						attempt,
						maxAttempts,
						ex.getMessage());
				recoveryHint = recoveryPlanner.plan(ex, lastRaw);
			}
			catch (Exception ex) {
				log.error("Incident analysis failed during LLM/parse cycle (attempt {}/{})", attempt, maxAttempts, ex);
				throw new IncidentAssistantException(
						"Incident analysis failed during LLM/parse cycle (attempt " + attempt + "/" + maxAttempts + ")",
						ex);
			}
		}

		log.error(
				"Incident analysis exhausted retries ({}). Last error: {}",
				maxAttempts,
				lastFailure == null ? "unknown" : lastFailure.getMessage());
		throw new IncidentAssistantException(
				"Incident analysis failed after " + maxAttempts + " attempts. Last validation error: "
						+ (lastFailure == null ? "unknown" : lastFailure.getMessage()));
	}
}
