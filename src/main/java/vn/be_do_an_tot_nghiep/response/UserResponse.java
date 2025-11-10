package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.be_do_an_tot_nghiep.model.User;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
//    private Long id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private String sex;
    private Date birthday;
    private String role;
    private String token;
    private String avatar;

    public UserResponse(User user) {
//        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.phone = user.getPhone();
        this.sex = user.getSex();
        this.birthday = user.getBirthday();
        this.token = user.getToken();
        this.avatar = user.getAvatar();

        if (user.getRoleId() == User.ROLE_ADMIN) {
            this.role = "ADMIN";
        } else if (user.getRoleId() == User.ROLE_USER) {
            this.role = "USER";
        } else {
            this.role = "UNKNOWN";
        }
    }
}
