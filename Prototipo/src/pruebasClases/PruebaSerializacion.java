/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PruebaSerializacion is part of MOP.
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

package pruebasClases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import utilitarios.DirectoriosYArchivos;

public class PruebaSerializacion {
	public static final int cantReales = 100000;
//	public static final String dirDisco = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBA-SERIALIZACION";
	public static final String dirDisco = "D:/PRUEBA-SERIALIZACION";	

	public static final int cantLecturas = 10;

		
		
	
	
	public static void main(String[] args){
		Paquete paq = new Paquete();
		for(int i=0; i<cantReales; i++){
			paq.cargaReal((double)i);
		}
		String dirArch = dirDisco + "/" + "objeto";
		DirectoriosYArchivos.siExisteElimina(dirArch);
		System.out.println("Empieza a escribir "+System.currentTimeMillis());
		try {
			utilitarios.ManejaObjetosEnDisco.guardarEnDisco(dirDisco, "objeto", paq);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Termina de escribir " + System.currentTimeMillis());
		for(int i=1; i<=cantLecturas;i++){
			Paquete paq2 = (Paquete)utilitarios.ManejaObjetosEnDisco.traerDeDisco(dirDisco, "objeto");
			System.out.println("Termina lectura "+ i + " " + System.currentTimeMillis());
		}
		System.out.println("Termina de ejecutar " + System.currentTimeMillis());
	}

}
