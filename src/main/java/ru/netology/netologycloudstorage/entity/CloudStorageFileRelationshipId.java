package ru.netology.netologycloudstorage.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudStorageFileRelationshipId implements Serializable {

    String name;
    Integer owner;
}