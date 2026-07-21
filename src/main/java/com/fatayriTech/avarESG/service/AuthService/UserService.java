package com.fatayriTech.avarESG.service.AuthService;


import com.fatayriTech.avarESG.dto.request.UserRequests.CreateUserRequest;
import com.fatayriTech.avarESG.dto.request.UserRequests.UpdateUserRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}