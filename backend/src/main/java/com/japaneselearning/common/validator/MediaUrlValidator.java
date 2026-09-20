package com.japaneselearning.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MediaUrlValidator implements ConstraintValidator<ValidMediaUrl, String> {

    @Value("${app.media.trusted-domains:}")
    private String trustedDomainsConfig;

    private List<String> trustedDomains;

    @Override
    public void initialize(ValidMediaUrl constraintAnnotation) {
        if (trustedDomainsConfig != null && !trustedDomainsConfig.isBlank()) {
            trustedDomains = Arrays.stream(trustedDomainsConfig.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .map(d -> d.startsWith(".") ? d.substring(1) : d)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank handle mandatory check if needed
        }

        try {
            URL url = new URL(value);
            
            // 1. Check Protocol (only http/https are safe)
            String protocol = url.getProtocol().toLowerCase();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                return false;
            }

            // 2. Check Trusted Domains (if configured)
            if (trustedDomains != null && !trustedDomains.isEmpty()) {
                String host = url.getHost().toLowerCase();
                boolean isTrusted = false;
                for (String trustedDomain : trustedDomains) {
                    if (host.equals(trustedDomain) || host.endsWith("." + trustedDomain)) {
                        isTrusted = true;
                        break;
                    }
                }
                if (!isTrusted) {
                    return false;
                }
            }

            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    // For unit testing purposes where Spring might not inject
    public void setTrustedDomainsConfig(String trustedDomainsConfig) {
        this.trustedDomainsConfig = trustedDomainsConfig;
    }
}
