package vn.be_do_an_tot_nghiep.request;

import org.springframework.beans.factory.annotation.Autowired;
import vn.be_do_an_tot_nghiep.repository.UserRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class UserValidator {
    private UserRepository userRepository;


    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0[0-9]{9}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public static void validateCreate(UserRequest req) {

        // name
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new RuntimeException("Tên không được để trống");

        // email
        if (req.getEmail() == null || !EMAIL_PATTERN.matcher(req.getEmail()).matches())
            throw new RuntimeException("Email không hợp lệ");

        // phone
        if (req.getPhone() == null || !PHONE_PATTERN.matcher(req.getPhone()).matches())
            throw new RuntimeException("Số điện thoại không hợp lệ");

        if (req.getBirthday() == null)
            throw new RuntimeException("Ngày sinh không được để trống");

        LocalDate birthday = new java.sql.Date(req.getBirthday().getTime()).toLocalDate();
        if (Period.between(birthday, LocalDate.now()).getYears() < 13)
            throw new RuntimeException("Người dùng phải từ 13 tuổi trở lên");
    }
}
