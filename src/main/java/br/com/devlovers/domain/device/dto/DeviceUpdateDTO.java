package br.com.devlovers.domain.device.dto;

import br.com.devlovers.domain.device.enums.DeviceType;
import br.com.devlovers.domain.device.enums.Sensor;
import br.com.devlovers.domain.device.validations.IP;

public record DeviceUpdateDTO(

    String name,
    String branch,
    String function,
    String equipment,
    Long patrimony,
    String tag,
    String sector,
    Double minWorkingTemp,
    Double maxWorkingTemp,
    Double minWorkingHumidity,
    Double maxWorkingHumidity,

    DeviceType deviceType,
    Sensor sensor,
    
    @IP
    String ip
    
) {

}
