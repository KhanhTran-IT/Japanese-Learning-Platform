package com.japaneselearning.common.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.io.IOException;

/**
 * Custom Logstash JSON provider that masks sensitive data in the "message" field
 * of structured JSON logs (production profile).
 * <p>
 * Delegates to {@link LogMaskingConverter#maskSensitiveData(String)} so that
 * the same redaction rules apply to both human-readable and JSON log formats.
 * <p>
 * Registered in logback-spring.xml as a {@code <provider>} inside the LogstashEncoder.
 */
public class MaskedMessageProvider extends AbstractJsonProvider<ILoggingEvent> {

    public static final String FIELD_MESSAGE = "message";

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String message = event.getFormattedMessage();
        if (message != null) {
            message = LogMaskingConverter.maskSensitiveData(message);
        }
        generator.writeStringField(FIELD_MESSAGE, message);
    }
}
