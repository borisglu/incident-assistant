package com.boris.ai.incident.assistant.agent.recovery;

import com.boris.ai.incident.assistant.agent.model.RecoveryHint;
import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;

public interface IncidentAnalysisRecoveryPlanner {

	RecoveryHint plan(StructuredOutputValidationException failure, String rawModelOutput);
}
