package app.services;

import app.DBConnection.Repository;
import app.models.Person;
import app.services.utils.MsgDisplay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private Repository repository;

    @BeforeEach
    public void setUp() {
        // 创建测试用的 Repository 实例
        repository = new Repository("src/main/resources/database/test-db-load.csv",
                "src/main/resources/database/test-session-load.csv");
        userService = new UserService(repository);
    }

    @Test
    public void testCreateAndDeleteUser_Success() {
        Random random = new Random();
        String username = "newUser" + random.nextInt(1000);
        String password = "password";
        String name = "Test User";
        String status = "active";

        userService.createUser(username, password, name, status);

        Person newUser = repository.findUserByUsername(username);
        assertNotNull(newUser, "User should be added successfully");
        assertEquals(username, newUser.getUsername(), "Username should match");
        assertEquals(name, newUser.getName(), "Name should match");
        assertEquals(status, newUser.getStatus(), "Status should match");

    }

//    @Test
//    public void testCreateUser_UsernameAlreadyExists() {
//        String existingUsername = "test1"; // 假设这个用户名已经存在
//        String password = "password";
//        String name = "Test User";
//        String status = "active";
//
//        // 尝试创建已存在用户名的用户
//        userService.createUser(existingUsername, password, name, status);
//
//        // 验证用户不应被添加到 Repository
//        long userCount = repository.getPersonsList().stream()
//                .filter(person -> person.getUsername().equals(existingUsername))
//                .count();
//        assertEquals(1, userCount, "User should not be created if username already exists");
//    }
//
//    @Test
//    public void testCreateUser_StatusTooLong() {
//        String username = "userWithLongStatus";
//        String password = "password";
//        String name = "Test User";
//        String longStatus = "ThisStatusIsWayTooLongForTheApplicationToHandleCorrectly";
//
//        // 尝试创建状态过长的用户
//        userService.createUser(username, password, name, longStatus);
//
//        // 验证用户不应被添加到 Repository
//        assertNull(repository.findUserByUsername(username), "User should not be created if status is too long");
//    }
//
//    @Test
//    public void testJoinUser_Success() {
//        String username = "joinUser";
//        String password = "password";
//        String confirmPassword = "password";
//        String name = "Join User";
//        String status = "active";
//
//        // 调用 joinUser 方法
//        userService.joinUser(username, password, confirmPassword, name, status);
//
//        // 验证用户是否成功添加到 Repository
//        Person newUser = repository.findUserByUsername(username);
//        assertNotNull(newUser, "User should be added successfully");
//        assertEquals(username, newUser.getUsername(), "Username should match");
//        assertEquals(name, newUser.getName(), "Name should match");
//        assertEquals(status, newUser.getStatus(), "Status should match");
//    }
//
//    @Test
//    public void testJoinUser_PasswordMismatch() {
//        String username = "userWithMismatchedPassword";
//        String password = "password";
//        String confirmPassword = "differentPassword";
//        String name = "Test User";
//        String status = "active";
//
//        // 尝试创建密码不匹配的用户
//        userService.joinUser(username, password, confirmPassword, name, status);
//
//        // 验证用户不应被添加到 Repository
//        assertNull(repository.findUserByUsername(username), "User should not be created if passwords do not match");
//    }
//
//    @Test
//    public void testLoginUser_Success() {
//        String username = "test1"; // 假设这个用户名存在
//        String password = "123";   // 对应的密码
//
//        // 调用 loginUser 方法
//        userService.loginUser(username, password);
//
//        // 因为没有实际返回值，只需确保没有异常抛出，测试基本逻辑是否通过
//        assertEquals("test1", repository.findUserByUsername(username).getUsername(), "User should log in successfully");
//    }
//
//    @Test
//    public void testLoginUser_InvalidUsernameOrPassword() {
//        String username = "invalidUser";
//        String password = "wrongPassword";
//
//        // 调用 loginUser 方法
//        userService.loginUser(username, password);
//
//        // 验证用户登录失败，不应抛出异常，且无返回结果
//        assertNull(repository.findUserByUsername(username), "Invalid login should not allow access");
//    }
}
