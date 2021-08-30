package sink;

public class Transaction {
    public String op_es;
    public String op_type;
    public String op_table_name;
    public String _id;
    public String ex_id;
    public String name;
    public String oper_name;
    public String sex;
    public String age;
    public String number;
    public String court;
    public String type;
    public String province;
    public String doc_number;
    public String date;
    public String case_number;
    public String ex_department;
    public String final_duty;
    public String execution_status;
    public String execution_desc;
    public String publish_date;
    public String concern_count;
    public String ops_flag;
    public String relatives;
    public String is_related;
    public String related_companies;
    public String u_tags;
    public String amount;
    public String source;
    public String eid;
    public String create_date;
    public String import_date;
    public String last_update_time;
    public String created_time;
    public String row_update_time;
    public String pid;
    public String p_eid;
    public String p_ename;
    public String status;
    public String case_relation;

    public Transaction(String op_es, String op_type, String op_table_name, String _id, String ex_id, String name, String oper_name, String sex, String age, String number, String court, String type, String province, String doc_number, String date, String case_number, String ex_department, String final_duty, String execution_status, String execution_desc, String publish_date, String concern_count, String ops_flag, String relatives, String is_related, String related_companies, String u_tags, String amount, String source, String eid, String create_date, String import_date, String last_update_time, String created_time, String row_update_time, String pid, String p_eid, String p_ename, String status, String case_relation) {
        this.op_es = op_es;
        this.op_type = op_type;
        this.op_table_name = op_table_name;
        this._id = _id;
        this.ex_id = ex_id;
        this.name = name;
        this.oper_name = oper_name;
        this.sex = sex;
        this.age = age;
        this.number = number;
        this.court = court;
        this.type = type;
        this.province = province;
        this.doc_number = doc_number;
        this.date = date;
        this.case_number = case_number;
        this.ex_department = ex_department;
        this.final_duty = final_duty;
        this.execution_status = execution_status;
        this.execution_desc = execution_desc;
        this.publish_date = publish_date;
        this.concern_count = concern_count;
        this.ops_flag = ops_flag;
        this.relatives = relatives;
        this.is_related = is_related;
        this.related_companies = related_companies;
        this.u_tags = u_tags;
        this.amount = amount;
        this.source = source;
        this.eid = eid;
        this.create_date = create_date;
        this.import_date = import_date;
        this.last_update_time = last_update_time;
        this.created_time = created_time;
        this.row_update_time = row_update_time;
        this.pid = pid;
        this.p_eid = p_eid;
        this.p_ename = p_ename;
        this.status = status;
        this.case_relation = case_relation;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "op_es='" + op_es + '\'' +
                ", op_type='" + op_type + '\'' +
                ", op_table_name='" + op_table_name + '\'' +
                ", _id='" + _id + '\'' +
                ", ex_id='" + ex_id + '\'' +
                ", name='" + name + '\'' +
                ", oper_name='" + oper_name + '\'' +
                ", sex='" + sex + '\'' +
                ", age='" + age + '\'' +
                ", number='" + number + '\'' +
                ", court='" + court + '\'' +
                ", type='" + type + '\'' +
                ", province='" + province + '\'' +
                ", doc_number='" + doc_number + '\'' +
                ", date='" + date + '\'' +
                ", case_number='" + case_number + '\'' +
                ", ex_department='" + ex_department + '\'' +
                ", final_duty='" + final_duty + '\'' +
                ", execution_status='" + execution_status + '\'' +
                ", execution_desc='" + execution_desc + '\'' +
                ", publish_date='" + publish_date + '\'' +
                ", concern_count='" + concern_count + '\'' +
                ", ops_flag='" + ops_flag + '\'' +
                ", relatives='" + relatives + '\'' +
                ", is_related='" + is_related + '\'' +
                ", related_companies='" + related_companies + '\'' +
                ", u_tags='" + u_tags + '\'' +
                ", amount='" + amount + '\'' +
                ", source='" + source + '\'' +
                ", eid='" + eid + '\'' +
                ", create_date='" + create_date + '\'' +
                ", import_date='" + import_date + '\'' +
                ", last_update_time='" + last_update_time + '\'' +
                ", created_time='" + created_time + '\'' +
                ", row_update_time='" + row_update_time + '\'' +
                ", pid='" + pid + '\'' +
                ", p_eid='" + p_eid + '\'' +
                ", p_ename='" + p_ename + '\'' +
                ", status='" + status + '\'' +
                ", case_relation='" + case_relation + '\'' +
                '}';
    }

    public String getOp_es() {
        return op_es;
    }

    public String getOp_type() {
        return op_type;
    }

    public String getOp_table_name() {
        return op_table_name;
    }

    public String get_id() {
        return _id;
    }

    public String getEx_id() {
        return ex_id;
    }

    public String getName() {
        return name;
    }

    public String getOper_name() {
        return oper_name;
    }

    public String getSex() {
        return sex;
    }

    public String getAge() {
        return age;
    }

    public String getNumber() {
        return number;
    }

    public String getCourt() {
        return court;
    }

    public String getType() {
        return type;
    }

    public String getProvince() {
        return province;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public String getDate() {
        return date;
    }

    public String getCase_number() {
        return case_number;
    }

    public String getEx_department() {
        return ex_department;
    }

    public String getFinal_duty() {
        return final_duty;
    }

    public String getExecution_status() {
        return execution_status;
    }

    public String getExecution_desc() {
        return execution_desc;
    }

    public String getPublish_date() {
        return publish_date;
    }

    public String getConcern_count() {
        return concern_count;
    }

    public String getOps_flag() {
        return ops_flag;
    }

    public String getRelatives() {
        return relatives;
    }

    public String getIs_related() {
        return is_related;
    }

    public String getRelated_companies() {
        return related_companies;
    }

    public String getU_tags() {
        return u_tags;
    }

    public String getAmount() {
        return amount;
    }

    public String getSource() {
        return source;
    }

    public String getEid() {
        return eid;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getImport_date() {
        return import_date;
    }

    public String getLast_update_time() {
        return last_update_time;
    }

    public String getCreated_time() {
        return created_time;
    }

    public String getRow_update_time() {
        return row_update_time;
    }

    public String getPid() {
        return pid;
    }

    public String getP_eid() {
        return p_eid;
    }

    public String getP_ename() {
        return p_ename;
    }

    public String getStatus() {
        return status;
    }

    public String getCase_relation() {
        return case_relation;
    }

}
