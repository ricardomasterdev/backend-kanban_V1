package com.seuprojeto.kanban.security;
import jakarta.persistence.*; import java.util.*;
@Entity @Table(name="users")
public class UserAccount {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false,unique=true,length=160) private String email;
  @Column(nullable=false) private String password;
  @Column(nullable=false) private boolean enabled = true;
  @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="user_roles",
      joinColumns=@JoinColumn(name="user_id"), inverseJoinColumns=@JoinColumn(name="role_id"))
  private Set<Role> roles = new HashSet<>();
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
  public String getPassword(){return password;} public void setPassword(String password){this.password=password;}
  public boolean isEnabled(){return enabled;} public void setEnabled(boolean e){this.enabled=e;}
  public Set<Role> getRoles(){return roles;} public void setRoles(Set<Role> r){this.roles=r;}
}
