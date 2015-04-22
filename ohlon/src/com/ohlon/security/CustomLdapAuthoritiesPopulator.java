package com.ohlon.security;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
		Collection<GrantedAuthority> gas = new HashSet<GrantedAuthority>();
		gas.add(new SimpleGrantedAuthority("ROLE_USER"));
		return gas;
	}
	
}
