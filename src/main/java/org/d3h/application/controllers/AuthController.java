package org.d3h.application.controllers;

import org.d3h.application.models.Role;
import org.d3h.application.models.RoleName;
import org.d3h.application.models.User;
import org.d3h.application.payload.ApiResponse;
import org.d3h.application.payload.users.JwtResponse;
import org.d3h.application.payload.users.LoginRequest;
import org.d3h.application.payload.users.SignUpRequest;
import org.d3h.application.repositories.RoleRepository;
import org.d3h.application.repositories.UserRepository;
import org.d3h.application.security.JwtTokenProvider;
import org.d3h.application.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;
    
    @Autowired
    UserController userController;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

    	if(userRepository.existsByUsername(loginRequest.getUsernameOrEmail()))		
    		userRepository.findByUsername(loginRequest.getUsernameOrEmail()).get().setOnline(true);
    	else 
    		if(userRepository.existsByEmail(loginRequest.getUsernameOrEmail()))
    			userRepository.findByEmail(loginRequest.getUsernameOrEmail()).get().setOnline(true);
    	
    	Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        //added : 28/06/2020
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(), 
												 roles));
        
        //return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
    	
    	System.out.println(signUpRequest.getRole());
    	
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //Role userRole = new Role();
        Set<String> strRoles = new HashSet<>(); 
        strRoles.add(signUpRequest.getRole());
		Set<Role> roles = new HashSet<>();
		
		strRoles.forEach(role -> {
			switch (role) {
			case "ROLE_ADMIN":
				Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				roles.add(adminRole);

				break;
			case "ROLE_MODERATOR":
				Role modRole = roleRepository.findByName(RoleName.ROLE_MODERATOR)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				roles.add(modRole);

				break;
			default:
				Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				roles.add(userRole);
			}
		});
		
		/*
        if(signUpRequest.getRole() == "ROLE_USER") {
        	userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new AppException("User Role not set."));
        }
        else {
        	userRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new AppException("User Role not set."));
        }
        */
		
    	//System.out.println(userRole.getName());
        
        /*Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));*/

		/*
        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);
		*/
		
		// added
		user.setRoles(roles);
		User result = userRepository.save(user);
		//---
		
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}
