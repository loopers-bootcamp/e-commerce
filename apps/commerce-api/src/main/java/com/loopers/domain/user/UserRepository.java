package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findUserByName(String name);

    boolean existsUserByName(String name);

    User saveUser(User user);

}
