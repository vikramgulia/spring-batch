package com.barley.batch.config;

import java.sql.ResultSet;

import javax.sql.DataSource;

import com.barley.batch.listener.JobCompletionNotificationListener;
import com.barley.batch.model.RecordSO;
import com.barley.batch.model.WriterSO;
import com.barley.batch.processor.RecordProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Bean
    public ItemReader<RecordSO> reader() {
        return new JdbcCursorItemReaderBuilder<RecordSO>().name("the-reader")
                .sql("select id, firstName, lastname, random_num from reader").dataSource(dataSource)
                .rowMapper((ResultSet resultSet, int rowNum) -> {
                    if (!(resultSet.isAfterLast()) && !(resultSet.isBeforeFirst())) {
                        RecordSO recordSO = new RecordSO();
                        recordSO.setFirstName(resultSet.getString("firstName"));
                        recordSO.setLastName(resultSet.getString("lastname"));
                        recordSO.setId(resultSet.getInt("Id"));
                        recordSO.setRandomNum(resultSet.getString("random_num"));

                        LOGGER.info("RowMapper record : {}", recordSO);
                        return recordSO;
                    } else {
                        LOGGER.info("Returning null from rowMapper");
                        return null;
                    }
                }).build();
    }

    @Bean
    public ItemProcessor<RecordSO, WriterSO> processor() {
        return new RecordProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<WriterSO> writer(DataSource dataSource, ItemPreparedStatementSetter<WriterSO> setter) {
        return new JdbcBatchItemWriterBuilder<WriterSO>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .itemPreparedStatementSetter(setter)
                .sql("insert into writer (id, full_name, random_num) values (?,?,?)").dataSource(dataSource).build();
    }

    @Bean
    public ItemPreparedStatementSetter<WriterSO> setter() {
        return (item, ps) -> {
            ps.setLong(1, item.getId());
            ps.setString(2, item.getFullName());
            ps.setString(3, item.getRandomNum());
        };
    }

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener).flow(step1)
                .end().build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<WriterSO> writer, ItemReader<RecordSO> reader) {
        return stepBuilderFactory.get("step1").<RecordSO, WriterSO>chunk(5).reader(reader).processor(processor())
                .writer(writer).build();
    }
}
