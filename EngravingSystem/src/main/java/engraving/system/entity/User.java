package engraving.system.entity;

import javax.persistence.*;

@Entity
@Table(name="userinfo")
public class User{
	
	//employee
	@Id
	private int employeeId;
	
	public int getEmployeeId() {
		return employeeId;
	}
	
	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}
	
	//name
	@Column(length = 100, nullable = false)
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	//email
	@Column(length = 100, nullable = false)
	private String email;
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	//password
	@Column(length = 100, nullable = false)
	private String password;
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	//写真
	@Column(length = 100)
	private String photo;
	
	public String getPhoto() {
		return photo;
	}
	
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	
	//権限
	@Column(name = "authority",length = 1,nullable = false)
	private String authority;
	
	public String getAuthorirty() {
		return authority;
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	//削除フラグ
	@Column(nullable = false)
	private boolean isDeleted;
	
	public boolean getIsDeleted() {
		return isDeleted;
	}
	
	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
