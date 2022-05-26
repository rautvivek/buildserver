package com.demo.buildserver.component;


import com.demo.buildserver.dto.EventDTO;
import com.demo.buildserver.entity.Event;
import com.demo.buildserver.exception.FileProcessingException;
import com.demo.buildserver.service.EventService;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Data
@Builder
@Log4j2
public class EventLogParserComponent {

    private final EventService eventService;
    private final int batchSize;
    private final int threadCount;

    @Autowired
    public EventLogParserComponent(EventService eventService,
                                   @Value("${batch.size}") int batchSize,
                                   @Value("${thread.count}") int threadCount) {
        this.eventService = eventService;
        this.batchSize = batchSize;
        this.threadCount = threadCount;
    }


    /**
     * Reads input file line by line sequential manner using buffered reader
     * and stream file data.
     * Executor framework is used to process data in parallel
     */
    public void startFileProcessing(String inputLogFileName) throws FileProcessingException {
        List<Future<String>> futures = new ArrayList<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputLogFileName))) {
            Iterator iterator = bufferedReader.lines().iterator();
            Map<String, String> processMap = new ConcurrentHashMap<>();
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            List<EventDTO> recordList = new ArrayList();
            while (iterator.hasNext()) {
                for (int startIndex = 0; startIndex < batchSize; startIndex++) {
                    if (iterator.hasNext()) {
                        String line = (String) iterator.next();
                        if (Strings.isNotEmpty(line)) {
                            EventDTO eventDTO = new Gson().fromJson(line, EventDTO.class);
                            recordList.add(eventDTO);
                        }
                    }
                }
                List<EventDTO> recordsTobeProcess = new ArrayList(recordList);
                futures.add(executorService.submit(() ->
                        eventService.writeDataToDb(recordsTobeProcess, processMap))
                );
                recordList.clear();
            }
            futures.forEach(future -> {
                try {
                    log.debug("future return value: {}", future.get());
                } catch (Exception e) {
                    log.error("Error occurred in Thread Future: {}", e);
                }
            });
            DisplayLongerEvents();
            executorService.shutdown();
        }
        catch (NoSuchFileException e) {
            log.error("Error occurred while reading input file :File Not Present");
            throw new FileProcessingException("logfile is not present at given path. Given input logfile path is incorrect. Please check path: " + inputLogFileName,e);

        } catch (IOException ex){
            log.error("IOException occurred while reading input file :{}",ex);
            throw new FileProcessingException("IOException occurred while reading input file ",ex);
        }
    }


    /**
     * Method is only for demonstration purpose
     * Display Final Result on console
     * shows count of longer events.
     */
    private void DisplayLongerEvents() {
        List<Event> results = eventService.getLongerEvents();
        log.info("######################### Longer Events  #########################");
        results.forEach(event -> log.info(event.toString()));
        log.info("longer event count:: {}",results.size());
    }

}
