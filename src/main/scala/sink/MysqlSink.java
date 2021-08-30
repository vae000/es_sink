package sink;


import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.configuration.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;


public class MysqlSink extends RichSinkFunction<Transaction>{
    private PreparedStatement state ;
    private Connection conn ;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        conn = getConnection();
        String sql = "insert into t_executions(op_es,op_type,op_table_name,_id,ex_id,name,oper_name,sex,age,number,court,type,province,doc_number,date,case_number,ex_department,final_duty,execution_status,execution_desc,publish_date,concern_count,ops_flag,relatives,is_related,related_companies,u_tags,amount,source,eid,create_date,import_date,last_update_time,created_time,row_update_time,pid,p_eid,p_ename,status,case_relation) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        state = this.conn.prepareStatement(sql);
    }
    @Override
    public void close() throws Exception {
        super.close();
        if (conn != null) {
            conn.close();
        }
        if (state != null) {
            state.close();
        }
    }

    @Override
    public void invoke(Transaction value, Context context) throws Exception {
        state.setString(1,value.getOp_es());
        state.setString(2,value.getOp_type());
        state.setString(3,value.getOp_table_name());
        state.setString(4,value.get_id());
        state.setString(5,value.getEx_id() );
        state.setString(6,value.getName());
        state.setString(7,value.getOper_name());
        state.setString(8,value.getSex());
        state.setString(9,value.getAge());
        state.setString(10,value.getNumber());
        state.setString(11,value.getCourt());
        state.setString(12,value.getType());
        state.setString(13,value.getProvince());
        state.setString(14,value.getDoc_number());
        state.setString(15,value.getDate());
        state.setString(16,value.getCase_number());
        state.setString(17,value.getEx_department());
        state.setString(18,value.getFinal_duty());
        state.setString(19,value.getExecution_status());
        state.setString(20,value.getExecution_desc());
        state.setString(21,value.getPublish_date());
        state.setString(22,value.getConcern_count());
        state.setString(23,value.getOps_flag());
        state.setString(24,value.getRelatives());
        state.setString(25,value.getIs_related());
        state.setString(26,value.getRelated_companies());
        state.setString(27,value.getU_tags());
        state.setString(28,value.getAmount());
        state.setString(29,value.getSource());
        state.setString(30,value.getEid());
        state.setString(31,value.getCreate_date());
        state.setString(32,value.getImport_date());
        state.setString(33,value.getLast_update_time());
        state.setString(34,value.getCreate_date());
        state.setString(35,value.getRow_update_time());
        state.setString(36,value.getPid());
        state.setString(37,value.getP_eid());
        state.setString(38,value.getP_ename());
        state.setString(39,value.getStatus());
        state.setString(40,value.getCase_relation());
        state.executeUpdate();
    }

    private static Connection getConnection() {
        Connection conn = null;
        try {
            String jdbc = "com.mysql.jdbc.Driver";
            Class.forName(jdbc);
            String url = "jdbc:mysql://192.168.34.126:3306/test";
            String user = "root";
            String password = "root";
            conn = DriverManager.getConnection(url +
                            "?useUnicode=true" +
                            "&useSSL=false" +
                            "&characterEncoding=UTF-8" +
                            "&serverTimezone=UTC",
                    user,
                    password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

}
