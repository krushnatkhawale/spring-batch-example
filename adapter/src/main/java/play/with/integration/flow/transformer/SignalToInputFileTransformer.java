package play.with.integration.flow.transformer;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.forceDelete;

public class SignalToInputFileTransformer {

    public File transform(File signalFile) throws IOException {
        String signalFilePath = signalFile.getAbsolutePath();
        String inputFilePath = signalFilePath.substring(0, signalFilePath.indexOf(".GO"));
        forceDelete(signalFile);
        return new File(inputFilePath);
    }

}
