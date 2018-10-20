package play.with.integration.batch.model;

import org.springframework.batch.core.StepExecution;
import play.with.integration.batch.util.JsonUtils;

import java.io.Serializable;

public class StepReport implements Serializable {
    private String name;
    private Count stats;

    private StepReport(String name, Count stats) {
        this.name = name;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Count getStats() {
        return stats;
    }

    public void setStats(Count stats) {
        this.stats = stats;
    }

    public static StepReport obtainStats(StepExecution stepExecution) {
        Count stats = Count.obtainStats(stepExecution);
        return new StepReport(stepExecution.getStepName(), stats);
    }

    @Override
    public String toString() {
        return JsonUtils.toString(this);
    }

}
