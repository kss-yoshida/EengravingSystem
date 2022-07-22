package engraving.system.entity;

import javax.persistence.*;

@Entity
@Table(name="attendanceinfo")
public class Attendance {
	
	//勤怠ID
	@Id
	private int attendanceId;
	
	public int getAttendanseId() {
		return attendanceId;
	}
	
	public void setAttendanceId(int attendanceId) {
		this.attendanceId = attendanceId;
	}
	
	//社員番号
	@Column(nullable = false)
	private String employeeId;
	
	public String getEmployeeId() {
		return employeeId;
	}
	
	public void setEmployeeId(String string) {
		this.employeeId = string;
	}
	
	//日付
	@Column(nullable = false)
	private String day;
	
	public String getDay() {
		return day;
	}
	
	public void setDay(String day) {
		this.day = day;
	}
	
	//出勤時間
	@Column(nullable = false)
	private String startTime;
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	//退勤時間
	private String finishTime;
	
	public String getFinishTime() {
		return finishTime;
	}
	
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
	
	//出勤時打刻時間
	@Column(nullable = false)
	private String startEngrave;
	
	public String getStartEngrave() {
		return startEngrave;
	}
	
	public void setStartEngrave(String startEngrave) {
		this.startEngrave = startEngrave;
	}
	
	//退勤時打刻時間
	private String finishEngrave;
	
	public String getFinishEngrave() {
		return finishEngrave;
	}
	
	public void setFinishEngrave(String finishEngrave) {
		this.finishEngrave = finishEngrave;
	}
	
	//休憩時間
	private String breakTime;
	
	public String getBreakTime() {
		return breakTime;
	}
	
	public void setBreakTime(String breakTime) {
		this.breakTime = breakTime;
	}
	
	//残業時間
	private String overTime;
	
	public String getOverTime() {
		return overTime;
	}
	
	public void setOverTime(String overTime) {
		this.overTime = overTime;
	}
}
