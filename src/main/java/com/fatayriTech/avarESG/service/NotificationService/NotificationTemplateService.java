package com.fatayriTech.avarESG.service.NotificationService;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationTemplateService {

    public String render(
            String template,
            String fallback,
            Map<String, String> variables
    ) {
        String result =
                template == null ||
                        template.isBlank()
                        ? fallback
                        : template;

        if (result == null) {
            result = "";
        }

        if (variables == null) {
            return result;
        }

        for (Map.Entry<String, String> entry :
                variables.entrySet()) {

            String key =
                    "{" + entry.getKey() + "}";

            String value =
                    entry.getValue() == null
                            ? "-"
                            : entry.getValue();

            result = result.replace(
                    key,
                    value
            );
        }

        return result;
    }
}