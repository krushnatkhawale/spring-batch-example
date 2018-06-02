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

        //items.stream().map(Person::getName).forEach(LOGGER::info);

        LOGGER.info(format("Sum of letters of all people in the list: %s", getSum(items)));
    }

    private long getSum(List<? extends Person> items) {
        return items.stream()
                .map(Person::getName)
                .mapToInt(String::length)
                .sum();
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