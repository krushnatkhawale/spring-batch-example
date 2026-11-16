package play.with.integration.flow.filter;


import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class FileValidator {

    private String invalidFilenamePattern;

    public void setInvalidFilenamePattern(String invalidFilenamePattern) {
        this.invalidFilenamePattern = invalidFilenamePattern;
    }

    public boolean filter(File file){
        boolean status = !isValid(file);
        log.info("Input file: {}, validation status: {}", file.getName(), status);
        return status;
    }

    /**
     * business logic to filter files
     * @param file input file to validate
     * @return true if file valid, false otherwise
     */
    private boolean isValid(File file) {
        return !file.getName().contains(invalidFilenamePattern);
    }
}