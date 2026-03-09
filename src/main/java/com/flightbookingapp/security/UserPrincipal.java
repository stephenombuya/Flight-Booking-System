package com.flightbookingapp.security;

import com.flightbookingapp.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} adapter for our {@link User} entity.
 */
public class UserPrincipal implements UserDetails {

    @Getter private final Long id;
    @Getter private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountLocked;

    public UserPrincipal(User user) {
        this.id           = user.getId();
        this.email        = user.getEmail();
        this.password     = user.getPassword();
        this.enabled      = user.isEnabled();
        this.accountLocked = user.isAccountLocked();
        this.authorities  = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override public String getUsername()                                      { return email; }
    @Override public String getPassword()                                      { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities()  { return authorities; }
    @Override public boolean isEnabled()                                       { return enabled; }
    @Override public boolean isAccountNonLocked()                              { return !accountLocked; }
    @Override public boolean isAccountNonExpired()                             { return true; }
    @Override public boolean isCredentialsNonExpired()                         { return true; }
}
