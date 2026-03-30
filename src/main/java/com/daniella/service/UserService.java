package com.daniella.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.daniella.dto.PasswordChangeDTO;
import com.daniella.dto.UserRequest;
import com.daniella.dto.UserResponseDTO;
import com.daniella.dto.UserUpdateDTO;

@Service
public interface UserService {
	
	void createUser (UserRequest userRequest) ;
	UserResponseDTO getUserById(Long id);
	List<UserResponseDTO> getAllUser();
	UserResponseDTO getByEmail (String email);
	void updateUserDetails(String email, UserUpdateDTO updateDTO);
	void updatePassword(String email, PasswordChangeDTO passswordDTO);

}
