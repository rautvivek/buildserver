package com.demo.buildserver.service;


import com.demo.buildserver.dto.EventDTO;
import com.demo.buildserver.entity.Event;
import com.demo.buildserver.repository.EventRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.demo.buildserver.constants.ApplicationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getAllLongerEventsTest() {
        eventService.getLongerEvents();
        Mockito.verify(eventRepository, times(1)).findByAlert(ALERT_TRUE);

    }

    @Test
    public void getEventByIdTest() {
        String id = "abcdefg";
        Event event = Event.builder().id(id).startTime(323333l).type("APPLICATION_LOG").build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        Assertions.assertThat(eventService.getEventById(id).getStartTime()).isEqualTo(323333l);
    }

    @Test
    public void saveTest() {
        Event event = Event.builder().id("abcdef").startTime(12345444l).type("APPLICATION_LOG").build();
        eventService.save(event);
        Mockito.verify(eventRepository, times(1)).save(event);
    }

    @Test
    public void writeDataToDbTest() {
        List<EventDTO> recordSet = new ArrayList<>();
        Map<String, String> processMap = new ConcurrentHashMap<>();
        EventDTO eventDTO1 = EventDTO.builder()
                .id("12345")
                .state(EVENT_STARTED)
                .timestamp(233333l)
                .build();
        EventDTO eventDTO2 = EventDTO.builder()
                .id("12345")
                .state(EVENT_FINISHED)
                .timestamp(233338l)
                .build();
        Event returnEvent = Event.builder().id("12345").duration(5l).alert(ALERT_TRUE).build();
        when(eventRepository.save(any())).thenReturn(returnEvent);
        recordSet.add(eventDTO1);
        recordSet.add(eventDTO2);
        assertThat(eventService.writeDataToDb(recordSet, processMap)).isEqualTo(SUCCESS);
    }

}
