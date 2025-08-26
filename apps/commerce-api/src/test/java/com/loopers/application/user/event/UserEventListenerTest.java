package com.loopers.application.user.event;

import com.loopers.domain.point.PointService;
import com.loopers.domain.saga.SagaCommand;
import com.loopers.domain.saga.SagaService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.event.UserEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class UserEventListenerTest {

    @InjectMocks
    private UserEventListener sut;

    @Mock
    private PointService pointService;
    @Mock
    private SagaService sagaService;

    @DisplayName("사용자가 회원가입했을 때:")
    @Nested
    class UserIsJoined {

        @DisplayName("그 사용자의 포인트를 생성한다.")
        @Test
        void createPointOfThatUser() {
            // given
            User user = Instancio.create(User.class);
            UserEvent.Join event = UserEvent.Join.from(user);

            // when
            sut.createUserPoint(event);

            // then
            ArgumentCaptor<SagaCommand.Inbound> captor = ArgumentCaptor.forClass(SagaCommand.Inbound.class);
            verify(sagaService, times(1)).inbound(captor.capture());
            verify(pointService, times(1)).create(event.userId());

            assertThat(captor.getValue().eventKey()).isEqualTo(event.eventKey());
            assertThat(captor.getValue().eventName()).isEqualTo(event.eventName());
        }

    }

}
