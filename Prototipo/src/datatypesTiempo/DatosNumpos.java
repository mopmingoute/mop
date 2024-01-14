/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosNumpos is part of MOP.
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

package datatypesTiempo;

import java.util.ArrayList;

import persistencia.EscritorResumenSimulacionParalelo;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;

public class DatosNumpos {

	private ArrayList<String> nombresIntMuestreo; // lista de los nombres de los intervalos de muestreo: fecha-IM=nn
	private int cantEsc; // cantidad de escenarios
	private int cantIM; // cantidad de intervalos de muestreo
	private ArrayList<ArrayList<Integer>> numpos; // primer índice escenario, segundo índice intervalo de muestreo
	private static int cantLineasCabezal;
	private static ArrayList<ArrayList<String>> cabezalExtendido;

	static {
		cantLineasCabezal = EscritorResumenSimulacionParalelo.getCantLineasCabezal();
	}

	public DatosNumpos(ArrayList<String> nombresIntMuestreo, int cantEsc, int cantIM,
			ArrayList<ArrayList<Integer>> numpos) {
		super();
		this.nombresIntMuestreo = nombresIntMuestreo;
		this.cantEsc = cantEsc;
		this.cantIM = cantIM;
		this.numpos = numpos;
	}

	/**
	 * Carga un datatype DatosNumpos de un archivo
	 * 
	 * @param archNumpos
	 */
	public static DatosNumpos leeDatosNumpos(String archNumpos) {

		ArrayList<ArrayList<Integer>> aux2d = new ArrayList<ArrayList<Integer>>();
		cabezalExtendido = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> texto;
		texto = LeerDatosArchivo.getDatos(archNumpos);
		for (int i = 0; i < cantLineasCabezal + 1; i++) {
			ArrayList<String> aux1l = new ArrayList<String>();
			for (int j = 0; j < texto.get(i).size(); j++) {
				aux1l.add(texto.get(i).get(j));
			}
			cabezalExtendido.add(aux1l);
		}
		ArrayList<String> nomIM = texto.get(cantLineasCabezal);
		int cIM = nomIM.size();
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos(texto, archNumpos);
		int iInit = cantLineasCabezal + 2;
		int ce = 0;
		for (int i = iInit; i < texto.size(); i++) {
			ce++;
			ArrayList<Integer> aux = new ArrayList<Integer>();
			ArrayList<String> al = ale.cargaLista(i, "ESCENARIO");
			for (int j = 0; j < al.size() - 1; j++) {
				int d = Integer.parseInt(al.get(j + 1));
				aux.add(d);
			}
			aux2d.add(aux);
		}
		DatosNumpos result = new DatosNumpos(nomIM, ce, cIM, aux2d);
		return result;
	}

	/**
	 * Escribe el DatosNumpos dn en el archivo archNumpos
	 * 
	 * @param archNumpos
	 * @param dn
	 */
	public static void escribeDatosNumpos(String archNumpos, DatosNumpos dn) {

		DirectoriosYArchivos.siExisteElimina(archNumpos);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cantLineasCabezal + 1; i++) {
			for (int j = 0; j < cabezalExtendido.get(i).size(); j++) {
				sb.append(cabezalExtendido.get(i).get(j) + "\t");
			}
			if (i < cantLineasCabezal)
				sb.append("\n");
		}
		DirectoriosYArchivos.agregaTexto(archNumpos, sb.toString());

		for (int i = 0; i < dn.getCantEsc(); i++) {
			sb = new StringBuilder();
			sb.append("ESCENARIO\t" + i + "\t");
			for (int j = 0; j < dn.getCantIM(); j++) {
				sb.append(dn.getNumpos().get(i).get(j) + "\t");
			}
			DirectoriosYArchivos.agregaTexto(archNumpos, sb.toString());
		}
	}

	public ArrayList<String> getNombresIntMuestreo() {
		return nombresIntMuestreo;
	}

	public void setNombresIntMuestreo(ArrayList<String> nombresIntMuestreo) {
		this.nombresIntMuestreo = nombresIntMuestreo;
	}

	public int getCantEsc() {
		return cantEsc;
	}

	public void setCantEsc(int cantEsc) {
		this.cantEsc = cantEsc;
	}

	public int getCantIM() {
		return cantIM;
	}

	public void setCantIM(int cantIM) {
		this.cantIM = cantIM;
	}

	public ArrayList<ArrayList<Integer>> getNumpos() {
		return numpos;
	}

	public void setNumpos(ArrayList<ArrayList<Integer>> numpos) {
		this.numpos = numpos;
	}

	public static int getCantLineasCabezal() {
		return cantLineasCabezal;
	}

	public static void setCantLineasCabezal(int cantLineasCabezal) {
		DatosNumpos.cantLineasCabezal = cantLineasCabezal;
	}

	public static ArrayList<ArrayList<String>> getCabezalExtendido() {
		return cabezalExtendido;
	}

	public static void setCabezalExtendido(ArrayList<ArrayList<String>> cabezalExtendido) {
		DatosNumpos.cabezalExtendido = cabezalExtendido;
	}

	public static void main(String[] args) {

		String dir = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS/PRUEBAS-SALIDAS-MAYO22";
		String nomEnt = "numposEntrada.txt";
		String nomSal = "numposSalida.txt";
		String arch = dir + "/" + nomEnt;
		DatosNumpos dn = leeDatosNumpos(arch);
		System.out.println("Termina lectura numpos");
		arch = dir + "/" + nomSal;
	//	dn.escribeDatosNumpos(arch, dn);
		System.out.println("Termina escritura numpos");
	}

}
