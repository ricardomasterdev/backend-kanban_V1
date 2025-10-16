package com.seuprojeto.kanban.repository;
import com.seuprojeto.kanban.security.UserAccount; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);
}
