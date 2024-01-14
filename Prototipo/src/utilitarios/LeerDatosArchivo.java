/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LeerDatosArchivo is part of MOP.
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



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ut469262
 */
public class LeerDatosArchivo {

    /**
     * Lee un archivo de texto por líneas y separa dentro de las líneas por blancos,
     * eliminando comentarios //
     * @param dirArchivo
     * @return resultado es el ArrayList<ArrayList<String>> con el texto separado en líneas
     */
    public static ArrayList<ArrayList<String>> getDatos(String dirArchivo){
        BufferedReader entrada = null;
        ArrayList<ArrayList<String>> resultado =
                                            new ArrayList<ArrayList<String>>();
        ArrayList<String> lineaResul;
        String[] datosLinea;
        String linea;
        String sep = "[\\s]+"; // Separador de datos.
        String coment = "//"; // Indicador de comentario.
        int posCom; // Posición del inicio del comentario.
        int j;

        try{
            File archivo = new File(dirArchivo);
            entrada = new BufferedReader(new FileReader(archivo));
            while( (linea = entrada.readLine()) != null){
                // Eliminación de los comentarios de las lóneas.
                if( (posCom = linea.indexOf(coment)) > -1 ){
                    linea = (posCom == 0)?"": linea.substring(0, posCom);              
                }
                // Si la lónea no es una lónea de blancos, entonces se agregan
                // los datos y se agrega una nueva "lónea" a resultado.               
                if( !linea.matches("[\\s]*") && !(linea.length()==0)){
                    datosLinea = linea.split(sep);
                    lineaResul = new ArrayList<String>();
                    for(j = 0; j < datosLinea.length; j++){
                        /* Si el dato NO es un string de longitud cero "",
                         * lo cual podróa ocurrir si
                         * la lónea comienza por ej. con tabulador, se agrega
                         * el dato. */
                        if( !datosLinea[j].equalsIgnoreCase("") ){
                            lineaResul.add(datosLinea[j]);
                        }
                    }
                    resultado.add(lineaResul);
                }
            }
            entrada.close();
        }catch(FileNotFoundException ex){
                System.out.println("------ERROR en LeerDatosArchivo-------");
            System.out.println("No se encontró el archivo: " + dirArchivo);
            System.out.println("------------------\n");
            //System.exit(1);
        }catch(IOException ex){
            System.out.println("------ERROR en LeerDatosArchivo-------");
            System.out.println("Al leer el archivo: " + dirArchivo +
                    ", se produjo la excepción: " + ex.toString() );
            System.out.println("------------------\n");
            //System.exit(1);
        }


        return resultado;
    }


}
