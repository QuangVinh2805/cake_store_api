package vn.be_do_an_tot_nghiep.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "product_taste")
public class ProductTaste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private Long productId;

    @Size(max = 255)
    @NotNull
    @Column(name = "taste", nullable = false)
    private String taste;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @ColumnDefault("0")
    @Column(name = "quantity")
    private Long quantity;

    @ColumnDefault("current_timestamp()")
    @Column(name = "created_at")
    private Date createdAt;

    @ColumnDefault("current_timestamp()")
    @Column(name = "updated_at")
    private Date updatedAt;

    @NotNull
    @Column(name = "price", nullable = false)
    private Long price;

    @Lob
    @Column(name = "second_des")
    private String secondDes;

}