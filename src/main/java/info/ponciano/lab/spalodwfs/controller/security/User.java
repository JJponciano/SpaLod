package info.ponciano.lab.spalodwfs.controller.security;

import java.util.ArrayList;

public class User {
   

    private String username;
    private String password;
    private ArrayList<String> roles;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<String>();
        this.roles.add("USER");
    }


    public User(String _username, String _password, ArrayList<String> _roles) {
        this.username = _username;
        this.password = _password;
        this.roles = _roles;
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

    public ArrayList<String> getRoles()
    {
        return roles;
    }
}
