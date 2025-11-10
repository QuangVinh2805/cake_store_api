package vn.be_do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.User;
import vn.be_do_an_tot_nghiep.repository.UserRepository;
import vn.be_do_an_tot_nghiep.request.ChangePasswordRequest;
import vn.be_do_an_tot_nghiep.request.LoginRequest;
import vn.be_do_an_tot_nghiep.request.UserRequest;
import vn.be_do_an_tot_nghiep.response.LoginResponse;
import vn.be_do_an_tot_nghiep.response.UserResponse;
import vn.be_do_an_tot_nghiep.util.JwtUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user))
                .toList();
    }

    public UserResponse getUserByToken(String token) {
        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token đã hết hạn");
        }

        User user = userRepository.findAll()
                .stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với token này"));
        return new UserResponse(user);
    }

    public UserResponse createUser(UserRequest req) {
        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setSex(req.getSex());
        user.setRoleId(req.getRoleId());
        user.setBirthday(req.getBirthday());
        return new UserResponse(userRepository.save(user));
    }

    public UserResponse updateUser(String token, UserRequest req, MultipartFile avatar) {
        // ✅ Tìm user bằng token
        User user = userRepository.findAll()
                .stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

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
        if (req.getEmail() != null && userRepository.findByEmail(req.getEmail()) != null)
            throw new RuntimeException("Email đã tồn tại");

        if (req.getPhone() != null && userRepository.findByPhone(req.getPhone()) != null)
            throw new RuntimeException("Số điện thoại đã tồn tại");

        String hashedPassword = passwordEncoder.encode(req.getPassword());

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(hashedPassword);
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setSex(req.getSex());
        user.setBirthday(req.getBirthday());
        user.setRoleId(2L);

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

        String token = jwtUtil.generateToken(user.getPhone());
        user.setToken(token);

        userRepository.save(user);
        return new UserResponse(user);
    }


    public void changePassword(String token, ChangePasswordRequest req) {
        // Bỏ "Bearer " nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Lấy phone từ token
        String phone = jwtUtil.getPhoneFromToken(token);

        // Tìm user theo phone
        User user = userRepository.findAll().stream()
                .filter(u -> phone.equals(u.getPhone()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // Đổi mật khẩu mới (mã hóa)
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
}
