package Introduction.B_Sources;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.jdbc.source.reader.extractor.ResultExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.jdbc.source.JdbcSource;
import scala.Tuple2;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>Using DataStream API in this example!
 * In this example you will learn how to use JdbcSource to read data from table
 * In this example desired table schema is like this! (name VARCHAR, age INTEGER)</p>
 *
 * <h2>Postgres connection Dependency</h2>
 * We use these dependencies
 * <pre>
 * {@code
 * <dependency>
 *   <groupId>org.postgresql</groupId>
 *   <artifactId>postgresql</artifactId>
 *   <version>42.7.2</version>
 * </dependency>
 * <dependency>
 *   <groupId>org.apache.flink</groupId>
 *   <artifactId>flink-jdbc_2.12</artifactId>
 *   <version>1.10.3</version>
 * </dependency>
 * <dependency>
 *   <groupId>org.apache.flink</groupId>
 *   <artifactId>flink-connector-jdbc</artifactId>
 *   <version>3.2.0-1.19</version>
 * </dependency>
 * }
 * </pre>
 *
 * <h2>Postgres URL Format</h2>
 * <p><strong>Critical Note:</strong> The correct format for the Postgres URL in Flink is:</p>
 * <pre>
 * <a href=>jdbc:postgresql://HOST:PORT/DBNAME</a>
 * </pre>
 *
 * @author Seyed Mohamad Hasan Tabatabaei Asl
 */
public class PostgresSource {
    public static void main(String[] args) {
        /*
        First step in any flink job is to make a StreamExecutionEnvironment!
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        /*
        Now the Main Part of this example is our JdbcSource!
         */
        //defining result executor which extracts data from table
        //for table schema like => (name VARCHAR, age INTEGER)
        ResultExtractor<Tuple2<String, Integer>> myExecutor = new ResultExtractor<Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> extract(ResultSet resultSet) throws SQLException {
                return new Tuple2<>(
                        resultSet.getString(1),// name column
                        resultSet.getInt(2));// age column
            }
        };
        //configuration: remember to config these for your own!
        String tableName = "my_table" ;
        String jdbcUrl = "jdbc:postgresql://localhost:5432/your_data_base";
        String jdbcUsername = "USERNAME";
        String jdbcPassword = "PASS";
        //finally the jdbc source
        JdbcSource<Tuple2<String, Integer>> myJdbcSource = JdbcSource.<Tuple2<String, Integer>>builder()
                .setSql("select * from "+tableName)
                .setDBUrl(jdbcUrl)
                .setUsername(jdbcUsername)
                .setPassword(jdbcPassword)
                .setDriverName("org.postgresql.Driver")
                .setResultExtractor(myExecutor)
                .setTypeInformation(TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {}))
                .build();

        /*
        Now we introduce the source to our StreamExecutionEnvironment!
        And receive a DataStream in return!
         */
        DataStream<Tuple2<String, Integer>> myDataStream =
                env.fromSource(myJdbcSource, WatermarkStrategy.noWatermarks(), "MySimpleSource");
        //letting the data stream get printed as new values float in it!
        myDataStream.print();

        /*
        Finally you must lunch the Game!
        Without this, the job never execute!
         */
        try {
            env.execute("Jdbc source job example!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
