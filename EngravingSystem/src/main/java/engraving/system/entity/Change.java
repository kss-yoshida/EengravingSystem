package engraving.system.entity;



import javax.persistence.*;

@Entity
@Table(name="changeinfo")
public class Change {

//	変更履歴ID
	@Id
	private int changeId;
	
	private void setChangeId(int id){
		this.changeId=id;
	}
	
	public int getChangeId(){
		return this.changeId;
	}
	
//	変更対応管理者番号
	@Column(name="admin_id" ,nullable=false)
	private String adminId;
	
	public void setAdminId(String id) {
		this.adminId=id;
	}
	public String getAdminId() {
		return this.adminId;
	}
	
//	変更対象データ
	@Column(name="data_name" ,nullable=false)
	private String dataName;
	
	public void setDataName(String name) {
		this.dataName=name;
	}
	public String getDataName() {
		return this.dataName;
	}
	
//	変更前のデータ
	@Column(name="before_name" ,nullable=false)
	private String beforeData;
	
	public void setBeforeData(String data) {
		this.beforeData=data;
	}
	public String getBeforeData() {
		return this.beforeData;
	}
	
//	変更後のデータ
	@Column(name="after_data" ,nullable=false)
	private String afterData;
	
	public void setAfterData(String data) {
		this.afterData=data;
	}
	public String getAfterData() {
		return this.afterData;
	}
//	変更対象社員番号
	@Column(name="employee_id" ,nullable=false)
	private String employeeId;
	
	public void setEmployeeId(String id) {
		this.employeeId=id;
	}
	public String getEmployeeId() {
		return this.employeeId;
	}
	
//	変更日時
	@Column(name="is_updated" ,nullable=false)
	private String isUpdated;
	
	public void setIsUpdated(String date) {
		this.isUpdated=date;
	}
	public String getIsUpdated() {
		return this.isUpdated;
	}
}
