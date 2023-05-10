package info.ponciano.lab.spalodwfs.controller.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam("username") String username, @RequestParam("password") String password) {
        
        User user = new User(username, password);
        userService.save(user);
        
        return ResponseEntity.ok("User registered successfully");
    }
}
