package play.with.integration.flow.filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileValidator {
    Logger logger = LoggerFactory.getLogger(FileValidator.class);

    public boolean filter(File file){
        boolean status = !isValid(file);
        logger.info("Input file: {}, validation status: {}", file.getName(), status);
        return status;
    }

    /**
     * business logic to filter files
     * @param file input file to validate
     * @return true if file valid, false otherwise
     */
    private boolean isValid(File file) {
        return file.getName().contains("test");
    }
}
