package play.with.integration.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import play.with.integration.batch.model.Person;

import java.util.List;

import static java.lang.String.format;

public class CustomItemWriter implements ItemWriter<Person>, StepExecutionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomItemWriter.class);

    @Override
    public void write(List<? extends Person> items){
        items.stream().map(Object::toString).forEach(LOGGER::info);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().putString("JobSummary", stepExecution.getSummary());
        return stepExecution.getExitStatus();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }
}