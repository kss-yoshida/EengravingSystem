package engraving.system.entity;



import javax.persistence.*;

@Entity
@Table(name="requestinfo")
public class Request {
//	リクエストID
	@Id
	@Column(name="request_id")
	private int requestId;
	
	public void setRequestId(int id) {
		this.requestId=id;
	}
	public int getRequestId() {
		return this.requestId;
	}
	
//	社員番号
	@Column(name="employee_id" ,nullable=false)
	private int employeeId;
	
	public void setEmployeeId(int id) {
		this.employeeId=id;
	}
	public int getEmployeeId() {
		return this.employeeId;
	}
	
//	変更希望の勤怠情報ID
	@Column(name="attendance_id" ,nullable=false)
	private int attendanceId;
	public void setAttendanceId(int id) {
		this.attendanceId=id;
	}
	public int getAttendanceId() {
		return this.attendanceId;
	}
	
//	変更希望出勤時間
	@Column(name="change_start_engrave" ,nullable=true)
	private String changeStartEngrave;
	public void setChangeStartEngrave(String time) {
		this.changeStartEngrave=time;
	}
	public String getChange_start_engrave() {
		return this.changeStartEngrave;
	}
	
//	変更希望退勤時間
	@Column(name="change_finish_engrave" ,nullable=true)
	private String changeFinishEngrave;
	public void setChangeFinishEngrave(String time) {
		this.changeFinishEngrave=time;
	}
	public String getChangeFinishEngrave() {
		return this.changeFinishEngrave;
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
	private Boolean isDeleted;
	public void setIsDeleted(Boolean flag) {
		this.isDeleted=flag;
	}
	public Boolean getIseleted() {
		return this.isDeleted;
	}

}
