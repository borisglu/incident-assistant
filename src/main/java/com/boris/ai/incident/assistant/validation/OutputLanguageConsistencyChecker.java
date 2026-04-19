package com.boris.ai.incident.assistant.validation;

import org.springframework.stereotype.Component;

import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;
import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;

/**
 * Lightweight guardrail: if the incident text is Latin-only but the model output is clearly Cyrillic-heavy,
 * treat it as an unexpected language mix and trigger recovery.
 */
@Component
public class OutputLanguageConsistencyChecker {

	public void assertConsistentLanguage(String incidentDescription, IncidentAnalysisResult result) {
		if (incidentDescription == null || result == null) {
			return;
		}
		if (!looksLatinHeavy(incidentDescription)) {
			return;
		}
		String blob = (result.getCategory() + "\n" + result.getSummary()).toLowerCase();
		if (cyrillicRatio(blob) > 0.25) {
			throw new StructuredOutputValidationException(
					"Model output appears to be in an unexpected script (expected English for machine-readable fields)");
		}
	}

	private static boolean looksLatinHeavy(String text) {
		int letters = 0;
		int latin = 0;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isLetter(ch)) {
				letters++;
				if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
					latin++;
				}
			}
		}
		return letters >= 8 && (latin / (double) letters) > 0.75;
	}

	private static double cyrillicRatio(String text) {
		int letters = 0;
		int cyrillic = 0;
		for (int i = 0; i < text.length(); i++) {
			Character.UnicodeBlock block = Character.UnicodeBlock.of(text.charAt(i));
			if (Character.isLetter(text.charAt(i))) {
				letters++;
				if (block == Character.UnicodeBlock.CYRILLIC) {
					cyrillic++;
				}
			}
		}
		if (letters == 0) {
			return 0.0;
		}
		return cyrillic / (double) letters;
	}
}
