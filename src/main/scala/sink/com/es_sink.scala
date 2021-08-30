package sink.com

import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}
import org.apache.flink.types._
import sink.com.Source2.mysql_sources16

object es_sink {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment//.enableCheckpointing( 30 * 1000)

    //env.setStateBackend(new HashMapStateBackend())
   val checkpointConfig = env.getCheckpointConfig
//    checkpointConfig.setMinPauseBetweenCheckpoints(30 * 1000)
//    checkpointConfig.setCheckpointTimeout(30 * 1000)
    checkpointConfig.setCheckpointStorage("")
    new EmbeddedRocksDBStateBackend(true);
    //checkpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    val tableEnv = StreamTableEnvironment.create(env)

    tableEnv.executeSql(Source.source_mysql0)
    tableEnv.executeSql(Source.source_mysql1)
    tableEnv.executeSql(Source.source_mysql2)
    tableEnv.executeSql(Source.source_mysql3)
    tableEnv.executeSql(Source.source_mysql4)
    tableEnv.executeSql(Source.source_mysql5)
    tableEnv.executeSql(Source.source_mysql6)
    tableEnv.executeSql(Source.source_mysql7)
    tableEnv.executeSql(Source.source_mysql8)
    tableEnv.executeSql(Source.source_mysql9)
    tableEnv.executeSql(Source.source_mysql10)
    tableEnv.executeSql(Source.source_mysql11)
    tableEnv.executeSql(Source.source_mysql12)
    tableEnv.executeSql(Source.source_mysql13)
    tableEnv.executeSql(Source.source_mysql14)
    tableEnv.executeSql(Source.source_mysql15)
    tableEnv.executeSql(Source2.mysql_sources16)
    tableEnv.executeSql(Source2.mysql_sources17)
    tableEnv.executeSql(Source2.mysql_sources18)
    tableEnv.executeSql(Source2.mysql_sources19)
    tableEnv.executeSql(Source2.mysql_sources20)
    tableEnv.executeSql(Source2.mysql_sources21)
    tableEnv.executeSql(Source2.mysql_sources22)
    tableEnv.executeSql(Source2.mysql_sources23)
    tableEnv.executeSql(Source2.mysql_sources24)
    tableEnv.executeSql(Source2.mysql_sources25)
    tableEnv.executeSql(Source2.mysql_sources26)
    tableEnv.executeSql(Source2.mysql_sources27)
    tableEnv.executeSql(Source2.mysql_sources28)
    tableEnv.executeSql(Source2.mysql_sources29)
    tableEnv.executeSql(Source2.mysql_sources30)
    tableEnv.executeSql(Source2.mysql_sources31)





    val es_sink =
      """
        |CREATE TABLE es (
        |        eid string,
        |        id bigint ,
        |        outhor_num string ,
        |        outhor_date string ,
        |        authorize_num string ,
        |        authorize_date string ,
        |        patent_name string ,
        |        request_num string ,
        |        request_num_standard string,
        |        request_date string ,
        |        outhor_change_pub_date string ,
        |        outhor_change_pub_no string ,
        |        authorize_change_pub_date string ,
        |        authorize_change_pub_no string ,
        |        decrypt_pub_date string ,
        |        type string ,
        |        type_name string ,
        |        authorize_tag string ,
        |        category_num_ipc string ,
        |        brief string ,
        |        patent_person_ori string,
        |        patent_person string ,
        |        app_person string ,
        |        designer string ,
        |        agent_people string ,
        |        agent string,
        |        agent_info_eid string ,
        |        pct_req_data string ,
        |        pct_pub_data string ,
        |        pct_date string ,
        |        priority string ,
        |        priority_local string ,
        |        qrcode string ,
        |        address string ,
        |        zipcode string ,
        |        status string ,
        |        last_status string,
        |        compare_files string,
        |        patent_img string ,
        |        org_patent_img string ,
        |        annex string ,
        |        related_companies string ,
        |        source string ,
        |        url string ,
        |        u_tags bigint ,
        |     is_history bigint ,
        |  create_time bigint ,
        |  row_update_time string,
        |  PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        |  'connector' = 'elasticsearch-7',
        |  'hosts' = 'http://10.255.65.253:9201',
        |  'index' = 't_patents',
        |  'format' = 'json'
        |)
        |""".stripMargin
    tableEnv.executeSql(es_sink)

    val view1 =
      """
        |create view view1 (id,outhor_num,outhor_date,authorize_num,authorize_date,patent_name,request_num,request_num_standard,request_date,outhor_change_pub_date,outhor_change_pub_no,authorize_change_pub_date,authorize_change_pub_no,decrypt_pub_date,type,type_name,authorize_tag,category_num_ipc,brief,patent_person_ori,patent_person,app_person,designer,agent_people,agent,agent_info_eid,pct_req_data,pct_pub_data,pct_date,priority,priority_local,qrcode,address,zipcode,status,last_status,compare_files,patent_img,org_patent_img,annex,related_companies,source,url,u_tags,is_history,create_time,row_update_time) as
        |select * from t_patents_new_0
        |union all
        |select * from t_patents_new_1
        |union all
        |select * from t_patents_new_2
        |union all
        |select * from t_patents_new_3
        |union all
        |select * from t_patents_new_4
        |union all
        |select * from t_patents_new_5
        |union all
        |select * from t_patents_new_6
        |union all
        |select * from t_patents_new_7
        |union all
        |select * from t_patents_new_8
        |union all
        |select * from t_patents_new_9
        |union all
        |select * from t_patents_new_10
        |union all
        |select * from t_patents_new_11
        |union all
        |select * from t_patents_new_12
        |union all
        |select * from t_patents_new_13
        |union all
        |select * from t_patents_new_14
        |union all
        |select * from t_patents_new_15
        |""".stripMargin
    tableEnv.executeSql(view1)

   val view2 =
    """
      |create view view2 (id,_id,eid,ename,role,history_role_tag,type,type_name,patent_name,authorize_tag,outhor_num,outhor_date,authorize_num,authorize_date,outhor_change_pub_date,outhor_change_pub_no,authorize_change_pub_date,authorize_change_pub_no,request_num_standard,brief,patent_person_ori,patent_person,app_person,agent,agent_info_eid,category_num_ipc,request_date,status,last_status,related_companies,source,url,u_tags,is_history,create_time,row_update_time
) as
      |select * from t_patents_relations_new_0
      |union all
      |select * from t_patents_relations_new_1
      |union all
      |select * from t_patents_relations_new_2
      |union all
      |select * from t_patents_relations_new_3
      |union all
      |select * from t_patents_relations_new_4
      |union all
      |select * from t_patents_relations_new_5
      |union all
      |select * from t_patents_relations_new_6
      |union all
      |select * from t_patents_relations_new_7
      |union all
      |select * from t_patents_relations_new_8
      |union all
      |select * from t_patents_relations_new_9
      |union all
      |select * from t_patents_relations_new_10
      |union all
      |select * from t_patents_relations_new_11
      |union all
      |select * from t_patents_relations_new_12
      |union all
      |select * from t_patents_relations_new_13
      |union all
      |select * from t_patents_relations_new_14
      |union all
      |select * from t_patents_relations_new_15
      |""".stripMargin
   tableEnv.executeSql(view2)

    val sinksql =
      """
        |insert into es
        |select b.eid,a.* from view1  as a
        |left join view2 b
        |on
        |a.request_num_standard = b.request_num_standard
        |""".stripMargin



    tableEnv.executeSql(sinksql)

  }
}
