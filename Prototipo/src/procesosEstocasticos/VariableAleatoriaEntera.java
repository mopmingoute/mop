/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableAleatoriaEntera is part of MOP.
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

public class VariableAleatoriaEntera {
	
		private String nombre;
		private boolean muestreada;				//puede ser muestreada (tipo 1) no muestreada (tipo 0)
		private ProcesoEstocastico pe;
		private int valor;
		double [] ultimoMuestreo;
		
		
		
	
		public VariableAleatoriaEntera(String nombre, boolean muestreada, ProcesoEstocastico pe, int valor) {
			super();  
			this.nombre = nombre;
			this.pe = pe; 
			this.muestreada = muestreada;
			this.valor = valor;
		}


		public VariableAleatoriaEntera() {
			super(); 
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

		public int getValor() {
			return valor;
		}

		public void setValor(int valor) {
			this.valor = valor;
		}

		public void guardarDatoMuestreado(int i) {
			if (muestreada) {
				ultimoMuestreo[i] = valor;
			}
			
		}
	

	}


