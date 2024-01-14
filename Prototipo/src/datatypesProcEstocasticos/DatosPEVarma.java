/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEVarma is part of MOP.
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

package datatypesProcEstocasticos;


public class DatosPEVarma {
	
	
	private DatosGeneralesPE datGen; 
	private boolean muestreado;

	
	private int NA; // cantidad de rezagos de las variables considerados en la parte AR
	private int NM; // cantidad de rezagos de las innovaciones, además de las innovaciones contemporáneas
	private int NVA; // cantidad de variables aleatorias del proceso.
	private int cantInno; // cantidad de innovaciones aleatorias, normalmente igual a cantVar
	

	private int NE; // Cantidad de variables exógenas
	

	/**
	 * Lista de matrices de coeficientes autoregresivos
	 * primer índice rezagos i=1,...NA de las variables empezando en 1
	 * segundo índice ecuación, o fila de la matriz Ai
	 * tercer índice variable, o columna de la matriz Ai
	 */
	private double[][][] A; 
	
	
	/**
	 * Lista de matrices de coeficientes de medias móviles
	 * primer índice rezagos de los residuos empezando en 0, hasta NB
	 * segundo índice ecuación
	 * tercer índice innovación aleatoria normal de media nula y desvío 1, independiente de las otras innovaciones
	 */
	private double[][][] M;
	
	
	
	/**
	 * Matriz de vectores propios resultante de la descomposición de la matriz de
	 * covarianzas de los residuos
	 */
	private double[][] B;
	
	/**
	 * Matriz diagonal con los valores propios resultante de la descomposición
	 */
	private double[][] D; 
	
	/**
	 * Lista de vectores con coeficientes de efectos de variables exógenas
	 * primer índice variable exógena, segundo índice variable afectada (1 a NE)
	 */
	private double[][] X;
	
	/**
	 * Valores iniciales de las variables de estado asociadas a cada una de las variables aleatorias normalizadas rezagadas, 
	 * en la fórmula de arriba las variables y(t-1) ... y(t-NA)
	 * Si la respectiva variable aleatoria no es una variable de estado el valor no se considera.
	 * Primer indice rezago, hay NA valores
	 * Segundo índice variable aleatoria (fila de las Ai, es decir hay NVA valores)
	 */
	private double[][] varsEstadoVAIniciales;	
	
	/**
	 * Variables de estado asociadas a cada una de las normales sorteadas con las innovaciones rezagadas, 
	 * en la fórmula de arriba: las variables u(t-1) + ...+ u(t-NM)
	 * Si la respectiva variable aleatoria no es una variable de estado el valor no se considera.
	 * Primer indice rezago, hay NM valores. El get(0) es el rezago 1.
	 * Segundo índice innovación normal, hay cantInnovaciones valores.
	 */	
	private double[][] varsEstadoUIniciales;	
	
	private DatosTransformaciones datTrans;
	private DatosAgregadorLineal agregador;
	private DatosDiscretizacionesVEPE datDis;


	public DatosGeneralesPE getDatGen() {
		return datGen;
	}

	public void setDatGen(DatosGeneralesPE datGen) {
		this.datGen = datGen;
	}
	

	public boolean isMuestreado() {
		return muestreado;
	}

	public void setMuestreado(boolean muestreado) {
		this.muestreado = muestreado;
	}

	public int getNA() {
		return NA;
	}

	public void setNA(int nA) {
		NA = nA;
	}

	public int getNM() {
		return NM;
	}

	public void setNM(int nM) {
		NM = nM;
	}

	public int getNVA() {
		return NVA;
	}

	public void setNVA(int nVA) {
		NVA = nVA;
	}

	public int getCantInno() {
		return cantInno;
	}

	public void setCantInno(int cantInno) {
		this.cantInno = cantInno;
	}

	public int getNE() {
		return NE;
	}

	public void setNE(int nE) {
		NE = nE;
	}

	public double[][][] getA() {
		return A;
	}

	public void setA(double[][][] a) {
		A = a;
	}

	public double[][][] getM() {
		return M;
	}

	public void setM(double[][][] m) {
		M = m;
	}

	public double[][] getX() {
		return X;
	}

	public void setX(double[][] x) {
		X = x;
	}

	
	public double[][] getB() {
		return B;
	}

	public void setB(double[][] b) {
		B = b;
	}

	public double[][] getD() {
		return D;
	}

	public void setD(double[][] d) {
		D = d;
	}

	public double[][] getVarsEstadoVAIniciales() {
		return varsEstadoVAIniciales;
	}

	public void setVarsEstadoVAIniciales(double[][] varsEstadoVAIniciales) {
		this.varsEstadoVAIniciales = varsEstadoVAIniciales;
	}

	public double[][] getVarsEstadoUIniciales() {
		return varsEstadoUIniciales;
	}

	public void setVarsEstadoUIniciales(double[][] varsEstadoUIniciales) {
		this.varsEstadoUIniciales = varsEstadoUIniciales;
	}

	public DatosTransformaciones getDatTrans() {
		return datTrans;
	}

	public void setDatTrans(DatosTransformaciones datTrans) {
		this.datTrans = datTrans;
	}

	public DatosAgregadorLineal getAgregador() {
		return agregador;
	}

	public void setAgregador(DatosAgregadorLineal agregador) {
		this.agregador = agregador;
	}

	public DatosDiscretizacionesVEPE getDatDis() {
		return datDis;
	}

	public void setDatDis(DatosDiscretizacionesVEPE datDis) {
		this.datDis = datDis;
	} 
	
	
	
	
}
