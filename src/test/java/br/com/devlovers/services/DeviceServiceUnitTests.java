package br.com.devlovers.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.cassandra.core.EntityWriteResult;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.DeviceByApiKey;
import br.com.devlovers.domain.device.DeviceByApiKey.DeviceByApiKeyKey;
import br.com.devlovers.domain.device.DeviceById;
import br.com.devlovers.domain.device.DeviceByUserId;
import br.com.devlovers.domain.device.DeviceByUserId.DeviceByUserIdKey;
import br.com.devlovers.domain.device.dto.DeviceUpdateDTO;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.device.enums.DeviceType;
import br.com.devlovers.domain.device.enums.Function;
import br.com.devlovers.domain.device.enums.Sensor;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.domain.user.UserById;
import br.com.devlovers.domain.user.enums.Role;
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
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DeviceServiceUnitTests {

        @Mock
        private DeviceRepository deviceRepository;

        @Mock
        private DeviceByIdRepository deviceByIdRepository;

        @Mock
        private DeviceByApiKeyRepository deviceByApiKeyRepository;

        @Mock
        private DeviceByUserIdRepository deviceByUserIdRepository;

        @Mock
        private UserByIdRepository userByIdRepository;

        @Mock
        private ReadingRepository readingRepository;

        @Mock
        private WebClient.Builder webClientBuilder;

        @Mock
        private WebClient webClient;

        @Mock
        private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

        @Mock
        private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

        @Mock
        private WebClient.ResponseSpec responseSpec;

        @Mock
        private ReactiveCassandraTemplate cassandraTemplate;

        @InjectMocks
        private DeviceService deviceService;

        private Device device;
        private DeviceById deviceById;
        private DeviceByApiKey deviceByApiKey;
        private UserById userById;

        private final UUID DEVICE_ID = UUID.randomUUID();
        private final UUID USER_ID = UUID.randomUUID();
        private final String API_KEY = "sampleApiKey";
        private final String DEVICE_NAME = "TestDevice";

        @BeforeEach
        void setUp() {
                device = new Device(new Device.DeviceKey(DEVICE_NAME, DEVICE_ID), Branch.SAO_PAULO, Function.EQUIPMENT,
                                "Geladeira 04", 5512L, "SON-OFF-TEST-TAG", "Laboratório", 15.2, 15.9, 40.1, 60.2, true,
                                "192.168.1.3",
                                API_KEY, DeviceType.THR316, Sensor.AM2301, USER_ID);
                deviceById = new DeviceById(device);
                deviceByApiKey = new DeviceByApiKey(device, new DeviceByApiKeyKey(API_KEY, DEVICE_ID));
                userById = new UserById(USER_ID, "test_user@gmail.com", Role.USER);
        }

        @Test
        @DisplayName("Should insert device when everything is OK")
        void insertDeviceCase1() {
                when(userByIdRepository.findById(USER_ID))
                                .thenReturn(Mono.just(new UserById(USER_ID, "test_user@gmail.com", Role.USER)));
                when(deviceRepository.findByKey_Name(DEVICE_NAME)).thenReturn(Mono.empty());
                when(deviceRepository.save(device)).thenReturn(Mono.just(device));

                when(deviceByIdRepository.save(any(DeviceById.class))).thenReturn(Mono.empty());
                when(deviceByApiKeyRepository.save(any(DeviceByApiKey.class))).thenReturn(Mono.empty());
                when(deviceByUserIdRepository.save(any(DeviceByUserId.class))).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.insertDevice(device))
                                .expectNext(device)
                                .verifyComplete();

                verify(deviceRepository, times(1)).save(device);
                verify(deviceByIdRepository, times(1)).save(any(DeviceById.class));
                verify(deviceByApiKeyRepository, times(1)).save(any(DeviceByApiKey.class));
                verify(deviceByUserIdRepository, times(1)).save(any(DeviceByUserId.class));
        }

        @Test
        @DisplayName("Should throw exception when user does not exist on device insert")
        void insertDeviceCase2() {
                when(userByIdRepository.findById(USER_ID)).thenReturn(Mono.empty());
                lenient().when(deviceRepository.findByKey_Name(DEVICE_NAME)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.insertDevice(device))
                                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                                                throwable.getMessage()
                                                                .equals("Usuário de ID " + USER_ID + " não encontrado"))
                                .verify();

                verify(deviceRepository, never()).save(any(Device.class));
                verify(deviceByIdRepository, never()).save(any(DeviceById.class));
                verify(deviceByApiKeyRepository, never()).save(any(DeviceByApiKey.class));
                verify(deviceByUserIdRepository, never()).save(any(DeviceByUserId.class));
        }

        @Test
        @DisplayName("Should throw exception when device with the same name already exists in the database")
        void insertDeviceCase3() {
                when(userByIdRepository.findById(USER_ID)).thenReturn(Mono.just(userById));
                when(deviceRepository.findByKey_Name(DEVICE_NAME)).thenReturn(Mono.just(device));

                StepVerifier.create(deviceService.insertDevice(device))
                                .expectErrorMatches(throwable -> throwable instanceof DeviceAlreadyExistsException &&
                                                throwable.getMessage()
                                                                .equals("Dispositivo " + DEVICE_NAME
                                                                                + " já existente na base de dados"))
                                .verify();

                verify(deviceRepository, never()).save(any(Device.class));
                verify(deviceByIdRepository, never()).save(any(DeviceById.class));
                verify(deviceByApiKeyRepository, never()).save(any(DeviceByApiKey.class));
                verify(deviceByUserIdRepository, never()).save(any(DeviceByUserId.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should insert reading when everything is OK")
        void insertReadingCase1() {
                when(deviceByApiKeyRepository.findByKeyApiKey(API_KEY)).thenReturn(Mono.just(deviceByApiKey));
                when(cassandraTemplate.insert(any(Reading.class), any(InsertOptions.class)))
                                .thenReturn(Mono.just(mock(EntityWriteResult.class)));

                StepVerifier.create(deviceService.insertReading(API_KEY, ReadingType.TEMPERATURE, 25.0))
                                .expectNextMatches(r -> r.getKey().getDeviceId().equals(DEVICE_ID)
                                                && r.getValue().equals(25.0))
                                .verifyComplete();

                verify(deviceByApiKeyRepository, times(1)).findByKeyApiKey(API_KEY);
                verify(cassandraTemplate, times(1)).insert(any(Reading.class), any(InsertOptions.class));
        }

        @Test
        @DisplayName("Should throw exception when API key is not found for device")
        void insertReadingCase2() {
                when(deviceByApiKeyRepository.findByKeyApiKey(API_KEY)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.insertReading(API_KEY, ReadingType.TEMPERATURE, 25.0))
                                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                                .verify();

                verify(deviceByApiKeyRepository, times(1)).findByKeyApiKey(API_KEY);
                verify(cassandraTemplate, never()).insert(any(Reading.class), any(InsertOptions.class));
        }

        @Test
        @DisplayName("Should return readings when everything is OK")
        void findReadingsCase1() {
            Instant start = Instant.now().minusSeconds(3600);
            Instant end = Instant.now();

            Reading reading = new Reading(
                    new Reading.DeviceReadingKey(DEVICE_ID, LocalDate.now(), ReadingType.TEMPERATURE, Instant.now()), 25.0);

            List<LocalDate> dateRange = DateRangePicker.generate(start, end);

            when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));

            for (LocalDate date : dateRange) {
                when(readingRepository.findByAllCriteria(DEVICE_ID, date, ReadingType.TEMPERATURE, start, end))
                        .thenReturn(Flux.just(reading));
            }

            StepVerifier.create(deviceService.findReadings(DEVICE_ID, ReadingType.TEMPERATURE, start, end))
                    .expectNextCount(dateRange.size())
                    .verifyComplete();

            verify(readingRepository, times(dateRange.size()))
                    .findByAllCriteria(eq(DEVICE_ID), any(LocalDate.class), eq(ReadingType.TEMPERATURE), eq(start), eq(end));
        }

        @Test
        @DisplayName("Should throw exception when device does not exist for readings retrieval")
        void findReadingsCase2() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.findReadings(DEVICE_ID, ReadingType.TEMPERATURE,
                                Instant.now().minusSeconds(3600), Instant.now()))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Dispositivo não encontrado: ID " + DEVICE_ID))
                                .verify();

                verify(cassandraTemplate, never()).select(any(Query.class), eq(Reading.class));
        }

        @Test
        @DisplayName("Should return devices linked to user when everything is OK")
        void findDevicesByUserIdCase1() {
                DeviceByUserId deviceByUserId = new DeviceByUserId(device, new DeviceByUserIdKey(USER_ID, DEVICE_ID));
                when(userByIdRepository.findById(USER_ID)).thenReturn(Mono.just(userById));
                when(deviceByUserIdRepository.findByKeyOwnerId(USER_ID)).thenReturn(Flux.just(deviceByUserId));

                StepVerifier.create(deviceService.findDevicesByUserId(USER_ID))
                                .expectNext(deviceByUserId)
                                .verifyComplete();

                verify(deviceByUserIdRepository, times(1)).findByKeyOwnerId(USER_ID);
        }

        @Test
        @DisplayName("Should throw exception when user does not exist for devices retrieval")
        void findDevicesByUserIdCase2() {
                when(userByIdRepository.findById(USER_ID)).thenReturn(Mono.empty());
                lenient().when(deviceByUserIdRepository.findByKeyOwnerId(any(UUID.class))).thenReturn(Flux.empty());

                StepVerifier.create(deviceService.findDevicesByUserId(USER_ID))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Usuário de ID " + USER_ID + " não encontrado"))
                                .verify();

                verify(deviceByUserIdRepository, never()).findByKeyOwnerId(any(UUID.class));
        }

        @Test
        @DisplayName("Should find device by API key when everything is OK")
        void findDeviceByApiKeyCase1() {
                when(deviceByApiKeyRepository.findByKeyApiKey(API_KEY)).thenReturn(Mono.just(deviceByApiKey));

                StepVerifier.create(deviceService.findDeviceByApiKey(API_KEY))
                                .expectNext(deviceByApiKey)
                                .verifyComplete();

                verify(deviceByApiKeyRepository, times(1)).findByKeyApiKey(API_KEY);
        }

        @Test
        @DisplayName("Should throw exception when device is not found by API key")
        void findDeviceByApiKeyCase2() {
                when(deviceByApiKeyRepository.findByKeyApiKey(API_KEY)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.findDeviceByApiKey(API_KEY))
                                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                                                throwable.getMessage()
                                                                .equals("Dispositivo não encontrado com a chave de API fornecida: "
                                                                                + API_KEY))
                                .verify();

                verify(deviceByApiKeyRepository, times(1)).findByKeyApiKey(API_KEY);
        }

        @Test
        @DisplayName("Should return device by ID when everything is OK")
        void findDeviceByIdCase1() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));

                StepVerifier.create(deviceService.findDeviceById(DEVICE_ID))
                                .expectNext(deviceById)
                                .verifyComplete();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should throw exception when device does not exist by ID")
        void findDeviceByIdCase2() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.findDeviceById(DEVICE_ID))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Dispositivo não encontrado: ID " + DEVICE_ID))
                                .verify();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should return device by name when everything is OK")
        void findDeviceByNameCase1() {
                when(deviceRepository.findByKey_Name(DEVICE_NAME)).thenReturn(Mono.just(device));

                StepVerifier.create(deviceService.findDeviceByName(DEVICE_NAME))
                                .expectNext(device)
                                .verifyComplete();

                verify(deviceRepository, times(1)).findByKey_Name(DEVICE_NAME);
        }

        @Test
        @DisplayName("Should throw exception when device does not exist by name")
        void findDeviceByNameCase2() {
                when(deviceRepository.findByKey_Name(DEVICE_NAME)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.findDeviceByName(DEVICE_NAME))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Dispositivo não encontrado: " + DEVICE_NAME))
                                .verify();

                verify(deviceRepository, times(1)).findByKey_Name(DEVICE_NAME);
        }

        @Test
        @DisplayName("Should update device when everything is OK")
        void updateDeviceCase1() {
                DeviceUpdateDTO updateData = new DeviceUpdateDTO("UpdatedName", "SAO_PAULO", null, null, null, null, null,
                                null, null, null, null, null, null, null);

                when(deviceByApiKeyRepository.findByKeyApiKey(anyString())).thenReturn(Mono.empty());
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));
                when(deviceRepository.findById(any(Device.DeviceKey.class))).thenReturn(Mono.just(device));
                when(deviceRepository.save(any(Device.class))).thenReturn(Mono.just(device));
                when(deviceByIdRepository.save(any(DeviceById.class))).thenReturn(Mono.just(deviceById));

                when(deviceByUserIdRepository.findByKeyOwnerId(any(UUID.class))).thenReturn(Flux.empty());

                StepVerifier.create(deviceService.updateDevice(DEVICE_ID, updateData))
                                .expectNext(device)
                                .verifyComplete();

                verify(deviceRepository, times(1)).save(any(Device.class));
                verify(deviceByIdRepository, times(1)).save(any(DeviceById.class));
        }

        @Test
        @DisplayName("Should throw exception when device does not exist for update")
        void updateDeviceCase2() {
                DeviceUpdateDTO updateData = new DeviceUpdateDTO("UpdatedName", "HEC", null, null, null, null, null,
                                null, null, null, null, null, null, null);

                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.updateDevice(DEVICE_ID, updateData))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Dispositivo não encontrado: ID " + DEVICE_ID))
                                .verify();

                verify(deviceRepository, never()).save(any(Device.class));
        }

        @Test
        @DisplayName("Should delete device and related entries when everything is OK")
        void deleteDeviceCase1() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));
                when(deviceRepository.findById(any(Device.DeviceKey.class))).thenReturn(Mono.just(device));
                when(deviceRepository.save(any(Device.class))).thenReturn(Mono.just(device));
                when(deviceByIdRepository.delete(deviceById)).thenReturn(Mono.empty());
                when(deviceByApiKeyRepository.deleteById(any(DeviceByApiKeyKey.class))).thenReturn(Mono.empty());
                when(deviceByUserIdRepository.deleteById(any(DeviceByUserIdKey.class))).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.deleteDevice(DEVICE_ID))
                                .verifyComplete();

                verify(deviceRepository, times(1)).save(any(Device.class));
                verify(deviceByIdRepository, times(1)).delete(deviceById);
                verify(deviceByApiKeyRepository, times(1)).deleteById(any(DeviceByApiKeyKey.class));
                verify(deviceByUserIdRepository, times(1)).deleteById(any(DeviceByUserIdKey.class));
        }

        @Test
        @DisplayName("Should throw exception when device does not exist for deletion")
        void deleteDeviceCase2() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.empty());

                StepVerifier.create(deviceService.deleteDevice(DEVICE_ID))
                                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                                                throwable.getMessage()
                                                                .equals("Dispositivo não encontrado: ID " + DEVICE_ID))
                                .verify();

                verify(deviceRepository, never()).save(any(Device.class));
        }

        @Test
        @DisplayName("Should restart device when everything is OK")
        void restartDeviceCase1() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));
                when(webClientBuilder.build()).thenReturn(webClient);
                Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
                Mockito.<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(anyString()))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("OK"));

                StepVerifier.create(deviceService.restartDevice(DEVICE_ID))
                                .verifyComplete();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should throw exception when device ID is not found for restart")
        void restartDeviceCase2() {

                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.empty());
                lenient().when(webClientBuilder.build()).thenReturn(webClient);
                lenient().<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
                lenient().<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(anyString()))
                                .thenReturn(requestHeadersSpec);
                lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

                StepVerifier.create(deviceService.restartDevice(DEVICE_ID))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Dispositivo não encontrado: ID " + DEVICE_ID))
                                .verify();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should throw exception on network failure when restarting device")
        void restartDeviceCase3() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));
                when(webClientBuilder.build()).thenReturn(webClient);
                Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
                Mockito.<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(anyString()))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(String.class))
                                .thenReturn(Mono.error(new RuntimeException("Network error")));

                StepVerifier.create(deviceService.restartDevice(DEVICE_ID))
                                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                                                ex.getMessage().equals("Network error"))
                                .verify();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should force read when everything is OK")
        void forceReadCase1() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));
                when(webClientBuilder.build()).thenReturn(webClient);
                Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
                Mockito.<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(anyString()))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("OK"));

                StepVerifier.create(deviceService.forceRead(DEVICE_ID))
                                .verifyComplete();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should throw exception when device ID is not found for force read")
        void forceReadCase2() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.empty());
                lenient().when(webClientBuilder.build()).thenReturn(webClient);
                lenient().<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
                lenient().<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(anyString()))
                                .thenReturn(requestHeadersSpec);
                lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

                StepVerifier.create(deviceService.forceRead(DEVICE_ID))
                                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException &&
                                                ex.getMessage().equals("Dispositivo não encontrado: ID " + DEVICE_ID))
                                .verify();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }

        @Test
        @DisplayName("Should throw exception on network failure when forcing read")
        void forceReadCase3() {
                when(deviceByIdRepository.findById(DEVICE_ID)).thenReturn(Mono.just(deviceById));
                when(webClientBuilder.build()).thenReturn(webClient);
                Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
                Mockito.<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(anyString()))
                                .thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                when(responseSpec.bodyToMono(String.class))
                                .thenReturn(Mono.error(new RuntimeException("Network error")));

                StepVerifier.create(deviceService.forceRead(DEVICE_ID))
                                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                                                ex.getMessage().equals("Network error"))
                                .verify();

                verify(deviceByIdRepository, times(1)).findById(DEVICE_ID);
        }
}