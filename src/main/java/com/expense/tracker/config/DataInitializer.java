package com.expense.tracker.config;

import com.expense.tracker.entity.Category;
import com.expense.tracker.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class DataInitializer {
    @Bean CommandLineRunner seed(CategoryRepository repo){return args->{if(repo.count()==0){repo.save(new Category("Food"));repo.save(new Category("Travel"));repo.save(new Category("Shopping"));repo.save(new Category("Medical"));repo.save(new Category("Entertainment"));}};}
}
