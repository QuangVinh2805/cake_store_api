package vn.be_do_an_tot_nghiep.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.ProductTaste;

import java.util.List;

@Repository
public interface ProductTasteRepository extends JpaRepository<ProductTaste,Long> {
    @Query(
            value = "SELECT * FROM product_taste WHERE id = :productTasteId",
            nativeQuery = true
    )
    ProductTaste findByProductTasteId(Long productTasteId);

    ProductTaste findByProductIdAndTaste(Long productId, String taste);


    List<ProductTaste> findByProductId(Long productId);


    @Query("""
        SELECT pt FROM ProductTaste pt
        WHERE pt.productId = :productId
        ORDER BY pt.id ASC
    """)
    List<ProductTaste> findProductTasteByProductId(
            Long productId,
            Pageable pageable
    );

    // tìm product theo taste
    @Query("""
        SELECT DISTINCT pt.productId FROM ProductTaste pt
        WHERE pt.taste = :taste
    """)
    List<Long> findProductIdsByTaste(String taste);


    @Query("""
        SELECT p, pt
        FROM Product p
        JOIN ProductTaste pt ON pt.productId = p.id
        WHERE p.status = 1
    """)
    List<Object[]> findAllProductWithTaste();

    boolean existsByProductIdAndTaste(Long productId, String taste);



}
