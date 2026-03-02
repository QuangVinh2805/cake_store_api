package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.Tag;

import java.util.List;
import java.util.Optional;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByTagNameIgnoreCase(String tagName);

    boolean existsByTagNameIgnoreCaseAndIdNot(String tagName, Long id);

    @Query(value = "select * from tag where status = 1",nativeQuery = true)
    List<Tag> findAllByStatus();


    boolean existsByTagName(String tagName);


    Tag findByTagName(String tagName);




}
