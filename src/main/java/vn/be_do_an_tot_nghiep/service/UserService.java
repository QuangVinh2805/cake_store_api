package vn.be_do_an_tot_nghiep.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vn.be_do_an_tot_nghiep.model.Role;
import vn.be_do_an_tot_nghiep.model.User;
import vn.be_do_an_tot_nghiep.repository.RoleRepository;
import vn.be_do_an_tot_nghiep.repository.UserRepository;
import vn.be_do_an_tot_nghiep.request.*;
import vn.be_do_an_tot_nghiep.response.LoginResponse;
import vn.be_do_an_tot_nghiep.response.ProductListResponse;
import vn.be_do_an_tot_nghiep.response.UserResponse;
import vn.be_do_an_tot_nghiep.util.JwtUtil;
import vn.be_do_an_tot_nghiep.util.RandomUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private JavaMailSender emailSender;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


//    public List<UserResponse> getAllUsers() {
//        return userRepository.findAll().stream()
//                .map(user -> new UserResponse(user))
//                .toList();
//    }


    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rows = userRepository.findAllUser(pageable);

        return rows.map(u -> {
            UserResponse res = new UserResponse();
            res.setEmail((String) u[0]);
            res.setName((String) u[1]);
            res.setAddress((String) u[2]);
            res.setPhone((String) u[3]);
            res.setSex((String) u[4]);
            res.setToken((String) u[5]);
            res.setAvatar((String) u[6]);
            res.setHashId((String) u[7]);
            res.setStatus(((Number) u[8]).longValue());

            Long roleId = ((Number) u[9]).longValue();
            if (roleId == User.ROLE_ADMIN) {
                res.setRole("ADMIN");
            } else if (roleId == User.ROLE_USER) {
                res.setRole("USER");
            } else {
                res.setRole("UNKNOWN");
            }

            return res;
        });
    }


    public UserResponse getUserByToken(String token) {
        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Phiên đăng nhập của bạn đã quá hạn,vui lòng đăng nhập lại!");
        }

        User user = userRepository.findAll()
                .stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với token này"));
        return new UserResponse(user);
    }


    public UserResponse getUserByHashId(String hashId) {
        User user = userRepository.findAll()
                .stream()
                .filter(u -> hashId.equals(u.getHashId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("HashId không hợp lệ"));

        return new UserResponse(user);
    }

    public UserResponse createUser(UserRequest req) {
        UserValidator.validateCreate(req);

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email đã được sử dụng");

        if (userRepository.existsByPhone(req.getPhone()))
            throw new RuntimeException("Số điện thoại đã được sử dụng");

        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (userRepository.findByPhone(req.getPhone()) != null) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }

        String randomPassword = RandomStringUtils.randomAlphanumeric(8);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(randomPassword);

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(hashedPassword);
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setSex(req.getSex());
        user.setRoleId(req.getRoleId());
        user.setBirthday(req.getBirthday());
        user.setHashId(RandomUtil.generateHashId());
        user.setStatus(1L);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vinhquangngo2805@gmail.com");
        message.setTo(req.getEmail());
        message.setSubject("Thông báo");
        message.setText(
                "Chúc mừng bạn đã đăng ký tài khoản thành công.\n" +
                        "Mật khẩu của bạn là: " + randomPassword
        );
        emailSender.send(message);

        return new UserResponse(userRepository.save(user));
    }

    public UserResponse updateUser(String token, UserRequest req, MultipartFile avatar) {
        UserValidator.validateCreate(req);

        User user = userRepository.findAll()
                .stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (!req.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        if (!req.getPhone().equals(user.getPhone())
                && userRepository.existsByPhone(req.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }


        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setSex(req.getSex());
        user.setBirthday(req.getBirthday());

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                File filePath = new File(dir, fileName);
                avatar.transferTo(filePath);

                // Nếu user đã có avatar cũ → có thể xoá nếu muốn
                if (user.getAvatar() != null) {
                    File oldFile = new File(System.getProperty("user.dir") + user.getAvatar());
                    if (oldFile.exists()) oldFile.delete();
                }

                user.setAvatar("/uploads/avatar/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh đại diện", e);
            }
        }

        // ✅ Lưu và trả về kết quả
        return new UserResponse(userRepository.save(user));
    }

    public UserResponse updateUserByHashId(String hashId, UserRequest req, MultipartFile avatar) {
        UserValidator.validateCreate(req);

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email đã được sử dụng");

        if (userRepository.existsByPhone(req.getPhone()))
            throw new RuntimeException("Số điện thoại đã được sử dụng");

        User user = userRepository.findAll()
                .stream()
                .filter(u -> hashId.equals(u.getHashId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("HashId không hợp lệ"));

        // ✅ Cập nhật thông tin cơ bản
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setSex(req.getSex());
        user.setBirthday(req.getBirthday());

        // ✅ Nếu có file avatar mới thì lưu lại
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                File filePath = new File(dir, fileName);
                avatar.transferTo(filePath);

                // Nếu user đã có avatar cũ → có thể xoá nếu muốn
                if (user.getAvatar() != null) {
                    File oldFile = new File(System.getProperty("user.dir") + user.getAvatar());
                    if (oldFile.exists()) oldFile.delete();
                }

                user.setAvatar("/uploads/avatar/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh đại diện", e);
            }
        }

        // ✅ Lưu và trả về kết quả
        return new UserResponse(userRepository.save(user));
    }


    public void deleteUser(String token) {
        User user = userRepository.findAll()
                .stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        userRepository.delete(user);
    }

    public LoginResponse login(LoginRequest request) {
        // Tìm user theo số điện thoại
        User user = userRepository.findAll()
                .stream()
                .filter(u -> request.getPhone().equals(u.getPhone()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sai số điện thoại hoặc mật khẩu"));

        if (user.getStatus() != 1) {
            throw new RuntimeException("Tài khoản đã bị khóa!");
        }
        // So sánh mật khẩu mã hóa
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai số điện thoại hoặc mật khẩu");
        }

        // Sinh token JWT
        String token = jwtUtil.generateToken(user.getPhone());
        user.setToken(token);
        userRepository.save(user);

        return new LoginResponse(user);
    }

    public UserResponse register(UserRequest req, MultipartFile avatar) {
        UserValidator.validateCreate(req);

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email đã được sử dụng");

        if (userRepository.existsByPhone(req.getPhone()))
            throw new RuntimeException("Số điện thoại đã được sử dụng");

        if (req.getEmail() != null && userRepository.findByEmail(req.getEmail()) != null)
            throw new RuntimeException("Email đã tồn tại");

        if (req.getPhone() != null && userRepository.findByPhone(req.getPhone()) != null)
            throw new RuntimeException("Số điện thoại đã tồn tại");

        String randomPassword = RandomStringUtils.randomAlphanumeric(8);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(randomPassword);

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(hashedPassword);
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setSex(req.getSex());
        user.setBirthday(req.getBirthday());
        user.setRoleId(2L);
        user.setStatus(1L);
        user.setHashId(RandomUtil.generateHashId());

        // ✅ Xử lý lưu avatar nếu có
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                File filePath = new File(dir, fileName);
                avatar.transferTo(filePath);

                user.setAvatar("/uploads/avatar/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh đại diện", e);
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vinhquangngo2805@gmail.com");
        message.setTo(req.getEmail());
        message.setSubject("Thông báo");
        message.setText(
                "Chúc mừng bạn đã đăng ký tài khoản thành công.\n" +
                        "Mật khẩu của bạn là: " + randomPassword
        );
        emailSender.send(message);

        String token = jwtUtil.generateToken(user.getPhone());
        user.setToken(token);

        userRepository.save(user);
        return new UserResponse(user);
    }


    public void changePassword(String token, ChangePasswordRequest req) {

        if (req.getOldPassword() == null || req.getNewPassword() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu không được để trống"
            );
        }

        // ✅ Validate mật khẩu mới
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        if (!req.getNewPassword().matches(passwordRegex)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu phải từ 8 ký tự, gồm chữ hoa, chữ thường và số"
            );
        }

        // Bỏ "Bearer " nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String phone = jwtUtil.getPhoneFromToken(token);

        User user = userRepository.findAll().stream()
                .filter(u -> phone.equals(u.getPhone()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Người dùng không tồn tại"
                ));

        // ❌ Mật khẩu cũ sai
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu cũ không đúng"
            );
        }

        // ✅ Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }


    public void logout(String token) {
        User user = userRepository.findAll()
                .stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với token này"));

        user.setToken(null);
        userRepository.save(user);
    }


    public void changeStatus(String hashId){
        User user = userRepository.findAll()
                .stream()
                .filter(u -> hashId.equals(u.getHashId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("HashId không hợp lệ"));

        user.setStatus(user.getStatus() == 1L ? 0L : 1L);
        userRepository.save(user);
    }

    public UserResponse changeUserRole(String hashId, ChangeRoleRequest req) {

        // validate roleName
        if (req.getRoleName() == null || req.getRoleName().trim().isEmpty()) {
            throw new RuntimeException("Role không được để trống");
        }

        // tìm user
        User user = userRepository.findByHashId(hashId);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        // tìm role theo roleName
        Role role = roleRepository.findByRole(req.getRoleName());
        if (role == null) {
            throw new RuntimeException("Role không tồn tại");
        }

        // nếu role không đổi thì thôi
        if (role.getId().equals(user.getRoleId())) {
            throw new RuntimeException("User đã có role này rồi");
        }

        // set role mới
        user.setRoleId(role.getId());

        return new UserResponse(userRepository.save(user));
    }

    public Page<UserResponse> searchUsers(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rows = userRepository.searchUsers(keyword, pageable);

        return rows.map(u -> {
            UserResponse res = new UserResponse();
            res.setEmail((String) u[0]);
            res.setName((String) u[1]);
            res.setAddress((String) u[2]);
            res.setPhone((String) u[3]);
            res.setSex((String) u[4]);
            res.setRole((String) u[5]);
            res.setToken((String) u[6]);
            res.setAvatar((String) u[7]);
            res.setHashId((String) u[8]);
            res.setStatus(((Number) u[9]).longValue());
            return res;
        });
    }


    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy email!");
        }

        // 1. tạo mật khẩu ngẫu nhiên
        String newPassword = RandomStringUtils.randomAlphanumeric(8);

        // 2. encode password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vinhquangngo2805@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Thông báo");
        message.setText(
                "Chúc mừng bạn đã đăng ký tài khoản thành công.\n" +
                        "Mật khẩu của bạn là: " + newPassword
        );
        emailSender.send(message);
    }


}
