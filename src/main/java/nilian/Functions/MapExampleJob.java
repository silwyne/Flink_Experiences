package nilian.Functions;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <h1>Map Function example</h1>
 * <p>In this job you will learn how to map each row of your data stream
 * into something new you want with your own defined function (USER_DEFINED)!</p>
 *
 * <h2>STORY EXPLANATIONS OF JOB</h2>
 * </p>we will get some source with this schema (name, score, level)
 * and return (name, totalScore) in result ! hint : totalScore = score * level !
 * In this story we have the data stream of players score in each level they go.
 * And we calculate their total score of the level as the level is higher the score of it worth more!
 *</p>
 *
 * @author seyed mohamad hasan tabatabaei asl
 */
public class MapExampleJob {
    public static void main(String[] args) {
        /*
        First step in any flink job is to make a StreamExecutionEnvironment!
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Setting Parallelism to 1 in simulated sources increases the log uniqueness

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
        Now we Map the input data stream into result using our MapFunction!
         */
        //Making Map Function
        MapFunction<String, Tuple2<String, Integer>> mapFunction = new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String s) throws Exception {

                String[] splitedString = s.split(",");// splitting the csv into its values

                int totalScore = //calculate the total score
                        Integer.parseInt(splitedString[1]) * Integer.parseInt(splitedString[2]);

                return new Tuple2<>(
                        splitedString[0],// name value
                        totalScore // total calculated score
                        );
            }
        };

        /*
        Now we use the MapFunction to process the input stream and get the result!
         */
        DataStream<Tuple2<String, Integer>> resultStream = myDataStream.map(mapFunction);

        // printing final result
        resultStream.print();

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

    private static DataGeneratorSource<String> getSource(){
        Random random = new Random() ;
        List<String> names = Arrays.asList("Mina", "David", "John", "Mohammad", "saleh", "amir", "arash", "elenor");
        GeneratorFunction<Long, String> generatorFunction =
                index -> names.get(Math.abs(random.nextInt()) % names.size())+","+ // get some player name !
                        Math.abs(random.nextInt(1000))+","+ //limiting the score to 1k !
                        Math.abs(random.nextInt(100)); //limiting the level to 100 !
        return
                new DataGeneratorSource<>(generatorFunction, Long.MAX_VALUE,
                        RateLimiterStrategy.perSecond(10), Types.STRING);
    }
}
