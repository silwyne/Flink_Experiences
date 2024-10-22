package nilian.Sources.DataStreamAPI;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.jdbc.source.JdbcSource;
import org.apache.flink.connector.jdbc.source.reader.extractor.ResultExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import scala.Tuple2;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Using DataStream API in this example!
 * In this example you will learn how to use JdbcSource to read data from table
 * In this example desired table schema is like this! (name VARCHAR, age INTEGER)
 *
 * @author seyed mohamad hasan tabatabaei asl
 */
public class JdbcSourceExample {
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
            env.execute("MyFirstFlink Job!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
