package de.example.multiplservicesconsul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class ConsulMultipleServicesRegistrar {

    private final ConsulClient consulClient;

    public ConsulMultipleServicesRegistrar() {
        this.consulClient = new ConsulClient();
    }

    @PostConstruct
    public void registerAdditionalServices() {
        NewService additionalService1 = new NewService();
        // additionalService1.setId("additional-service-X");
        additionalService1.setName("additional-service-X");
        // additionalService1.setAddress("localhost");
        consulClient.agentServiceRegister(additionalService1);

        NewService additionalService2 = new NewService();
        // additionalService2.setId("additional-service-X");
        additionalService2.setName("additional-service-Y");
        // additionalService2.setAddress("localhost");
        consulClient.agentServiceRegister(additionalService2);
    }

    @PreDestroy
    public void deregisterAdditionalServices() {
        consulClient.agentServiceDeregister("additional-service-1");
        consulClient.agentServiceDeregister("additional-service-2");
    }
}
