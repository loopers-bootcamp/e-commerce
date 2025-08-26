package com.loopers.application.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.event.UserEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class UserEventListenerTest {

    @InjectMocks
    private UserEventListener sut;

    @Mock
    private PointService pointService;

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
            verify(pointService, times(1)).create(event.userId());
        }

    }

}
