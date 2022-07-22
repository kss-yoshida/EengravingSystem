package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import engraving.system.entity.Attendance;

import java.util.*;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

	// 退勤打刻時間とそれに付随するデータを更新する処理
	public ArrayList<Attendance> findByDayAndEmployeeId(String day, String string);

	// 対象のユーザーの勤怠情報を受け取る処理
	public ArrayList<Attendance> findByEmployeeIdAndDayLike(String employeeId, String day);
}
