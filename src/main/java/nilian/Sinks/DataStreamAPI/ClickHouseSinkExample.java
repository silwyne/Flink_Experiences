package nilian.Sinks.DataStreamAPI;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import scala.Tuple2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * In this job we sink our datastream into clickhouse.
 * Note : make sure the table exists in clickhouse before you sink you data
 * we use clickhouse driver for sinking:
 * ```maven
 * <dependency>
 *     <groupId>com.clickhouse</groupId>
 *     <artifactId>clickhouse-jdbc</artifactId>
 *     <version>0.7.0</version>
 * </dependency>
 * ```
 *
 * @author seyed mohamad hasan tabatabaei asl
 */
public class ClickHouseSinkExample {
    public static void main(String[] args) throws Exception {
        /*
        First step in any flink job
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment() ;

        // first generating data
        Random random = new Random() ;
        List<String> names = Arrays.asList("Mina", "David", "John", "Mohammad");
        GeneratorFunction<Long, Tuple2<String, Integer>> generatorFunction =
                index -> new Tuple2<>(names.get(Math.abs(random.nextInt()) % names.size()), Math.abs(random.nextInt(120)));
        // getting the source
        DataGeneratorSource<Tuple2<String, Integer>> MySimpleSource =
                new DataGeneratorSource<>(generatorFunction, Long.MAX_VALUE,
                        RateLimiterStrategy.perSecond(10), TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {}));


        /*
        Now we introduce the source to our StreamExecutionEnvironment!
        And receive a DataStream in return!
         */
        DataStream<Tuple2<String, Integer>> myDataStream =
                env.fromSource(MySimpleSource, WatermarkStrategy.noWatermarks(), "my simulated source");

        // making sink function
        SinkFunction<Tuple2<String, Integer>> jdbcSink = JdbcSink.sink(
                "INSERT INTO people values (?, ?)",
                new JdbcStatementBuilder<Tuple2<String, Integer>>() {
                    @Override
                    public void accept(PreparedStatement preparedStatement, Tuple2<String, Integer> input) throws SQLException {
                        preparedStatement.setString(1, input._1);
                        preparedStatement.setInt(2, input._2);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:clickhouse:http://@localhost:8123")
                        .withDriverName("com.clickhouse.jdbc.ClickHouseDriver")
                        .withUsername("USERNAME")
                        .withPassword("PASSWORD")
                        .build()
        );


        // sinking finally
        myDataStream.addSink(jdbcSink) ;


        env.execute("click-house sinking job example");
    }

}
