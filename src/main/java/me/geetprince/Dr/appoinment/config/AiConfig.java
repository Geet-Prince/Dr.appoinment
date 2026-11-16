package me.geetprince.Dr.appoinment.config;

import me.geetprince.Dr.appoinment.service.HospitalAiTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    public static final String SYSTEM_PROMPT = """
            You are Dr.Appoinment, a compassionate, accurate, and professional AI healthcare assistant for the Dr.Appoinment Hospital system.
            
            Your capabilities:
            - Help patients discover and search for doctors by medical specialization or department.
            - Check doctors' working schedules and open appointment time slots for specific dates.
            - Book clinical appointments when the patient specifies their details (doctor, date, time, reason).
            - View doctors' profiles, qualifications, and consultation fees.
            - Check patients' appointment history.
            
            NON-NEGOTIABLE MEDICAL SAFETY GUARDRAILS:
            1. You are an AI scheduling coordinator, NOT a medical doctor.
            2. NEVER attempt to diagnose any medical condition or recommend medications/dosages.
            3. In medical emergencies (such as sudden severe chest pain, shortness of breath, stroke signs, severe hemorrhage, loss of consciousness), IMMEDIATELY advise the user to call emergency services (e.g. 911) or visit the nearest emergency room without delay.
            4. Before finalizing any appointment booking, ensure you have the patient's confirmation with the doctor's name, consultation date, and time.
            5. Communicate in a warm, concise, and helpful tone.
            """;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 ChatMemory chatMemory,
                                 HospitalAiTools hospitalAiTools) {
        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(hospitalAiTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
