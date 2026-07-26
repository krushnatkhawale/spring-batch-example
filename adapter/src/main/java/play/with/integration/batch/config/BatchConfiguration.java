package play.with.integration.batch.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestTemplate;
import play.with.integration.batch.listener.JobListener;
import play.with.integration.batch.model.Person;
import play.with.integration.batch.writer.CustomItemWriter;

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

    @Bean
    @StepScope
    public FlatFileItemReader<Person> reader(@Value("#{jobParameters['input.file.name']}") String resource) {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personReader")
                .resource(new FileSystemResource(resource))
                .lineMapper((line, lineNumber) -> new Person(line))
                .build();
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
    public Job batchJob(JobRepository jobRepository, Step step1, JobListener jobListener) {
        return new JobBuilder(jobName, jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .listener(jobListener)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      ItemReader reader, ItemProcessor processor, CustomItemWriter writer) {
        return new StepBuilder(stepName, jobRepository)
                .<Person, Person>chunk(chunkSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}