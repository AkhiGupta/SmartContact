package com.tns.controller;


import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.tns.entities.Contact;
import com.tns.entities.User;
import com.tns.helper.Message;
import com.tns.repo.ContactRepo;
import com.tns.repo.MyRepo;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class homeController {
	
	@Autowired
	private MyRepo repo;
	
	
	@Autowired
	private ContactRepo contactRepo;
	
	@ModelAttribute
	public void addComondata(Model m, Principal p)//through principal we can unique identify username
	{
		String userName = p.getName();
		System.out.println("Username is :  "+userName );
		
		User user = repo.getUserByUserName(userName);
		System.out.println("Username is :  "+user);
		m.addAttribute(user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model m)
	{
		m.addAttribute("title","HomePage");
		return "normal/dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContact(Model m, Principal p) {
		
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/addContact";
	}
	
	//processing the add contact form
	@PostMapping("/process-contact")
		 public String processContact(@ModelAttribute("contact")  @RequestParam("image") MultipartFile file,@Valid Contact contact,
				 BindingResult bindingResult,Principal p, HttpSession session) {
		try {
		String name = p.getName();
		User user = this.repo.getUserByUserName(name);
		
		//processing and uploading the file
		if(file.isEmpty()) //file:- Return the name of the parameter in the multipart form.
		{
			System.out.println("image not uploaded ");
			contact.setImage("contact.png");
		}else {
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING );
			System.out.println("image uploaded Successfully");
		}
		contact.setUser(user);
		user.getContacts().add(contact);
		this.repo.save(user);
		
		System.out.println("DATA "+contact);
		session.setAttribute("message", new Message("Your Contact is Added","success "));
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error"+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something get went","danger"));
		}
		
		return "normal/addContact";
	}
	//show contact details (pagination)
	//per page 5[n]
	//current page 0
	
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m,Principal p)
	{
		m.addAttribute("title","All contacts");
		
		String userName = p.getName();
		User user = this.repo.getUserByUserName(userName);
		
		//Currentpage page
		//Contact perpage 5
		PageRequest pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepo.findContactsByUser(user.getId(),pageable);
		System.out.println(contacts);
		m.addAttribute("contacts",contacts);
		m.addAttribute("CurrentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	//showing particular contact details
	@RequestMapping("/{cId}/contacts")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal p) {
		System.out.println("Cid "+cId);
		Optional<Contact> contactOptional = this.contactRepo.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = p.getName();
		User  user = this.repo.getUserByUserName(userName);
		
		//to check whether A (person) can Access only A ContactDetails not B details
		if(user.getId()==contact.getUser().getId())
		{
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
		}
		
		
		return "normal/contactDetail";
	}
//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model m,HttpSession session){
		
		Contact contact = this.contactRepo.findById(cId).get();

		
//---------We have to unlink from user table to contact 
		contact.setUser(null);
//-----------------------------------
		this.contactRepo.delete(contact);
		System.out.println("deleted");
		
		session.setAttribute("message", new Message("Contact Deleted Successfully","success"));
		return "redirect:/user/show_contacts/0";
	}
	
//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model m) {
		
		
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepo.findById(cId).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
		
	}
//update contact handler ,@RequestParam("image") MultipartFile file,Model m,HttpSession session,Principal p 
	@PostMapping("/processUpdate")
	
	public String updateHandler(@ModelAttribute("contact")  @RequestParam("image") MultipartFile file,@Valid Contact contact,
			 BindingResult bindingResult,Principal p, HttpSession session) {
		
		
		try {
		
			//delete contact
			Contact oldContactDelete = this.contactRepo.findById(contact.getcId()).get();
			
			//image
			if(!file.isEmpty())
			{
				//filework and rewrite 
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldContactDelete.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDelete.getImage()); 
			}
			User user=this.repo.getUserByUserName(p.getName());
			contact.setUser(user);
			this.contactRepo.save(contact);
			session.setAttribute("message", new Message("your Contact is Updated","success"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println("Hello------Akhilesh");
		System.out.println("Contact "+contact.getName());
		System.out.println("Contact Id"+contact.getcId());
		
		
		return "redirect:/user/"+contact.getcId()+"/contacts";
		//"redirect:/user/show_contacts/0";
	}
	
	//your profile handler
	@GetMapping("/user-profile")
	public String yourProfile(Model m) {
		m.addAttribute("title","Your Profile Pagse");
		return "normal/profile";
	}

	//home handler
	@GetMapping("/dashboard")
	public String Home(Model m) {
		m.addAttribute("title","HomePage");
		return "normal/dashboard";
	}
}