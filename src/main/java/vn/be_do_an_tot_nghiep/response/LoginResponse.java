package vn.be_do_an_tot_nghiep.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.be_do_an_tot_nghiep.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String avatar;

    public LoginResponse(User user) {
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


