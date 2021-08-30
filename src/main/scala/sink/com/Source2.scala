package sink.com

object Source2 {
  def main(args: Array[String]): Unit = {
  }

  def mysql_sources16 : String  =  {
    val mysql_sources16  =
      """
        |CREATE TABLE t_patents_relations_new_0 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_0',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
         mysql_sources16
  }
  def mysql_sources17 : String  =  {
    val mysql_sources17  =
      """
        |CREATE TABLE t_patents_relations_new_1 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_1',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources17
  }
  def mysql_sources18 : String  =  {
    val mysql_sources18  =
      """
        |CREATE TABLE t_patents_relations_new_2 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_2',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources18
  }

  def mysql_sources19 : String  =  {
    val mysql_sources19  =
      """
        |CREATE TABLE t_patents_relations_new_3 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_3',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources19
  }
  def mysql_sources20 : String  =  {
    val mysql_sources20  =
      """
        |CREATE TABLE t_patents_relations_new_4 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_4',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources20
  }
  def mysql_sources21 : String  =  {
    val mysql_sources21  =
      """
        |CREATE TABLE t_patents_relations_new_5 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_5',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources21
  }
  def mysql_sources22 : String  =  {
    val mysql_sources22  =
      """
        |CREATE TABLE t_patents_relations_new_6 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_6',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources22
  }
  def mysql_sources23 : String  =  {
    val mysql_sources23  =
      """
        |CREATE TABLE t_patents_relations_new_7 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_7',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources23
  }
  def mysql_sources24 : String  =  {
    val mysql_sources24  =
      """
        |CREATE TABLE t_patents_relations_new_8 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_8',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources24
  }
  def mysql_sources25 : String  =  {
    val mysql_sources25  =
      """
        |CREATE TABLE t_patents_relations_new_9 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_9',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources25
  }
  def mysql_sources26 : String  =  {
    val mysql_sources26  =
      """
        |CREATE TABLE t_patents_relations_new_10 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_10',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources26
  }
  def mysql_sources27 : String  =  {
    val mysql_sources27  =
      """
        |CREATE TABLE t_patents_relations_new_11 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_11',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources27
  }
  def mysql_sources28 : String  =  {
    val mysql_sources28  =
      """
        |CREATE TABLE t_patents_relations_new_12 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_12',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources28
  }
  def mysql_sources29 : String  =  {
    val mysql_sources29  =
      """
        |CREATE TABLE t_patents_relations_new_13 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_13',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources29
  }
  def mysql_sources30 : String  =  {
    val mysql_sources30  =
      """
        |CREATE TABLE t_patents_relations_new_14 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_14',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources30
  }
  def mysql_sources31 : String  =  {
    val mysql_sources31  =
      """
        |CREATE TABLE t_patents_relations_new_15 (
        |id bigint,
        |_id string,
        |eid string,
        |ename string,
        |role string,
        |history_role_tag string,
        |type string,
        |type_name string,
        |patent_name string,
        |authorize_tag string,
        |outhor_num string,
        |outhor_date string,
        |authorize_num string,
        |authorize_date string,
        |outhor_change_pub_date string,
        |outhor_change_pub_no string,
        |authorize_change_pub_date string,
        |authorize_change_pub_no string,
        |request_num_standard string,
        |brief string,
        |patent_person_ori string,
        |patent_person string,
        |app_person string,
        |agent string,
        |agent_info_eid string,
        |category_num_ipc string,
        |request_date string,
        |status string,
        |last_status string,
        |related_companies string,
        |source string,
        |url string,
        |u_tags bigint,
        |is_history bigint,
        |create_time bigint,
        |row_update_time string,
        |PRIMARY KEY (request_num_standard) NOT ENFORCED
        |) WITH (
        | 'connector' = 'mysql-cdc',
        | 'hostname' = '10.255.65.84',
        | 'port' = '3306',
        | 'username' = 'app',
        | 'password' = 'Syx@123456',
        | 'database-name' = 'db_intellectual_property',
        | 'table-name' = 't_patents_relations_new_15',
        | 'scan.incremental.snapshot.enabled' = 'false'
        |)
        |""".stripMargin
    mysql_sources31
  }

}
