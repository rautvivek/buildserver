
#######################################################################################
# Main Class - BuildserverApplication
    1) Application run by main class (Need to pass Command Line Argument - File Path)
    2) File path should be end with slash(e.g. c:/logfile/)
    3) Name of File should be logfile.txt 
#######################################################################################

# Output 
    1) Makes entry in database for each event( id, start_time, end_time, type, host, duration, alert)
    2) Longer events count and their details are displayed on console (id, type, host, duration, alert)
 
    

# Database
    Project uses file based database
    1) Application database - buildserverdb  
	2) relative db directory - database
    3) Text table - EVENT

# Resources
    1) application.properties
         a) Input Log File Configuration - Events Log File Name (logfile.txt)
         b) ALERT Configuration - alerting for longer events
         c) THREAD POOL Configuration - thread count and batch size configuration
         d) FILE BASED DATABASE Configuration - file db configuration
         e) Default username and password configuration used
    2) data.sql
        a) Text table ("event") creation script.
        b) Text based table used for readability, though it has little performance overhead.

# File Reading
    1) Reading is streamed process.
    2) Reading is single threaded sequential process.
    3) Memory efficiency achieved by buffered reader.

# Executor Framework
     1) File Processing is multithreaded process.
     2) Multithreading is achieved by Executor framework FixedThreadPool.
     3) Thread pool and batch size configuration is present in application.properties.
     4) thread.count - maximum threads configuration in thread pool.
        batch.size - feeds(number of records) to each thread.
     5) Threads process records in parallel and save/update in a database. Duration and Alert value is also calculated.

# processMap
    1) processMap is concurrentHashMap. This map is top level of all threads and shared between all.
    2) To maintain synchronization of one id between all threads, processMap is used.
    3) When thread starts processing any record, it acquires lock on that record by making entry in process map.
       When thread finishes his work on that record, acquired lock has been released by removing entry from process map.
     
# Exception Handling
    FileProcessingException - Custom Exception created to show readable messages for file processing errors.
  
# Test Cases
    1) Test cases are present in test folder.
    2) Test database - buildserverdbtest in test-cases-database folder which different from main application database.
    3) testlogfile.txt - file in project root directory used for test cases run





