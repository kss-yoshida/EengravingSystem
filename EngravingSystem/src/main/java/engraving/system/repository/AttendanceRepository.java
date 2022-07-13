package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import engraving.system.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

}