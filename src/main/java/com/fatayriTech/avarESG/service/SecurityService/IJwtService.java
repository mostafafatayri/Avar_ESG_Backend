package com.fatayriTech.avarESG.service.SecurityService;



public interface IJwtService {

    String generateToken(String user);

    String extractEmail(String token);

    boolean isTokenValid(String token);
}
