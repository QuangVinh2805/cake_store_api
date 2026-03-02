package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    SELECT 
        p.hashId,
        p.name,
        p.image,
        p.price,
        p.category.id,
        p.category.name,
        p.status,
        t.tagName
    FROM Product p
    LEFT JOIN ProductTag pt ON p.id = pt.productId.id
    LEFT JOIN Tag t ON pt.tagId.id = t.id
    WHERE p.category.id = :categoryId
      AND p.status = 1
""")
    Page<Object[]> findProductsByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );


    @Query("""
    SELECT 
        p.hashId,
        p.name,
        p.image,
        p.price,
        p.category.id,
        p.category.name,
        p.status,
        t.tagName
    FROM Product p
    JOIN ProductTag pt ON p.id = pt.productId.id 
    JOIN Tag t ON pt.tagId.id = t.id OR t.tagName = pt.tag
    WHERE t.id = :tagId
      AND p.status = 1
""")
    Page<Object[]> findProductsByTagId(
            @Param("tagId") Long tagId,
            Pageable pageable
    );




    @Query("""
    SELECT 
        p.hashId,
        p.name,
        p.image,
        p.price,
        p.category.id,
        p.category.name,
        p.status,
        t.tagName
    FROM Product p
    JOIN ProductTag pt ON p.id = pt.productId.id
    JOIN Tag t ON pt.tagId.id = t.id
    WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:tagId IS NULL OR t.id = :tagId)
      AND p.status = 1
""")
    Page<Object[]> findProductsByCategoryAndTag(
            @Param("categoryId") Long categoryId,
            @Param("tagId") Long tagId,
            Pageable pageable
    );



    @Query(
            value = "SELECT * FROM product WHERE id = :productId",
            nativeQuery = true
    )

    Product findByProductId(Long productId);


    @Query(
            value = """
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
    WHERE p.status = 1
      AND p.name LIKE %:keyword%
    GROUP BY p.id
    """,countQuery = """
        SELECT COUNT(*)
        FROM product p
        WHERE p.status = 1
        AND p.name LIKE %:keyword%
    """,
            nativeQuery = true
    )
    Page<Object[]> searchProductByStatus(@Param("keyword") String keyword,Pageable pageable);


    @Query(
            value = """
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
    WHERE p.name LIKE %:keyword%
    GROUP BY p.id
    """,countQuery = """
        SELECT COUNT(*)
        FROM product p
        AND p.name LIKE %:keyword%
    """,
            nativeQuery = true
    )
    Page<Object[]> searchAllProduct(@Param("keyword") String keyword,Pageable pageable);



    // category + taste
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN ProductTaste pt ON pt.productId = p.id
        WHERE p.category.id = :categoryId
          AND pt.taste = :taste
          AND p.hashId <> :hashId
          AND p.status = 1
    """)
    List<Product> findByCategoryAndTaste(
            Long categoryId,
            String taste,
            String hashId,
            Pageable pageable
    );

    // category
    @Query("""
        SELECT p FROM Product p
        WHERE p.category.id = :categoryId
          AND p.hashId <> :hashId
          AND p.status = 1
    """)
    List<Product> findByCategory(
            Long categoryId,
            String hashId,
            Pageable pageable
    );

    // random
    @Query("""
        SELECT p FROM Product p
        WHERE p.hashId <> :hashId
          AND p.status = 1
        ORDER BY function('RAND')
    """)
    List<Product> findRandom(
            String hashId,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT
            p.hash_id,
            p.name,
            p.image,
            p.price,
            p.category_id,
            c.name AS category_name,
            p.status,
            GROUP_CONCAT(t.tag SEPARATOR ',') AS tags
        FROM product p
        LEFT JOIN product_tag t ON p.id = t.product_id
        LEFT JOIN category c ON p.category_id = c.id
        WHERE p.status = 1
        GROUP BY p.id
    """,
            countQuery = """
        SELECT COUNT(*)
        FROM product p
        WHERE p.status = 1
    """,
            nativeQuery = true
    )
    Page<Object[]> findAllWithGroupedTagsByStatus(Pageable pageable);


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
""", countQuery = """
        SELECT COUNT(*)
        FROM product p
    """, nativeQuery = true)
    Page<Object[]> findAllProductWithGroupedTags(Pageable pageable );






}
