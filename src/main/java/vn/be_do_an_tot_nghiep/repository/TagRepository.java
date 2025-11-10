package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.Tag;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

}
