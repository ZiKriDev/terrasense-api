package br.com.devlovers.domain.device;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import br.com.devlovers.domain.device.dto.DeviceUpdateDTO;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.device.enums.DeviceType;
import br.com.devlovers.domain.device.enums.Function;
import br.com.devlovers.domain.device.enums.Sensor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("tb_devices_by_id")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class DeviceById {

    @PrimaryKey
    private UUID id;

    private String name;
    private Branch branch;
    private Function function;
    private String typeOfEquipment;
    private Long patrimony;
    private String tag;
    private String sector;
    private Double minWorkingTemp;
    private Double maxWorkingTemp;
    private Double minWorkingHumidity;
    private Double maxWorkingHumidity;
    private String ip;
    private String apiKey;
    private UUID ownerId;
    private DeviceType deviceType;
    private Sensor sensor;
    private Boolean isActive;

    public DeviceById(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.branch = device.getBranch();
        this.function = device.getFunction();
        this.typeOfEquipment = device.getTypeOfEquipment();
        this.patrimony = device.getPatrimony();
        this.tag = device.getTag();
        this.sector = device.getSector();
        this.minWorkingTemp = device.getMinWorkingTemp();
        this.maxWorkingTemp = device.getMaxWorkingTemp();
        this.minWorkingHumidity = device.getMinWorkingHumidity();
        this.maxWorkingHumidity = device.getMaxWorkingHumidity();
        this.ip = device.getIp();
        this.apiKey = device.getApiKey();
        this.ownerId = device.getOwnerId();
        this.isActive = device.getIsActive();
        this.deviceType = device.getDeviceType();
        this.sensor = device.getSensor();
    }

    public void update(DeviceUpdateDTO data) {
        if (data.name() != null) {
            setName(data.name());
        }

        if (data.branch() != null) {
            this.branch = Branch.valueOf(data.branch());
        }

        if (data.function() != null) {
            this.function = Function.valueOf(data.function());
        }

        if (data.equipment() != null) {
            this.typeOfEquipment = data.equipment();
        }

        if (data.patrimony() != null) {
            this.patrimony = data.patrimony();
        }

        if (data.tag() != null) {
            this.tag = data.tag();
        }

        if (data.sector() != null) {
            this.sector = data.sector();
        }

        if (data.minWorkingTemp() != null) {
            this.minWorkingTemp = data.minWorkingTemp();
        }

        if (data.maxWorkingTemp() != null) {
            this.maxWorkingTemp = data.maxWorkingTemp();
        }

        if (data.minWorkingHumidity() != null) {
            this.minWorkingHumidity = data.minWorkingHumidity();
        }

        if (data.maxWorkingHumidity() != null) {
            this.maxWorkingHumidity = data.maxWorkingHumidity();
        }

        if (data.ip() != null) {
            this.ip = data.ip();
        }

        if (data.deviceType() != null) {
            this.deviceType = data.deviceType();
        }

        if (data.sensor() != null) {
            this.sensor = data.sensor();
        }
    }

}