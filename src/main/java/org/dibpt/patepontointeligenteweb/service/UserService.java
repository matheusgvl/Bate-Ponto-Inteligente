package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.controller.CreateUserDTO;
import org.dibpt.patepontointeligenteweb.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(CreateUserDTO createUserDTO) {

    }
}
