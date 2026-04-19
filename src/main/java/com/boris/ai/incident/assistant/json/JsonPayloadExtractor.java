package com.boris.ai.incident.assistant.json;

import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;

/**
 * Extracts a JSON object payload from model output that may include markdown fences or prose.
 */
public final class JsonPayloadExtractor {

	private JsonPayloadExtractor() {
	}

	public static String extractJsonObject(String modelOutput) {
		if (modelOutput == null || modelOutput.isBlank()) {
			throw new StructuredOutputValidationException("Model output is empty");
		}
		String text = modelOutput.strip();
		String fenced = tryExtractFencedJson(text);
		if (fenced != null) {
			return fenced;
		}
		int start = text.indexOf('{');
		int end = text.lastIndexOf('}');
		if (start < 0 || end <= start) {
			throw new StructuredOutputValidationException("No JSON object found in model output");
		}
		return text.substring(start, end + 1).strip();
	}

	private static String tryExtractFencedJson(String text) {
		String lower = text.toLowerCase();
		int idx = lower.indexOf("```json");
		if (idx < 0) {
			idx = lower.indexOf("```");
			if (idx < 0) {
				return null;
			}
		}

		int afterOpeningFence = idx + 3;
		if (lower.regionMatches(idx, "```json", 0, "```json".length())) {
			afterOpeningFence = idx + "```json".length();
		}

		int startFence = text.indexOf('\n', afterOpeningFence);
		int contentStart = startFence >= 0 ? startFence + 1 : afterOpeningFence;
		int endFence = text.indexOf("```", contentStart);
		if (endFence < 0) {
			return null;
		}
		return text.substring(contentStart, endFence).strip();
	}
}
