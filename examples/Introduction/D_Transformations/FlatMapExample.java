package Introduction.D_Transformations;


import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <h1>FlatMap Function example</h1>
 * <p>In this example  you will learn how to use flat map function.
 * Well when your data process might want to resolve more than result for each input datastream row;
 * You might want to consider FlatMap Function!
 * Which gives you the ability to produce more than one result rows for each input row</p>
 *
 * <h2>STORY EXPLANATIONS OF JOB</h2>
 * </p>
 * we have a simple input datastream that only gives us a simple name as String!
 * And we will produce two rows of (name, point, xp) tuple3 for that with the input name!
 * input as name -> process(FlatMap) ->  result (name, point, xp), ...
 * </p>
 *
 * @author seyed mohamad hasan tabatabaei asl
 */
public class FlatMapExample {


    public static void main(String[] args) {
        /*
        First step in any flink job is to make a StreamExecutionEnvironment!
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Setting Parallelism to 1 in simulated sources increases the log uniqueness

        // lest make it more realistic
        env.setParallelism(1);// to make sure we have more unique produced sample rows
        /*
        Getting some source as it is not the main point of the example!
         */
        DataGeneratorSource<String> mySimpleSource = getSource() ;
        /*
        Now we introduce the source to our StreamExecutionEnvironment!
        And receive a DataStream in return!
         */
        DataStream<String> myDataStream = env.fromSource(mySimpleSource, WatermarkStrategy.noWatermarks(), "MySimpleSource");

        /*
        Now we Map the input data stream into more than one result rows using our FlatMapFunction!
         */
        // Making FlatMapFunction
        FlatMapFunction<String, Tuple3<String, Integer, Integer>> mapFunction = new FlatMapFunction<String, Tuple3<String, Integer, Integer>>() {

            private final Random random = new Random();
            @Override
            public void flatMap(String string, Collector<Tuple3<String, Integer, Integer>> collector) throws Exception {

                // decide the number of result rows
                int num_of_rows_to_produce = Math.abs(random.nextInt(5)) + 1;
                // generating result rows
                for(int i = 0 ; i < num_of_rows_to_produce ; i ++) {
                    collector.collect(new Tuple3<>(
                            string,
                            Math.abs(random.nextInt(1000)),//point
                            Math.abs(random.nextInt(100))//xp
                    ));
                }
            }
        };

        /*
        Now we use the FlatMapFunction to process the input stream and get the result!
         */
        DataStream<Tuple3<String, Integer, Integer>> resultStream = myDataStream.flatMap(mapFunction);

        // printing final result
        resultStream.print();

        /*
        Finally you must lunch the Game!
        Without this, the job never execute!
         */
        try {
            env.execute("FlatMap example Job!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static DataGeneratorSource<String> getSource(){
        Random random = new Random() ;
        List<String> names = Arrays.asList("Mina", "David", "John", "Mohammad", "saleh", "amir", "arash", "elenor");
        GeneratorFunction<Long, String> generatorFunction =
                index -> names.get(Math.abs(random.nextInt()) % names.size()); // just a name
        return
                new DataGeneratorSource<>(generatorFunction, Long.MAX_VALUE,
                        RateLimiterStrategy.perSecond(10), Types.STRING);
    }

}
