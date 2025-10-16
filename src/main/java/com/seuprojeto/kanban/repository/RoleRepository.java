package com.seuprojeto.kanban.repository;
import com.seuprojeto.kanban.security.Role; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RoleRepository extends JpaRepository<Role, Long> { Optional<Role> findByName(String name); }
