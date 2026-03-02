package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.be_do_an_tot_nghiep.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByPhone(String phone);
    User findByToken(String token);
    User findByHashId(String hashId);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailAndTokenNot(String email, String token);
    boolean existsByPhoneAndTokenNot(String phone, String token);


    @Query(value = """
    SELECT
        u.email,
        u.name,
        u.address,
        u.phone,
        u.sex,
        u.token,
        u.avatar,
        u.hash_id,
        u.status,
        u.role_id
    FROM user u
""",
            countQuery = """
    SELECT COUNT(*)
    FROM user u
""",
            nativeQuery = true)
    Page<Object[]> findAllUser(Pageable pageable);


    @Query(
            value = """
        SELECT
            u.email,
            u.name,
            u.address,
            u.phone,
            u.sex,
            CASE 
                WHEN u.role_id = 1 THEN 'ADMIN'
                WHEN u.role_id = 2 THEN 'USER'
                ELSE 'UNKNOWN'
            END AS role,
            u.token,
            u.avatar,
            u.hash_id,
            u.status
        FROM `user` u
        WHERE
            LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR u.phone LIKE CONCAT('%', :keyword, '%')
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM `user` u
        WHERE
            LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR u.phone LIKE CONCAT('%', :keyword, '%')
        """,
            nativeQuery = true
    )
    Page<Object[]> searchUsers(
            @Param("keyword") String keyword,
            Pageable pageable
    );





}
