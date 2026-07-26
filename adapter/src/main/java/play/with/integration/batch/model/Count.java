package play.with.integration.batch.model;

import org.springframework.batch.core.step.StepExecution;
import play.with.integration.batch.util.JsonUtils;

import java.io.Serializable;

public class Count implements Serializable {
    private long readCount;
    private long readSkipCount;
    private long processSkipCount;
    private long writeCount;
    private long writeSkipCount;

    public Count(long readCount, long readSkipCount, long processSkipCount, long writeCount, long writeSkipCount) {
        this.readCount = readCount;
        this.readSkipCount = readSkipCount;
        this.processSkipCount = processSkipCount;
        this.writeCount = writeCount;
        this.writeSkipCount = writeSkipCount;
    }

    public long getReadCount() {
        return readCount;
    }

    public void setReadCount(long readCount) {
        this.readCount = readCount;
    }

    public long getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(long readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    public long getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(long processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    public long getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(long writeCount) {
        this.writeCount = writeCount;
    }

    public long getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(long writeSkipCount) {
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