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

    @Query(value = """
    SELECT
        p.hash_id,
        p.name,
        p.image,
        p.price,
        p.category_id,
        c.name as category_name,
        p.status,
        GROUP_CONCAT(t.tag SEPARATOR ',') as tags
    FROM product p
    LEFT JOIN product_tag t ON p.id = t.product_id
    LEFT JOIN category c ON p.category_id = c.id
    GROUP BY p.id
""", nativeQuery = true)
    List<Object[]> findAllWithGroupedTags();





    List<ProductTag> findAllByProductId_IdAndTagIn(
            Long productId,
            List<String> tags
    );

    boolean existsByProductId_IdAndTag(Long productId, String tag);




}
