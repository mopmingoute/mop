/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EnumeradorLexicografico is part of MOP.
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

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author ut469262
 *
 *         Genera vectores de cantDigitos enteros ordenados, en forma sucesiva
 *         donde cada vector aparece en orden lexicogrófico.
 *
 */

public class EnumeradorLexicografico {

	private String nombre;
	private int cantDigitos;
	private int[] cotasInferiores;
	private int[] cotasSuperiores;
	private int[] cantCasosAcum; // cantidad de casos por unidad de dógitos; cantCasosAcum[0] no tiene sentido
	private int cursor;
	private int[] proximoADevolver;
	private boolean terminado;
	private int cantTotalVectores;
	/**
	 * proximoADevolver es el próximo vector que devolveró el enumerador.
	 *
	 * cursor indica la posición del óltimo dógito que cambió cuando se creó el
	 * vector proximoADevolver.
	 *
	 * Las posiciones del cursor varóan desde cantDigitos-1 hasta 0 es decir que el
	 * orden es por ejemplo: 1111, 1112, 1113, etc.
	 */

	/**
	 * Lista que tiene ordenados los vectores que genera el enumerador
	 */
	private ArrayList<int[]> listaVectorDeOrdinal;

	/**
	 * Constructor. Devuelve el enumerador inicializado para devolver el primer
	 * vector.
	 * 
	 * @param cantDigitos
	 * @param cotasInferiores
	 * @param cotasSuperiores
	 */
	public EnumeradorLexicografico(int cantDigitos, int[] cotasInferiores, int[] cotasSuperiores) {
		if (cantDigitos > 0) {
			this.cantDigitos = cantDigitos;
			this.cotasInferiores = cotasInferiores;
			this.cotasSuperiores = cotasSuperiores;
			int[] cantCasos = new int[cantDigitos];

			for (int j = 0; j < cantDigitos; j++) {
				cantCasos[j] = cotasSuperiores[j] - cotasInferiores[j] + 1;
			}
			cantCasosAcum = new int[cantDigitos];
			cantCasosAcum[cantDigitos - 1] = 1;
			for (int j = cantDigitos - 2; j >= 0; j--) {
				cantCasosAcum[j] = cantCasos[j + 1] * cantCasosAcum[j + 1];
			}
			assert (cotasInferiores.length == cantDigitos) : "Error en cantidad de cotas inferiores en enumerador";
			assert (cotasSuperiores.length == cantDigitos) : "Error en cantidad de cotas superiores";
			proximoADevolver = cotasInferiores.clone();
			cursor = cantDigitos - 1;
			terminado = false;
			cantTotalVectores = devuelveOrdinalDeVector(cotasSuperiores) + 1;
		} else {
			terminado = true;
			cantTotalVectores = 0;
		}
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getCantDigitos() {
		return cantDigitos;
	}

	public void setCantDigitos(int cantDigitos) {
		this.cantDigitos = cantDigitos;
	}

	public int[] getCotasInferiores() {
		return cotasInferiores;
	}

	public void setCotasInferiores(int[] cotasInferiores) {
		this.cotasInferiores = cotasInferiores;
	}

	public int[] getCotasSuperiores() {
		return cotasSuperiores;
	}

	public void setCotasSuperiores(int[] cotasSuperiores) {
		this.cotasSuperiores = cotasSuperiores;
	}

	public int[] getCantCasosAcum() {
		return cantCasosAcum;
	}

	public void setCantCasosAcum(int[] cantCasosAcum) {
		this.cantCasosAcum = cantCasosAcum;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public int[] getProximoADevolver() {
		return proximoADevolver;
	}

	public void setProximoADevolver(int[] proximoADevolver) {
		this.proximoADevolver = proximoADevolver;
	}

	public boolean isTerminado() {
		return terminado;
	}

	public void setTerminado(boolean terminado) {
		this.terminado = terminado;
	}

	/*
	 * Devuelve la lista ordenada de vectores int[] que genera el enumerador
	 */
	public ArrayList<int[]> getListaVectorDeOrdinal() {
		return listaVectorDeOrdinal;
	}

	public void setListaVectorDeOrdinal(ArrayList<int[]> listaVectorDeOrdinal) {
		this.listaVectorDeOrdinal = listaVectorDeOrdinal;
	}

	public int getCantTotalVectores() {
		return cantTotalVectores;
	}

	public void setCantTotalVectores(int cantTotalVectores) {
		this.cantTotalVectores = cantTotalVectores;
	}

	/**
	 * Devuelve el próximo vector y si se terminó la enumeración devuelve null
	 *
	 * @return vectorDevuelto array de int de dimensión cantDógitos con los valores
	 *         del enumerador, o null si terminó la enumeración.
	 */
	public int[] devuelveVector() {
		if (terminado != true) {
			int[] vectorDevuelto = new int[cantDigitos];
			vectorDevuelto = proximoADevolver.clone();
			cursor = cantDigitos - 1;

			while (proximoADevolver[cursor] == cotasSuperiores[cursor]) {
				proximoADevolver[cursor] = cotasInferiores[cursor];
				if (cursor == 0) {
					terminado = true;
					return vectorDevuelto;
				}
				cursor = cursor - 1;
			}
			proximoADevolver[cursor]++;

			return vectorDevuelto;
		} else {
			return null;
		}
	}

	/**
	 * Crea la tabla que para un vector devuelve el ordinal en la enumeración
	 * empezando en 0
	 */
	public void creaTablaYListaOrdinales() {
		Integer ordinal = 0;
		listaVectorDeOrdinal = new ArrayList<int[]>();
		while (terminado != true) {
			int[] vector = this.devuelveVector();
			listaVectorDeOrdinal.add(vector);
			ordinal++;
		}
		inicializaEnum();
	}

	/**
	 * Inicializa el enumerador al primer vector posible
	 */
	public void inicializaEnum() {
		if (cantDigitos > 0) {
			proximoADevolver = cotasInferiores.clone();
			terminado = false;
		} else {
			terminado = true;
		}
	}

	/**
	 * Devuelve el vector que aparece en el orden ordinal (empezando en 0) Requiere
	 * que la tablaOrdinal haya sido creada
	 * 
	 * @param ordinal
	 * @return
	 */
	public int[] devuelveVectorDeOrdinal(int ordinal) {
		int[] vector = listaVectorDeOrdinal.get(ordinal);
		return vector;
	}

	/**
	 * Devuelve el ordinal empezando en cero, de un vector dado No modifica el
	 * vector de entrada
	 * 
	 * @param vector
	 * @return
	 */
	public int devuelveOrdinalDeVector(int[] vector) {
		int ord = 0;
		for (int j = 0; j < cantDigitos; j++) {
			ord = ord + cantCasosAcum[j] * (vector[j] - cotasInferiores[j]);
		}
		return ord;
	}

	public static void main(String[] args) {
		int[] vector;
		int[] cotasInf = { 0, 0, 1 };
		int[] cotasSup = { 3, 1, 4 };
		EnumeradorLexicografico enumerador = new EnumeradorLexicografico(3, cotasInf, cotasSup);
		enumerador.creaTablaYListaOrdinales();
		int ordinal = 0;
		do {
			vector = enumerador.devuelveVector();
			if (vector != null) {
				System.out.print(ordinal + "\t");
				System.out.print(vector[0]);
				System.out.print("\t");
				System.out.print(vector[1]);
				System.out.print("\t");
				System.out.print(vector[2]);
				System.out.print("\n");
			}
			ordinal++;
		} while (vector != null);
		System.out.println("Devuelve el vector de ordinal 7");
		System.out.print(enumerador.devuelveVectorDeOrdinal(7)[0] + "\t");
		System.out.print(enumerador.devuelveVectorDeOrdinal(7)[1] + "\t");
		System.out.print(enumerador.devuelveVectorDeOrdinal(7)[2]);
		System.out.print("\n");
		System.out.println("Devuelve el ordinal del vector 2,  1 , 2 ");
		int[] vector2 = { 2, 1, 2 };
		System.out.println(enumerador.devuelveOrdinalDeVector(vector2));

	}

	public ArrayList<int[]> dameKEstadosAleatorios(int k) {
		if (k > listaVectorDeOrdinal.size()) {
			return listaVectorDeOrdinal;
		}
		Random random = new Random();
		return (ArrayList<int[]>) IntStream.generate(() -> random.nextInt(listaVectorDeOrdinal.size())).distinct()
				.limit(k).mapToObj(listaVectorDeOrdinal::get).collect(Collectors.toList());
	}

}
