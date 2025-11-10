package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.ProductTag;
import vn.be_do_an_tot_nghiep.response.ProductTagResponse;

import java.util.List;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    List<ProductTag> findByTagId_Id(Long tagId);

    @Query("""
    SELECT new vn.be_do_an_tot_nghiep.response.ProductTagResponse(
        t.tag,
        p.hashId,
        p.name,
        p.image,
        p.price,
        p.category.id
    )
    FROM Product p
    LEFT JOIN ProductTag t ON p.id = t.productId.id
""")
    List<ProductTagResponse> findAllWithTags();


}
