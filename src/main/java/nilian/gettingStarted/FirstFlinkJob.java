package nilian.gettingStarted;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This Flink Job is making random simulated rows of data!
 * In this job you will learn :
 * 1. how to initialize a flink job
 * 2. how to introduce a source to flink
 * 3. simple tip about executing flink job
 *
 * @author seyed mohammad hasan tabatabaei asl
 */
public class FirstFlinkJob {
    public static void main(String[] args) {

        /*
        First step in any flink job is to make a StreamExecutionEnvironment!
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Setting Parallelism to 1 in simulated sources increases the log uniqueness
        // This decrease the chance of having same log many times!
        env.setParallelism(1) ;

        /*
        Now you might want to have some Sources. but as This example is not about Source in Flink!
        I simply use a simple simulated Source!
         */
        //This source is simply made in format of a csv ! => name,number
        Random random = new Random() ;
        List<String> names = Arrays.asList("Mina", "David", "John", "Mohammad");
        GeneratorFunction<Long, String> generatorFunction =
                index -> names.get(Math.abs(random.nextInt()) % names.size())+","+random.nextInt();
        //finally the source
        DataGeneratorSource<String> MySimpleSource =
                new DataGeneratorSource<>(generatorFunction, Long.MAX_VALUE,
                        RateLimiterStrategy.perSecond(10), Types.STRING);

        /*
        Now we introduce the source to our StreamExecutionEnvironment!
        And receive a DataStream in return!
         */
        DataStream<String> myDataStream = env.fromSource(MySimpleSource, WatermarkStrategy.noWatermarks(), "MySimpleSource");
        //letting the data stream get printed as new values float in it!
        myDataStream.print();

        /*
        Finally you must lunch the Game!
        Without this, the job never execute!
         */
        try {
            env.execute("MyFirstFlink Job!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
