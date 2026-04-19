package com.boris.ai.incident.assistant.agent;

import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;

/**
 * Orchestrates staged incident triage: parse → enrich → generate structured output → validate, with recovery retries.
 */
public interface IncidentTriageAgent {

	IncidentAnalysisResult analyze(String rawIncidentDescription);
}
