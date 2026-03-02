package vn.be_do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Entity
@Table(name = "review")
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productTasteId;

    private Long userId;

    private String comment;

    private Long rate;

    private String image;

    private Date createdAt;
    private Date updatedAt;
}
