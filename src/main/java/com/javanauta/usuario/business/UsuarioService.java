package com.javanauta.usuario.business;

import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.EnderecoDTO;
import com.javanauta.usuario.business.dto.TelefoneDTO;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructore.entity.Endereco;
import com.javanauta.usuario.infrastructore.entity.Telefone;
import com.javanauta.usuario.infrastructore.entity.Usuario;
import com.javanauta.usuario.infrastructore.exceptions.ConflictException;
import com.javanauta.usuario.infrastructore.exceptions.ResourceNotFoundException;
import com.javanauta.usuario.infrastructore.repository.EnderecoRepository;
import com.javanauta.usuario.infrastructore.repository.TelefoneRepository;
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
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

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

    public UsuarioDTO buscaUsuarioPorEmail(String email) {
        try {
            return usuarioConverter.paraUsuarioDTO(usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email não encontrado " + email)));
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Email não encontrado " + email);
        }
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

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {
        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(() -> new ResourceNotFoundException("Id não encontrado " +idEndereco));
        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity );
        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO telefoneDTO) {
        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() -> new ResourceNotFoundException("Telefone Não encontrado " +idTelefone));
        Telefone telefone = usuarioConverter.updateTelefone(telefoneDTO, entity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastroEndereo(String token, EnderecoDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email não localizado " + email));
        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());
        Endereco enderecoEntity = enderecoRepository.save(endereco);
        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastroTelefone(String token, TelefoneDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email não localizado " + email));
        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());
        Telefone TelefoneEntity = telefoneRepository.save(telefone);
        return usuarioConverter.paraTelefoneDTO(TelefoneEntity);
    }

}
