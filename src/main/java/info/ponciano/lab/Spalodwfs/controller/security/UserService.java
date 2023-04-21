package info.ponciano.lab.spalodwfs.controller.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class UserService implements UserDetailsService {
    private final String DB_FILE = "./src/main/java/info/ponciano/lab/spalodwfs/controller/security/users.txt";


    public User findByUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    String password = parts[1];
                    List<String> roles = Arrays.asList(parts[2].split(";"));
                    return new User(username, password, roles);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(User user) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DB_FILE, true))) {
            String roles = String.join(";", user.getRoles());
            pw.println(user.getUsername() + "," + user.getPassword() + "," + roles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user != null) {
            List<GrantedAuthority> authorities = user.getRoles()
            .stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(), 
            authorities     
            );
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }

    
}
