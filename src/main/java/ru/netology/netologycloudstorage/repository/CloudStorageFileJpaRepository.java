package ru.netology.netologycloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.netologycloudstorage.entity.CloudStorageFile;
import ru.netology.netologycloudstorage.entity.CloudStorageFileRelationshipId;

@Repository
public interface CloudStorageFileJpaRepository extends JpaRepository <CloudStorageFile, CloudStorageFileRelationshipId>,
        CloudStorageFileJpaRepositoryCustom {

}