/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LectorPropiedades is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package utilitarios;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



public class LectorPropiedades {
	String result = "";
	String nombre_arch = "";
	InputStream inputStream;
	

	public LectorPropiedades(String nombre_arch) {
		super();
		this.nombre_arch = nombre_arch;		
	}


	public String getProp(String prop_name) throws IOException {

		try {
			Properties prop = new Properties();
			String propFileName =  nombre_arch;

			inputStream = new FileInputStream(propFileName);
			System.out.println(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}

			result = prop.getProperty(prop_name);
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} 
		return result.substring(1, result.length()-1);
	}
	public static void main(String[] args) {
		LectorPropiedades lp = new LectorPropiedades("mop.conf");
		String toto = "";
		try {
			 toto = lp.getProp("rutaEntradas");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(toto);

	}
}
