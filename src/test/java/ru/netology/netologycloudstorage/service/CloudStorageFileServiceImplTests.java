package ru.netology.netologycloudstorage.service;

import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.Link;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.netology.netologycloudstorage.entity.CloudStorageFile;
import ru.netology.netologycloudstorage.exception.InputDataError;
import ru.netology.netologycloudstorage.exception.UploadingFileError;
import ru.netology.netologycloudstorage.repository.CloudStorageFileJpaRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class CloudStorageFileServiceImplTests {

    @Mock
    private CloudStorageFileJpaRepository cloudStorageFileJpaRepositoryMock;
    @Mock
    private RestClient restClientMock;
    @InjectMocks
    private CloudStorageFileServiceImpl cloudStorageFileServiceImpl;

    @BeforeAll
    public static void beforeAll() {
        System.out.println("CloudStorageFileServiceImpl tests started");
    }

    @BeforeEach
    public void setUp() {
        System.out.println("CloudStorageFileServiceImpl test started");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("CloudStorageFileServiceImpl test completed");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("CloudStorageFileServiceImpl tests completed");
    }

    @Test
    public void testGetUploadLink_whenCalled_callOnceGetUploadLink()
            throws WrongMethodException, ServerIOException, IOException {
        //given:
        Mockito.when(restClientMock.getUploadLink(Mockito.anyString(), Mockito.anyBoolean()))
                .thenReturn(new Link());
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.getUploadLink("test", 1);
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .getUploadLink(Mockito.anyString(), Mockito.anyBoolean());
    }

    @ParameterizedTest
    @MethodSource("sourceForGetUploadLinkTest")
    public void testGetUploadLink_whenGetUploadLinkThrowsException_throwIDEException(Class<Exception> ex)
            throws WrongMethodException, ServerIOException, IOException {
        //given:
        Mockito.when(restClientMock.getUploadLink(Mockito.anyString(), Mockito.anyBoolean()))
                .thenThrow(ex);
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.getUploadLink(Mockito.anyString(), Mockito.anyInt()));
    }

    @Test
    public void testUploadFile_whenCalled_callGetUploadLinkOnce() throws ServerException, IOException {
        //given:
        byte[] bytes = new byte[1];
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.uploadFile(1,
                "file",
                new MockMultipartFile("file", bytes));
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .getUploadLink(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void testUploadFile_whenCalled_callOnceUploadFile() throws ServerException, IOException {
        //given:
        byte[] bytes = new byte[1];
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.uploadFile(1,
                "file",
                new MockMultipartFile("file", bytes));
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .uploadFile(Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testUploadFile_whenCalled_callOnceSaveFile() {
        //given:
        byte[] bytes = new byte[1];
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.uploadFile(1,
                "file",
                new MockMultipartFile("file", bytes));
        //then:
        Mockito.verify(cloudStorageFileJpaRepositoryMock, Mockito.times(expected))
                .saveFile(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @ParameterizedTest
    @MethodSource("sourceForUploadFileTest")
    public void testUploadFile_whenUploadFileThrowsException_throwIDEException(Class<Exception> ex)
            throws ServerException, IOException {
        //given:
        Mockito.doThrow(ex)
                .when(restClientMock)
                .uploadFile(Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());
        byte[] bytes = new byte[1];
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.uploadFile(1,
                        "file",
                        new MockMultipartFile("file", bytes)));
    }

    @Test
    public void testUploadFile_whenSaveFileReturnsZero_callDeleteFileOnce()
            throws ServerException, IOException {
        //given:
        Mockito.when(cloudStorageFileJpaRepositoryMock.saveFile(Mockito.anyString(),
                        Mockito.anyInt(),
                        Mockito.anyInt()))
                .thenReturn(1);
        byte[] bytes = new byte[1];
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.uploadFile(1,
                "file",
                new MockMultipartFile("file", bytes));
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .getUploadLink(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void testCreateTempFile_whenCalled_returnTempFile() throws IOException {
        //given:
        File tempDir = new File("src/test/temp");
        tempDir.mkdir();
        int userId = 1;
        byte[] bytes = new byte[1];
        //when:
        File actual = cloudStorageFileServiceImpl.createTempFile(tempDir,
                1,
                new MockMultipartFile("file", bytes));
        //then:
        Assertions.assertNotNull(actual);
        actual.delete();
        tempDir.delete();
    }

    @Test
    public void testDownloadFile_whenFilenameBlank_throwIDEException() {
        //given:
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.downloadFile(1,
                        ""));
    }

    @Test
    public void testDownloadFile_whenCalled_callOnceDownloadFile() throws ServerException, IOException {
        //given:
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.downloadFile(1, "file");
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .downloadFile(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("sourceForDownloadFileTest")
    public void testDownloadFile_whenDownloadFileThrowsException_throwUFEException(Class<Exception> ex)
            throws ServerException, IOException {
        //given:
        Mockito.doThrow(ex)
                .when(restClientMock)
                .downloadFile(Mockito.anyString(), Mockito.any(), Mockito.any());
        //when:
        //then:
        Assertions.assertThrows(UploadingFileError.class,
                () -> cloudStorageFileServiceImpl.downloadFile(1,
                        "file"));
    }

    @Test
    public void testDeleteFile_whenFilenameBlank_throwIDEException() {
        //given:
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.deleteFile(1,
                        ""));
    }

    @Test
    public void testDeleteFile_whenCalled_callOnceDelete() throws ServerIOException, IOException {
        //given:
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.deleteFile(1, "file");
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .delete(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void testDeleteFile_whenCalled_callOnceDeleteFile() {
        //given:
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.deleteFile(1, "file");
        //then:
        Mockito.verify(cloudStorageFileJpaRepositoryMock, Mockito.times(expected))
                .deleteFile(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void testRenameFile_whenFilenameBlank_throwIDEException() {
        //given:
        int expected = 1;
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.renameFile("",
                        new CloudStorageFile(), 1));
    }

    @Test
    public void testRenameFile_whenNewFileNull_throwIDEException() {
        //given:
        CloudStorageFile newFile = null;
        int expected = 1;
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.renameFile("file", newFile, 1));
    }

    @Test
    public void testRenameFile_whenNewFileNameBlank_throwIDEException() {
        //given:
        CloudStorageFile newFile = new CloudStorageFile();
        newFile.setName("");
        int expected = 1;
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.renameFile("file", newFile, 1));
    }

    @Test
    public void testRenameFile_whenCalled_callOnceMove() throws ServerIOException, IOException {
        //given:
        CloudStorageFile newFile = new CloudStorageFile();
        newFile.setName("newFile");
        int expected = 2;
        //when:
        cloudStorageFileServiceImpl.renameFile("file", newFile, 1);
        //then:
        Mockito.verify(restClientMock, Mockito.atMost(expected))
                .move(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    public void testRenameFile_whenCalled_callOnceRenameFile() {
        //given:
        CloudStorageFile newFile = new CloudStorageFile();
        newFile.setName("newFile");
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.renameFile("file", newFile, 1);
        //then:
        Mockito.verify(cloudStorageFileJpaRepositoryMock, Mockito.times(expected))
                .renameFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @ParameterizedTest
    @MethodSource("sourceForUploadFileTest")
    public void testRenameFile_whenMoveThrowsException_throwUFEException(Class<Exception> ex)
            throws ServerException, IOException {
        //given:
        Mockito.doThrow(ex)
                .when(restClientMock)
                .move(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
        CloudStorageFile csf = new CloudStorageFile();
        csf.setName("newFile");
        //when:
        //then:
        Assertions.assertThrows(UploadingFileError.class,
                () -> cloudStorageFileServiceImpl.renameFile("file",
                        csf, 1));
    }

    @Test
    public void testMakeFolder_whenCalled_callOnceMakeFolder() throws ServerIOException, IOException {
        //given:
        int expected = 1;
        //when:
        cloudStorageFileServiceImpl.makeFolder(1);
        //then:
        Mockito.verify(restClientMock, Mockito.times(expected))
                .makeFolder(Mockito.anyString());
    }

    @Test
    public void testGetFilesList_whenLimitLessThanOne_throwException() {
        //given:
        int userId = 1;
        int limit = -1;
        //when:
        //then:
        Assertions.assertThrows(InputDataError.class,
                () -> cloudStorageFileServiceImpl.getFilesList(userId, limit));
    }

    @Test
    public void testGetFilesList_whenCalled_callOnceFindByOwner() {
        //given:
        int expected = 1;
        CloudStorageFile file = new CloudStorageFile();
        List<CloudStorageFile> list = List.of(file);
        Optional<List<CloudStorageFile>> opt = Optional.of(list);
        Mockito.doReturn(opt)
                .when(cloudStorageFileJpaRepositoryMock)
                .findByOwner(Mockito.anyInt(), Mockito.any());
        //when:
        cloudStorageFileServiceImpl.getFilesList(1, 1);
        //then:
        Mockito.verify(cloudStorageFileJpaRepositoryMock, Mockito.times(expected))
                .findByOwner(Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testGetFilesList_whenListNotPresent_throwException() {
        //given:
        Optional<List<CloudStorageFile>> opt = Optional.empty();
        Mockito.when(cloudStorageFileJpaRepositoryMock.findByOwner(Mockito.anyInt(), Mockito.any()))
                .thenReturn(opt);
        //when:
        //then:
        Assertions.assertThrows(RuntimeException.class,
                () -> cloudStorageFileServiceImpl.getFilesList(1, 1));
    }

    public static Stream<Arguments> sourceForGetUploadLinkTest() {
        //given:
        return Stream.of(Arguments.of(WrongMethodException.class),
                Arguments.of(ServerIOException.class),
                Arguments.of(IOException.class));
    }

    public static Stream<Arguments> sourceForUploadFileTest() {
        //given:
        return Stream.of(Arguments.of(ServerIOException.class),
                Arguments.of(IOException.class));
    }

    public static Stream<Arguments> sourceForDownloadFileTest() {
        //given:
        return Stream.of(Arguments.of(ServerIOException.class),
                Arguments.of(IOException.class));
    }
}