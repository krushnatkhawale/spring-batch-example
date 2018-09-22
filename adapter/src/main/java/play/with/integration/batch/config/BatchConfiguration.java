package play.with.integration.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import play.with.integration.batch.listener.JobListener;
import play.with.integration.batch.model.Person;
import play.with.integration.batch.writer.CustomItemWriter;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${batch.jobName:HelloJob}")
    private String jobName;

    @Value("${batch.stepName:HelloStep}")
    private String stepName;

    @Value("${batch.chunkSize:5}")
    private int chunkSize;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Person> reader(@Value("#{jobParameters['input.file.name']}") String resource) {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setLineMapper((line, lineNumber) -> new Person(line));
        reader.setResource(new FileSystemResource(resource));
        return reader;
    }

    @Bean
    public ItemProcessor processor() {
        return new PassThroughItemProcessor();
    }

    @Bean
    public CustomItemWriter writer() {
        return new CustomItemWriter();
    }

    @Bean
    public JobListener jobListener() {
        return new JobListener();
    }

    @Bean
    public Job batchJob(Step step1, JobListener jobListener) {
        return jobBuilderFactory.get(jobName)
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .listener(jobListener)
                .build();
    }

    @Bean
    public Step step1(ItemReader reader, ItemProcessor processor, CustomItemWriter writer) {
        return stepBuilderFactory.get(stepName)
                .<Person, Person>chunk(chunkSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}