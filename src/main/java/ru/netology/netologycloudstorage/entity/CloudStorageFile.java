package ru.netology.netologycloudstorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@IdClass(CloudStorageFileRelationshipId.class)
@Table(name = "files", schema = "cloud_storage")
@JsonIgnoreProperties({"owner"})
public class CloudStorageFile {

    @JsonProperty("filename")
    @Id
    @Column(nullable = false)
    String name;
    @JsonProperty("size")
    @Column(nullable = false)
    Integer size;
    @Id
    @Column(nullable = false)
    Integer owner;
}