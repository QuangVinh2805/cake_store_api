package vn.be_do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long orderId;

    private Long amount;

    private String paymentMethod;

    private String status; // SUCCESS | FAILED

    private LocalDateTime createdAt = LocalDateTime.now();
}
