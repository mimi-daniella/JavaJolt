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
	
	long getTotalUsers();
	 
	void deleteUser(Long id);
	void suspendUser(Long id);
	void reactivateUser(Long id);
	void updateUserAvatar(String email, String avatarPath);
}
