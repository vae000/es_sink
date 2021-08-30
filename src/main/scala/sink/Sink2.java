package sink;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.core.fs.Path;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.sink.filesystem.RollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Map;
import java.util.Properties;
import java.util.regex.*;
import static sun.misc.Version.println;
import java.util.Arrays;


public class Sink2 {
    public static void main(String[] args) throws Exception {
        String localPath = "F://";
        System.setProperty("java.security.auth.login.config", localPath + "/jaas.conf");
        System.setProperty("java.security.krb5.conf", localPath + "/krb5.conf");

        String fieldDelimiter = "|";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // checkpoint
        //env.enableCheckpointing(10_000);
        //env.setStateBackend((StateBackend) new FsStateBackend("file:///E://checkpoint"));
        //env.setStateBackend((StateBackend) new FsStateBackend("hdfs://kms-1:8020/checkpoint"));
        // CheckpointConfig config = env.getCheckpointConfig();
        //config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);

        // source
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "8.136.179.149:9092");
        props.setProperty("zookeeper.connect", "8.136.179.149:2181");
//        props.setProperty("security.protocol", "SASL_PLAINTEXT");
//        props.setProperty("sasl.kerberos.service.name", "kafka");
//        props.setProperty("sasl.mechanism", "GSSAPI");
        props.setProperty("auto.offset.reset","latest");
        props.setProperty("group.id", "gp");



                //resultstream.print();
        FlinkKafkaConsumer011<String> consumer = new FlinkKafkaConsumer011<String>(
                "pl", new SimpleStringSchema(), props);
        //consumer.setStartFromEarliest();
        consumer.setCommitOffsetsOnCheckpoints(true);
        consumer.setStartFromGroupOffsets();
        DataStream<String> stream= env.addSource(consumer);

        // transform
        SingleOutputStreamOperator<String> resultstream = stream
                .filter(new FilterFunction<String>() {
                    // 过滤掉DDL操作
                    @Override
                    public boolean filter(String jsonVal) throws Exception {
                        JSONObject record = JSON.parseObject(jsonVal, Feature.OrderedField);
                        return record.getString("isDdl").equals("false");
                    }
                })
                .map(new MapFunction<String, String>() {

                    @Override
                    public String map(String value) throws Exception {

                        StringBuilder fieldsBuilder = new StringBuilder();
                        // 解析JSON数据
                        JSONObject record = JSON.parseObject(value, Feature.OrderedField);
                        // 获取最新的字段值
                        JSONArray data = record.getJSONArray("data");
                        // 遍历，字段值的JSON数组，只有一个元素
                        for (int i = 0; i < data.size(); i++) {
                            // 获取到JSON数组的第i个元素
                            JSONObject obj = data.getJSONObject(i);
                            if (obj != null && i!=data.size()-1) {
                                fieldsBuilder.append(record.getLong("es")); //业务时间戳 event time
                                fieldsBuilder.append(fieldDelimiter);
                                fieldsBuilder.append(record.getString("type")); // 操作类型
                                fieldsBuilder.append(fieldDelimiter);
                                fieldsBuilder.append(record.getString("table")); // 表名
                                for (Map.Entry<String, Object> hashMap : obj.entrySet()) {
                                    fieldsBuilder.append(fieldDelimiter);
                                    String field =  String.valueOf(hashMap.getValue()).replaceAll("[\t\n\r]", "");
                                    fieldsBuilder.append(field); // 表字段数据
                                }
                                fieldsBuilder.append("\n");
                            }else {
                                fieldsBuilder.append(record.getLong("es")); //业务时间戳 event time
                                fieldsBuilder.append(fieldDelimiter);
                                fieldsBuilder.append(record.getString("type")); // 操作类型
                                fieldsBuilder.append(fieldDelimiter);
                                fieldsBuilder.append(record.getString("table")); // 表名
                                for (Map.Entry<String, Object> hashMap : obj.entrySet()) {
                                    fieldsBuilder.append(fieldDelimiter);
                                    String field =  String.valueOf(hashMap.getValue()).replaceAll("[\t\n\r]", "");
                                    fieldsBuilder.append(field); // 表字段数据
                                }
                            }
                        }


                        return fieldsBuilder.toString();
                    }
                });
        //resultstream.print();
              /*
//              * 转换type,DELETE转为1，UPDATE,INSERT,SELECT均转为0
//              * */
//              .flatMap(new FlatMapFunction<String, String>() {
//                  @Override
//                  public void flatMap(String s, Collector<String> collector) throws Exception {
//                      String[] array = s.split("\n");
//
//                      for (int i = 0; i<array.length;i++){
//                      String[] val = array[i].split(",");
//                        if (val[1].equals("DELETE")) {
//                           Arrays.fill(val, 1, 2, "1");
//                             collector.collect(ArrayUtils.toString(val, ","));
//                        } else {
//                            Arrays.fill(val, 1, 2, "0");
//                            collector.collect(ArrayUtils.toString(val, ","));
//
//                        }
//                      }
//                  }
//              })
//              //去掉{}
//              .map(new MapFunction<String, String>() {
//                  @Override
//                  public String map(String s) throws Exception {
//                      return s.replace("{","").replace("}","");
//                  }
//              });
              resultstream.print();
        OutputTag<String> outputTag1 = new OutputTag<String>("s1") {};
        OutputTag<String> outputTag2 = new OutputTag<String>("s2") {};
        OutputTag<String> outputTag3 = new OutputTag<String>("s3") {};


        SingleOutputStreamOperator<String> outputStream = resultstream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String s, Context context, Collector<String> collector) throws Exception {
                String[] element = s.split("|");

                boolean m1 = Pattern.matches("t_biddingsall_cache_\\d+", element[2]);
                boolean m2 = Pattern.matches("stu_\\d+", element[2]);

                if (m1) {
                    context.output(outputTag1,s);
                }else if (m2) {
                    context.output(outputTag2,s);
                } else {
                    context.output(outputTag3, s);
                }}
        });

        DataStream<String> stream1 = outputStream.getSideOutput(outputTag1);
        DataStream<String> stream2 = outputStream.getSideOutput(outputTag2);
        DataStream<String> stream3 = outputStream.getSideOutput(outputTag3);

       // stream1.addSink(new MysqlSink());
        //stream1.addSink(new MysqlSink());
          stream2.print();


        env.execute("sink-job");
    }}

