package com.expense.tracker.config;

import com.expense.tracker.entity.AppUser;
import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.Role;
import com.expense.tracker.repository.AppUserRepository;
import com.expense.tracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

  @Bean
  CommandLineRunner seed(
      CategoryRepository categories,
      AppUserRepository users,
      PasswordEncoder encoder,
      @Value("${app.admin.email}") String adminEmail,
      @Value("${app.admin.password}") String adminPassword) {

    return args -> {
      if (categories.count() == 0) {
        for (String name :
            new String[] {
              "Food", "Travel", "Shopping", "Medical", "Entertainment", "Subscription"
            }) {
          categories.save(new Category(name));
        }
      }

      String email = adminEmail.trim().toLowerCase();
      AppUser configured = users.findByEmailIgnoreCase(email).orElse(null);

      if (configured != null && configured.getRole() == Role.USER) {
        throw new IllegalStateException(
            "The configured admin email belongs to a customer account. Choose a different"
                + " ADMIN_EMAIL.");
      }

      if (configured == null) {
        configured = new AppUser();
        configured.setFullName("ExpenseFlow Administrator");
        configured.setEmail(email);
      }

      configured.setRole(Role.ADMIN);
      configured.setActive(true);
      configured.setPassword(encoder.encode(adminPassword));
      users.save(configured);

      // ExpenseFlow has exactly one active administrator: the configured admin account.
      for (AppUser admin : users.findAllByRole(Role.ADMIN)) {
        if (!admin.getId().equals(configured.getId()) && admin.isActive()) {
          admin.setActive(false);
          users.save(admin);
        }
      }
    };
  }
}
