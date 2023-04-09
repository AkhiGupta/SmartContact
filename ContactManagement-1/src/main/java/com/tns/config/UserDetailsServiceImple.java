package com.tns.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.tns.entities.User;
import com.tns.repo.MyRepo;

//login Role BAsed authorization security step 2
public class UserDetailsServiceImple implements UserDetailsService  {

	@Autowired
	private MyRepo repo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// fetching from the databases
		User user = repo.getUserByUserName(username);
		if(user==null)
		{
			throw new UsernameNotFoundException("Could not found userName");
		}
		CustomUserDetails customDetails = new CustomUserDetails(user);
		return customDetails;
	}

}
