package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MigrationSchema {
    private Integer installed_rank;
    private String version;
    private Long CRC;
    private String installed_by;
    private String filename;
    private String type;

    public Integer getInstalled_rank() {
        return installed_rank;
    }

    public void setInstalled_rank(Integer installed_rank) {
        this.installed_rank = installed_rank;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getCRC() {
        return CRC;
    }

    public void setCRC(Long CRC) {
        this.CRC = CRC;
    }

    public String getInstalled_by() {
        return installed_by;
    }

    public void setInstalled_by(String installed_by) {
        this.installed_by = installed_by;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
