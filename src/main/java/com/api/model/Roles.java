package com.api.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

//La clase no puede llamarse Role, en singular, no parece que de problemas pero a Eclipse no le gusta.
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Roles {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ERole name;
  
  public Roles() {
	  
  }
  
  public Roles(ERole name) {
	    this.name = name;
  }
}