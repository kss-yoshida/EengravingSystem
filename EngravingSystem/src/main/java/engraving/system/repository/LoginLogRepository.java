package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import engraving.system.entity.LoginLog;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, String> {

  	public ArrayList<LoginLog> findByEmployeeIdLikeAndLoginTimeLike(String employeeId, String loginTime);
  
}
