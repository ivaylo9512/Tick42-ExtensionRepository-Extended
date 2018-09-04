package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.Spec.UserRegistrationSpec;
import com.tick42.quicksilver.models.User;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.security.JwtGenerator;
import com.tick42.quicksilver.services.base.UserService;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private JwtGenerator jwtGenerator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtGenerator jwtGenerator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(User user) {
        return userRepository.create(user);
    }

    @Override
    public void update(User user) {
        userRepository.update(user);
    }

    @Override
    public UserDTO setState(int id, String state) {
        User user = userRepository.findById(id);

        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        switch (state) {
            case "enable":
                user.setIsActive(true);
                break;
            case "block":
                user.setIsActive(false);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid user state. Use \"enable\" or \"block\".");
        }
        return new UserDTO(userRepository.update(user));
    }

    @Override
    public List<UserDTO> findAll(String state) {
        List<User> users = new ArrayList<>();

        if (state == null) {
            state = "";
        }
        switch (state) {
            case "active":

                users = userRepository.findUsersByState(true);
                break;
            case "blocked":
                users = userRepository.findUsersByState(false);
                break;
            default:
                users = userRepository.findAll();
        }
        List<UserDTO> usersDto = users
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return usersDto;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDTO findById(int id) {
        User user = userRepository.findById(id);
        if (user != null) {
            return new UserDTO(user);
        }
        throw new UserNotFoundException("User doesn't exist.");
    }

    @Override
    public User login(User user) throws InvalidCredentialsException {
        String username = user.getUsername();
        String password = user.getPassword();
        User foundUser = userRepository.findByUsername(username);
        if (foundUser != null && password.equals(foundUser.getPassword())) {
            if (foundUser.getIsActive()) {

                return foundUser;
            }
            throw new UserIsDisabledException("User is disabled.");
        }
        throw new InvalidCredentialsException("Invalid credentials.");
    }

    @Override
    public User register(UserRegistrationSpec userRegistrationSpec) {
        User registeredUser = userRepository.findByUsername(userRegistrationSpec.getUsername());
        if (userRegistrationSpec.getPassword().equals(userRegistrationSpec.getRepeatPassword())) {
            throw new PasswordsMissMatchException("Passwords must match");
        }
        if (registeredUser != null) {
            throw new UsernameExistsException("Username is already taken.");
        }
        String username = userRegistrationSpec.getUsername();
        String password = userRegistrationSpec.getPassword();
        String role = "USER";
        User user = new User(username, password, role);
        return userRepository.create(user);
    }

    @Override
    public String generateToken(User user) {
        if (jwtGenerator.generate(user) != null) {
            return jwtGenerator.generate(user);
        }
        throw new GenerateTokenException("Couldn't generate authentication token");
    }
}
