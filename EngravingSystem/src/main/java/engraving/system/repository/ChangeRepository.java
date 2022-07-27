package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import engraving.system.entity.Change;

@Repository
public interface ChangeRepository extends JpaRepository<Change, String> {
	//ユーザーIDで検索する
	public ArrayList<Change> findByEmployeeIdLike(String employeeId);
	
	//管理者IDで検索する
	public ArrayList<Change> findByAdminIdLike(String adminId);
	
	//ユーザーIDと管理者IDで検索する
	public ArrayList<Change> findByEmployeeIdAndAdminIdLike(String employeeId, String adminId);
	
	
}