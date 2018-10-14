package play.with.integration.flow.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.forceDelete;

public class NoInputFileSignalFileFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoInputFileSignalFileFilter.class);

    public boolean filter(File signalFile) throws IOException {
        String signalFilePath = signalFile.getAbsolutePath();
        String inputFilePath = signalFilePath.substring(0, signalFilePath.indexOf(".GO"));
        File inputFile = new File(inputFilePath);
        if (inputFile.exists()) {
            LOGGER.info("Input file found, deleting signal file {}", signalFile.getName());
            return true;
        } else {
            String message = String.format("Couldn't find input file for signal file '%s'", signalFile);
            LOGGER.info(message);
            forceDelete(signalFile);
            return false;
        }
    }
}