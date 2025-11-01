package org.dibpt.patepontointeligenteweb.service;

import org.dibpt.patepontointeligenteweb.Entity.User;
import org.dibpt.patepontointeligenteweb.controller.CreateUserDTO;
import org.dibpt.patepontointeligenteweb.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID createUser(CreateUserDTO createUserDTO) {
        var entity = new User(createUserDTO.userName());
        var UserSaved = userRepository.save(entity);

        return UserSaved.getUserId();
    }

    public Optional<User> getUserById(String userId) {
        var user = userRepository.findById(UUID.fromString(userId));
        return user;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public void deleteUserById(String userId){
        var id = UUID.fromString(userId);
        var userExists = userRepository.existsById(id);
        if(userExists){
            userRepository.deleteById(id);
        }
    }
}
