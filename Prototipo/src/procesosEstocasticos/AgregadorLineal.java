/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AgregadorLineal is part of MOP.
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

import datatypesProcEstocasticos.DatosAgregadorLineal;

/**
 * Premultiplica el vector de estado de la simulaci�n por una matrizAgregacion
 * @author ut469262
 *
 */
public class AgregadorLineal extends AgregadorDeEstados {
	
	/**
	 * Tiene tantas filas como VE de optimizaci�n se quieran obtener y tantas columnas 
	 * la cantidad de VE  m�s variables ex�genas se tienen en el proceso de simulaci�n	
	 */
	private double[][] matrizAgregacion;  
	
	public AgregadorLineal(DatosAgregadorLineal dat){
		matrizAgregacion = dat.getMatrizAgregacion();
		this.nombrePEOptim = dat.getNombrePEOptim();
		this.nombrePESimul = dat.getNombrePESimul();
		this.nombresVESimul = dat.getNombresVESimul();

	}
	
	
	
	/**
	 * post multiplica la matrizAgregacion por el vector valoresVESimul.
	 */	
	@Override
	public double[] devuelveEstadoOptim(double[] valoresVESimul, double[] valoresVExo) {
		double[] result;
		if(valoresVExo!=null){
			double[] valores = matrices.Oper.yuxtaVecs(valoresVESimul, valoresVExo);
			result = matrices.Oper.matPorVector(matrizAgregacion, valores);		
		}else{
			double[] valores = valoresVESimul;
			result = matrices.Oper.matPorVector(matrizAgregacion, valores);	
		}
		
		return result;
	}



	public double[][] getMatrizAgregacion() {
		return matrizAgregacion;
	}



	public void setMatrizAgregacion(double[][] matrizAgregacion) {
		this.matrizAgregacion = matrizAgregacion;
	}
	
	

}
