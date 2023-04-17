package info.ponciano.lab.spalodwfs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import info.ponciano.lab.spalodwfs.model.FormData;
import info.ponciano.lab.spalodwfs.services.FormDataService;

@RequestMapping("/api")
public class Home {

    @Autowired
    private FormDataService formDataService;

    @PostMapping("/home")
    public ResponseEntity<Void> saveFormData(@RequestBody FormData formData) {
      System.out.println("################### #################");
      System.out.println(formData);
      System.out.println("################### #################");
        formDataService.saveFormData(formData);
        return ResponseEntity.ok().build();
    }
}