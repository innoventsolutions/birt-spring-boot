package sample.auth.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Service // It has to be annotated with @Service.
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// hard coding the users. All passwords must be encoded.
		final List<AppUser> users = Arrays.asList(new AppUser(1, "omar", encoder.encode("12345"), "USER"),
				new AppUser(2, "admin", encoder.encode("12345"), "ADMIN"));

		for (AppUser appUser : users) {
			if (appUser.getUsername().equals(username)) {

				// Remember that Spring needs roles to be in this format: "ROLE_" + userRole (i.e. "ROLE_ADMIN")
				// So, we need to set it to that format, so we can verify and compare roles (i.e. hasRole("ADMIN")).
				List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_" + appUser.getRole());

				// The "User" class is provided by Spring and represents a model class for user to be returned by UserDetailsService
				// And used by auth manager to verify and check user authentication.
				return new User(appUser.getUsername(), appUser.getPassword(), grantedAuthorities);
			}
		}

		// If user not found. Throw this exception.
		throw new UsernameNotFoundException("Username: " + username + " not found");
	}

	@Getter
	@Setter
	@AllArgsConstructor
	private static class AppUser {
		private Integer id;
		private String username;
		private String password;
		private String role;
	}
}