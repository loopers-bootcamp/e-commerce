package com.loopers.domain.user;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<UserResult.GetUser> getUser(String userName) {
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

        return UserResult.Join.from(user);
    }

}
