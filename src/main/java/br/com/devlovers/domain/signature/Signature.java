package br.com.devlovers.domain.signature;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Table("tb_signatures")
public class Signature implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SignatureKey {

        @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
        private UUID userId;

        @PrimaryKeyColumn(name = "picture_id", type = PrimaryKeyType.CLUSTERED)
        private UUID pictureId;
    }

    @PrimaryKey
    private SignatureKey key;

    @Column("file_path")
    private String filePath;

    @Column("file_name")
    private String fileName;

    private String fileExtension;

    private String name;

    public SignatureKey getKey() {
        return key;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setKey(SignatureKey key) {
        this.key = key;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
