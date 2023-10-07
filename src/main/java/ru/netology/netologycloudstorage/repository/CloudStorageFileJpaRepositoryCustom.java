package ru.netology.netologycloudstorage.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.netologycloudstorage.entity.CloudStorageFile;

import java.util.List;
import java.util.Optional;

public interface CloudStorageFileJpaRepositoryCustom {

    @Modifying
    @Transactional
    @Query(value = "insert into cloud_storage.files (name, size, owner)\n" +
            "values (:name, :size, :user_id)", nativeQuery = true)
    Integer saveFile(@Param("name") String filename,
                  @Param("size") Integer size,
                  @Param("user_id") Integer userId);

    @Modifying
    @Transactional
    @Query(("delete from CloudStorageFile f where f.name like :name and f.owner = :user_id"))
    void deleteFile(@Param("name") String filename,
                    @Param("user_id") Integer userId);

    @Modifying
    @Transactional
    @Query(("update CloudStorageFile f set f.name = :new_name where f.name like :name and f.owner = :owner"))
    void renameFile(@Param("name") String filename,
                    @Param("new_name") String newFilename,
                    @Param("owner") Integer userId);

    @Query(("select s from CloudStorageFile s where s.owner = :owner"))
    Optional<List<CloudStorageFile>> findByOwner(@Param("owner") Integer owner,
                                                 Pageable pageable);
}