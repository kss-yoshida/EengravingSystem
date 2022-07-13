package engraving.system.entity;



import javax.persistence.*;

@Entity
@Table(name="changeinfo")
public class Change {

//	変更履歴ID
	@Id
	private int change_id;
	
	private void setChange_id(int id){
		this.change_id=id;
	}
	
	public int getChange_id(){
		return this.change_id;
	}
	
//	変更対応管理者番号
	@Column(name="admin_id" ,nullable=false)
	private int admin_id;
	
	public void setAdmin_id(int id) {
		this.admin_id=id;
	}
	public int getAdmin_id() {
		return this.admin_id;
	}
	
//	変更対象データ
	@Column(name="data_name" ,nullable=false)
	private String data_name;
	
	public void setData_name(String name) {
		this.data_name=name;
	}
	public String getData_name() {
		return this.data_name;
	}
	
//	変更前のデータ
	@Column(name="before_name" ,nullable=false)
	private String before_data;
	
	public void setBefore_data(String data) {
		this.before_data=data;
	}
	public String getBefore_data() {
		return this.before_data;
	}
	
//	変更後のデータ
	@Column(name="after_data" ,nullable=false)
	private String after_data;
	
	public void setafter_data(String data) {
		this.after_data=data;
	}
	public String getAfter_data() {
		return this.after_data;
	}
//	変更対象社員番号
	@Column(name="employee_id" ,nullable=false)
	private int employee_id;
	
	public void setEmployee_id(int id) {
		this.employee_id=id;
	}
	public int getEmployee_id() {
		return this.employee_id;
	}
	
//	変更日時
	@Column(name="is_updated" ,nullable=false)
	private String is_updated;
	
	public void setIs_updated(String date) {
		this.is_updated=date;
	}
	public String getIs_updated() {
		return this.is_updated;
	}
}
