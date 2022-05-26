package com.demo.buildserver.service;


import com.demo.buildserver.dto.EventDTO;
import com.demo.buildserver.entity.Event;
import com.demo.buildserver.repository.EventRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;


import static com.demo.buildserver.constants.ApplicationConstants.*;


@Service
@Log4j2
public class EventService {

    private final EventRepository eventRepository;
    private final Long alertThreshold;

    @Autowired
    public EventService(EventRepository eventRepository,
                        @Value("${event.alert.true.threshold:4}") Long alertThreshold) {
        this.eventRepository = eventRepository;
        this.alertThreshold = alertThreshold;
    }

    private BiConsumer<Map<String, String>, String> acquireLock = (map, key) -> map.put(key, LOCKED);
    private BiConsumer<Map<String, String>, String> releaseLock = (map, key) -> map.remove(key);
    private BiFunction<Map<String, String>, String, String> tryLock = (map, key) -> map.get(key);
    private Predicate<String> isLocked = record -> Objects.nonNull(record);

    public List<Event> getLongerEvents() {
        return eventRepository.findByAlert(ALERT_TRUE);
    }

    /**
     * @param id- Event Id
     * @return Event data
     */
    public Event getEventById(String id) {
        Optional<Event> eventAuditOptional = eventRepository.findById(id);
        return eventAuditOptional.orElse(null);
    }

    @Transactional
    public void save(Event event) {
        log.debug("saving Event {} ", event);
        eventRepository.save(event);
    }


    /**
     * @param records    - record chucks as an input to process.
     * @param processMap - Current in-progress record ids of all
     * @ current executing threads
     * @ All threads execute this method parallel
     * @ Method process record and calculate event duration
     * @ Makes Alert and duration entry in the database
     */
    public String writeDataToDb(List<EventDTO> records, Map<String, String> processMap) {
        records.stream().forEach(record -> {
            String processState = tryLock.apply(processMap, record.getId());
            while (isLocked.test(processState)) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // check, if other ongoing thread is working on a same record id. If so then wait until he finishes his work.
                log.debug("Thread is waiting as another thread is processing same Log ID");
                processState = tryLock.apply(processMap, record.getId());
            }

            // lock record for processing
            acquireLock.accept(processMap, record.getId());
            log.debug("record added in process Map. record Id {}", record.getId());

            Event event = getEventById(record.getId());
            if (Objects.nonNull(event)) {
                updateStartOrEndTimeEventAudit(event, record);
                setDurationAndAlert(event);
            } else {
                event = Event.builder().id(record.getId())
                        .host(record.getHost())
                        .type(record.getType())
                        .build();
                updateStartOrEndTimeEventAudit(event, record);
            }
            save(event);
            //release lock after processing
            releaseLock.accept(processMap, record.getId());
        });
        return SUCCESS;
    }

    /**
     * @param eventToBeUpdate - Database Table Entity
     * @param eventDto        - File Record
     * @ Set Event Start and Finish Time to database Entity
     */
    private void updateStartOrEndTimeEventAudit(Event eventToBeUpdate, EventDTO eventDto) {
        log.debug("File event is getting parsed and event start and end time set to dbEntity id {}", eventToBeUpdate.getId());
        if (EVENT_STARTED.equalsIgnoreCase(eventDto.getState().trim()))
            eventToBeUpdate.setStartTime(eventDto.getTimestamp());
        else if (EVENT_FINISHED.equalsIgnoreCase(eventDto.getState().trim()))
            eventToBeUpdate.setEndTime(eventDto.getTimestamp());
    }

    /**
     * @param event - Database entity
     * @ Set event duration
     * @ Set Alert Type - true/false depends on configured Threshold value
     */
    private void setDurationAndAlert(Event event) {
        log.debug("calculating duration and alert value for an event id {}", event.getId());
        if (event.getStartTime() != null && event.getEndTime() != null) {
            Long duration = event.getEndTime() - event.getStartTime();
            event.setDuration(duration);
            if (event.getDuration().compareTo(alertThreshold) > 0)
                event.setAlert(ALERT_TRUE);
            else
                event.setAlert(ALERT_FALSE);
        }
    }

}
