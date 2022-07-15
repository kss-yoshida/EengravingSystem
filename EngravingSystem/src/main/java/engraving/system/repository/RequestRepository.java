package engraving.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import engraving.system.entity.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

}