package com.loopers.domain.user;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.domain.user.event.UserEvent;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @ReadOnlyTransactional
    public Optional<UserResult.GetUser> getUser(String userName) {
        if (!StringUtils.hasText(userName)) {
            return Optional.empty();
        }

        return userRepository.findUserByName(userName)
                .map(UserResult.GetUser::from);
    }

    @Transactional
    public UserResult.Join join(UserCommand.Join command) {
        if (userRepository.existsUserByName(command.getUserName())) {
            throw new BusinessException(CommonErrorType.CONFLICT, "이미 가입된 사용자입니다.");
        }

        User user = command.toEntity();
        userRepository.saveUser(user);

        eventPublisher.publishEvent(UserEvent.Join.from(user));

        return UserResult.Join.from(user);
    }

}
