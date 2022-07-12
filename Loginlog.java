package engraving.entity;

import javax.persistence.*;

@Entity

@Table(name = "loginlog")
public class Loginlog {

	// ログインID
	@Id
	@Column(name = "login_id")
	private int login_id;

	public int getLogin_id() {
		return login_id;
	}

	public void setLogin_id(int login_id) {
		this.login_id = login_id;
	}

	// 社員番号
	@Column(name = "employee_id")
	private int employee_id;

	public void setEmployee_id(int id) {
		this.employee_id = id;
	}

	public int getEmployee_id() {
		return this.employee_id;
	}

	// ログイン日時
	@Column(name = "login_time")
	private String login_time;

	public String getLogin_time() {
		return login_time;
	}

	public void setLogin_time(String login_time) {
		this.login_time = login_time;
	}
}