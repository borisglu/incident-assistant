package com.boris.ai.incident.assistant.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.boris.ai.incident.assistant.exception.StructuredOutputValidationException;

class JsonPayloadExtractorTest {

	@Test
	void extractsRawJsonObject() {
		String raw = "Here you go:\n{\"a\":1}\nThanks";
		assertThat(JsonPayloadExtractor.extractJsonObject(raw)).isEqualTo("{\"a\":1}");
	}

	@Test
	void extractsFencedJson() {
		String raw = """
				Some text
				```json
				{"a": 2}
				```
				""";
		assertThat(JsonPayloadExtractor.extractJsonObject(raw)).isEqualTo("{\"a\": 2}");
	}

	@Test
	void extractsFencedJsonWithoutLeadingNewline() {
		String raw = "```json{\"a\":3}```";
		assertThat(JsonPayloadExtractor.extractJsonObject(raw)).isEqualTo("{\"a\":3}");
	}

	@Test
	void failsWhenNoJson() {
		assertThatThrownBy(() -> JsonPayloadExtractor.extractJsonObject("no json here"))
				.isInstanceOf(StructuredOutputValidationException.class);
	}
}
