package vn.be_do_an_tot_nghiep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.request.ChangePasswordRequest;
import vn.be_do_an_tot_nghiep.request.ChangeRoleRequest;
import vn.be_do_an_tot_nghiep.request.LoginRequest;
import vn.be_do_an_tot_nghiep.request.UserRequest;
import vn.be_do_an_tot_nghiep.response.LoginResponse;
import vn.be_do_an_tot_nghiep.response.UserResponse;
import vn.be_do_an_tot_nghiep.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/getAll")
    public Page<UserResponse> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "12") int size) {
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/getInfo")
    public UserResponse getUserByToken(@RequestParam String token) {
        return userService.getUserByToken(token);
    }

    @GetMapping("/getInfoByHashId")
    public UserResponse getUserByHashId(@RequestParam String hashId) {
        return userService.getUserByHashId(hashId);
    }

    @PostMapping("/create")
    public UserResponse createUser(@RequestBody UserRequest req) {
        return userService.createUser(req);
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse updateUser(
            @RequestParam String token,
            @RequestPart("data") String data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        ObjectMapper mapper = new ObjectMapper();
        UserRequest req;
        try {
            req = mapper.readValue(data, UserRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
        }

        return userService.updateUser(token, req, avatar);
    }

    @PutMapping(value = "/updateByHashId", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse updateUserByHashId(
            @RequestParam String hashId,
            @RequestPart("data") String data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        ObjectMapper mapper = new ObjectMapper();
        UserRequest req;
        try {
            req = mapper.readValue(data, UserRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
        }

        return userService.updateUserByHashId(hashId, req, avatar);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String token) {
        userService.deleteUser(token);
        return ResponseEntity.ok("Đã xóa tài khoản thành công");
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse register(
            @RequestPart("data") String data,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        // Parse JSON thủ công
        ObjectMapper mapper = new ObjectMapper();
        UserRequest req;
        try {
            req = mapper.readValue(data, UserRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
        }

        return userService.register(req, avatar);
    }



    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestParam String token,
            @RequestBody ChangePasswordRequest req) {

        userService.changePassword(token, req);

        return ResponseEntity.ok(
                Map.of("message", "Đổi mật khẩu thành công")
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        userService.logout(token);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }


    @PutMapping("/changeStatus")
    public ResponseEntity<String> changeStatus(@RequestParam String hashId){
        userService.changeStatus(hashId);
        return ResponseEntity.ok("Đổi trạng thái thành công!");
    }

    @PutMapping("/changeRole")
    public ResponseEntity<UserResponse> changeRole(
            @RequestParam String hashId,
            @RequestBody ChangeRoleRequest request
    ) {
        return ResponseEntity.ok(userService.changeUserRole(hashId, request));
    }

    @GetMapping("/search")
    public Page<UserResponse> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return userService.searchUsers(keyword, page, size);
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok("Mật khẩu mới đã được gửi về email");
    }


}
