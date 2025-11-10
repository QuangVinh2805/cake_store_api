package vn.be_do_an_tot_nghiep.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String name;
    private String email;
    private String password;
    private String address;
    private String phone;
    private String sex;
    private Long roleId;
    private Date birthday;
}
