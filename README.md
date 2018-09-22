# Spring batch example

With power of spring integration file poller, it polls input (configurable) directory for text files, filters if filename contains **test** and passes this file to spring batch job for processing which in turn reads the file, maps data to Person class and then writer logs read data

### directory structure:
  
  - :file_folder: spring-batch-example  `<- root directory `
  
    - :file_folder: adapter             `<- spring batch application directory`
 
    - :file_folder: config              `<- externalized configuration directory (empty nothing externalized at this point)`

    - :file_folder: input               `<- input directory for spring integration to poll for` 

    - :file_folder: filtered            `<- filtered files are moved to this directory - filtered if file name contains 'test'` 
