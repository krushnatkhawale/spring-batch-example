package play.with.integration.batch.model;

import org.springframework.batch.core.StepExecution;
import play.with.integration.batch.util.JsonUtils;

import java.io.Serializable;

public class Count implements Serializable {
    private int readCount;
    private int readSkipCount;
    private int processSkipCount;
    private int writeCount;
    private int writeSkipCount;

    public Count(int readCount, int readSkipCount, int processSkipCount, int writeCount, int writeSkipCount) {
        this.readCount = readCount;
        this.readSkipCount = readSkipCount;
        this.processSkipCount = processSkipCount;
        this.writeCount = writeCount;
        this.writeSkipCount = writeSkipCount;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public int getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(int readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    public int getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(int processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    public int getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(int writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    public static Count obtainStats(StepExecution step) {
        return new Count(step.getReadCount(), step.getReadSkipCount(), step.getProcessSkipCount(), step.getWriteCount(), step.getWriteSkipCount());
    }

    @Override
    public String toString() {
        return JsonUtils.toString(this);
    }

}