package Introduction.E_TableApi;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import static org.apache.flink.table.api.Expressions.*;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * <h3>Casting DataStream into Table</h3>
 * <p>If you want to use Table Api in flink. you need to bring yor data into a table
 * That's why you need to know how to cast your DataStream into a Table Object</p>
 *
 * @author Seyed Mohamad Hasan Tabatabaei Asl
 */
public class CastDataStreamIntoTable {

    public static void main(String[] args) throws Exception {
        /*
        Silencing the logs
        to see table print better
         */
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(ch.qos.logback.classic.Level.WARN);


        /*
        STEP 1;
        First step in any flink job is to get the StreamExecution environment.
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        /*
        by Setting the parallelism to 1
        making log generation make unique logs
         */
        env.setParallelism(1);


        /*
        STEP 2;
        Now we get the StreamTableEnvironment from the StreamExecutionEnvironment
        which gives us access to work with `Table API`
         */
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        /*
        STEP 3;
        First we produce some sample data to work with
         */
        DataStream<String> simulated_stream = env.fromSource(getSource(), WatermarkStrategy.noWatermarks(), "my simulated source");

        /*
        STEP 4;
        Now we cast the DataStream type from String into Pojo(Plain Old Java Object)!
        "Take a look at the defined Pojo down here,
                because there is some tips with each pojo you define."
                                    - ...
        So we can bring the DataStream into TableApi much easier
         */
        DataStream<PlayerData> stream = simulated_stream
                .map(new MapFunction<String, PlayerData>() {
                    @Override
                    public PlayerData map(String string) throws Exception {
                        String[] fields = string.split(",");
                        return new PlayerData(
                                fields[0],
                                Integer.parseInt(fields[1]),
                                Integer.parseInt(fields[2])
                        );
                    }
                });


        /*
        STEP 5;
        Now we simply cast it into Table
         */
        Table t1_stream = tEnv.fromDataStream(stream);

        /*
        Doing some process
         */
        Table result = t1_stream
                .groupBy(col("name"))
                .select(
                    col("name"),
                    col("point").sum().as("sum_point"),
                    col("xp").sum().as("sum_xp")
                )
                .addColumns(currentTimestamp().as("event_time"));

        /*
        Finally printing the result
         */
        result.execute().print();


        env.execute("Table API sample job");
    }



    /*
    Generates sample data
     */
    private static DataGeneratorSource<String> getSource(){
        Random random = new Random() ;
        List<String> names = Arrays.asList("Mina", "David", "John", "Mohammad", "saleh", "amir", "arash", "elenor");
        GeneratorFunction<Long, String> generatorFunction =
                index -> names.get(Math.abs(random.nextInt()) % names.size())+","+ // get some player name !
                        Math.abs(random.nextInt(1000))+","+ // limiting the score to 1k !
                        Math.abs(random.nextInt(100)); // limiting the level to 100 !
        return
                new DataGeneratorSource<>(generatorFunction, Long.MAX_VALUE,
                        RateLimiterStrategy.perSecond(100), Types.STRING);
    }

    /*
    Here is our Pojo(Plain Old Java Object)
    TIPS FOR Pojo:
    1. has a constructor which gets no param
    2. has a constructor which gets all data fields
    3. as extra_tip you must Override toString function because you might want to print your datastream into stdout!
     */
    public static class PlayerData {
        public String name;
        public int point;
        public int xp;

        // constructor which gets no param
        public PlayerData() {}

        // constructor which gets all data fields
        public PlayerData(String name, int point, int xp) {
            this.name = name;
            this.point = point;
            this.xp = xp;
        }

        // toString function returns Pojo in String format
        @Override
        public String toString() {
            /*
             this is the string format of your Pojo
             Which might be Json or Csv, Or what ever you like.
             */
            // I like csv
            return name+','+point+','+xp;
        }
    }

}
