package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import engraving.system.entity.Change;

@Repository
public interface ChangeRepository extends JpaRepository<Change, String> {

}