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
package info.ponciano.lab.spalodwfs.mvc.models.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 * Parses the config file for the input formats
 * @author timo.homburg
 *
 */
public class ConfigParser {

	public Map<String,List<String>> maps=new TreeMap<>();
	
	public ConfigParser(String path) throws IOException {
		BufferedReader reader=new BufferedReader(new FileReader(new File(path)));
		String line;
		while((line=reader.readLine())!=null) {
			String[] linearr=line.split(",");
			List<String> linelist=Arrays.asList(linearr);
			linelist=linelist.subList(1, linelist.size());
			maps.put(linearr[0], linelist);
		}
		reader.close();
	}
	
	
}
