package br.com.devlovers.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.DeviceByApiKey;
import br.com.devlovers.domain.device.DeviceByApiKey.DeviceByApiKeyKey;
import br.com.devlovers.domain.device.DeviceById;
import br.com.devlovers.domain.device.DeviceByUserId;
import br.com.devlovers.domain.device.DeviceByUserId.DeviceByUserIdKey;
import br.com.devlovers.domain.device.dto.DeviceUpdateDTO;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.repositories.DeviceByApiKeyRepository;
import br.com.devlovers.repositories.DeviceByIdRepository;
import br.com.devlovers.repositories.DeviceByUserIdRepository;
import br.com.devlovers.repositories.DeviceRepository;
import br.com.devlovers.repositories.ReadingRepository;
import br.com.devlovers.repositories.UserByIdRepository;
import br.com.devlovers.services.exceptions.DeviceAlreadyExistsException;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import br.com.devlovers.util.DateRangePicker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceByIdRepository deviceByIdRepository;

    @Autowired
    private DeviceByApiKeyRepository deviceByApiKeyRepository;

    @Autowired
    private DeviceByUserIdRepository deviceByUserIdRepository;

    @Autowired
    private UserByIdRepository userByIdRepository;

    @Autowired
    private ReactiveCassandraTemplate cassandraTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ReadingRepository readingRepository;

    @Transactional
    public Mono<Device> insertDevice(Device device) {
        return userByIdRepository.findById(device.getOwnerId())
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Usuário de ID " + device.getOwnerId() + " não encontrado")))
                .then(deviceRepository.findByKey_Name(device.getName())
                        .filter(Device::getIsActive)
                        .flatMap(existingDevice -> Mono.<Device>error(
                                new DeviceAlreadyExistsException(
                                        "Dispositivo " + device.getName() + " já existente na base de dados")))
                        .switchIfEmpty(
                                Mono.defer(() -> {
                                    return deviceRepository.save(device)
                                            .flatMap(savedDevice -> {
                                                DeviceById deviceById = new DeviceById(savedDevice);

                                                DeviceByApiKeyKey apiKeyKey = new DeviceByApiKeyKey(savedDevice.getApiKey(), savedDevice.getId());
                                                DeviceByApiKey deviceByApiKey = new DeviceByApiKey(savedDevice, apiKeyKey);

                                                DeviceByUserIdKey userIdKey = new DeviceByUserIdKey(savedDevice.getOwnerId(), savedDevice.getId());
                                                DeviceByUserId deviceByUserId = new DeviceByUserId(savedDevice, userIdKey);

                                                return deviceByIdRepository.save(deviceById)
                                                        .then(deviceByApiKeyRepository.save(deviceByApiKey))
                                                        .then(deviceByUserIdRepository.save(deviceByUserId))
                                                        .thenReturn(savedDevice);
                                            });
                                })
                        ));
    }

    @Transactional
    public Mono<Reading> insertReading(String apiKey, ReadingType readingType, Double value) {
        return deviceByApiKeyRepository.findByKeyApiKey(apiKey)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: Chave " + apiKey)))
                .flatMap(deviceByApiKey -> {
                    Instant now = Instant.now();
                    Reading reading = new Reading(
                            new Reading.DeviceReadingKey(deviceByApiKey.getKey().getId(), LocalDate.now(), readingType, now),
                            value);

                    int ttl = 63072000;
                    return cassandraTemplate
                            .insert(reading, InsertOptions.builder().ttl(ttl).build())
                            .thenReturn(reading);
                });
    }

    public Flux<Reading> findReadings(UUID deviceId, ReadingType readingType, Instant start, Instant end) {
        return findDeviceById(deviceId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + deviceId)))
            .flatMapMany(device -> {
                List<LocalDate> dateRange = DateRangePicker.generate(start, end);
                
                if (dateRange.isEmpty()) {
                    return Flux.empty();
                }
                
                return Flux.concat(dateRange.stream()
                        .map(date -> readingRepository.findByAllCriteria(deviceId, date, readingType, start, end))
                        .collect(Collectors.toList()));
            });
    }

    public Flux<DeviceByUserId> findDevicesByUserId(UUID userId) {
        return userByIdRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário de ID " + userId + " não encontrado")))
                .flatMapMany(user -> deviceByUserIdRepository.findByKeyOwnerId(userId)
                        .filter(DeviceByUserId::getIsActive));
    }

    public Mono<DeviceByApiKey> findDeviceByApiKey(String apiKey) {
        return deviceByApiKeyRepository.findByKeyApiKey(apiKey)
                .filter(DeviceByApiKey::getIsActive)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Dispositivo não encontrado com a chave de API fornecida: " + apiKey)));
    }

    public Mono<DeviceById> findDeviceById(UUID id) {
        return deviceByIdRepository.findById(id)
                .filter(DeviceById::getIsActive)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + id)));
    }

    public Mono<Device> findDeviceByName(String name) {
        return deviceRepository.findByKey_Name(name)
                .filter(Device::getIsActive)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: " + name)));
    }

    @Transactional
    public Mono<Device> updateDevice(UUID id, DeviceUpdateDTO data) {
        return findDeviceById(id)
                .filter(DeviceById::getIsActive)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + id)))
                .flatMap(deviceById -> {
                    deviceById.update(data);

                    Device.DeviceKey deviceKey = new Device.DeviceKey(deviceById.getName(), id);

                    return deviceRepository.findById(deviceKey)
                            .flatMap(device -> {
                                device.update(data);

                                return deviceByApiKeyRepository.findByKeyApiKey(device.getApiKey())
                                        .flatMap(deviceByApiKey -> {
                                            deviceByApiKey.update(data);
                                            return deviceByApiKeyRepository.save(deviceByApiKey);
                                        })

                                        .then(deviceByUserIdRepository.findByKeyOwnerId(device.getOwnerId())
                                                .filter(deviceByUserId -> deviceByUserId.getKey().getId().equals(id))
                                                .flatMap(deviceByUserId -> {
                                                    deviceByUserId.update(data);
                                                    return deviceByUserIdRepository.save(deviceByUserId);
                                                })
                                                .then(deviceRepository.save(device))
                                                .then(deviceByIdRepository.save(deviceById))
                                                .thenReturn(device));
                            });
                });
    }

    @Transactional
    public Mono<Void> deleteDevice(UUID id) {
        return deviceByIdRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + id)))
                .flatMap(deviceById -> {
                        Device.DeviceKey deviceKey = new Device.DeviceKey(deviceById.getName(), id);

                    return deviceRepository.findById(deviceKey)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + id)))
                            .flatMap(device -> {
                                device.setIsActive(false);

                                DeviceByApiKeyKey deviceByApiKeyKey = new DeviceByApiKeyKey(device.getApiKey(), id);
                                DeviceByUserIdKey deviceByUserIdKey = new DeviceByUserIdKey(device.getOwnerId(), id);

                                return deviceRepository.save(device)
                                        .then(deviceByIdRepository.delete(deviceById))
                                        .then(deviceByApiKeyRepository.deleteById(deviceByApiKeyKey))
                                        .then(deviceByUserIdRepository.deleteById(deviceByUserIdKey))
                                        .then();
                            });
                });
    }

    public Mono<Void> restartDevice(UUID id) {
        return deviceByIdRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + id)))
                .flatMap(deviceById -> {
                    String deviceIp = deviceById.getIp();

                    String url = "http://" + deviceIp + "/?rst=";

                    return webClientBuilder.build()
                            .get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .then();
                });
    }

    public Mono<Void> forceRead(UUID id) {
        return deviceByIdRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Dispositivo não encontrado: ID " + id)))
                .flatMap(deviceById -> {
                    String deviceIp = deviceById.getIp();

                    String url = "http://" + deviceIp + "/cm?cmnd=TelePeriod";

                    return webClientBuilder.build()
                            .get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .then();
                });
    }
}