package info.ponciano.lab.spalodwfs.controller.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class UserService implements UserDetailsService {
    
    private final String DB_FILE = "./src/main/java/info/ponciano/lab/spalodwfs/controller/security/users.txt";

    // Finds a user by their username
    public User findByUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    String password = parts[1];
                    ArrayList<String> roles = new ArrayList<>(Arrays.asList(parts[2].split(";")));
                    return new User(username, password,roles);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Saves a new user to the user.txt file
    public void save(User user) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DB_FILE, true))) {
            String roles = String.join(";", user.getRoles());
            pw.println(user.getUsername() + "," + new BCryptPasswordEncoder(10).encode(user.getPassword()) + "," + roles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads a user by their username
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

    // Promotes a user to admin by updating their role in the user.txt file
    public void promoteUserToAdmin(String username) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && (!parts[2].contains("ADMIN")) ) {
                    parts[2] += ";ADMIN"; // Update the role to ADMIN
                    line = String.join(",", parts); // Join the parts back into a line
                }
                lines.add(line); // Add the line to the list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        // Rewrite the file with the updated information
        try (PrintWriter pw = new PrintWriter(new FileWriter(DB_FILE))) {
            for (String line : lines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
