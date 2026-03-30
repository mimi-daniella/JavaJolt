package com.daniella.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daniella.dto.PasswordChangeDTO;
import com.daniella.dto.UserRequest;
import com.daniella.dto.UserResponseDTO;
import com.daniella.dto.UserUpdateDTO;
import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.exception.BusinessException;
import com.daniella.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;



@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered.");
        }

        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match.");
        }

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.ROLE_CLIENT); // default role

        userRepository.save(user);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
        return UserResponseDTO.fromEntity(user);
    }

    @Override
    public List<UserResponseDTO> getAllUser() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        return UserResponseDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public void updateUserDetails(String email, UserUpdateDTO updateDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));

        user.setFirstName(updateDTO.firstName());
        user.setLastName(updateDTO.lastName());
        user.setEmail(updateDTO.email());

        userRepository.save(user);
    }

    @Override
    public void updatePassword(String email, PasswordChangeDTO passwordDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!passwordEncoder.matches(passwordDTO.oldPassword(), user.getPassword())) {
            throw new BusinessException("The current password you entered is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.newPassword()));
        userRepository.save(user);
    }

	
}