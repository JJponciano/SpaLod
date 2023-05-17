package info.ponciano.lab.spalodwfs.controller.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class UserService implements UserDetailsService {
    
    private final String DB_FILE = "./src/main/java/info/ponciano/lab/spalodwfs/controller/security/users.txt";

    // Finds a user by their username
    public User findByUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(username)) {
                    String password = parts[2];
                    ArrayList<String> roles = new ArrayList<>(Arrays.asList(parts[3].split(";")));
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
            String uuid = generateUUID();
            String roles = String.join(";", user.getRoles());
            pw.println(uuid+","+user.getUsername() + "," + new BCryptPasswordEncoder(10).encode(user.getPassword()) + "," + roles);
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
                if (parts[1].equals(username) && (!parts[3].contains("ADMIN")) ) {
                    parts[3] += ";ADMIN"; // Update the role to ADMIN
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
    
    public static String generateUUID() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        // Set version (4) and variant (2) bits
        randomBytes[6] &= 0x0f;  // Version (4) bits
        randomBytes[6] |= 0x40;
        randomBytes[8] &= 0x3f;  // Variant (2) bits
        randomBytes[8] |= 0x80;

        return UUID.nameUUIDFromBytes(randomBytes).toString();
    }

    public String getUUID(String username)
    {   
        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(username)) {
                    return parts[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
