package com.api.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.dto.request.LoginRequest;
import com.api.dto.request.SignupRequest;
import com.api.dto.response.MessageResponse;
import com.api.dto.response.UserInfoResponse;
import com.api.model.ERole;
import com.api.model.Roles;
import com.api.model.User;
import com.api.repository.RoleRepository;
import com.api.repository.UserRepository;
import com.api.security.jwt.JwtUtils;
import com.api.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge=3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	JwtUtils jwtUtils;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		
		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());
		
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new UserInfoResponse(userDetails.getId(),
																											userDetails.getUsername(),
																											roles));
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
		if (userRepository.existsByUsername(signupRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario ya existente"));
		}
		
		
		//Crear el usuario
		User user = new User(signupRequest.getUsername(),
				encoder.encode(signupRequest.getPassword()));
		
		Set<String> strRoles = signupRequest.getRole();
		Set<Roles> roles=new HashSet<>();
		
		if (strRoles == null) {
			Roles userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(()-> new RuntimeException("Error: Rol no encontrado."));
		roles.add(userRole);
		}else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Roles adminRole= roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(()-> new RuntimeException("Error: Rol no encontrado."));
					roles.add(adminRole);
					
					break;
				default:
					Roles userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(()-> new RuntimeException("Error: Rol no encontrado."));
					roles.add(userRole);
				}
			});
		}
		
		user.setRoles(roles);
		userRepository.save(user);
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	
	@PostMapping("/signout")
	public ResponseEntity<?> logoutUser(){
		ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(new MessageResponse("Te has desconectado con exito"));
	}
}
