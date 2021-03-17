package org.d3h.application.services;

import java.util.ArrayList;
import java.util.List;

import org.d3h.application.models.Role;
import org.d3h.application.models.User;
import org.d3h.application.payload.users.UserDetail;
import org.d3h.application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	// Get users :
	public List<UserDetail> getUsers(){
		
		List<UserDetail> users = new ArrayList<>();
		
		for(User u : userRepository.findAll() ) {
			Role[] roles = new Role[1];
			u.getRoles().toArray(roles);
			String role = roles[0].getName().name();
			UserDetail user = new UserDetail(
					u.getId(),
					u.getName(),
					u.getUsername(),
					u.getEmail(),
					u.isOnline(),
					u.isDisabled(),
					role,
					u.getCreatedAt().toString()
					
			);
			user.toString();
			users.add(user);
		}
			
		return users;
	}

	public boolean setOffline(String username) {
		
		User u = userRepository.findByUsername(username).get();
		
		System.out.println("Avant : "+u.isOnline());
		u.setId(userRepository.findByUsername(username).get().getId());
		u.setOnline(false);
		userRepository.save(u);
		
		System.out.println("Aprés : "+u.isOnline());
		System.out.println(u.toString());
		return true;
	}
}
