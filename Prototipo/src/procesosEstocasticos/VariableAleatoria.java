/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableAleatoria is part of MOP.
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

package procesosEstocasticos;

import utilitarios.Constantes;

/**
 * Clase que representa una variable aleatoria
 * @author ut602614
 *
 */

public class VariableAleatoria {
	private String nombre;
	private boolean muestreada;				//puede ser muestreada (tipo 1) no muestreada (tipo 0)
	private ProcesoEstocastico pe;
//	private ProcesoEstocastico peOptim;
	
	// se usa en la simulación y en la optimización si el PE de la VA no es muestreado
	private Double valor;
	
	// se usa en la simulación y en la optimización si el PE de la VA es muestreado
	private double[] ultimoMuestreo;    
	
	/**
	 * Se usa sólo en la optimización para las VA de PE que tienen estado en la optimización.
	 * Ultimos valores de la variable aleatoria muestreada
	 * primer índice: recorre sorteos en la optimización
	 * segundo índice: recorre intervalos de muestreo.
	 */
	private double[][] ultimoMuestreoOptim;
	
	
	public VariableAleatoria(String nombre, boolean muestreada, ProcesoEstocastico pe,
			Double valor) {
		super();
		this.nombre = nombre;
		this.pe = pe;
		this.muestreada = muestreada;
		this.valor = valor;		
	}
	public VariableAleatoria(VariableAleatoriaEntera v) {
		super();
		this.nombre = v.getNombre();
		this.pe = v.getPe();
		this.muestreada = v.isMuestreada();
		this.valor = (double)v.getValor();
	}

	public VariableAleatoria() {
		super();
	}

	public Double getValor() {
		return valor;
	}
	
	public void setValor(Double valor) {
		this.valor = valor; 
	}
	
	public String getNombre() {
		return nombre; 
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public ProcesoEstocastico getPe() {
		return pe;
	}
	public void setPe(ProcesoEstocastico pe) {
		this.pe = pe;
	}

	public boolean isMuestreada() {
		return muestreada;
	}

	public void setMuestreada(boolean muestreada) {
		this.muestreada = muestreada;
	}

	public double[] getUltimoMuestreo() {
		if (muestreada)
			return ultimoMuestreo;
		return null;
	}

	public void setUltimoMuestreo(double[] ultimoMuestreo) {
		if (muestreada)	this.ultimoMuestreo = ultimoMuestreo;
	}

	public void crearDatosMuestreados(int length) {
		if (muestreada)	ultimoMuestreo = new double[length];
		
	}
	


	public void guardarDatoMuestreado(int i) {
		if (muestreada) {
			ultimoMuestreo[i] = valor;
		}
		
	}
	
	
	public void guardarDatoMuestreadoOptim(int isort, int imuestreo) {
		
		if (this.pe instanceof ProcesoBootstrapDiscreto) {
			if (valor < 0) { 
				if (Constantes.NIVEL_CONSOLA > 1) {
					System.out.println("VALOR NEGATIVO EN PROCESO " + this.pe.getNombre());
				//System.exit(1);
				}
			}
			
		}
		ultimoMuestreoOptim[isort][imuestreo] = valor;

	}	
	
	

	public double[][] getUltimoMuestreoOptim() {
		return ultimoMuestreoOptim;
	}

	public void setUltimoMuestreoOptim(double[][] ultimoMuestreoOptim) {
		this.ultimoMuestreoOptim = ultimoMuestreoOptim;
	}

	public void crearDatosMuestreadosOptim(int cantSorteos, int cantIntMuestreo) {
		ultimoMuestreoOptim = new double[cantSorteos][cantIntMuestreo];
		
	}

//	public ProcesoEstocastico getPeOptim() {
//		return peOptim;
//	}
//
//	public void setPeOptim(ProcesoEstocastico peOptim) {
//		this.peOptim = peOptim;
//	}
	
	
	
}
