package info.ponciano.lab.spalodwfs.controller.security;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    // private String username;
    // private String password;
    // private List<String> roles;

    // public User(String username,String password) {
    //     this.username = username;
    //     this.password = password;
    //     this.roles = new ArrayList<>();
    //     this.roles.add("USER");
    // }

    // @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    // public User(@JsonProperty("username") String _username,
    //             @JsonProperty("password") String _password,
    //             @JsonProperty("roles") List<String> _roles) {
    //     this.username = _username;
    //     this.password = _password;
    //     this.roles = _roles;
    // }

    // // Getters and setters
    // public String getUsername() {
    //     return username;
    // }

    // public void setUsername(String username) {
    //     this.username = username;
    // }

    // @JsonIgnore
    // public String getPassword() {
    //     return password;
    // }

    // @JsonProperty("password")
    // public void setPassword(String password) {
    //     this.password = password;
    // }

    // public List<String> getRoles() {
    //     return roles;
    // }

    // public void setRoles(List<String> roles) {
    //     this.roles = roles;
    // }

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
