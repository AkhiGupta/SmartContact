package com.tns.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tns.entities.User;

public interface MyRepo extends JpaRepository<User, Integer> {
	
	@Query("select u from User u where u.email = :email")
	public User getUserByUserName(@Param("email") String email);

}
