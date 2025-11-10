package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.Product;
import vn.be_do_an_tot_nghiep.response.ProductTagResponse;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByHashId(String hashId);

    @Query(value = """
        SELECT p.* FROM product p
        JOIN product_tag pt ON p.id = pt.product_id
        WHERE pt.tag_id = :tagId
        """, nativeQuery = true)
    List<Product> findAllByTagId(@Param("tagId") Long tagId);

    @Query("""
    SELECT new vn.be_do_an_tot_nghiep.response.ProductTagResponse(
        t.tagName,
        p.hashId,
        p.name,
        p.image,
        p.price,
        p.category.id
    )
    FROM Product p
    LEFT JOIN ProductTag pt ON p.id = pt.productId.id
    LEFT JOIN Tag t ON pt.tagId.id = t.id
    WHERE p.category.id = :categoryId AND p.status = 1
    ORDER BY p.createdAt DESC
""")
    List<ProductTagResponse> findProductsByCategoryId(@Param("categoryId") Long categoryId);


    @Query("""
    SELECT new vn.be_do_an_tot_nghiep.response.ProductTagResponse(
            t.tagName,
        p.hashId,
        p.name,
        p.image,
        p.price,
        p.category.id
    )
    FROM Product p
    JOIN ProductTag pt ON p.id = pt.productId.id
    JOIN Tag t ON pt.tagId.id = t.id
    WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:tagId IS NULL OR t.id = :tagId)
      AND p.status = 1
    ORDER BY p.createdAt DESC
""")
    List<ProductTagResponse> findProductsByCategoryAndTag(
            @Param("categoryId") Long categoryId,
            @Param("tagId") Long tagId
    );


}
