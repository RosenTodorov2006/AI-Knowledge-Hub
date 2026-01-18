package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
@EnableAsync
public class AiKnowledgeHub {
    public static void main(String[] args) {
        // Превключваме на PyTorch
        System.setProperty("ai.djl.default_engine", "PyTorch");

        try {
            ai.djl.engine.Engine.getEngine("PyTorch");
            System.out.println("✅ PyTorch Engine loaded successfully!");
        } catch (Exception e) {
            System.out.println("⚠️ PyTorch failed, check dependencies.");
        }
        SpringApplication.run(AiKnowledgeHub.class, args);
    }
}