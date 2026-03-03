package com.example.DANMONHOCJ22E.service;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  @Autowired
  private UserRepository repo;

  public List<User> findAll() {
    return repo.findAll();
  }

  public User save(User u) {
    return repo.save(u);
  }
}
