package com.javanauta.usuario.infrastructore.repository;

import com.javanauta.usuario.infrastructore.entity.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
}
