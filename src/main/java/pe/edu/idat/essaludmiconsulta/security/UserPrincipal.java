package pe.edu.idat.essaludmiconsulta.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.idat.essaludmiconsulta.model.Usuario;

import java.util.Collection;
import java.util.List;

/**
 * Adapta la entidad Usuario al contrato UserDetails que exige Spring Security.
 * El rol se expone como authority con prefijo "ROLE_" (convención de Spring
 * Security), por lo que en los controllers se usa hasRole("PACIENTE"),
 * hasRole("MEDICO") o hasRole("ADMIN") sin repetir el prefijo.
 */
public class UserPrincipal implements UserDetails {

    private final Usuario usuario;

    public UserPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getIdUsuario() {
        return usuario.getIdUsuario();
    }

    /** DNI del paciente autenticado (null si el rol no es PACIENTE). */
    public String getDniPaciente() {
        return usuario.getPaciente() != null ? usuario.getPaciente().getDni() : null;
    }

    /** CMP del médico autenticado (null si el rol no es MEDICO). */
    public String getCmpMedico() {
        return usuario.getMedico() != null ? usuario.getMedico().getCmp() : null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getActivo());
    }
}
