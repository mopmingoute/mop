/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DirectoriosYArchivos is part of MOP.
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFileChooser;
	
	
public class DirectoriosYArchivos {
	
	


	

    /**
     * Contiene mótodos para crear y manejar archivos y directorios.
     */
    /**
     * Crea un archivo con lóneas de texto a partir de un String[][]
     * Hay una lónea para cada valor de i [i][.]
     * Cada lónea tiene Strings separados por un separador sep, dado por el usuario 
     * El texto es grabado en el archivo dirArchivo.
     */
    public static void creaTexto(String[][] texto, String dirArchivo, String sep) {
    	
	
        try {
            dirArchivo = barraAscendente(dirArchivo);
            File archGraba = new File(dirArchivo);
            // Crear el archivo si no existe
            boolean success = archGraba.createNewFile();
            if (success) {
                // El archivo no existóa y fue creado
            } else {
                // El archivo ya existóa
            }
            String linea;
            PrintStream print = new PrintStream(archGraba);
            for (int ilin = 0; ilin < texto.length; ilin++) {
                linea = "";
                for (int icol = 0; icol < texto[ilin].length; icol++) {
                    if (texto[ilin][icol] != null) {
                        linea = linea + texto[ilin][icol] + sep;

                    }else{
                        linea = linea + " " + sep;                        
                    }
                }
                print.println(linea);                
            }
            print.close();
        }  catch (IOException e) {
    		System.out.print("Error en archivo" + dirArchivo);
    		System.exit(1);
        }
    }

    /**
     * @author ut469262
     * Graba un texto en un archivo. Si el archivo no existe lo crea.
     * @param dirArchivo es el nombre del archivo incluso el path.
     * @param texto es el texto a grabar.
     */
    public static void grabaTexto(String dirArchivo, String texto)  {
        try {
            dirArchivo = barraAscendente(dirArchivo);
            File archGraba = new File(dirArchivo);
            // Crear el archivo si no existe
            boolean success = archGraba.createNewFile();
            if (success) {
                // El archivo no existóa y fue creado
            } else {
                // El archivo ya existóa
            }
            PrintStream print = new PrintStream(archGraba);
            print.println(texto);
            print.close();
        } catch (IOException e) {
    		System.out.print("Error al grabar el archivo" + dirArchivo);
    		System.exit(1);
        }
    }

    /**
     * Graba un texto en un archivo. Si el archivo no existe lo crea.
     * @param dirArchivo es el nombre del archivo incluso el path.
     * @param texto es el texto a grabar.
     */
    public static void agregaTexto(String dirArchivo, String texto)  {
        dirArchivo = barraAscendente(dirArchivo);
        try {
            File archGraba = new File(dirArchivo);
            if (!archGraba.exists()) {
                archGraba.createNewFile();
            }
            FileOutputStream fileStream = new FileOutputStream(archGraba, true);
            PrintStream print = new PrintStream(fileStream);
            print.println(texto);
            print.close();
        } catch (IOException e) {
    		System.out.print("Error en archivo" + dirArchivo);
    		System.exit(1);
        }
    }

    /**
     * Graba un texto en un archivo. Si el archivo no existe lo crea.
     * @param dirArchivo es el nombre del archivo incluso el path.
     * @param arrayst es el texto a grabar por filas y columnas
     * @parm sep es un separador de columnas a grabar
     */
    public static void agregaTexto(String dirArchivo, String[][] arrayst, String sep)  {
        dirArchivo = barraAscendente(dirArchivo);
        try {
            File archGraba = new File(dirArchivo);
            if (!archGraba.exists()) {
                archGraba.createNewFile();
            }
            FileOutputStream fileStream = new FileOutputStream(archGraba, true);
            PrintStream print = new PrintStream(fileStream);
            String texto = "";
            for(int ifil =0;ifil <arrayst.length;ifil++){
                for(int icol=0;icol<arrayst[ifil].length;icol++){
                    if(arrayst[ifil][icol]!= null){
                        texto += arrayst[ifil][icol] + sep;
                    }else{
                        texto += " " + sep;
                    }
                }                
                if(ifil!=arrayst.length-1) texto += "\r\n";                
            }
  
            print.println(texto);
            print.close();
        } catch (IOException e) {
    		System.out.print("Error en archivo" + dirArchivo);
    		System.exit(1);
        }
    }    
   
    
    /**
     * Graba un texto en un archivo. Si el archivo no existe lo crea.
     * @param dirArchivo es el nombre del archivo incluso el path.
     * @param arrayst es el texto a grabar por filas y columnas
     * @parm sep es un separador de columnas a grabar
     */
    public static void agregaTexto(String dirArchivo, ArrayList<ArrayList<String>> arrayst, String sep)  {
        dirArchivo = barraAscendente(dirArchivo);
        try {
            File archGraba = new File(dirArchivo);
            if (!archGraba.exists()) {
                archGraba.createNewFile();
            }
            FileOutputStream fileStream = new FileOutputStream(archGraba, true);
            PrintStream print = new PrintStream(fileStream);
            String texto = "";
            for(int ifil =0;ifil <arrayst.size();ifil++){
                for(int icol=0;icol<arrayst.get(ifil).size();icol++){
                    if(arrayst.get(ifil).get(icol)!= null){
                        texto += arrayst.get(ifil).get(icol) + sep;
                    }else{
                        texto += " " + sep;
                    }
                }                
                if(ifil!=arrayst.size()-1) texto += "\r\n";                
            }
                
            print.println(texto);
            print.close();
        } catch (IOException e) {
    		System.out.print("Error en archivo" + dirArchivo);
    		System.exit(1);
        }
    }    
    
    
    /**
     * Graba un texto en un archivo USANDO StreamBuilder. Si el archivo no existe lo crea.
     * @param dirArchivo es el nombre del archivo incluso el path.
     * @param arrayst es el texto a grabar por filas y columnas
     * @parm sep es un separador de columnas a grabar
     */
    public static void agregaTextoSB(String dirArchivo, ArrayList<ArrayList<String>> arrayst, String sep)  {
        dirArchivo = barraAscendente(dirArchivo);
        try {
            File archGraba = new File(dirArchivo);
            if (!archGraba.exists()) {
                archGraba.createNewFile();
            }
            FileOutputStream fileStream = new FileOutputStream(archGraba, true);
            PrintStream print = new PrintStream(fileStream);
            StringBuilder texto = new StringBuilder();
            for(int ifil =0;ifil <arrayst.size();ifil++){
                for(int icol=0;icol<arrayst.get(ifil).size();icol++){
                    if(arrayst.get(ifil).get(icol)!= null){
                        texto.append(arrayst.get(ifil).get(icol));
                        texto.append(sep);
                    }else{
                        texto.append(" " + sep);
                    }
                } 
                if(ifil!=arrayst.size()-1) texto.append("\r\n");                
            }
                
            print.println(texto.toString());
            print.close();
        } catch (IOException e) {
    		System.out.print("Error en archivo" + dirArchivo);
    		System.exit(1);
        }
    }        
    
    
    
    /**
     * Crea un directorio de nombre dirNuevo en el directorio dirRaiz
     * Si no existe dirRaiz hay un error.
     * @param dirRaiz es el path del directorio raóz.
     * @param dirNuevo es el nombre del subdirectorio a agregar.
     */
    public static void creaDirectorio(String dirRaiz, String dirNuevo)  {
        dirRaiz = barraAscendente(dirRaiz);
        dirNuevo = barraAscendente(dirNuevo);
        File pathDirRaiz = new File(dirRaiz);
        if (!pathDirRaiz.exists()) {
    		System.out.print("El directorio raíz " + dirRaiz + " no existe");
    		System.exit(1);
            
        }
        File pathDirectorio = new File(dirRaiz + "/" + dirNuevo);
        System.out.println(pathDirectorio);

        if (!pathDirectorio.mkdir()) {
            System.out.println("El directorio " + pathDirectorio.toString() + " ya existóa");
        }
      //  System.out.println("PASE POR ACA");
    }

    /**
     * @param dirArch es el archivo incluso path cuya existencia quiere verificarse
     * @return result es true si el archivo existe y false de lo contrario
     */
    public static boolean existeArchivo(String dirArch) {
        dirArch = barraAscendente(dirArch);
        File fin = new File(dirArch);
        boolean result = fin.exists();
        return result;
    }
    
    
    /**
     * @param dirDirec es el directorio cuya existencia quiere verificarse
     * @return result es true si el directorio existe y false de lo contrario
     */
    public static boolean existeDirectorio(String dirDir){
    	dirDir = barraAscendente(dirDir);
    	File pathDirectorio = new File(dirDir);
    	return pathDirectorio.exists();    	
    }

    
    /**
     * Devuelve la lista de archivos de un directorio, sin incluir los
     * subdirectorios
     */
    public static ArrayList<File> dameNombresArchivosDeDir(String rutaDir) {
    	ArrayList<File> listaArchivos = new ArrayList<File>();
		File folder = new File(rutaDir);
		File[] lista = folder.listFiles();
		for(int ia=0; ia<lista.length; ia++) {
			if (lista[ia].isFile())  listaArchivos.add(lista[ia]);
		}
		return listaArchivos;
    }
    
    
    /**
     * Elimina el archivo o directorio dirArch
     * Si es un directorio y no está vacío lanza una excepción
     * @param dirArch
     * @return
     */
    public static boolean eliminaArchivo(String dirArch) {
        dirArch = barraAscendente(dirArch);
        File f = new File(dirArch);
        // Make sure the file or directory exists and isn't write protected
        if (!f.exists()) {
            return false;
        }
        if (!f.canWrite()) {
            throw new IllegalArgumentException("Delete: write protected: " + dirArch);
        }
        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
            String[] files = f.list();
            if (files.length > 0) {
                throw new IllegalArgumentException(
                        "Delete: directory not empty: " + dirArch);
            }
        }
        // Attempt to delete it
        boolean success = f.delete();
//        if (!success) {
//            throw new IllegalArgumentException("Delete: deletion failed");
//        }
        return success;
    }
    
    
    /**
     * Elimina todos los archivos de un directorio dir,
     * sin eliminar el propio directorio
     */
    public static void eliminaArchivosYNoDir(String dir) {
    	File fdir = new File(dir);
    	for (File file: fdir.listFiles())
    	    if (!file.isDirectory())
    	        file.delete();
    }
    
    /**
     * Si existe el archivo dirArch lo elimina
     */
    public static void siExisteElimina(String dirArch){
    	if(existeArchivo(dirArch)) eliminaArchivo(dirArch);   	
    }
    
    
    /**
     * Devuelve la ruta del directorio final al que pertenece una ruta de archivo completa
     */
    public static String devuelveDirDeRutaArch(String rutaArch) {
		String[] tokens = rutaArch.split("/");
		String dir = "";
		for(int it=0; it<tokens.length-2; it++) {
			dir += tokens[it];
		}
    	return dir;   	   	
    }
    
    
    /**
     * Devuelve el nombre y extensión de un archivo (sin la ruta de directorios) a partir de la ruta
     * completa del archivo
     */
    public static String devuelveNombreYExtArch(String rutaArch) {
		String[] tokens = rutaArch.split("/");
    	return tokens[tokens.length-1];   	   	
    }   
    
    /**
     * Devuelve el nombre SIN extensión de un archivo y sin la ruta de directorios a partir de la ruta
     * completa del archivo
     */
    public static String devuelveNombreSinExtArch(String rutaArch) {
		String[] tokens = rutaArch.split("\\\\");
    	String nomYExt = tokens[tokens.length-1]; 
    	String[] tokens2 = nomYExt.split("\\.");
    	return tokens2[0];
    }     
    
    
    
    /**
     * Devuelve la extensión de un archivo a partir de la ruta
     * completa del archivo
     */
    public static String devuelveExtArch(String rutaArch) {
		String[] tokens = rutaArch.split("\\\\");
    	String nomYExt = tokens[tokens.length-1]; 
    	String[] tokens2 = nomYExt.split("\\.");
    	return tokens2[1];
    }      
   
    
    
    
    /**
     * Sustituye la barra descendente \ por la ascendente en el string input
     * @param input
     * @return 
     */
    public static String barraAscendente(String input) {
        StringBuffer buffer = new StringBuffer(input);
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '\\') {
                output.append('/');
            } else {
                output.append(buffer.charAt(i));
            }
        }
        return output.toString();
    }
    
    /**
     * Carga en un ArrayList<ArrayList<String>> las primeras n líneas de un archivo arch de texto
     * @param arch
     * @return
     */
    public static ArrayList<ArrayList<String>> leePrimerasNLineas(String arch, int n){
    	ArrayList<ArrayList<String>> nlin = new ArrayList<ArrayList<String>>();
    	ArrayList<ArrayList<String>> texto = utilitarios.LeerDatosArchivo.getDatos(arch);
    	for(int ilin=0; ilin<n; ilin++) {
    		nlin.add(texto.get(ilin));
    	}    	
    	return nlin;
       	
    }
    

    @SuppressWarnings("empty-statement")
    public static void copy(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
            }
            System.out.print("Overwrite existing file " + toFile.getName() + "? (Y/N): ");
            System.out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            String response = in.readLine();
            if (!response.equals("Y") && !response.equals("y")) {
                throw new IOException("FileCopy: " + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    ;
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    /**
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
     */
    public static void copy2(String stSrc, String stDst) throws IOException {
        File src = new File(stSrc);
        File dst = new File(stDst);
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    
    /**
     * Devuelve un String con un path de un archivo o directorio
     * 
     * @return 
     * @param "DIR" para leer un nombre de directorio y "ARCH" para un nombre de archivo
     * @param dirDefecto es el directorio por defecto que aparece en la pantalla
     */
    public static String pideDirOArch(String tipo, String dirDefecto) {  
        String stArch = "";                
        JFileChooser jfc = new JFileChooser();
        jfc.setSelectedFile(new File(dirDefecto));
        if(tipo.equalsIgnoreCase("DIR")){
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setDialogTitle("ELIJA UN DIRECTORIO");            
        }else if(tipo.equalsIgnoreCase("ARCH")) {
             jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
             jfc.setDialogTitle("ELIJA UN ARCHIVO");                         
        }
        jfc.setPreferredSize(new Dimension(800, 400));
        int result = jfc.showOpenDialog(null);
        if(result == JFileChooser.APPROVE_OPTION){
            File arch = jfc.getSelectedFile();
            stArch = arch.getPath();
        }else{
    		System.out.print("NO SE ELIGIó ARCHIVO O DIRECTORIO");
    		System.exit(1);        	
        }
        return DirectoriosYArchivos.barraAscendente(stArch); 
        
    }    
    
    public static void crearDirsRuta(String ruta) {
        
    	Path path = Paths.get(ruta);
    	try {
    		Files.createDirectories(path);
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}


    }
    
    
    /**
     * Escribe en un archivo nuevo el ArrayList de String, a razón de un elemento por línea
     * @param archivo
     * @param al
     */
    public static void escribeArrayList(String archivo, ArrayList<String> al) {
		StringBuilder sb = new StringBuilder();
		for(String s: al) {
			sb.append(s + "\n");
		}
		DirectoriosYArchivos.siExisteElimina(archivo);
		DirectoriosYArchivos.agregaTexto(archivo, sb.toString());		
    } 
    
    /**
     * Escribe en el archivo una matriz de double[nfil, ncol] con columnas separadas por el separador sep
     * a razón de una fila por línea del archivo.
     * En la primera línea escribe una línea de titulos titCol [1 x ncol+1], empezando en la columna 0. Si titCol==null no escribe la línea
     * En la primera columna de las filas siguientes a la inicial escribe una columna titFil [nfil x 1]. Si titFil==null no escribe la columna
     * Si el archivo no existe lo crea. Si existe, agrega las líneas.
     * 
     * 
     * @param archivo
     * @param mat
     * @param sep
     */
    public static void agregaTablaReal(String archivo, String[] titCol, String[] titFil, double[][] mat, String sep) {
    	StringBuilder sb = new StringBuilder();
    	if(titFil!= null && titFil.length!=mat.length) System.out.println("Error en dimensión de títulos de filas en agregaTablaReal");
    	if(titCol!=null && titCol.length!=mat[0].length+1) System.out.println("Error en dimensión de títulos de columnas en agregaTablaReal");
    	if(titCol!=null) {    		
    		for(int j=0; j<titCol.length; j++) {
    			sb.append(titCol[j] + sep);
    		}
    	}
    	for(int i=0; i<mat.length; i++) {
    		if(titFil!=null) sb.append(titFil[i] + sep);
    		for(int j=0; j<mat[0].length; j++) {
    			sb.append(mat[i][j] + sep);   			
    		}
    		if(i<mat.length-1)sb.append("\n");
    	}
    	agregaTexto(archivo, sb.toString());
    }
 
    
    /**
     * Escribe en el archivo una matriz de Double[nfil, ncol] con columnas separadas por el separador sep
     * a razón de una fila por línea del archivo.
     * En la primera línea escribe una línea de titulos titCol [1 x ncol+1], empezando en la columna 0. Si titCol==null no escribe la línea
     * En la primera columna de las filas siguientes a la inicial escribe una columna titFil [nfil x 1]. Si titFil==null no escribe la columna
     * Si el archivo no existe lo crea. Si existe, agrega las líneas.
     * 
     * 
     * @param archivo
     * @param mat
     * @param sep
     */    
    public static void agregaTablaReal(String archivo, String[] titCol, String[] titFil, Double[][] mat, String sep) {
       	StringBuilder sb = new StringBuilder();
    	if(titFil!= null && titFil.length!=mat.length) System.out.println("Error en dimensión de títulos de filas en agregaTablaReal");
    	if(titCol!=null && titCol.length!=mat[0].length+1) System.out.println("Error en dimensión de títulos de columnas en agregaTablaReal");
    	if(titCol!=null) {    		
    		for(int j=0; j<titCol.length; j++) {
    			sb.append(titCol[j] + sep);
    		}
    	}
    	for(int i=0; i<mat.length; i++) {
    		if(titFil!=null) sb.append(titFil[i] + sep);
    		for(int j=0; j<mat[0].length; j++) {
    			sb.append(mat[i][j] + sep);   			
    		}
    		sb.append("\n");
    	}
    	agregaTexto(archivo, sb.toString());
    }

    
    public static void main(String[] args) throws IOException {
 
  
        String[] s1 = new String[] {"1", "2", "3"};
        String[] s2 = new String[] {"1", "2", "3"};        
        String[][] st = new String[2][];
        st[0]=s1;
        st[1]=s2;
        String dirArchivo = "";
        agregaTexto(dirArchivo, st, ",");
        agregaTexto(dirArchivo, st, ",");        
    }
	

}
