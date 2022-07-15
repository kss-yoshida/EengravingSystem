package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import engraving.system.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}