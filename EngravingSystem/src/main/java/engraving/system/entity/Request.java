package engravingsytem.entity;



import javax.persistence.*;

@Entity
@Table(name="requestinfo")
public class Request {
//	リクエストID
	@Id
	@Column(name="request_id")
	private int request_id;
	
	public void setRequest_id(int id) {
		this.request_id=id;
	}
	public int getRequest_id() {
		return this.request_id;
	}
	
//	社員番号
	@Column(name="employee_id" ,nullable=false)
	private int employee_id;
	
	public void setEmployee_id(int id) {
		this.employee_id=id;
	}
	public int getEmployee_id() {
		return this.employee_id;
	}
	
//	変更希望の勤怠情報ID
	@Column(name="attendance_id" ,nullable=false)
	private int attendance_id;
	public void setAttendance_id(int id) {
		this.attendance_id=id;
	}
	public int getAttendance_id() {
		return this.attendance_id;
	}
	
//	変更希望出勤時間
	@Column(name="change_start_engrave" ,nullable=true)
	private String change_start_engrave;
	public void setChange_start_engrave(String time) {
		this.change_start_engrave=time;
	}
	public String getChange_start_engrave() {
		return this.change_start_engrave;
	}
	
//	変更希望退勤時間
	@Column(name="change_finish_engrave" ,nullable=true)
	private String change_finish_engrave;
	public void setChange_finish_engrave(String time) {
		this.change_finish_engrave=time;
	}
	public String getChange_finish_engrave() {
		return this.change_finish_engrave;
	}
	
//	変更希望理由
	@Column(name="comment" ,nullable=false)
	private String comment;
	public void setComment(String comment) {
		this.comment=comment;
	}
	public String getCommetn() {
		return this.comment;
	}

	
//	削除フラグ
	@Column(name="is_deleted" ,nullable=false)
	private Boolean is_deleted;
	public void setIs_deleted(Boolean flag) {
		this.is_deleted=flag;
	}
	public Boolean getIs_deleted() {
		return this.is_deleted;
	}

}
