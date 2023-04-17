/*
 * Copyright (C) 2021 Dr. Jean-Jacques Ponciano.
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
package info.ponciano.lab.spalodwfs.mvc.controllers.last;

import info.ponciano.lab.spalodwfs.controller.storage.FileDownloadController;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;

/**
 *
 * @author jean-jacques ponciano
 */
@Controller
public class Home {
       @GetMapping("/")
    public String thematicMaps(Model model) throws OntoManagementException  {
        String rtn="home";
           init();
           return rtn;
    }

    private void init() {
        File f = new File(FileDownloadController.DIR);
        if (!f.exists())
            f.mkdir();
    }

    @GetMapping("/home")
    public String home(Model model) throws OntoManagementException  {
        String rtn="home";
        init();
        return rtn;
    }

}
