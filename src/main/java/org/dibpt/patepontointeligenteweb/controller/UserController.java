package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.Entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserDTO createUserDTO) {
        return null;
    }

    @GetMapping("/{UserId}")
    public ResponseEntity<User> getUserById(@PathVariable("UserId") Long id) {
        return null;
    }
}
