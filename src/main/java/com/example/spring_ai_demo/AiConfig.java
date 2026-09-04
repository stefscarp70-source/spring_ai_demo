package com.example.spring_ai_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig {
    @Bean
    ChatClient chaClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        Sei un assistente tecnico Java.
                        Rispondi sempre in italiano.
                        Sii conciso e non superare 5 frasi.
                        
                        Quando utilizzi un tool per recuperare informazioni,
                        basa la risposta esclusivamente sui dati restituiti dal tool.
                        Non aggiungere informazioni provenienti dalla tua conoscenza
                        generale che non siano presenti nei risultati del tool.
                        """)
                .build();
    }
}
