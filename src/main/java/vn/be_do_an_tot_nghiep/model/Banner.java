package vn.be_do_an_tot_nghiep.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Size(max = 255)
    @Column(name = "product_name")
    private String productName;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 255)
    @Column(name = "background")
    private String background;

    @Size(max = 50)
    @Column(name = "color_button", length = 50)
    private String colorButton;

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