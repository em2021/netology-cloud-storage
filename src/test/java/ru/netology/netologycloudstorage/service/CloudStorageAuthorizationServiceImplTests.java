package ru.netology.netologycloudstorage.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.netologycloudstorage.entity.User;
import ru.netology.netologycloudstorage.entity.UserSession;
import ru.netology.netologycloudstorage.repository.CloudStorageUserJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class CloudStorageAuthorizationServiceImplTests {

    @Mock
    private CloudStorageUserJpaRepository cloudStorageUserJpaRepositoryMock;
    @InjectMocks
    private CloudStorageAuthorizationServiceImpl cloudStorageAuthorizationServiceImpl;

    @BeforeAll
    public static void beforeAll() {
        System.out.println("CloudStorageAuthorizationServiceImpl tests started");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("CloudStorageAuthorizationServiceImpl test started");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("CloudStorageAuthorizationServiceImpl test completed");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("CloudStorageAuthorizationServiceImpl tests completed");
    }

    @Test
    public void testAuthorizeUser_whenOptionalIsEmpty_throwException() {
        //given:
        Optional<List<User>> user = Optional.empty();
        Mockito.when(cloudStorageUserJpaRepositoryMock
                        .findByLoginAndPassword(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(user);
        //when:
        //then:
        Assertions.assertThrows(RuntimeException.class,
                () -> cloudStorageAuthorizationServiceImpl.authorizeUser(Mockito.anyString(), Mockito.anyString()));
    }

    @ParameterizedTest
    @MethodSource("sourceForAuthorizeUser")
    public void testAuthorizeUser_whenPasswordOrLoginDontMatch_throwException(String login, String password) {
        //given:
        User user = new User(1,
                "User",
                "Test",
                1,
                "911",
                "user@test.com",
                "123");
        Optional userOpt = Optional.of(List.of(user));
        Mockito.when(cloudStorageUserJpaRepositoryMock
                        .findByLoginAndPassword(login, password))
                .thenReturn(userOpt);
        //when:
        //then:
        Assertions.assertThrows(RuntimeException.class,
                () -> cloudStorageAuthorizationServiceImpl.authorizeUser(login, password));
    }

    @Test
    public void testAuthorizeUser_whenPasswordOrLoginMatch_userSessionReturned() {
        //given:
        User user = new User(1,
                "User",
                "Test",
                1,
                "911",
                "user@test.com",
                "123");
        Optional userOpt = Optional.of(List.of(user));
        Mockito.when(cloudStorageUserJpaRepositoryMock.findByLoginAndPassword(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(userOpt);
        //when:
        String actual = cloudStorageAuthorizationServiceImpl.authorizeUser("user@test.com", "123");
        //then:
        Assertions.assertNotNull(actual);
    }

    @Test
    public void testGetUserIdBySession_whenUserFound_returnUserId() {
        //given:
        String sessionId = "123";
        Integer expected = 1;
        UserSession userSession = new UserSession(sessionId, 1);
        Optional<UserSession> userSessionOpt = Optional.of(userSession);
        Mockito.when(cloudStorageUserJpaRepositoryMock.findBySessionId(sessionId))
                .thenReturn(userSessionOpt);
        //when:
        Integer actual = cloudStorageAuthorizationServiceImpl.getUserIdBySession("123");
        //then:
        Assertions.assertEquals(actual, expected);
    }

    @Test
    public void testGetUserIdBySession_whenUserNotFound_throwException() {
        //given:
        String sessionId = "123";
        Optional<UserSession> userSessionOpt = Optional.empty();
        Mockito.when(cloudStorageUserJpaRepositoryMock.findBySessionId(Mockito.anyString()))
                .thenReturn(userSessionOpt);
        //when:
        //then:
        Assertions.assertThrows(RuntimeException.class,
                () -> cloudStorageAuthorizationServiceImpl.getUserIdBySession(sessionId));
    }

    @Test
    public void testLogoutUser_whenCalled_callOnceDeleteUserSession() {
        //given:
        //when:
        cloudStorageAuthorizationServiceImpl.logoutUser(Mockito.anyString());
        //then:
        Mockito.verify(cloudStorageUserJpaRepositoryMock, Mockito.times(1))
                .deleteUserSession(Mockito.anyString());
    }

    @Test
    public void testGenerateUserSessionId_whenCalled_returnString() {
        //given:
        //when:
        String sessionId = cloudStorageAuthorizationServiceImpl.generateUserSessionId();
        //then:
        Assertions.assertNotNull(sessionId);
    }

    @Test
    public void testSaveSessionId_whenCalled_callOnceSaveSessionId() {
        //given:
        int expected = 1;
        //when:
        cloudStorageAuthorizationServiceImpl.saveSessionId(Mockito.anyString(), Mockito.anyInt());
        //then:
        Mockito.verify(cloudStorageUserJpaRepositoryMock, Mockito.times(expected))
                .saveSessionId(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void testTrimSessionId_whenCalled_returnTrimmedSessionId() {
        //given:
        String sessionId = "Bearer 123";
        String expected = "123";
        //when:
        String actual = cloudStorageAuthorizationServiceImpl.trimSessionId(sessionId);
        //then:
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> sourceForAuthorizeUser() {
        //given:
        return Stream.of(Arguments.of("wrongLogin", "123"),
                Arguments.of("user@test.com", "wrongPassword"));
    }
}