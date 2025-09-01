package com.loopers.domain.saga;

import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SagaServiceIntegrationTest {

    private final SagaService sut;

    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("인바운드 이벤트가 발생했을 때:")
    @Nested
    class Inbound {

        @DisplayName("페이로드가 null이라면, 인박스에 null로 저장한다.")
        @Test
        void saveNullToInbox_withPayloadAsNull() {
            // given
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    null
            );

            // when
            SagaResult.Inbound inbound = sut.inbound(command);

            // then
            Inbox inbox = entityManager
                    .createQuery("select i from Inbox i where i.eventKey = :eventKey and i.eventName = :eventName", Inbox.class)
                    .setParameter("eventKey", inbound.eventKey())
                    .setParameter("eventName", inbound.eventName())
                    .getSingleResult();

            assertThat(inbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(inbox.getEventName()).isEqualTo(command.eventName());
            assertThat(inbox.getPayload()).isNull();
        }

        @DisplayName("페이로드가 Map이라면, 인박스에 그대로 저장한다.")
        @Test
        void saveAsItIsToInbox_withPayloadAsMap() {
            // given
            Map<String, Object> payload = Map.of("foo", 100, "bar", "002");
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    payload
            );

            // when
            SagaResult.Inbound inbound = sut.inbound(command);

            // then
            Inbox inbox = entityManager
                    .createQuery("select i from Inbox i where i.eventKey = :eventKey and i.eventName = :eventName", Inbox.class)
                    .setParameter("eventKey", inbound.eventKey())
                    .setParameter("eventName", inbound.eventName())
                    .getSingleResult();

            assertThat(inbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(inbox.getEventName()).isEqualTo(command.eventName());
            assertThat(inbox.getPayload()).containsAllEntriesOf(payload);
        }

        @DisplayName("페이로드가 자바 내장 타입이라면, 인박스에 래핑해서 저장한다.")
        @Test
        void saveWrapperToInbox_withPayloadAsJavaNativeType() {
            // given
            Double payload = 3.141592;
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    payload
            );

            // when
            SagaResult.Inbound inbound = sut.inbound(command);

            // then
            Inbox inbox = entityManager
                    .createQuery("select i from Inbox i where i.eventKey = :eventKey and i.eventName = :eventName", Inbox.class)
                    .setParameter("eventKey", inbound.eventKey())
                    .setParameter("eventName", inbound.eventName())
                    .getSingleResult();

            Map<String, Object> wrappedMap = Map.of("payload", payload);
            assertThat(inbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(inbox.getEventName()).isEqualTo(command.eventName());
            assertThat(inbox.getPayload()).containsAllEntriesOf(wrappedMap);
        }

        @DisplayName("페이로드가 커스텀 타입이라면, 인박스에 Map으로 변환해서 저장한다.")
        @Test
        void saveConvertedMapToInbox_withPayloadAsCustomType() {
            // given
            DummyEvent payload = Instancio.create(DummyEvent.class);
            SagaCommand.Inbound command = new SagaCommand.Inbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    payload
            );

            // when
            SagaResult.Inbound inbound = sut.inbound(command);

            // then
            Inbox inbox = entityManager
                    .createQuery("select i from Inbox i where i.eventKey = :eventKey and i.eventName = :eventName", Inbox.class)
                    .setParameter("eventKey", inbound.eventKey())
                    .setParameter("eventName", inbound.eventName())
                    .getSingleResult();

            Map<String, Object> convertedMap = Map.of("id", payload.id, "name", payload.name);
            assertThat(inbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(inbox.getEventName()).isEqualTo(command.eventName());
            assertThat(inbox.getPayload()).containsAllEntriesOf(convertedMap);
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
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    null
            );

            // when
            SagaResult.Outbound outbound = sut.outbound(command);

            // then
            Outbox outbox = entityManager
                    .createQuery("select o from Outbox o where o.eventKey = :eventKey and o.eventName = :eventName", Outbox.class)
                    .setParameter("eventKey", outbound.eventKey())
                    .setParameter("eventName", outbound.eventName())
                    .getSingleResult();

            assertThat(outbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(outbox.getEventName()).isEqualTo(command.eventName());
            assertThat(outbox.getPayload()).isNull();
        }

        @DisplayName("페이로드가 Map이라면, 아웃박스에 그대로 저장한다.")
        @Test
        void saveAsItIsToOutbox_withPayloadAsMap() {
            // given
            Map<String, Object> payload = Map.of("foo", 100, "bar", "002");
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    payload
            );

            // when
            SagaResult.Outbound outbound = sut.outbound(command);

            // then
            Outbox outbox = entityManager
                    .createQuery("select o from Outbox o where o.eventKey = :eventKey and o.eventName = :eventName", Outbox.class)
                    .setParameter("eventKey", outbound.eventKey())
                    .setParameter("eventName", outbound.eventName())
                    .getSingleResult();

            assertThat(outbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(outbox.getEventName()).isEqualTo(command.eventName());
            assertThat(outbox.getPayload()).containsAllEntriesOf(payload);
        }

        @DisplayName("페이로드가 자바 내장 타입이라면, 아웃박스에 래핑해서 저장한다.")
        @Test
        void saveWrapperToOutbox_withPayloadAsJavaNativeType() {
            // given
            Double payload = 3.141592;
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    payload
            );

            // when
            SagaResult.Outbound outbound = sut.outbound(command);

            // then
            Outbox outbox = entityManager
                    .createQuery("select o from Outbox o where o.eventKey = :eventKey and o.eventName = :eventName", Outbox.class)
                    .setParameter("eventKey", outbound.eventKey())
                    .setParameter("eventName", outbound.eventName())
                    .getSingleResult();

            Map<String, Object> wrappedMap = Map.of("payload", payload);
            assertThat(outbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(outbox.getEventName()).isEqualTo(command.eventName());
            assertThat(outbox.getPayload()).containsAllEntriesOf(wrappedMap);
        }

        @DisplayName("페이로드가 커스텀 타입이라면, 아웃박스에 Map으로 변환해서 저장한다.")
        @Test
        void saveConvertedMapToOutbox_withPayloadAsCustomType() {
            // given
            DummyEvent payload = Instancio.create(DummyEvent.class);
            SagaCommand.Outbound command = new SagaCommand.Outbound(
                    UUID.randomUUID().toString(),
                    "domain.eventName",
                    payload
            );

            // when
            SagaResult.Outbound outbound = sut.outbound(command);

            // then
            Outbox outbox = entityManager
                    .createQuery("select o from Outbox o where o.eventKey = :eventKey and o.eventName = :eventName", Outbox.class)
                    .setParameter("eventKey", outbound.eventKey())
                    .setParameter("eventName", outbound.eventName())
                    .getSingleResult();

            Map<String, Object> convertedMap = Map.of("id", payload.id, "name", payload.name);
            assertThat(outbox.getEventKey()).isEqualTo(command.eventKey());
            assertThat(outbox.getEventName()).isEqualTo(command.eventName());
            assertThat(outbox.getPayload()).containsAllEntriesOf(convertedMap);
        }

    }

    // -------------------------------------------------------------------------------------------------

    private record DummyEvent(Integer id, String name) {
    }

}
