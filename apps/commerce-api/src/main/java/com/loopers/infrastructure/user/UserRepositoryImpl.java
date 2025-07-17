package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findUserByName(String name) {
        return userJpaRepository.findByName(name);
    }

    @Override
    public boolean existsUserByName(String name) {
        return userJpaRepository.existsByName(name);
    }

    @Override
    public User saveUser(User user) {
        return userJpaRepository.save(user);
    }

}
