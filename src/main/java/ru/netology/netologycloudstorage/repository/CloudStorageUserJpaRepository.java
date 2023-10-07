package ru.netology.netologycloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.netologycloudstorage.entity.User;

@Repository
public interface CloudStorageUserJpaRepository extends JpaRepository<User, Integer>, CloudStorageUserRepositoryCustom {

}