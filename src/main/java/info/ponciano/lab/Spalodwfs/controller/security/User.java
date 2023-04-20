package info.ponciano.lab.spalodwfs.controller.security;

import java.util.List;

public class User {
    private String username;
    private String password;
    private List<String> roles;

    public User(String username, String password, List<String> roles2) {
        this.username = username;
        this.password = password;
        this.roles = roles2;
    }

    // Getters and setters
    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public List<String> getRoles()
    {
        return roles;
    }
}
