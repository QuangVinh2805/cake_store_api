package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.be_do_an_tot_nghiep.model.Transactions;

public interface TransactionsRepository extends JpaRepository<Transactions, Long> {
    Transactions findByOrderId(Long orderId);
}
