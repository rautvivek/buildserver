package com.demo.buildserver;

import com.demo.buildserver.component.EventLogParserComponent;
import com.demo.buildserver.exception.FileProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class BuildserverApplication  implements CommandLineRunner {
	private final EventLogParserComponent eventLogParserComponent;
	private final String logFileName;

	public BuildserverApplication(EventLogParserComponent eventLogParserComponent,
								  @Value("${log.file.name:logfile.txt}") String logFileName) {
		this.eventLogParserComponent = eventLogParserComponent;
		this.logFileName = logFileName;
	}

	@Override
	public void run(String... commandLineArgs) {
		String fileWithPath="";
		if (commandLineArgs.length == 0) {
			log.error("File Path has not been provided as a Command Line Argument. Please provide File Path");
		} else {
			fileWithPath = commandLineArgs[0] + logFileName;
			log.info("File Path URL: {}", fileWithPath);
		}
		try {
			eventLogParserComponent.startFileProcessing(fileWithPath);
		} catch (Exception ex) {
			log.error("Exception in processing File {}",ex);
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(BuildserverApplication.class, args);
	}

}
