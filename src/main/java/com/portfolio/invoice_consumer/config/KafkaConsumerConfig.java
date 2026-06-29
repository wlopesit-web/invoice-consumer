package com.portfolio.invoice_consumer.config;

import com.portfolio.invoice_consumer.dto.InvoiceDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // Captura o endereço do Kafka direto do seu application.properties
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Captura o ID do grupo direto do seu application.properties
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, InvoiceDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //  padrão do Kafka: lê tudo da fila inicialmente como texto puro (String)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
        //return new DefaultKafkaConsumerFactory<>(props);
    }

    /*
    // 1. Cria a Fábrica de Consumidores customizada, definindo as regras de tradução de dados
    @Bean
    public ConsumerFactory<String, InvoiceDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Configurações básicas de fiação de rede
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Configuração manual dos Deserializadores (Chave é texto puro, Valor é o nosso JSON)
        JsonDeserializer<InvoiceDTO> deserializer = new JsonDeserializer<>(InvoiceDTO.class);
        deserializer.addTrustedPackages("*"); // Medida de segurança do Spring para confiar no nosso pacote
        deserializer.setUseTypeHeaders(false); // Ignora cabeçalhos de tipo do produtor para evitar conflito de pacotes

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
     */


    // 2. Cria o Container que o Spring vai usar por trás da anotação @KafkaListener
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InvoiceDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InvoiceDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // Amarra a nossa fábrica de consumidores customizada dentro do container
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // Ensina o Spring Boot a criar a ferramenta de conversão de JSON que estava faltando
    @org.springframework.context.annotation.Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}
