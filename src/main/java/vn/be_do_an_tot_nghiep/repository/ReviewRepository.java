package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.be_do_an_tot_nghiep.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findByUserIdAndProductTasteId(Long userId, Long productTasteId);

    List<Review> findByProductTasteIdOrderByCreatedAtDesc(Long productTasteId);

    @Query("select avg(r.rate) from Review r where r.productTasteId = :productTasteId")
    Double avgRate(@Param("productTasteId") Long productTasteId);

    @Query("""
        select r
        from Review r
        join ProductTaste pt on r.productTasteId = pt.id
        join Product p on pt.productId = p.id
        where p.hashId = :hashId
        order by r.createdAt desc
    """)
    List<Review> findByProductHashId(@Param("hashId") String hashId);

    @Query("""
        select avg(r.rate)
        from Review r
        join ProductTaste pt on r.productTasteId = pt.id
        join Product p on pt.productId = p.id
        where p.hashId = :hashId
    """)
    Double avgRateByProductHashId(@Param("hashId") String hashId);

    @Query(value = """
        SELECT 
            r.id,
            u.name,
            u.avatar,
            p.name,
            pt.taste,
            r.comment,
            r.rate,
            r.created_at
        FROM review r
        JOIN user u ON r.user_id = u.id
        JOIN product_taste pt ON r.product_taste_id = pt.id
        JOIN product p ON pt.product_id = p.id
        ORDER BY r.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findLatestReviews();

}
