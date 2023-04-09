package com.tns.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tns.entities.User;
import com.tns.helper.Message;
import com.tns.repo.MyRepo;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
public class MyController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder; 
	
	@Autowired
	private MyRepo repo;
	 
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title","HOME - Smart Contact");
		return "home";
	}
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title","ABOUT - Smart Contact");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title","REGISTER - Smart Contact");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		m.addAttribute("title","Sign-up");
		return "signin";
	}
//handler for register user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user")User user ,BindingResult bresult,@RequestParam(value="agreement",defaultValue="false")boolean agreement,Model m,HttpSession session)
	{
		try {
			if(!agreement)
			{
				System.out.println("u have not agree");
				throw new Exception("u have not agree");
			}
			
			if(bresult.hasErrors())
			{
				System.out.println("ERROR"+bresult.toString());
				m.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
		 	
			System.out.println("Aggrement "+agreement);
			System.out.println(("User"+user));
			
			User result=this.repo.save(user);
			 
			m.addAttribute("user",new User());
			session.setAttribute("message", new Message("User Registerd Sucessfully", "alert-sucess"));
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user",user);
			session.setAttribute("message", new Message("Something Get Went "+e.getMessage(), "alert-error"));
			return "signup";
		}
		
	}
	  

}
