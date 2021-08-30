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
import org.apache.flink.api.java.utils.ParameterTool;
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
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.flink.util.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.regex.*;
import static sun.misc.Version.println;
import java.util.Arrays;

public class HdfsSink {
    public static void main(String[] args) throws Exception {
       String localPath = "/home/flink";
        System.setProperty("java.security.auth.login.config", localPath + "/jaas.conf");
       System.setProperty("java.security.krb5.conf", localPath + "/krb5.conf");
       System.out.println();
        String fieldDelimiter = "|";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //env.setParallelism(4);
        ParameterTool parameter = ParameterTool.fromArgs(args);
        String groupid = parameter.get("groupid");
        String offset = parameter.get("offset");

        // checkpoint
        env.enableCheckpointing(1800_000);  //30分钟
        //env.setStateBackend((StateBackend) new FsStateBackend("file:///E://checkpoint"));
        env.setStateBackend((StateBackend) new FsStateBackend("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/checkpoint"));
        CheckpointConfig config = env.getCheckpointConfig();
        config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


        // source
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "cq-college-cdh-64-5:9092");
        props.setProperty("zookeeper.connect", "cq-college-cdh-64-3:2181,cq-college-cdh-64-4:2181,cq-college-cdh-64-5:2181");
        props.setProperty("security.protocol", "SASL_PLAINTEXT");
        props.setProperty("sasl.kerberos.service.name", "kafka");
        props.setProperty("sasl.mechanism", "GSSAPI");
        props.setProperty("auto.offset.reset",offset);
        props.setProperty("group.id", groupid);
        FlinkKafkaConsumer011<String> consumer = new FlinkKafkaConsumer011<String>(
                "canal_topic", new SimpleStringSchema(), props);
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
              /*
//              * 转换type,DELETE转为1，UPDATE,INSERT,SELECT均转为0
//              * */
//              .flatMap(new FlatMapFunction<String, String>() {
//                  @Override
//                  public void flatMap(String s, Collector<String> collector) throws Exception {
//                      String[] array = s.split("\n");
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
        //resultstream.print();
     //分流操作
        //OutputTag<String> outputTag1 = new OutputTag<String>("s1") {};
        OutputTag<String> outputTag2 = new OutputTag<String>("s2") {};
        OutputTag<String> outputTag3 = new OutputTag<String>("s3") {};
        OutputTag<String> outputTag4 = new OutputTag<String>("s4") {};
        OutputTag<String> outputTag5 = new OutputTag<String>("s5") {};
        OutputTag<String> outputTag6 = new OutputTag<String>("s6") {};
        OutputTag<String> outputTag7 = new OutputTag<String>("s7") {};
        OutputTag<String> outputTag8 = new OutputTag<String>("s8") {};
        OutputTag<String> outputTag9 = new OutputTag<String>("s9") {};
        OutputTag<String> outputTag10 = new OutputTag<String>("s10") {};
        OutputTag<String> outputTag11 = new OutputTag<String>("s11") {};
        OutputTag<String> outputTag12 = new OutputTag<String>("s12") {};
        OutputTag<String> outputTag13 = new OutputTag<String>("s13") {};
        OutputTag<String> outputTag14 = new OutputTag<String>("s14") {};
        OutputTag<String> outputTag15 = new OutputTag<String>("s15") {};
        OutputTag<String> outputTag16 = new OutputTag<String>("s16") {};
        OutputTag<String> outputTag17 = new OutputTag<String>("s17") {};
        OutputTag<String> outputTag18 = new OutputTag<String>("s18") {};
        OutputTag<String> outputTag19 = new OutputTag<String>("s19") {};
        OutputTag<String> outputTag20 = new OutputTag<String>("s20") {};
        OutputTag<String> outputTag21 = new OutputTag<String>("s21") {};
        OutputTag<String> outputTag22 = new OutputTag<String>("s22") {};
        OutputTag<String> outputTag23 = new OutputTag<String>("s23") {};
        OutputTag<String> outputTag24 = new OutputTag<String>("s24") {};
        OutputTag<String> outputTag25 = new OutputTag<String>("s25") {};
        OutputTag<String> outputTag26 = new OutputTag<String>("s26") {};
        OutputTag<String> outputTag27 = new OutputTag<String>("s27") {};
        OutputTag<String> outputTag28 = new OutputTag<String>("s28") {};
        OutputTag<String> outputTag29 = new OutputTag<String>("s29") {};
        OutputTag<String> outputTag30 = new OutputTag<String>("s30") {};
        OutputTag<String> outputTag31 = new OutputTag<String>("s31") {};
        OutputTag<String> outputTag32 = new OutputTag<String>("s32") {};
        OutputTag<String> outputTag33 = new OutputTag<String>("s33") {};
        OutputTag<String> outputTag34 = new OutputTag<String>("s34") {};
        OutputTag<String> outputTag35 = new OutputTag<String>("s35") {};
        OutputTag<String> outputTag36 = new OutputTag<String>("s36") {};
        OutputTag<String> outputTag37 = new OutputTag<String>("s37") {};
        OutputTag<String> outputTag38 = new OutputTag<String>("s38") {};
        OutputTag<String> outputTag39 = new OutputTag<String>("s39") {};
        OutputTag<String> outputTag40 = new OutputTag<String>("s40") {};
        OutputTag<String> outputTag41 = new OutputTag<String>("s41") {};
        OutputTag<String> outputTag42 = new OutputTag<String>("s42") {};
        OutputTag<String> outputTag43 = new OutputTag<String>("s43") {};
        OutputTag<String> outputTag44 = new OutputTag<String>("s44") {};
        OutputTag<String> outputTag45 = new OutputTag<String>("s45") {};
        OutputTag<String> outputTag46 = new OutputTag<String>("s46") {};
        OutputTag<String> outputTag47 = new OutputTag<String>("s47") {};
        OutputTag<String> outputTag48 = new OutputTag<String>("s48") {};
        OutputTag<String> outputTag49 = new OutputTag<String>("s49") {};
        OutputTag<String> outputTag50 = new OutputTag<String>("s50") {};
        OutputTag<String> outputTag51 = new OutputTag<String>("s51") {};
        OutputTag<String> outputTag52 = new OutputTag<String>("s52") {};
        OutputTag<String> outputTag53 = new OutputTag<String>("s53") {};
        OutputTag<String> outputTag54 = new OutputTag<String>("s54") {};
        OutputTag<String> outputTag55 = new OutputTag<String>("s55") {};
        OutputTag<String> outputTag56 = new OutputTag<String>("s56") {};
        OutputTag<String> outputTag57 = new OutputTag<String>("s57") {};
        OutputTag<String> outputTag58 = new OutputTag<String>("s58") {};
        OutputTag<String> outputTag59 = new OutputTag<String>("s59") {};
        OutputTag<String> outputTag60 = new OutputTag<String>("s60") {};
        OutputTag<String> outputTag61 = new OutputTag<String>("s61") {};
        OutputTag<String> outputTag62 = new OutputTag<String>("s62") {};
        OutputTag<String> outputTag63 = new OutputTag<String>("s63") {};
        OutputTag<String> outputTag64 = new OutputTag<String>("s64") {};
        OutputTag<String> outputTag65 = new OutputTag<String>("s65") {};
        OutputTag<String> outputTag66 = new OutputTag<String>("s66") {};
        OutputTag<String> outputTag67 = new OutputTag<String>("s67") {};
        OutputTag<String> outputTag68 = new OutputTag<String>("s68") {};
        OutputTag<String> outputTag69 = new OutputTag<String>("s69") {};
        OutputTag<String> outputTag70 = new OutputTag<String>("s70") {};
        OutputTag<String> outputTag71 = new OutputTag<String>("s71") {};
        OutputTag<String> outputTag72 = new OutputTag<String>("s72") {};
        OutputTag<String> outputTag73 = new OutputTag<String>("s73") {};
        OutputTag<String> outputTag74 = new OutputTag<String>("s74") {};
        OutputTag<String> outputTag75 = new OutputTag<String>("s75") {};
        OutputTag<String> outputTag76 = new OutputTag<String>("s76") {};
        OutputTag<String> outputTag77 = new OutputTag<String>("s77") {};
        OutputTag<String> outputTag78 = new OutputTag<String>("s78") {};
        OutputTag<String> outputTag79 = new OutputTag<String>("s79") {};
        OutputTag<String> outputTag80 = new OutputTag<String>("s80") {};
        OutputTag<String> outputTag81 = new OutputTag<String>("s81") {};
        OutputTag<String> outputTag82 = new OutputTag<String>("s82") {};
        OutputTag<String> outputTag83 = new OutputTag<String>("s83") {};
        OutputTag<String> outputTag84 = new OutputTag<String>("s84") {};
        OutputTag<String> outputTag85 = new OutputTag<String>("s85") {};
        OutputTag<String> outputTag86 = new OutputTag<String>("s86") {};
        OutputTag<String> outputTag87 = new OutputTag<String>("s87") {};
        OutputTag<String> outputTag88 = new OutputTag<String>("s88") {};
        OutputTag<String> outputTag89 = new OutputTag<String>("s89") {};
        OutputTag<String> outputTag90 = new OutputTag<String>("s90") {};
        OutputTag<String> outputTag91 = new OutputTag<String>("s91") {};
        OutputTag<String> outputTag92 = new OutputTag<String>("s92") {};
        OutputTag<String> outputTag93 = new OutputTag<String>("s93") {};
        OutputTag<String> outputTag94 = new OutputTag<String>("s94") {};
        OutputTag<String> outputTag95 = new OutputTag<String>("s95") {};
        OutputTag<String> outputTag96 = new OutputTag<String>("s96") {};
        OutputTag<String> outputTag97 = new OutputTag<String>("s97") {};
        OutputTag<String> outputTag98 = new OutputTag<String>("s98") {};
        OutputTag<String> outputTag99 = new OutputTag<String>("s99") {};
        OutputTag<String> outputTag100 = new OutputTag<String>("s100") {};
        OutputTag<String> outputTag101 = new OutputTag<String>("s101") {};
        OutputTag<String> outputTag102 = new OutputTag<String>("s102") {};
        OutputTag<String> outputTag103 = new OutputTag<String>("s103") {};
        OutputTag<String> outputTag104 = new OutputTag<String>("s104") {};



        SingleOutputStreamOperator<String> outputStream = resultstream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String s, Context context, Collector<String> collector) throws Exception {
                String[] element = s.split("\\|");

                boolean m102 = Pattern.matches("t_bidding_content_\\d+", element[2]);
                boolean m103 = Pattern.matches("t_bidding_info_\\d+", element[2]);
                boolean m104 = Pattern.matches("t_bidding_related_\\d+", element[2]);
                boolean m2 = Pattern.matches("t_biddings_all", element[2]);
                boolean m3 = Pattern.matches("t_credit_info_\\d+", element[2]);
                boolean m4 = Pattern.matches("t_domains_alls_\\d+", element[2]);
                boolean m5 = Pattern.matches("t_restricted_consumer", element[2]);
                boolean m6 = Pattern.matches("t_terminationcaseitem", element[2]);
                boolean m7 = Pattern.matches("t_abnormal_enterprises", element[2]);
                boolean m8 = Pattern.matches("t_creditimportexport_data", element[2]);
                boolean m9 = Pattern.matches("t_general_taxpayer", element[2]);
                boolean m10 = Pattern.matches("t_goudi_information", element[2]);
                boolean m11 = Pattern.matches("t_huge_tax_punishment", element[2]);
                boolean m12 = Pattern.matches("t_pay_taxes", element[2]);
                boolean m13 = Pattern.matches("t_auctions_relations_\\d+", element[2]);
                boolean m14 = Pattern.matches("t_cases_relations_\\d+", element[2]);
                boolean m15 = Pattern.matches("t_copyrights_relations_\\d+", element[2]);
                boolean m16 = Pattern.matches("t_kaitinggonggaos_relations_\\d+", element[2]);
                boolean m17 = Pattern.matches("t_lawsuits_relations_\\d+", element[2]);
                boolean m18 = Pattern.matches("t_notices_relations_\\d+", element[2]);
                boolean m19 = Pattern.matches("t_auctions", element[2]);
                boolean m20 = Pattern.matches("t_cases", element[2]);
                boolean m21 = Pattern.matches("t_executedpersons", element[2]);
                boolean m22 = Pattern.matches("t_executions", element[2]);
                boolean m23 = Pattern.matches("t_huanbaochufas", element[2]);
                boolean m24 = Pattern.matches("t_kaitinggonggaos_\\d+", element[2]);
                boolean m25 = Pattern.matches("t_new_jobs_\\d+", element[2]);
                boolean m26 = Pattern.matches("t_notices", element[2]);
                boolean m27 = Pattern.matches("t_overduetaxs", element[2]);
                boolean m28 = Pattern.matches("t_report_details_\\d+", element[2]);
                boolean m29 = Pattern.matches("t_abnormal_\\d+", element[2]);
                boolean m30 = Pattern.matches("t_address_\\d+", element[2]);
                boolean m31 = Pattern.matches("t_administrative_punishment_\\d+", element[2]);
                boolean m32 = Pattern.matches("t_branches_\\d+", element[2]);
                boolean m33 = Pattern.matches("t_change_records_\\d+", element[2]);
                boolean m34 = Pattern.matches("t_checkups_\\d+", element[2]);
                boolean m35 = Pattern.matches("t_clear_account_\\d+", element[2]);
                boolean m36 = Pattern.matches("t_double_checkups_\\d+", element[2]);
                boolean m37 = Pattern.matches("t_emails_\\d+", element[2]);
                boolean m38 = Pattern.matches("t_employees_alls_\\d+", element[2]);
                boolean m39 = Pattern.matches("t_enterprise_\\d+", element[2]);
                boolean m40 = Pattern.matches("t_equityquality_\\d+", element[2]);
                boolean m41 = Pattern.matches("t_history_names_\\d+", element[2]);
                boolean m42 = Pattern.matches("t_history_oper_names_\\d+", element[2]);
                boolean m43 = Pattern.matches("t_history_regist_capis_\\d+", element[2]);
                boolean m44 = Pattern.matches("t_judicial_freezes_\\d+", element[2]);
                boolean m45 = Pattern.matches("t_knowledge_properties_\\d+", element[2]);
                boolean m46 = Pattern.matches("t_license_info_\\d+", element[2]);
                boolean m47 = Pattern.matches("t_mortgages_\\d+", element[2]);
                boolean m48 = Pattern.matches("t_partners_alls_\\d+", element[2]);
                boolean m49 = Pattern.matches("t_serious_illegal_\\d+", element[2]);
                boolean m50 = Pattern.matches("t_simple_cancellation_announcement_\\d+", element[2]);
                boolean m51 = Pattern.matches("t_social_security_\\d+", element[2]);
                boolean m52 = Pattern.matches("t_telephones_\\d+", element[2]);
                boolean m53 = Pattern.matches("t_websites_\\d+", element[2]);
                boolean m54 = Pattern.matches("t_patents_new_\\d+", element[2]);
                boolean m55 = Pattern.matches("t_patents_relations_new_\\d+", element[2]);
                boolean m56 = Pattern.matches("t_trademarks_\\d+", element[2]);
                boolean m57 = Pattern.matches("t_building", element[2]);
                boolean m58 = Pattern.matches("t_ccc", element[2]);
                boolean m59 = Pattern.matches("t_ccc_self_declaration", element[2]);
                boolean m60 = Pattern.matches("t_certificate_main_\\d+", element[2]);
                boolean m61 = Pattern.matches("t_cesi_certification", element[2]);
                boolean m62 = Pattern.matches("t_computer_project_manager", element[2]);
                boolean m63 = Pattern.matches("t_cosmetic_produce_license", element[2]);
                boolean m64 = Pattern.matches("t_drug_business", element[2]);
                boolean m65 = Pattern.matches("t_drug_permit_cn", element[2]);
                boolean m66 = Pattern.matches("t_drug_produce", element[2]);
                boolean m67 = Pattern.matches("t_electronic_certification_agency", element[2]);
                boolean m68 = Pattern.matches("t_estate_qualification", element[2]);
                boolean m69 = Pattern.matches("t_firefighting_system", element[2]);
                boolean m70 = Pattern.matches("t_food_additives", element[2]);
                boolean m71 = Pattern.matches("t_food_business_license", element[2]);
                boolean m72 = Pattern.matches("t_food_license", element[2]);
                boolean m73 = Pattern.matches("t_food_product_ent", element[2]);
                boolean m74 = Pattern.matches("t_food_product_license", element[2]);
                boolean m75 = Pattern.matches("t_industry_produce", element[2]);
                boolean m76 = Pattern.matches("t_industry_volunteer", element[2]);
                boolean m77 = Pattern.matches("t_license_drug", element[2]);
                boolean m78 = Pattern.matches("t_management_system", element[2]);
                boolean m79 = Pattern.matches("t_medical_instruments_business", element[2]);
                boolean m80 = Pattern.matches("t_medical_instruments_import", element[2]);
                boolean m81 = Pattern.matches("t_medical_instruments_produce", element[2]);
                boolean m82 = Pattern.matches("t_medical_instruments_registered", element[2]);
                boolean m83 = Pattern.matches("t_mining_exploration", element[2]);
                boolean m84 = Pattern.matches("t_mining_license", element[2]);
                boolean m85 = Pattern.matches("t_network_license", element[2]);
                boolean m86 = Pattern.matches("t_product_safe_license", element[2]);
                boolean m87 = Pattern.matches("t_radio_customs_approve", element[2]);
                boolean m88 = Pattern.matches("t_radio_model_approve", element[2]);
                boolean m89 = Pattern.matches("t_service", element[2]);
                boolean m90 = Pattern.matches("t_sewage_permit", element[2]);
                boolean m91 = Pattern.matches("t_telecommunications_license", element[2]);
                boolean m92 = Pattern.matches("t_enterprise_investment_\\d+", element[2]);
                boolean m93 = Pattern.matches("t_last_industry_\\d+", element[2]);
                boolean m94 = Pattern.matches("t_admin_division_code", element[2]);
                boolean m95 = Pattern.matches("t_currency_code", element[2]);
                boolean m96 = Pattern.matches("t_industry_code", element[2]);
                boolean m97 = Pattern.matches("t_job_title_code", element[2]);
                boolean m98 = Pattern.matches("t_country_code", element[2]);
                boolean m99 = Pattern.matches("t_econ_kind_code", element[2]);
                boolean m100 = Pattern.matches("t_procedure_code", element[2]);




                if (m102) {
                    context.output(outputTag102,s);
                }else if(m103){
                    context.output(outputTag103,s);
                }else if(m104){
                    context.output(outputTag104,s);
                } else if (m2) {
                    context.output(outputTag2,s);
                } else if (m3){
                    context.output(outputTag3,s);
                } else if (m4){
                    context.output(outputTag4,s);
                }else if(m5){
                    context.output(outputTag5,s);
                }else if(m6){
                    context.output(outputTag6,s);
                }else if(m7){
                    context.output(outputTag7,s);
                }else if(m8){
                    context.output(outputTag8,s);
                }else if(m9){
                    context.output(outputTag9,s);
                }else if(m10){
                    context.output(outputTag10,s);
                }else if(m11){
                    context.output(outputTag11,s);
                }else if(m12){
                    context.output(outputTag12,s);
                }else if(m13){
                    context.output(outputTag13,s);
                }else if(m14){
                    context.output(outputTag14,s);
                }else if(m15){
                    context.output(outputTag15,s);
                }else if(m16){
                    context.output(outputTag16,s);
                }else if(m17){
                    context.output(outputTag17,s);
                }else if(m18){
                    context.output(outputTag18,s);
                }else if(m19){
                    context.output(outputTag19,s);
                }else if(m20){
                    context.output(outputTag20,s);
                }else if(m21){
                    context.output(outputTag21,s);
                }else if(m22){
                    context.output(outputTag22,s);
                }else if(m23){
                    context.output(outputTag23,s);
                }else if(m24){
                    context.output(outputTag24,s);
                }else if(m25){
                    context.output(outputTag25,s);
                }else if(m26){
                    context.output(outputTag26,s);
                }else if(m27){
                    context.output(outputTag27,s);
                }else if(m28){
                    context.output(outputTag28,s);
                }else if(m29){
                    context.output(outputTag29,s);
                }else if(m30){
                    context.output(outputTag30,s);
                }else if(m31){
                    context.output(outputTag31,s);
                }else if(m32){
                    context.output(outputTag32,s);
                }else if(m33){
                    context.output(outputTag33,s);
                }else if(m34){
                    context.output(outputTag34,s);
                }else if(m35){
                    context.output(outputTag35,s);
                }else if(m36){
                    context.output(outputTag36,s);
                }else if(m37){
                    context.output(outputTag37,s);
                }else if(m38){
                    context.output(outputTag38,s);
                }else if(m39){
                    context.output(outputTag39,s);
                }else if(m40){
                    context.output(outputTag40,s);
                }else if(m41){
                    context.output(outputTag41,s);
                }else if(m42){
                    context.output(outputTag42,s);
                }else if(m43){
                    context.output(outputTag43,s);
                }else if(m44){
                    context.output(outputTag44,s);
                }else if(m45){
                    context.output(outputTag45,s);
                }else if(m46){
                    context.output(outputTag46,s);
                }else if(m47){
                    context.output(outputTag47,s);
                }else if(m48){
                    context.output(outputTag48,s);
                }else if(m49){
                    context.output(outputTag49,s);
                }else if(m50){
                    context.output(outputTag50,s);
                }else if(m51){
                    context.output(outputTag51,s);
                }else if(m52){
                    context.output(outputTag52,s);
                }else if(m53){
                    context.output(outputTag53,s);
                }else if(m54){
                    context.output(outputTag54,s);
                }else if(m55){
                    context.output(outputTag55,s);
                }else if(m56){
                    context.output(outputTag56,s);
                }else if(m57){
                    context.output(outputTag57,s);
                }else if(m58){
                    context.output(outputTag58,s);
                }else if(m59){
                    context.output(outputTag59,s);
                }else if(m60){
                    context.output(outputTag60,s);
                }else if(m61){
                    context.output(outputTag61,s);
                }else if(m62){
                    context.output(outputTag62,s);
                }else if(m63){
                    context.output(outputTag63,s);
                }else if(m64){
                    context.output(outputTag64,s);
                }else if(m65){
                    context.output(outputTag65,s);
                }else if(m66){
                    context.output(outputTag66,s);
                }else if(m67){
                    context.output(outputTag67,s);
                }else if(m68){
                    context.output(outputTag68,s);
                }else if(m69){
                    context.output(outputTag69,s);
                }else if(m70){
                    context.output(outputTag70,s);
                }else if(m71){
                    context.output(outputTag71,s);
                }else if(m72){
                    context.output(outputTag72,s);
                }else if(m73){
                    context.output(outputTag73,s);
                }else if(m74){
                    context.output(outputTag74,s);
                }else if(m75){
                    context.output(outputTag75,s);
                }else if(m76){
                    context.output(outputTag76,s);
                }else if(m77){
                    context.output(outputTag77,s);
                }else if(m78){
                    context.output(outputTag78,s);
                }else if(m79){
                    context.output(outputTag79,s);
                }else if(m80){
                    context.output(outputTag80,s);
                }else if(m81){
                    context.output(outputTag81,s);
                }else if(m82){
                    context.output(outputTag82,s);
                }else if(m83){
                    context.output(outputTag83,s);
                }else if(m84){
                    context.output(outputTag84,s);
                }else if(m85){
                    context.output(outputTag85,s);
                }else if(m86){
                    context.output(outputTag86,s);
                }else if(m87){
                    context.output(outputTag87,s);
                }else if(m88){
                    context.output(outputTag88,s);
                }else if(m89){
                    context.output(outputTag89,s);
                }else if(m90){
                    context.output(outputTag90,s);
                }else if(m91){
                    context.output(outputTag91,s);
                }else if(m92){
                    context.output(outputTag92,s);
                }else if(m93){
                    context.output(outputTag93,s);
                }else if(m94){
                    context.output(outputTag94,s);
                }else if(m95){
                    context.output(outputTag95,s);
                }else if(m96){
                    context.output(outputTag96,s);
                }else if(m97){
                    context.output(outputTag97,s);
                }else if(m98){
                    context.output(outputTag98,s);
                }else if(m99){
                    context.output(outputTag99,s);
                }else if(m100){
                    context.output(outputTag100,s);
                }else {
                    context.output(outputTag101,s);
                }

//
//                switch (element[2]){
//                    case "code_city_1":
//                        context.output(outputTag1,s);
//                        break;
//                    case "zz_2":
//                        context.output(outputTag2,s);
//                        break;
//                    default:
//                        context.output(outputTag3,s);
//                }
            }
        });
        DataStream<String> stream102 = outputStream.getSideOutput(outputTag102);
        DataStream<String> stream103 = outputStream.getSideOutput(outputTag103);
        DataStream<String> stream104 = outputStream.getSideOutput(outputTag104);
        DataStream<String> stream2 = outputStream.getSideOutput(outputTag2);
        DataStream<String> stream3 = outputStream.getSideOutput(outputTag3);
        DataStream<String> stream4 = outputStream.getSideOutput(outputTag4);
        DataStream<String> stream5 = outputStream.getSideOutput(outputTag5);
        DataStream<String> stream6 = outputStream.getSideOutput(outputTag6);
        DataStream<String> stream7 = outputStream.getSideOutput(outputTag7);
        DataStream<String> stream8 = outputStream.getSideOutput(outputTag8);
        DataStream<String> stream9 = outputStream.getSideOutput(outputTag9);
        DataStream<String> stream10 = outputStream.getSideOutput(outputTag10);
        DataStream<String> stream11 = outputStream.getSideOutput(outputTag11);
        DataStream<String> stream12 = outputStream.getSideOutput(outputTag12);
        DataStream<String> stream13 = outputStream.getSideOutput(outputTag13);
        DataStream<String> stream14 = outputStream.getSideOutput(outputTag14);
        DataStream<String> stream15 = outputStream.getSideOutput(outputTag15);
        DataStream<String> stream16 = outputStream.getSideOutput(outputTag16);
        DataStream<String> stream17 = outputStream.getSideOutput(outputTag17);
        DataStream<String> stream18 = outputStream.getSideOutput(outputTag18);
        DataStream<String> stream19 = outputStream.getSideOutput(outputTag19);
        DataStream<String> stream20 = outputStream.getSideOutput(outputTag20);
        DataStream<String> stream21 = outputStream.getSideOutput(outputTag21);
        DataStream<String> stream22 = outputStream.getSideOutput(outputTag22);
        DataStream<String> stream23 = outputStream.getSideOutput(outputTag23);
        DataStream<String> stream24 = outputStream.getSideOutput(outputTag24);
        DataStream<String> stream25 = outputStream.getSideOutput(outputTag25);
        DataStream<String> stream26 = outputStream.getSideOutput(outputTag26);
        DataStream<String> stream27 = outputStream.getSideOutput(outputTag27);
        DataStream<String> stream28 = outputStream.getSideOutput(outputTag28);
        DataStream<String> stream29 = outputStream.getSideOutput(outputTag29);
        DataStream<String> stream30 = outputStream.getSideOutput(outputTag30);
        DataStream<String> stream31 = outputStream.getSideOutput(outputTag31);
        DataStream<String> stream32 = outputStream.getSideOutput(outputTag32);
        DataStream<String> stream33 = outputStream.getSideOutput(outputTag33);
        DataStream<String> stream34 = outputStream.getSideOutput(outputTag34);
        DataStream<String> stream35 = outputStream.getSideOutput(outputTag35);
        DataStream<String> stream36 = outputStream.getSideOutput(outputTag36);
        DataStream<String> stream37 = outputStream.getSideOutput(outputTag37);
        DataStream<String> stream38 = outputStream.getSideOutput(outputTag38);
        DataStream<String> stream39 = outputStream.getSideOutput(outputTag39);
        DataStream<String> stream40 = outputStream.getSideOutput(outputTag40);
        DataStream<String> stream41 = outputStream.getSideOutput(outputTag41);
        DataStream<String> stream42 = outputStream.getSideOutput(outputTag42);
        DataStream<String> stream43 = outputStream.getSideOutput(outputTag43);
        DataStream<String> stream44 = outputStream.getSideOutput(outputTag44);
        DataStream<String> stream45 = outputStream.getSideOutput(outputTag45);
        DataStream<String> stream46 = outputStream.getSideOutput(outputTag46);
        DataStream<String> stream47 = outputStream.getSideOutput(outputTag47);
        DataStream<String> stream48 = outputStream.getSideOutput(outputTag48);
        DataStream<String> stream49 = outputStream.getSideOutput(outputTag49);
        DataStream<String> stream50 = outputStream.getSideOutput(outputTag50);
        DataStream<String> stream51 = outputStream.getSideOutput(outputTag51);
        DataStream<String> stream52 = outputStream.getSideOutput(outputTag52);
        DataStream<String> stream53 = outputStream.getSideOutput(outputTag53);
        DataStream<String> stream54 = outputStream.getSideOutput(outputTag54);
        DataStream<String> stream55 = outputStream.getSideOutput(outputTag55);
        DataStream<String> stream56 = outputStream.getSideOutput(outputTag56);
        DataStream<String> stream57 = outputStream.getSideOutput(outputTag57);
        DataStream<String> stream58 = outputStream.getSideOutput(outputTag58);
        DataStream<String> stream59 = outputStream.getSideOutput(outputTag59);
        DataStream<String> stream60 = outputStream.getSideOutput(outputTag60);
        DataStream<String> stream61 = outputStream.getSideOutput(outputTag61);
        DataStream<String> stream62 = outputStream.getSideOutput(outputTag62);
        DataStream<String> stream63 = outputStream.getSideOutput(outputTag63);
        DataStream<String> stream64 = outputStream.getSideOutput(outputTag64);
        DataStream<String> stream65 = outputStream.getSideOutput(outputTag65);
        DataStream<String> stream66 = outputStream.getSideOutput(outputTag66);
        DataStream<String> stream67 = outputStream.getSideOutput(outputTag67);
        DataStream<String> stream68 = outputStream.getSideOutput(outputTag68);
        DataStream<String> stream69 = outputStream.getSideOutput(outputTag69);
        DataStream<String> stream70 = outputStream.getSideOutput(outputTag70);
        DataStream<String> stream71 = outputStream.getSideOutput(outputTag71);
        DataStream<String> stream72 = outputStream.getSideOutput(outputTag72);
        DataStream<String> stream73 = outputStream.getSideOutput(outputTag73);
        DataStream<String> stream74 = outputStream.getSideOutput(outputTag74);
        DataStream<String> stream75 = outputStream.getSideOutput(outputTag75);
        DataStream<String> stream76 = outputStream.getSideOutput(outputTag76);
        DataStream<String> stream77 = outputStream.getSideOutput(outputTag77);
        DataStream<String> stream78 = outputStream.getSideOutput(outputTag78);
        DataStream<String> stream79 = outputStream.getSideOutput(outputTag79);
        DataStream<String> stream80 = outputStream.getSideOutput(outputTag80);
        DataStream<String> stream81 = outputStream.getSideOutput(outputTag81);
        DataStream<String> stream82 = outputStream.getSideOutput(outputTag82);
        DataStream<String> stream83 = outputStream.getSideOutput(outputTag83);
        DataStream<String> stream84 = outputStream.getSideOutput(outputTag84);
        DataStream<String> stream85 = outputStream.getSideOutput(outputTag85);
        DataStream<String> stream86 = outputStream.getSideOutput(outputTag86);
        DataStream<String> stream87 = outputStream.getSideOutput(outputTag87);
        DataStream<String> stream88 = outputStream.getSideOutput(outputTag88);
        DataStream<String> stream89 = outputStream.getSideOutput(outputTag89);
        DataStream<String> stream90 = outputStream.getSideOutput(outputTag90);
        DataStream<String> stream91 = outputStream.getSideOutput(outputTag91);
        DataStream<String> stream92 = outputStream.getSideOutput(outputTag92);
        DataStream<String> stream93 = outputStream.getSideOutput(outputTag93);
        DataStream<String> stream94 = outputStream.getSideOutput(outputTag94);
        DataStream<String> stream95 = outputStream.getSideOutput(outputTag95);
        DataStream<String> stream96 = outputStream.getSideOutput(outputTag96);
        DataStream<String> stream97 = outputStream.getSideOutput(outputTag97);
        DataStream<String> stream98 = outputStream.getSideOutput(outputTag98);
        DataStream<String> stream99 = outputStream.getSideOutput(outputTag99);
        DataStream<String> stream100 = outputStream.getSideOutput(outputTag100);
        DataStream<String> stream101 = outputStream.getSideOutput(outputTag101);








        // sink
        // 以下条件满足其中之一就会滚动生成新的文件
        RollingPolicy<String, String> rollingPolicy =  OnCheckpointRollingPolicy.build();
//                .withRolloverInterval(60L * 1000L * 60L) //滚动写入新文件的时间，60min。根据具体情况调节
//                .withMaxPartSize(1024 * 1024 * 128L) //设置每个文件的最大大小 ,默认是128M，这里设置为128M
//                .withInactivityInterval(60L * 1000L * 60L) //60min,未写入数据处于不活跃状态超时会滚动新文件
//                .build();


        StreamingFileSink<String> sink102 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_biddingt_bidding_content_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink103 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_biddingt_bidding_info_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink104 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_biddingt_bidding_related_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink2 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_business_reproducet_biddings_all_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink3 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_business_reproducet_credit_info_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink4 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_business_reproducet_domains_alls_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink5 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_business_reproducet_restricted_consumer_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink6 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_business_reproducet_terminationcaseitem_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink7 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_businesst_abnormal_enterprises_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink8 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_businesst_creditimportexport_data_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink9 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_businesst_general_taxpayer_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink10 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_businesst_goudi_information_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink11 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_businesst_huge_tax_punishment_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink12 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_businesst_pay_taxes_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink13 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_ent_relationst_auctions_relations_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink14 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_ent_relationst_cases_relations_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink15 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_ent_relationst_copyrights_relations_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink16 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_ent_relationst_kaitinggonggaos_relations_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink17 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_ent_relationst_lawsuits_relations_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink18 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_ent_relationst_notices_relations_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink19 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_auctions_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink20 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_cases_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink21 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_executedpersons_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink22 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_executions_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink23 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_huanbaochufas_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink24 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_kaitinggonggaos_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink25 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_new_jobs_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink26 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_notices_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink27 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_othert_overduetaxs_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink28 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterprise_reportst_report_details_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink29 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_abnormal_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink30 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_address_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink31 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_administrative_punishment_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink32 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_branches_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink33 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_change_records_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink34 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_checkups_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink35 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_clear_account_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink36 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_double_checkups_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink37 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_emails_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink38 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_employees_alls_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink39 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_enterprise_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink40 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_equityquality_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink41 = StreamingFileSink
                //.forRowFormat(new Path("file:///E://city6"), new SimpleStringEncoder<String>("UTF-8"))
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_history_names_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink42 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_history_oper_names_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink43 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_history_regist_capis_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink44 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_judicial_freezes_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink45 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_knowledge_properties_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink46 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_license_info_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink47 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_mortgages_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink48 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_partners_alls_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink49 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_serious_illegal_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink50 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_simple_cancellation_announcement_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink51 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_social_security_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink52 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_telephones_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink53 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_enterpriset_websites_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink54 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_intellectual_propertyt_patents_new_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink55 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_intellectual_propertyt_patents_relations_new_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink56 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_intellectual_propertyt_trademarks_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink57 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_building_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink58 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_ccc_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink59 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_ccc_self_declaration_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink60 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_certificate_main_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink61 = StreamingFileSink
                //.forRowFormat(new Path("file:///E://city6"), new SimpleStringEncoder<String>("UTF-8"))
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_cesi_certification_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink62 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_computer_project_manager_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink63 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_cosmetic_produce_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink64 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_drug_business_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink65 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_drug_permit_cn_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink66 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_drug_produce_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink67 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_electronic_certification_agency_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink68 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_estate_qualification_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink69 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_firefighting_system_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink70 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_food_additives_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink71 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_food_business_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink72 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_food_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink73 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_food_product_ent_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink74 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_food_product_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink75 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_industry_produce_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink76 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_industry_volunteer_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink77 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_license_drug_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink78 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_management_system_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink79 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_medical_instruments_business_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink80 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_medical_instruments_import_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink81 = StreamingFileSink
                //.forRowFormat(new Path("file:///E://city6"), new SimpleStringEncoder<String>("UTF-8"))
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_medical_instruments_produce_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink82 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_medical_instruments_registered_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink83 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_mining_exploration_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink84 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_mining_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink85 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_network_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink86 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_product_safe_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink87 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_radio_customs_approve_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink88 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_radio_model_approve_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink89 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_service_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink90 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_sewage_permit_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink91 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_qualification_certificatet_telecommunications_license_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink92 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_sub_enterprisest_enterprise_investment_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink93 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_sub_enterprisest_last_industry_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink94 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_admin_division_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink95 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_currency_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink96 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_industry_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink97 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_job_title_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink98 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_country_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink99 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_econ_kind_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink100 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/db_codet_procedure_code_external_update"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();
        StreamingFileSink<String> sink101 = StreamingFileSink
                .forRowFormat(new Path("hdfs://cq-college-cdh-64-3:8020/ods/qxb_update/unknow"), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new EventTimeBucketAssigner())
                .withRollingPolicy(rollingPolicy)
                .withBucketCheckInterval(1000)  // 桶检查间隔，这里设置1S
                .build();



        stream102.addSink(sink102);
        stream103.addSink(sink103);
        stream104.addSink(sink104);
        stream2.addSink(sink2);
        stream3.addSink(sink3);
        stream4.addSink(sink4);
        stream5.addSink(sink5);
        stream6.addSink(sink6);
        stream7.addSink(sink7);
        stream8.addSink(sink8);
        stream9.addSink(sink9);
        stream10.addSink(sink10);
        stream11.addSink(sink11);
        stream12.addSink(sink12);
        stream13.addSink(sink13);
        stream14.addSink(sink14);
        stream15.addSink(sink15);
        stream16.addSink(sink16);
        stream17.addSink(sink17);
        stream18.addSink(sink18);
        stream19.addSink(sink19);
        stream20.addSink(sink20);
        stream21.addSink(sink21);
        stream22.addSink(sink22);
        stream23.addSink(sink23);
        stream24.addSink(sink24);
        stream25.addSink(sink25);
        stream26.addSink(sink26);
        stream27.addSink(sink27);
        stream28.addSink(sink28);
        stream29.addSink(sink29);
        stream30.addSink(sink30);
        stream31.addSink(sink31);
        stream32.addSink(sink32);
        stream33.addSink(sink33);
        stream34.addSink(sink34);
        stream35.addSink(sink35);
        stream36.addSink(sink36);
        stream37.addSink(sink37);
        stream38.addSink(sink38);
        stream39.addSink(sink39);
        stream40.addSink(sink40);
        stream41.addSink(sink41);
        stream42.addSink(sink42);
        stream43.addSink(sink43);
        stream44.addSink(sink44);
        stream45.addSink(sink45);
        stream46.addSink(sink46);
        stream47.addSink(sink47);
        stream48.addSink(sink48);
        stream49.addSink(sink49);
        stream50.addSink(sink50);
        stream51.addSink(sink51);
        stream52.addSink(sink52);
        stream53.addSink(sink53);
        stream54.addSink(sink54);
        stream55.addSink(sink55);
        stream56.addSink(sink56);
        stream57.addSink(sink57);
        stream58.addSink(sink58);
        stream59.addSink(sink59);
        stream60.addSink(sink60);
        stream61.addSink(sink61);
        stream62.addSink(sink62);
        stream63.addSink(sink63);
        stream64.addSink(sink64);
        stream65.addSink(sink65);
        stream66.addSink(sink66);
        stream67.addSink(sink67);
        stream68.addSink(sink68);
        stream69.addSink(sink69);
        stream70.addSink(sink70);
        stream71.addSink(sink71);
        stream72.addSink(sink72);
        stream73.addSink(sink73);
        stream74.addSink(sink74);
        stream75.addSink(sink75);
        stream76.addSink(sink76);
        stream77.addSink(sink77);
        stream78.addSink(sink78);
        stream79.addSink(sink79);
        stream80.addSink(sink80);
        stream81.addSink(sink81);
        stream82.addSink(sink82);
        stream83.addSink(sink83);
        stream84.addSink(sink84);
        stream85.addSink(sink85);
        stream86.addSink(sink86);
        stream87.addSink(sink87);
        stream88.addSink(sink88);
        stream89.addSink(sink89);
        stream90.addSink(sink90);
        stream91.addSink(sink91);
        stream92.addSink(sink92);
        stream93.addSink(sink93);
        stream94.addSink(sink94);
        stream95.addSink(sink95);
        stream96.addSink(sink96);
        stream97.addSink(sink97);
        stream98.addSink(sink98);
        stream99.addSink(sink99);
        stream100.addSink(sink100);
        stream101.addSink(sink101);

        env.execute("sink-job");
    }
}
