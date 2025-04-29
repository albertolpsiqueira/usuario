package com.javanauta.usuario.business;

import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructore.entity.Usuario;
import com.javanauta.usuario.infrastructore.exceptions.ConflictException;
import com.javanauta.usuario.infrastructore.exceptions.ResourceNotFoundException;
import com.javanauta.usuario.infrastructore.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructore.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email) {
        try {
            boolean existe = verificaEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email já cadastrado " + email);
            }
        }catch (ConflictException e){
            throw new ConflictException("Email já cadastrado " + e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscaUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email não encontrado " + email));
    }

    public void deteaUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizadDadosUsuario(String token, UsuarioDTO dto) {
        //Aqui buscamos o usuário do email através do token (tirar a obrigatoriedade do email)
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        //Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);
        //Busca os dados do usuário no bandco de dados
        Usuario usuarioEntity =  usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email não localizado"));
        //Mesclou os dados recebidos na requisição
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);
        //Salvou os dados do usuario convertido e depois pegou o retorno e convertei para Usuario DTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

}
