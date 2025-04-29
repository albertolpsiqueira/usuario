package com.javanauta.usuario.infrastructore.repository;

import com.javanauta.usuario.infrastructore.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
}
