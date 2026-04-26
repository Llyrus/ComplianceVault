import com.compliancevault.dao.UserDAO;
import com.compliancevault.model.Role;
import com.compliancevault.model.User;
import com.compliancevault.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.mindrot.jbcrypt.BCrypt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService service;
    private FakeUserDAO fakeDAO;

    @BeforeEach
    void setUp() throws SQLException {
        fakeDAO = new FakeUserDAO();
        service = new AuthService(fakeDAO);

        // pre-register users
        service.registerUser("admin", "correctpassword", Role.ADMIN);
        service.registerUser("reception", "anotherpassword", Role.GENERAL);
    }

    @Test
    @DisplayName("correct Password Authenticates")
    void correctPasswordAuthenticates() throws SQLException {
        assertTrue(service.login("admin", "correctpassword"));
        assertTrue(service.isLoggedIn());
        assertEquals("admin", service.getCurrentUser().getUsername());
    }

    @Test
    @DisplayName("wrong Password Fails")
    void wrongPasswordFails() throws SQLException {
        assertFalse(service.login("admin", "wrongpassword"));
        assertFalse(service.isLoggedIn());
    }

    @Test
    @DisplayName("non-Existent Username Fails")
    void nonExistentUsernameFails() throws SQLException {
        // should return false, same as wrong password — no info leaked
        assertFalse(service.login("ghost", "anything"));
        assertFalse(service.isLoggedIn());
    }

    @Test
    @DisplayName("logout Clears Current User")
    void logoutClearsCurrentUser() throws SQLException {
        service.login("admin", "correctpassword");
        assertTrue(service.isLoggedIn());

        service.logout();
        assertFalse(service.isLoggedIn());
        assertNull(service.getCurrentUser());
    }

    @Test
    @DisplayName("admin Role Has Admin Access")
    void adminRoleHasAdminAccess() throws SQLException {
        service.login("admin", "correctpassword");
        assertTrue(service.hasAdminAccess());
    }

    @Test
    @DisplayName("general Role Doesn't Have Admin Access")
    void generalRoleDoesNotHaveAdminAccess() throws SQLException {
        service.login("reception", "anotherpassword");
        assertFalse(service.hasAdminAccess());
    }

    @Test
    @DisplayName("not Logged In Does Not Have Admin Access")
    void notLoggedInDoesNotHaveAdminAccess() {
        // no login at all
        assertFalse(service.hasAdminAccess());
    }

    @Test
    @DisplayName("bcrypt Produces Different Hashes For Same Password")
    void bcryptProducesDifferentHashesForSamePassword() {
        // bcrypt salts each hash, so same password should produce different hashes
        String hash1 = BCrypt.hashpw("samepassword", BCrypt.gensalt());
        String hash2 = BCrypt.hashpw("samepassword", BCrypt.gensalt());

        assertNotEquals(hash1, hash2);
        // but both should still verify against the original password
        assertTrue(BCrypt.checkpw("samepassword", hash1));
        assertTrue(BCrypt.checkpw("samepassword", hash2));
    }

    private static class FakeUserDAO extends UserDAO {
        Map<String, User> usersByUsername = new HashMap<>();
        int nextId = 1;

        @Override
        public void insert(User user) {
            user.setUserId(nextId++);
            usersByUsername.put(user.getUsername(), user);
        }

        @Override
        public User findByUsername(String username) {
            return usersByUsername.get(username);
        }
    }
}