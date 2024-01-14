/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LectorDireccionArchivoDirectorio is part of MOP.
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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public abstract class LectorDireccionArchivoDirectorio {
	
	
	/**
	 * 
	 * @param soloDirectorio si es true se espera una dirección de directorio, si es false una dirección de archivo
	 * @param texto texto a presentar en la pantalla de bósqueda
	 * @param dirArchConf nombre del archivo de configuraciones a leer
	 * @param nombreProp nombre de la propiedad (dirección) a leer en el archivo de configuraciones
	 * @return un string con el nombre del directorio o archivo seleccionado
	 */
	public static String direccionLeida2(Boolean soloDirectorio, String texto, String dirArchConf, String nombreProp){
		String direccion = null;
	    String rutaDefecto = null;	
		LectorPropiedades lprop = new LectorPropiedades(dirArchConf);
		try {
			rutaDefecto = lprop.getProp(nombreProp);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    JFileChooser buscaDireccion = new JFileChooser();
	    buscaDireccion.setCurrentDirectory((new File(rutaDefecto)));
    
	    buscaDireccion.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if(!soloDirectorio) buscaDireccion.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    
	    buscaDireccion.setDialogTitle(texto);
	    buscaDireccion.setPreferredSize(new Dimension(1000, 430));
	    int result = buscaDireccion.showOpenDialog(null);
	
	    if(result == JFileChooser.APPROVE_OPTION){
	        File archEstExist = buscaDireccion.getSelectedFile();
	        direccion = archEstExist.getPath();
	    }
	
	    return direccion;
	}
	    
	    
	/**
	 * 
	 * @param soloDirectorio si es true se espera una dirección de directorio, si es false una dirección de archivo
	 * @param dirInicial directorio de inicio de la bósqueda
	 * @param texto texto a presentar en la pantalla de bósqueda
	 * @param dirArchConf nombre del archivo de configuraciones a leer
	 * @param nombreProp nombre de la propiedad (dirección) a leer en el archivo de configuraciones
	 * @return un string con el nombre del directorio o archivo seleccionado
	 */
	public static String direccionLeida(Boolean soloDirectorio, String dirInicial, String texto){
		String direccion = null;
	   
		String rutaDefecto = dirInicial;
	    JFileChooser buscaDireccion = new JFileChooser();
	    buscaDireccion.setCurrentDirectory((new File(rutaDefecto)));
    
	    buscaDireccion.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if(!soloDirectorio) buscaDireccion.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    
	    buscaDireccion.setDialogTitle(texto);
	    buscaDireccion.setPreferredSize(new Dimension(1000, 400));
	    int result = buscaDireccion.showOpenDialog(null);
	
	    if(result == JFileChooser.APPROVE_OPTION){
	        File archEstExist = buscaDireccion.getSelectedFile();
	        direccion = archEstExist.getPath();
	    }
	
	    return direccion;
	}	
	
	
	public static void main(String[] args) {             
	    
		boolean soloDirectorio = true;
		String texto = "Elija un directorio";
		//Boolean soloDirectorio, String texto, String dirArchConf, String nombreProp
	  	String dir = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, texto," ", "");
	  	System.out.println(dir);
	  	soloDirectorio = false;	
	  	texto = "Elija un archivo";
	  	String arch = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, texto," ", "");
	  	System.out.println(arch);
	    	
	}
	
	
	
	
	
	
	

}
