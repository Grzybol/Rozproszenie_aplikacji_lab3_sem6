package org.bestservers;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class KafkaAlertProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaAlertProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAlert(String url, String badWord) {
        String message = String.format(
                "{\"timestamp\":\"%s\",\"url\":\"%s\",\"matched\":\"%s\"}",
                Instant.now(), url, badWord
        );
        kafkaTemplate.send("url-blacklist-alerts", message);
        System.out.println("🚨 Wysłano alert na Kafkę: " + message);
    }
}
