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

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import info.ponciano.lab.spalodwfs.model.TripleData;
import info.ponciano.lab.spalodwfs.model.TripleOperation;


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

    public void saveInOnt(User user)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(user.getUsername())) {
                    String uuid=parts[0];
                    // Create the tripleData object
                    TripleData tripleData = new TripleData(
                        "http://lab.ponciano.info/ont/spalod#"+uuid, //subject
                        "https://xmlns.com/foaf/0.1/:name", //predicate
                        "http://lab.ponciano.info/ont/spalod#"+user.getUsername()   //object
                    );

                    //ADD IN ONTOLOGY
                    // Create a new JSONObject
                    JSONObject json = new JSONObject();

                    // Add values to the JSON object
                    json.put("operation", "add");

                    // Create a nested JSON object for tripleData
                    JSONObject tripletJson = new JSONObject();
                    tripletJson.put("subject",tripleData.getSubject());
                    tripletJson.put("predicate", tripleData.getPredicate());
                    tripletJson.put("object", tripleData.getObject());

                    json.put("tripleData", tripletJson);

                    // Convert the JSON object to a string
                    String jsonString = json.toString();

                    // Send the tripleOperation to the update endpoint
                    String url = "https://localhost:8081/api/update"; // Modify the URL according to your endpoint

                    // Create custom SSLContext that trusts all certificates
                    SSLContext sslContext;
                    try {
                        sslContext = SSLContextBuilder
                                .create()
                                .loadTrustMaterial(new TrustSelfSignedStrategy())
                                .build();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    // Create custom HttpClient with the custom SSLContext
                    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
                    requestFactory.setHttpClient(
                            HttpClientBuilder
                                    .create()
                                    .setSSLContext(sslContext)
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                    .build()
                    );


                    // Create RestTemplate instance
                    RestTemplate restTemplate = new RestTemplate(requestFactory);
                    
                    // Set request headers
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    // Set request entity with headers
                    HttpEntity<String> requestEntity = new HttpEntity<>(jsonString, headers);

                    restTemplate.postForEntity(url, requestEntity, Void.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

}
