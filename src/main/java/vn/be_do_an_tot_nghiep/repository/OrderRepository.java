package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.Order;
import vn.be_do_an_tot_nghiep.response.RevenueByMonthResponse;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);


    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query(value = """
    SELECT 
        YEAR(created_at) AS year,
        MONTH(created_at) AS month,
        SUM(total_price) AS totalRevenue
    FROM `order`
    WHERE status = 2
    GROUP BY YEAR(created_at), MONTH(created_at)
    ORDER BY YEAR(created_at), MONTH(created_at)
""", nativeQuery = true)
    List<Object[]> getRevenueByMonthRaw();



    @Query(value = """
    SELECT 
        YEAR(created_at)  AS year,
        MONTH(created_at) AS month,
        SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS deliveredOrders,
        SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS canceledOrders
    FROM `order`
    WHERE status IN (2,3)
    GROUP BY YEAR(created_at), MONTH(created_at)
    ORDER BY year, month
""", nativeQuery = true)
    List<Object[]> getOrderStatusByMonth();


    @Query(value = """
    SELECT 
        SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS deliveredOrders,
        SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS canceledOrders
    FROM `order`
    WHERE status IN (2,3)
""", nativeQuery = true)
    Object getOrderStatusSummary();



    @Query("""
        SELECT o FROM Order o
        JOIN User u ON o.userId = u.id
        WHERE u.email LIKE %:keyword%
           OR u.phone LIKE %:keyword%
    """)
    Page<Order> searchByEmailOrPhone(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
    """)
    Page<Order> filterByStatus(
            @Param("status") Integer status,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.isCheckout = :isCheckout
    """)
    Page<Order> filterByCheckout(
            @Param("isCheckout") Integer isCheckout,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.createdAt BETWEEN :fromDate AND :toDate
    """)
    Page<Order> filterByDateRange(
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            Pageable pageable
    );

}
