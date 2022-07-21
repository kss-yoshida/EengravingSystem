package engraving.system.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import engraving.system.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	public ArrayList<User> findByEmployeeId(int id);

	public ArrayList<User> findByName(String name);

	public ArrayList<User> findByEmployeeIdAndName(int id, String name);

}
