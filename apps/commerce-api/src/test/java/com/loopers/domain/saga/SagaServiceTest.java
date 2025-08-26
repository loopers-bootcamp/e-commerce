package com.loopers.domain.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class SagaServiceTest {

    @InjectMocks
    private SagaService sut;

    @Mock
    private SagaRepository sagaRepository;
    @Spy
    private ObjectMapper objectMapper;

    @DisplayName("인바운드 이벤트가 발생했을 때:")
    @Nested
    class Inbound {

        @DisplayName("페이로드가 null이라면, 인박스에 null로 저장한다.")
        @Test
        void saveNullToInbox_withPayloadAsNull() {
            // given
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    null
            );

            // when
            sut.inbound(command);

            // then
            ArgumentCaptor<Inbox> captor = ArgumentCaptor.forClass(Inbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).isNull();
        }

        @DisplayName("페이로드가 Map이라면, 인박스에 그대로 저장한다.")
        @Test
        void saveAsItIsToInbox_withPayloadAsMap() {
            // given
            Map<String, Object> payload = Map.of("foo", 100, "bar", "002");
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    payload
            );

            // when
            sut.inbound(command);

            // then
            ArgumentCaptor<Inbox> captor = ArgumentCaptor.forClass(Inbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).containsAllEntriesOf(payload);
        }

        @DisplayName("페이로드가 자바 내장 타입이라면, 인박스에 래핑해서 저장한다.")
        @Test
        void saveWrapperToInbox_withPayloadAsJavaNativeType() {
            // given
            Double payload = 3.141592;
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    payload
            );

            // when
            sut.inbound(command);

            // then
            ArgumentCaptor<Inbox> captor = ArgumentCaptor.forClass(Inbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            Map<String, Object> wrappedMap = Map.of("payload", payload);
            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).containsAllEntriesOf(wrappedMap);
        }

        @DisplayName("페이로드가 커스텀 타입이라면, 인박스에 Map으로 변환해서 저장한다.")
        @Test
        void saveConvertedMapToInbox_withPayloadAsCustomType() {
            // given
            DummyEvent payload = Instancio.create(DummyEvent.class);
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    payload
            );

            // when
            sut.inbound(command);

            // then
            ArgumentCaptor<Inbox> captor = ArgumentCaptor.forClass(Inbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            Map<String, Object> convertedMap = Map.of("id", payload.id, "name", payload.name);
            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).containsAllEntriesOf(convertedMap);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("아웃바운드 이벤트가 발생했을 때:")
    @Nested
    class Outbound {

        @DisplayName("페이로드가 null이라면, 아웃박스에 null로 저장한다.")
        @Test
        void saveNullToOutbox_withPayloadAsNull() {
            // given
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    null
            );

            // when
            sut.outbound(command);

            // then
            ArgumentCaptor<Outbox> captor = ArgumentCaptor.forClass(Outbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).isNull();
        }

        @DisplayName("페이로드가 Map이라면, 아웃박스에 그대로 저장한다.")
        @Test
        void saveAsItIsToOutbox_withPayloadAsMap() {
            // given
            Map<String, Object> payload = Map.of("foo", 100, "bar", "002");
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    payload
            );

            // when
            sut.outbound(command);

            // then
            ArgumentCaptor<Outbox> captor = ArgumentCaptor.forClass(Outbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).containsAllEntriesOf(payload);
        }

        @DisplayName("페이로드가 자바 내장 타입이라면, 아웃박스에 래핑해서 저장한다.")
        @Test
        void saveWrapperToOutbox_withPayloadAsJavaNativeType() {
            // given
            Double payload = 3.141592;
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    payload
            );

            // when
            sut.outbound(command);

            // then
            ArgumentCaptor<Outbox> captor = ArgumentCaptor.forClass(Outbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            Map<String, Object> wrappedMap = Map.of("payload", payload);
            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).containsAllEntriesOf(wrappedMap);
        }

        @DisplayName("페이로드가 커스텀 타입이라면, 아웃박스에 Map으로 변환해서 저장한다.")
        @Test
        void saveConvertedMapToOutbox_withPayloadAsCustomType() {
            // given
            DummyEvent payload = Instancio.create(DummyEvent.class);
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID(),
                    "domain.eventName",
                    payload
            );

            // when
            sut.outbound(command);

            // then
            ArgumentCaptor<Outbox> captor = ArgumentCaptor.forClass(Outbox.class);
            verify(sagaRepository, times(1)).save(captor.capture());

            Map<String, Object> convertedMap = Map.of("id", payload.id, "name", payload.name);
            assertThat(captor.getValue().getEventKey()).isEqualTo(command.eventKey());
            assertThat(captor.getValue().getEventName()).isEqualTo(command.eventName());
            assertThat(captor.getValue().getPayload()).containsAllEntriesOf(convertedMap);
        }

    }

    // -------------------------------------------------------------------------------------------------

    private record DummyEvent(Long id, String name) {
    }

}
