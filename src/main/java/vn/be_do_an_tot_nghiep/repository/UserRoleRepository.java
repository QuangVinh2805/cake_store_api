package vn.be_do_an_tot_nghiep.repository;

import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.UserRole;


@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
