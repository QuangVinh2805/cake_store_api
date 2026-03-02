package vn.be_do_an_tot_nghiep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_USER = 2;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 200)
    @NotNull
    @Column(name = "password", nullable = false, length = 200)
    private String password;

    @Column(name = "birthday")
    private Date birthday;

    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 100)
    @Column(name = "address", length = 100)
    private String address;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @NotNull
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "phone")
    private String phone;

    @Size(max = 100)
    @Column(name = "sex", length = 100)
    private String sex;

    @Size(max = 500)
    @Column(name = "token", length = 500)
    private String token;

    @Size(max = 100)
    @Column(name = "avatar", length = 100)
    private String avatar;

    private String hashId;
    private Long status;


}