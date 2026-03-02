    package vn.be_do_an_tot_nghiep.model;
    
    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotNull;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.hibernate.annotations.ColumnDefault;
    
    import java.math.BigDecimal;
    import java.time.Instant;
    import java.time.LocalDateTime;
    import java.util.Date;
    
    @Getter
    @Setter
    @Entity
    @Table(name = "favourite_product")
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public class FavouriteProduct {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;
    
        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "product_id", nullable = false)
        private Product product;
    
        @NotNull
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

    
        @ColumnDefault("current_timestamp()")
        @Column(name = "created_at")
        private Date createdAt;
    
        @ColumnDefault("current_timestamp()")
        @Column(name = "updated_at")
        private Date updatedAt;
    
        @ColumnDefault("1")
        @Column(name = "status")
        private Long status;
    
    }