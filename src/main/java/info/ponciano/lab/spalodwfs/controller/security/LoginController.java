/*
 * Copyright (C)  2021 Dr Claire Prudhomme <claire@prudhomme.info).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package info.ponciano.lab.spalodwfs.controller.security;

import java.io.IOException;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController
{
      @Autowired
      private UserService userService;

      @RolesAllowed({"USER","ADMIN"})
      @RequestMapping("/user")
      public String getUser()
      {
                  return "Welcome User";
      }

      @RolesAllowed("ADMIN")
      @RequestMapping("/admin")
      public String getAdmin()
      {
            return "Welcome Admin";
      }

      @RolesAllowed("ADMIN")
      @RequestMapping("/addAdmin")
      public ResponseEntity<String> addAdmin(@RequestParam("username") String username)
      {
            try {
                  userService.promoteUserToAdmin(username);
            } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
            }
            return ResponseEntity.ok("Admin added successfully");
      }
      
      @RolesAllowed({"USER","ADMIN"})
      @GetMapping("/status")
      public ResponseEntity<String> getStatus()
      {
            return new ResponseEntity<>("Logged In",HttpStatus.OK);
      }
}
