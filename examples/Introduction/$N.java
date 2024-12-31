package Introduction;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.jdbc.source.JdbcSource;
import org.apache.flink.connector.jdbc.source.reader.extractor.ResultExtractor;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * My Way
 */
public class $N {

    public static class source {

        public static <T> JdbcSource<T> database(Properties props, ResultExtractor<T> resultExtractor) {
            return JdbcSource.<T>builder()
                    .setSql(props.getProperty("source.database.query"))
                    .setDBUrl(props.getProperty("source.database.url"))
                    .setUsername(props.getProperty("source.database.user"))
                    .setPassword(props.getProperty("source.database.pass"))
                    .setDriverName(props.getProperty("source.database.driver"))
                    .setResultExtractor(resultExtractor)
                    .setTypeInformation(TypeInformation.of(new TypeHint<T>() {}))
                    .build();
        }

        public static class kafka {

            public static <T> KafkaSource<T> single_topic(Properties props, DeserializationSchema<T> deserializationSchema) {
                return KafkaSource.<T>builder()
                        .setBootstrapServers(props.getProperty("source.kafka.bootstrap_servers"))
                        .setTopics(props.getProperty("source.kafka.topic"))
                        .setGroupId(props.getProperty("source.kafka.groupId"))
                        .setClientIdPrefix(props.getProperty("source.kafka.client_id_prefix"))
                        .setStartingOffsets(OffsetsInitializer.latest())
                        .setValueOnlyDeserializer(deserializationSchema)
                        .build();
            }

            /**
             * Gets used when you are reading from new created topics
             * or when there are multiple topics you want to read the data from.
             * for topics like ["topic-001", "topic-002", "topic-003", ....]
             * regex be something like this:
             * "topic-\\d{3}"
             *
             * @param props contains kafka source properties
             * @param deserializationSchema the way you deserialize each row
             * @return a kafka source
             * @param <T> type of your kafka source and deserializer
             */
            public static <T> KafkaSource<T> regex_topic(Properties props,
                                                             DeserializationSchema<T> deserializationSchema) {
                return KafkaSource.<T>builder()
                        .setBootstrapServers(props.getProperty("source.kafka.bootstrap_servers"))
                        .setTopicPattern(Pattern.compile(props.getProperty("source.kafka.topic")))
                        .setGroupId(props.getProperty("source.kafka.groupId"))
                        .setClientIdPrefix(props.getProperty("source.kafka.client_id_prefix"))
                        .setStartingOffsets(OffsetsInitializer.latest())
                        .setValueOnlyDeserializer(deserializationSchema)
                        .build();
            }

        }

    }


    public static class sink {

        public static class kafka {

            public static <T> KafkaSink<T> single_topic(Properties props, SerializationSchema<T> serializationSchema) {

                KafkaRecordSerializationSchema<T> serializer = KafkaRecordSerializationSchema.builder()
                        .setValueSerializationSchema(serializationSchema)
                        .setTopic(props.getProperty("sink.kafka.topic"))
                        .build();

                return KafkaSink.<T>builder()
                        .setBootstrapServers(props.getProperty("sink.kafka.bootstrap_servers"))
                        .setRecordSerializer(serializer)
                        .setKafkaProducerConfig(props)
                        .setTransactionalIdPrefix(props.getProperty("sink.kafka.transactional_id"))
                        .build();
            }


            /**
             * This gets used when you want to sink each row into some specific topic!
             * @param props props of kafka sink server
             * @param serializationSchema the way you serialize each row before writing into kafka
             * @param topicSelector the way you choose topics for each row from the row data itself!
             * @return kafka sink
             * @param <T> type of kafka sink and other objects
             */
            public static <T> KafkaSink<T> changing_topic(Properties props,
                                                          SerializationSchema<T> serializationSchema,
                                                          TopicSelector<T> topicSelector) {

                KafkaRecordSerializationSchema<T> serializer = KafkaRecordSerializationSchema.builder()
                        .setValueSerializationSchema(serializationSchema)
                        .setTopicSelector(topicSelector)
                        .build();

                return KafkaSink.<T>builder()
                        .setBootstrapServers(props.getProperty("sink.kafka.bootstrap_servers"))
                        .setRecordSerializer(serializer)
                        .setKafkaProducerConfig(props)
                        .setTransactionalIdPrefix(props.getProperty("sink.kafka.transactional_id"))
                        .build();
            }
        }

    }

}
