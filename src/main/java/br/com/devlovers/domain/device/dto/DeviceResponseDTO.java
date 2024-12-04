package br.com.devlovers.domain.device.dto;

import java.util.UUID;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.DeviceById;
import br.com.devlovers.domain.device.DeviceByUserId;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.device.enums.DeviceType;
import br.com.devlovers.domain.device.enums.Function;
import br.com.devlovers.domain.device.enums.Sensor;

public record DeviceResponseDTO(

    UUID id,
    String name,
    Branch branch,
    Function function,
    DeviceType deviceType,
    Sensor sensor,
    String equipment,
    String tag,
    String sector,
    String ip,
    Long patrimony,
    Double minWorkingTemp,
    Double maxWorkingTemp,
    Double minWorkingHumidity,
    Double maxWorkingHumidity,
    String apiKey

) {

    public DeviceResponseDTO(Device device) {
        this(device.getId(), device.getName(), device.getBranch(), device.getFunction(), device.getDeviceType(), device.getSensor(), device.getTypeOfEquipment(), device.getTag(), device.getSector(), device.getIp(), device.getPatrimony(), device.getMinWorkingTemp(), device.getMaxWorkingTemp(), device.getMinWorkingHumidity(), device.getMaxWorkingHumidity(), device.getApiKey());
    }

    public DeviceResponseDTO(DeviceById device) {
        this(device.getId(), device.getName(), device.getBranch(), device.getFunction(), device.getDeviceType(), device.getSensor(), device.getTypeOfEquipment(), device.getTag(), device.getSector(), device.getIp(), device.getPatrimony(), device.getMinWorkingTemp(), device.getMaxWorkingTemp(), device.getMinWorkingHumidity(), device.getMaxWorkingHumidity(), device.getApiKey());
    }

    public DeviceResponseDTO(DeviceByUserId device) {
        this(device.getKey().getId(), device.getName(), device.getBranch(), device.getFunction(), device.getDeviceType(), device.getSensor(), device.getTypeOfEquipment(), device.getTag(), device.getSector(), device.getIp(), device.getPatrimony(), device.getMinWorkingTemp(), device.getMaxWorkingTemp(), device.getMinWorkingHumidity(), device.getMaxWorkingHumidity(), device.getApiKey());
    }
}
