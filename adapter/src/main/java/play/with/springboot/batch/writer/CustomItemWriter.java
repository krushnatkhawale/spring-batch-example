package play.with.springboot.batch.writer;

import org.springframework.batch.item.ItemWriter;
import play.with.springboot.batch.model.Person;

import java.util.List;

public class CustomItemWriter implements ItemWriter<Person> {

    @Override
    public void write(List<? extends Person> items) throws Exception {
        System.out.println("WROTE: " + items);
    }
}