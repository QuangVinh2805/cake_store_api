package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.OrderDetail;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderId(Long orderId);

    @Query("""
        select count(od)
        from OrderDetail od
        join Order o on od.orderId = o.id
        where o.userId = :userId
          and od.productTasteId = :productTasteId
          and o.status = 2
    """)
    long countPurchasedProduct(
            @Param("userId") Long userId,
            @Param("productTasteId") Long productTasteId
    );

}
