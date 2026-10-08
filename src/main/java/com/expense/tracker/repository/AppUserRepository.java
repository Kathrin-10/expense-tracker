package com.expense.tracker.repository;

import com.expense.tracker.entity.AppUser;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  List<AppUser> findAllByRoleOrderByFullNameAsc(com.expense.tracker.entity.Role role);

  List<AppUser> findAllByRole(com.expense.tracker.entity.Role role);
}
