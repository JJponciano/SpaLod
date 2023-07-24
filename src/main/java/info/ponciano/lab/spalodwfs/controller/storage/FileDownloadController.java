/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.ponciano.lab.spalodwfs.controller.storage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Claire #Ponciano
 */
// @CrossOrigin(origins = ""+KB.SERVER+":8080")
@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/download")
public class FileDownloadController {

    public static final String DIR = "dynamic_storage/";
    public static final String DOWNLOAD_DATA = "/download/data/";

    @RequestMapping("/data/{fileName:.+}")
    public void downloadPDFResource(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("fileName") String fileName) {
        //If user is not authorized - he should be thrown out from here itself
        //Authorized user will download the file
        //  String dataDirectory = request.getServletContext().getRealPath("/data/");
        Path file = Paths.get(DIR, fileName);
        if (Files.exists(file)) {
            response.setContentType("application/");
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            try {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        } else {
            System.out.println(file + " not exists");
        }
    }

}
