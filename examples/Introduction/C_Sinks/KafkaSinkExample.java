package Introduction.C_Sinks;


import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This job sinks our datastream into Kafka.
 *
 * <p><strong>Important:</strong> Ensure the target topic exists in Kafka before sinking data.</p>
 *
 * <h2>Kafka Connector Dependency</h2>
 * Add the following to your pom.xml:
 * <pre>
 * {@code
 * <dependency>
 *     <groupId>org.apache.flink</groupId>
 *     <artifactId>flink-connector-kafka</artifactId>
 *     <version>3.2.0-1.19</version>
 * </dependency>
 * <dependency>
 *     <groupId>org.apache.flink</groupId>
 *     <artifactId>flink-connector-kafka-base_2.12</artifactId>
 *     <version>1.11.6</version>
 * </dependency>
 * <dependency>
 *     <groupId>org.apache.kafka</groupId>
 *     <artifactId>kafka-clients</artifactId>
 *     <version>3.7.1</version>
 * </dependency>
 * }
 * </pre>
 *
 *
 * @author Seyed Mohamad Hasan Tabatabaei Asl
 */
public class KafkaSinkExample {



    public static void main(String[] args) throws Exception {
        /*
        First step in any flink job
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment() ;

        env.setParallelism(1);// making logs more realistic


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

        // getting input stream as json
        DataStream<String> myDataStream =
                env.fromSource(MySimpleSource, WatermarkStrategy.noWatermarks(), "my simulated source")
                .map(new MapFunction<Tuple2<String, Integer>, String>() { // map into json
                    @Override
                    public String map(Tuple2<String, Integer> inp) throws Exception {
                        return "{\"name\": \""+inp._1+"\", \"point\":"+inp._2+"}";//mapping into json
                    }
                });

        /*
        Now we make a Kafka Sink using a record serializer!
        We actually need to serialize each row before writing it into kafka.
        but here we use a simple string serializer(SimpleStringSchema)!
        Steps:
        1. define a record serializer
        2. make a kafka sing using the record serializer
         */
        // our configuration is ->
        String TOPIC_NAME = "my-topic-name";
        String BOOTSTRAP_SERVER = "localhost:9092";

        // making a serializer
        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setValueSerializationSchema(new SimpleStringSchema())
                .setTopic(TOPIC_NAME)
                .build();

        // making kafka sinker
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(BOOTSTRAP_SERVER)
                .setRecordSerializer(serializer)
                .build();


        // sinking finally
        myDataStream.sinkTo(sink) ;

        env.execute("kafka sinking job example");
    }
}
