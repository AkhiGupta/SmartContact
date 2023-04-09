package com.tns.repo;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tns.entities.Contact;

public interface ContactRepo extends JpaRepository<Contact, Integer> {
	
	
	//pagination
	@Query("from Contact as c where c.user.id=:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId, PageRequest pageable);
	
}
