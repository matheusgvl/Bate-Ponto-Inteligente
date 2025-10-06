package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.Entity.User;
import org.dibpt.patepontointeligenteweb.controller.CreateUserDTO;
import org.dibpt.patepontointeligenteweb.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID createUser(CreateUserDTO createUserDTO) {
        var entity = new User(Instant.now(), null, UUID.randomUUID(), createUserDTO.userName());
        var UserSaved = userRepository.save(entity);

        return UserSaved.getUserId();
    }
}
