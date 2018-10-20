package play.with.integration.batch.model;

import play.with.integration.batch.util.DateUtils;
import play.with.integration.batch.util.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Report implements Serializable {

    private String filename;
    private Date startTime;
    private Date endTime;
    private Count jobStats;
    private List<StepReport> steps = new ArrayList<>();

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStartTime() {
        return startTime.toString();
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime.toString();
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Count getJobStats() {
        return jobStats;
    }

    public void setJobStats(Count jobStats) {
        this.jobStats = jobStats;
    }

    public List<StepReport> getSteps() {
        return steps;
    }

    public void setSteps(List<StepReport> steps) {
        this.steps = steps;
    }

    public String getDuration() {
        return DateUtils.diff(startTime, endTime);
    }

    @Override
    public String toString() {
        return JsonUtils.toString(this);
    }

}