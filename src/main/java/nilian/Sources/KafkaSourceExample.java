package nilian.Sources;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * <p>Using DataStream API in this example!
 * In this example you will learn how to read data from a kafka topic</p>
 *
 * <h2>Kafka connection Dependency</h2>
 * We use these dependencies
 * <pre>
 * {@code
 * <dependency>
 *    <groupId>org.apache.flink</groupId>
 *    <artifactId>flink-connector-kafka</artifactId>
 *    <version>3.2.0-1.19</version>
 * </dependency>
 * <dependency>
 *    <groupId>org.apache.flink</groupId>
 *    <artifactId>flink-connector-kafka-base_2.12</artifactId>
 *    <version>1.11.6</version>
 * </dependency>
 * <dependency>
 *    <groupId>org.apache.kafka</groupId>
 *    <artifactId>kafka-clients</artifactId>
 *    <version>3.7.1</version>
 * </dependency>
 * }
 * </pre>
 *
 * @author Seyed Mohamad Hasan Tabatabaei Asl
 */
public class KafkaSourceExample {
    public static void main(String[] args) {
        /*
        First step in any flink job is to make a StreamExecutionEnvironment!
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        /*
        Now the Main Part of this example is our KafkaSource! Here I use SimpleString with no Deserialization.
        because this example is not about deserialization!
         */
        //configuration: remember to config these for your own!
        String bootstrapServer = "localhost:9092" ;
        String topicName = "myTopic" ;
        String groupId = "MyGroup" ;
        String clientPrefix = "MyPrefix" ;
        // Based on your desire from latest data or earliest data you want to consume!
        OffsetsInitializer offsetsInitializer = OffsetsInitializer.latest();
        // as i said pure string ! ;)
        DeserializationSchema<String> deserializationSchema = new SimpleStringSchema() ;

        //finally the kafka source
        KafkaSource<String> MyKafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(topicName)
                .setGroupId(groupId)
                .setClientIdPrefix(clientPrefix)
                .setStartingOffsets(offsetsInitializer)
                .setValueOnlyDeserializer(deserializationSchema)
                .build();


        /*
        Now we introduce the source to our StreamExecutionEnvironment!
        And receive a DataStream in return!
         */
        DataStream<String> myDataStream = env.fromSource(MyKafkaSource, WatermarkStrategy.noWatermarks(), "My Kafka Source");
        //letting the data stream get printed as new values float in it!
        myDataStream.print();

        /*
        Finally you must lunch the Game!
        Without this, the job never execute!
         */
        try {
            env.execute("Kafka source job example!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
