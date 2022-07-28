package engraving.system.entity;

import javax.persistence.*;

@Entity
public class RequestDay {

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
	private String employeeId;
	
	public void setEmployeeId(String id) {
		this.employeeId=id;
	}
	public String getEmployeeId() {
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
	
	//日付
	@Column(name="day" , nullable=false)
	private String day;
	
	public void setDay(String day) {
		this.day = day;
	}
	
	public String getDay() {
		return day;
	}
	
//	変更前出勤時間
	@Column(nullable=true)
	private String beforeStartTime;
	public void setBeforeStartTime(String time) {
		this.beforeStartTime=time;
	}
	public String getBeforeStartTime() {
		return this.beforeStartTime;
	}
	
//	変更前退勤時間
	@Column(nullable=true)
	private String beforeFinishTime;
	public void setBeforeFinishTime(String time) {
		this.beforeFinishTime=time;
	}
	public String getBeforeFinishTime() {
		return this.beforeFinishTime;
	}
	
//	変更希望出勤時間
	@Column(name="change_start_time" ,nullable=true)
	private String changeStartTime;
	public void setChangeStarTime(String time) {
		this.changeStartTime=time;
	}
	public String getChangeStartTime() {
		return this.changeStartTime;
	}
	
//	変更希望退勤時間
	@Column(name="change_finish_time" ,nullable=true)
	private String changeFinishTime;
	public void setChangeFinishTime(String time) {
		this.changeFinishTime=time;
	}
	public String getChangeFinishTime() {
		return this.changeFinishTime;
	}
	
//	変更希望理由
	@Column(name="comment" ,nullable=false)
	private String comment;
	public void setComment(String comment) {
		this.comment=comment;
	}
	public String getComment() {
		return this.comment;
	}

}
