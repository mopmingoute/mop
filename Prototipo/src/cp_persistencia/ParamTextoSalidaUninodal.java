/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ParamTextoSalidaUninodal is part of MOP.
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

package cp_persistencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import utilitarios.AsistenteLectorEscritorTextos;

/**
 * Conserva los datos para interpretar las salidas de ResultadoEscenarios.txt que 
 * se producen en la programación estocástica.
 * Los números de fila empiezan en 0 y son desplazamientos respecto a la fila de inicio del escenario.
 * 
 * @author UT469262
 *
 */
public class ParamTextoSalidaUninodal {
	private int cantEsc;  // cantidad de escenarios
	private int cantFilas1Esc;  // cantidad de filas de un escenario
	private int filaCosmar;   // fila del costo marginal
	private int filaEncabezado; // 
	private int fIniFuentes;  // fila inicial de las fuentes
	private int largoFuentes; // largo en filas de las fuentes (cantidad de fuentes) sin el encabezado
	private int fIniUsos;
	private int largoUsos;    // ídem usos
	private int cantPostes;
	
	/**
	 * clave: nombre del participante (u oferta para las impoexpo)
	 * valor: fila inicial de otros datos del participante
	 */
	Hashtable<String, Integer> filaIniOtrosDatos;
	
	/**
	 * Lo mismo pero el valor es el largo en filas de los otros datos del participante
	 */
	Hashtable<String, Integer> largoOtrosDatos;
	

	public ParamTextoSalidaUninodal(int cantEsc, int cantPostes) {
		super();
		this.cantEsc = cantEsc;
		this.cantPostes = cantPostes;
		filaIniOtrosDatos =  new Hashtable<String, Integer>();
		largoOtrosDatos = new Hashtable<String, Integer>();
	}

	public int getCantEsc() {
		return cantEsc;
	}

	public void setCantEsc(int cantEsc) {
		this.cantEsc = cantEsc;
	}

	public int getCantFilas1Esc() {
		return cantFilas1Esc;
	}

	public void setCantFilas1Esc(int cantFilas1Esc) {
		this.cantFilas1Esc = cantFilas1Esc;
	}

	public int getfIniFuentes() {
		return fIniFuentes;
	}

	public void setfIniFuentes(int fIniFuentes) {
		this.fIniFuentes = fIniFuentes;
	}

	public int getLargoFuentes() {
		return largoFuentes;
	}

	public void setLargoFuentes(int largoFuentes) {
		this.largoFuentes = largoFuentes;
	}

	public int getfIniUsos() {
		return fIniUsos;
	}

	public void setfIniUsos(int fIniUsos) {
		this.fIniUsos = fIniUsos;
	}

	public int getLargoUsos() {
		return largoUsos;
	}

	public void setLargoUsos(int largoUsos) {
		this.largoUsos = largoUsos;
	}

	public int getCantPostes() {
		return cantPostes;
	}

	public void setCantPostes(int cantPostes) {
		this.cantPostes = cantPostes;
	}

	public Hashtable<String, Integer> getFilaIniOtrosDatos() {
		return filaIniOtrosDatos;
	}

	public void setFilaIniOtrosDatos(Hashtable<String, Integer> filaIniOtrosDatos) {
		this.filaIniOtrosDatos = filaIniOtrosDatos;
	}

	public Hashtable<String, Integer> getLargoOtrosDatos() {
		return largoOtrosDatos;
	}

	public void setLargoOtrosDatos(Hashtable<String, Integer> largoOtrosDatos) {
		this.largoOtrosDatos = largoOtrosDatos;
	}
	
	
	
	
	public int getFilaEncabezado() {
		return filaEncabezado;
	}

	public void setFilaEncabezado(int filaEncabezado) {
		this.filaEncabezado = filaEncabezado;
	}



	public int getFilaCosmar() {
		return filaCosmar;
	}

	public void setFilaCosmar(int filaCosmar) {
		this.filaCosmar = filaCosmar;
	}

	/**
	 * Crea en el archivo archSalida un texto con los parámetros a levantar desde excel
	 * separando campos por blancos
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos();
		sb.append(ale.escribeEtiqYEntero("cantEsc", cantEsc, "\t"));
		sb.append(ale.escribeEtiqYEntero("cantFilas1Esc", cantFilas1Esc, "\t"));
		sb.append(ale.escribeEtiqYEntero("fIniFuentes", fIniFuentes, "\t"));
		sb.append(ale.escribeEtiqYEntero("largoFuentes", largoFuentes, "\t"));
		sb.append(ale.escribeEtiqYEntero("fIniUsos", fIniUsos, "\t"));
		sb.append(ale.escribeEtiqYEntero("largoUsos", largoUsos, "\t"));		
		sb.append(ale.escribeEtiqYEntero("cantPostes", cantPostes, "\t"));			

		ArrayList<String> alp = new ArrayList<String>();
		alp.addAll(filaIniOtrosDatos.keySet());
		Collections.sort(alp);
		String l1 = "";
		String l2 = "";
		String l3 = "";
		for(int ip=0; ip<alp.size(); ip++) {
			String np = alp.get(ip);
			l1 += np + "\t";
			l2 += filaIniOtrosDatos.get(np) + "\t";
			l3 += largoOtrosDatos.get(np) + "\t";
		}
		sb.append(l1);
		sb.append("\n");		
		sb.append(l2);
		sb.append("\n");		
		sb.append(l3);	
		sb.append("\n");		
		sb.append(ale.escribeEtiqYDouble("filaCosmar", filaCosmar, "\t"));		
		return sb.toString();
		
		
	}
	
	
	
	
	
	
}
