package com.example.spring_ai_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
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

    @Bean
    public ChatClient researchChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    You are a music research assistant.

                    Your task is to identify upcoming album releases or just released
                    for the artists provided by the user.

                    Do not invent information.
                    """)
                .build();
    }
}
