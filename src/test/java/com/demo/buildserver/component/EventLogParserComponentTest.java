package com.demo.buildserver.component;


import com.demo.buildserver.exception.FileProcessingException;
import com.demo.buildserver.service.EventService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestPropertySource(value = "classpath:test-application.properties")
public class EventLogParserComponentTest {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventLogParserComponent eventLogParserComponent;

    @Test
    public void startFileProcessingTest() throws FileProcessingException {
        eventLogParserComponent.startFileProcessing("testlogfile.txt");
        assertThat(eventService.getLongerEvents().size()).isEqualTo(2);
    }

    @Test
    public void startFileProcessingExceptionTest() throws FileProcessingException {
        Assertions.assertThrows(FileProcessingException.class, ()->
                eventLogParserComponent.startFileProcessing("notFound.txt"));
    }

}
