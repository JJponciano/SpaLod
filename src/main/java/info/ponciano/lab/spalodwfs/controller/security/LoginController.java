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

import java.security.PublicKey;
import java.util.Base64;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.geosparql.spatial.property_functions.cardinal.SouthGeomPF;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController

{
      private final RsaKeys rsaKeys;

      public LoginController(RsaKeys rsaKeys) {
            this.rsaKeys = rsaKeys;
        }
    
      @GetMapping("/getKey")
      public ResponseEntity<String> getPublicKey() {
            PublicKey publicKey = rsaKeys.getPublicKey();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            return ResponseEntity.ok(publicKeyString);
      }

      @RolesAllowed({"USER","ADMIN"})
      @RequestMapping("/*")
      public String getUser()
      {
                  return "Welcome User";
      }

      @RolesAllowed("ADMIN")
      @RequestMapping("/admin")
      public String getAdmin()
      {
            System.out.println("curl successful");
            return "Welcome Admin";
      }

      
      

}
