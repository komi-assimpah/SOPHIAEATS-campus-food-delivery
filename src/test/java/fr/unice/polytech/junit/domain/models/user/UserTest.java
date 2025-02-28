package fr.unice.polytech.junit.domain.models.user;

import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.domain.models.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john.doe@example.com", "password");
    }


    //Test of conctructors
    @Test
    void testConstructorWithParametersAndType() {
        assertNotNull(user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("password", user.getPassword());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals(UserStatus.CAMPUS_STUDENT, user.getType());
        assertEquals(0.0, user.getBalance());
    }

    @Test
    void testConstructorWithParametersAndWithoutType() {
        User newUser = new User("Jane Doe", "jane.doe@example.com", "password456");
        assertNotNull(newUser.getId());
        assertEquals("Jane Doe", newUser.getName());
        assertEquals("password456", newUser.getPassword());
        assertEquals("jane.doe@example.com", newUser.getEmail());
        assertEquals(UserStatus.CAMPUS_STUDENT, newUser.getType());
        assertEquals(0.0, newUser.getBalance());
    }

//    @Test
//    void testDefaultConstructor() {
//        User defaultUser = new User();
//        assertNotNull(defaultUser.getId());
//        assertEquals("user", defaultUser.getName());
//        assertEquals(UserStatus.CAMPUS_STUDENT, defaultUser.getType());
//        assertEquals(0.0, defaultUser.getBalance(), 0.001);
//    }


    @Test
    void testSetName() {
        user.setName("Jane Doe");
        assertEquals("Jane Doe", user.getName());
    }

    @Test
    void testSetPassword() {
        user.setPassword("newpassword123");
        assertEquals("newpassword123", user.getPassword());
    }

    @Test
    void testSetEmail() {
        user.setEmail("newemail@example.com");
        assertEquals("newemail@example.com", user.getEmail());
    }

    @Test
    void testSetType() {
        user.setType(UserStatus.CAMPUS_ADMIN);
        assertEquals(UserStatus.CAMPUS_ADMIN, user.getType());
    }

    @Test
    void testGetId() {
        assertNotNull(user.getId());
    }

    @Test
    void testAddToBalance() {
        user.addToBalance(100.0);
        assertEquals(100.0, user.getBalance());
    }

    @Test
    void testWithdrawFromBalance() {
        user.addToBalance(100.0);
        user.withdrawFromBalance(50.0);
        assertEquals(50.0, user.getBalance());
    }

    @Test
    void testWithdrawExceedingBalance() {
        user.addToBalance(50.0);
        assertEquals(50.0, user.getBalance());
        assertThrows(IllegalArgumentException.class, () -> {
            user.withdrawFromBalance(100.0);
        });
    }

    @Test
    void testWithrawNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            user.withdrawFromBalance(-50.0);
        });
    }

    @Test
    void testAddNegativeAmount() {
        assertEquals(0.0, user.getBalance());
        assertThrows(IllegalArgumentException.class, () -> {
            user.addToBalance(-50.0);
        });
    }

    @Test
    void testSetNegativeBalance() {
        try {
            user.addToBalance(-100.0);
        } catch (IllegalArgumentException e) {
            assertEquals(0.0, user.getBalance());
        }
    }

    @Test
    void testUniqueUUID() {
        User anotherUser = new User("Jane Doe", "password456", "jane.doe@example.com");
        assertNotNull(anotherUser.getId());
        assertNotEquals(user.getId(), anotherUser.getId());  // Les UUID doivent être uniques

        User user1 = new User("Alice", "password1", "alice@example.com");
        User user2 = new User("Bob", "password2", "bob@example.com");
        assertNotEquals(user1.getId(), user2.getId());
    }

}
