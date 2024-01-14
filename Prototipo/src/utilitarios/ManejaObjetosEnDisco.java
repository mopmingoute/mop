/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ManejaObjetosEnDisco is part of MOP.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 *
 * @author ut600232
 */
public abstract class ManejaObjetosEnDisco {
    /**
     *
     * Contiene los mótodos para serializar y guardar en disco objetos y para
     * levantar de disco objetos serializados y reconstituirlos.
     *
     * Los nombres de directorio no terminan en /
     */





    /**
     * Guarda un objeto en el directorio especificado
     * y con el nombre especificado.
     *
     * @param rutaDirectorio es el path del directorio donde se guardaró el objeto serializado.
     * @param nombreArchivo es el nombre del archivo del objeto serializado.
     * @param objeto es el objeto a serializar.
     */
    public static void guardarEnDisco(String rutaDirectorio, String nombreArchivo, Object objeto) throws FileNotFoundException,
			IOException{
       rutaDirectorio = utilitarios.DirectoriosYArchivos.barraAscendente(rutaDirectorio);
       try{
            FileOutputStream f = new FileOutputStream(rutaDirectorio + "/" + nombreArchivo);
            ObjectOutputStream s = new ObjectOutputStream(f);

            /** Escritura.
             */
            s.writeObject(objeto);

            s.close();
            f.close();
        }catch(IOException e){
        	System.out.println("Error al serializar " + nombreArchivo);
        }
    }

    /**
     * Guarda un objeto en el directorio especificado
     * y con el nombre especificado.
     *
     * @param fNombre es el nombre del archivo que se crearó con el objeto serializado.
     * @param objeto es el objeto a serializar.
     */
    public static void guardarEnDisco(File fNombre, Object objeto) {
       try{
            FileOutputStream f = new FileOutputStream(fNombre);
            ObjectOutputStream s = new ObjectOutputStream(f);

            /** Escritura.
             */
            s.writeObject(objeto);

            s.close();
            f.close();
        }catch(Exception e){
            System.out.println("Error en mótodo guardarEnDisco, archivo " + fNombre);
            System.exit(0);
        }
    }

    
    
    public static Object traerDeDisco(String rutaDirectorio, String nombreArchivo) {  	
    	rutaDirectorio = utilitarios.DirectoriosYArchivos.barraAscendente(rutaDirectorio);
        Object objeto = null;
        try{
            FileInputStream f = new FileInputStream(rutaDirectorio + "/" + nombreArchivo);
            ObjectInputStream s = new ObjectInputStream(f);

            /** Escritura.
             */
            objeto = s.readObject();

            s.close();
            f.close();

        }catch(Exception e){
        	System.out.println(rutaDirectorio);
            System.out.println("Error en mótodo traerDeDisco, archivo " + nombreArchivo);
            System.exit(0);
        }
        return objeto;
    }

    public static Object traerDeDisco(String ruta) {
    	try {
	    	File f = new File(ruta);
	    	return traerDeDisco(f);
    	} catch(Exception e){
            System.out.println("Error en mótodo traerDeDisco, archivo " + ruta);
            System.exit(0);
        }
    	return null;
    }
    
    public static Object traerDeDisco(File fNombre) throws FileNotFoundException,
			IOException,
			ClassNotFoundException{
        Object objeto = null;

        try{
            FileInputStream f = new FileInputStream(fNombre);
            System.out.println(fNombre.getAbsolutePath());
            ObjectInputStream s = new ObjectInputStream(f);

            /** Escritura.
             */
            objeto = s.readObject();

            s.close();
            f.close();

        }catch(IOException e){
            System.out.println("NO SE PUEDE TRAER DE DISCO: " + fNombre.getAbsolutePath());
        }
        return objeto;
    }



    /**
     * Guarda una lista de objetos en el directorio especificado
     * y con el nombre especificado.
     *
     * @param rutaDirectorio es el path del directorio donde se guardaró el objeto serializado.
     * @param nombreArchivo es el nombre del archivo del objeto serializado.
     * @param objeto es el objeto a serializar.
     */
    public static void guardaListaObjEnDisco(String rutaDirectorio, String nombreArchivo,
            ArrayList<Object> listaObjetos) throws FileNotFoundException,IOException{
       rutaDirectorio = utilitarios.DirectoriosYArchivos.barraAscendente(rutaDirectorio);
       try{
            FileOutputStream f = new FileOutputStream(rutaDirectorio + "/" + nombreArchivo);
            ObjectOutputStream s = new ObjectOutputStream(f);

            /** Escritura.
             */
            for(Object objeto: listaObjetos){
                s.writeObject(objeto);
            }

            s.close();
            f.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public static ArrayList<Object> traerListaObjDeDisco(String rutaDirectorio, String nombreArchivo) throws FileNotFoundException,
			IOException,
			ClassNotFoundException{
        rutaDirectorio = utilitarios.DirectoriosYArchivos.barraAscendente(rutaDirectorio);
        ArrayList<Object> listaObjetos = new ArrayList<Object>();
        boolean sigo = true;
        FileInputStream f = null;
        ObjectInputStream s = null;
        try{
            f = new FileInputStream(rutaDirectorio + "/" + nombreArchivo);
            s = new ObjectInputStream(f);
            int contadorObj = 1;
            while(sigo){
                Object objeto = s.readObject();
                listaObjetos.add(objeto);
                if( Math.IEEEremainder(contadorObj,100)==0.0) 
                    System.out.println("lee objeto serializado numero" + contadorObj);
                contadorObj++;
            }

        }catch(IOException e){
            s.close();
            f.close();
        }
        return listaObjetos;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException{

        ArrayList<Object> listaObjetos = new ArrayList<Object>();
        
        
        for(int i = 1; i<10; i++){
            String snum = String.valueOf(i);
            ObjetoDePrueba obj = new ObjetoDePrueba(snum);
            listaObjetos.add(obj);
        }
        String direc = "D:/salidasModeloOp";
        String arch = "listaObjetos";

        guardaListaObjEnDisco(direc, arch, listaObjetos);

        ArrayList<Object> listaObjetos2 = new ArrayList<Object>();
        listaObjetos2 = traerListaObjDeDisco(direc, arch);
        for(Object ob2: listaObjetos2){
            ObjetoDePrueba op = (ObjetoDePrueba)ob2;
            System.out.println("Prueba de serialización");
            System.out.println(op.getNumero());
        }



    }




	
}

