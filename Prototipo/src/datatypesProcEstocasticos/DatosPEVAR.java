/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosPEVAR is part of MOP.
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


import java.util.Hashtable;



public class DatosPEVAR {
	
	
	private DatosGeneralesPE datGen; 
	private boolean muestreado;
	private String formaEstimacion;   // SOLO_VAR o VAR_Y_PVA
	private int NA; // cantidad de rezagos de las variables considerados en la parte AR
	private int NM; // cantidad de rezagos de las innovaciones, adem�s de las innovaciones contempor�neas
	private int NVA; // cantidad de variables aleatorias del proceso.
	private int cantInno; // cantidad de innovaciones aleatorias, normalmente igual a cantVar
	

	private int NEx; // Cantidad de variables exógenas 
	

	/**
	 * Lista de matrices de coeficientes autoregresivos
	 * primer �ndice rezagos i=1,...NA de las variables empezando en 1
	 * segundo �ndice ecuaci�n, o fila de la matriz Ai
	 * tercer �ndice variable, o columna de la matriz Ai
	 */
	private double[][][] A; 
	
	
	/**
	 * Lista de matrices de coeficientes de medias m�viles
	 * primer �ndice rezagos de los residuos empezando en 0, hasta NB
	 * segundo �ndice ecuaci�n
	 * tercer �ndice innovaci�n aleatoria normal de media nula y desv�o 1, independiente de las otras innovaciones
	 */
	private double[][][] M;
	
	
	
	/**
	 * Matriz de vectores propios resultante de la descomposici�n de la matriz de 
	 * covarianzas de los residuos
	 */
	private double[][] B;
	
	/**
	 * Matriz diagonal con los valores propios resultante de la descomposici�n 
	 */
	private double[][] D; 
	
	/**
	 * Lista de vectores con coeficientes de efectos de variables ex�genas
	 * primer �ndice variable ex�gena, segundo �ndice variable afectada (1 a NE)
	 */
	private double[][] X;
	
	/**
	 * Valores iniciales de las variables de estado asociadas a cada una de las variables aleatorias normalizadas rezagadas, 
	 * en la f�rmula de arriba las variables y(t-1) ... y(t-NA)
	 * Si la respectiva variable aleatoria no es una variable de estado el valor no se considera.
	 * Primer indice rezago, hay NA valores
	 * Segundo �ndice variable aleatoria (fila de las Ai, es decir hay NVA valores)
	 */
	private double[][] varsEstadoVAIniciales;	
	
	/**
	 * Variables de estado asociadas a cada una de las normales sorteadas con las innovaciones rezagadas, 
	 * en la f�rmula de arriba: las variables u(t-1) + ...+ u(t-NM)
	 * Si la respectiva variable aleatoria no es una variable de estado el valor no se considera.
	 * Primer indice rezago, hay NM valores. El get(0) es el rezago 1.
	 * Segundo �ndice innovaci�n normal, hay cantInnovaciones valores.
	 */	
	private double[][] varsEstadoUIniciales;	
	
	private DatosTransformaciones datTrans;
//	private DatosAgregadorLineal agregador;
	private DatosDiscretizacionesVEPE datDis;
	
	/**
	 * Cotas superiores e inferiores de las realizaciones de cada VA del proceso
	 * clave nombre de VA, valor cota inferior o superior
	 */
	private Hashtable<String, Double> cotaInfRealiz;
	private Hashtable<String, Double> cotaSupRealiz;
	
	/**
	 * Matriz para calcular la esperanza condicional de V dado H
     *  ME es la matriz tal que 
	 *  E(V│H )= Σ_V T * (T Σ_V T* )^(-1)  H =: ME H
	 *  Σ_V es la matriz de covarianzas de las VE totales del VAR.
	 *  ^(-1) es el operador invertir la matriz
	 *  T es la matriz de agregación de las VE
	 */
	private double[][] matrizEspCondicional; // matriz para calcular la esperanza condicional de V dado H
	
	
	/**
	 * Matriz para calcular la varianza condicional de V dado H
     *  MV es la matriz tal que 
	 *  Var(V│H) = Σ_V - Σ_V T* [ (T Σ_V T*)^(-1) ] T Σ_V  =: MV
	 *  Σ_V es la matriz de covarianzas muestrales de las VE del VAR completo.
	 *  T es la matriz de agregación de las VE
	 */	
	private double[][] matrizVarCondicional;
	
	/**
	 * Matrices de la descomposición espectral de la matrizVarCondicional MV
	 * dVC matriz diagonal de los valores propios
	 * dVCC matriz diagonal corregida haciendo cero exacto los valores
	 * que están por debajo de utilitarios.CONSTANTES.EPSILON_VAL_PROPIOS
	 * bVC matriz de cambio de base
	 */
	private double[][] dVC;
	private double[][] dVCC;
	private double[][] bVC;

	
	public DatosGeneralesPE getDatGen() {
		return datGen;
	}

	public void setDatGen(DatosGeneralesPE datGen) {
		this.datGen = datGen;
	}

	public String getFormaEstimacion() {
		return formaEstimacion;
	}

	public void setFormaEstimacion(String formaEstimacion) {
		this.formaEstimacion = formaEstimacion;
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

	public int getNEx() {
		return NEx;
	}

	public void setNE(int nEx) {
		NEx = nEx;
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

//	public DatosAgregadorLineal getAgregador() {
//		return agregador;
//	}
//
//	public void setAgregador(DatosAgregadorLineal agregador) {
//		this.agregador = agregador;
//	}

	public DatosDiscretizacionesVEPE getDatDis() {
		return datDis;
	}

	public void setDatDis(DatosDiscretizacionesVEPE datDis) {
		this.datDis = datDis;
	}

	public double[][] getMatrizEspCondicional() {
		return matrizEspCondicional;
	}

	public void setMatrizEspCondicional(double[][] matrizEspCondicional) {
		this.matrizEspCondicional = matrizEspCondicional;
	}

	public double[][] getMatrizVarCondicional() {
		return matrizVarCondicional;
	}

	public void setMatrizVarCondicional(double[][] matrizVarCondicional) {
		this.matrizVarCondicional = matrizVarCondicional;
	}

	public double[][] getdVC() {
		return dVC;
	}

	public void setdVC(double[][] dVC) {
		this.dVC = dVC;
	}

	public double[][] getdVCC() {
		return dVCC;
	}

	public void setdVCC(double[][] dVCC) {
		this.dVCC = dVCC;
	}

	public double[][] getbVC() {
		return bVC;
	}

	public void setbVC(double[][] bVC) {
		this.bVC = bVC;
	}

	public void setNEx(int nEx) {
		NEx = nEx;
	}

	public Hashtable<String, Double> getCotaInfRealiz() {
		return cotaInfRealiz;
	}

	public void setCotaInfRealiz(Hashtable<String, Double> cotaInfRealiz) {
		this.cotaInfRealiz = cotaInfRealiz;
	}

	public Hashtable<String, Double> getCotaSupRealiz() {
		return cotaSupRealiz;
	}

	public void setCotaSupRealiz(Hashtable<String, Double> cotaSupRealiz) {
		this.cotaSupRealiz = cotaSupRealiz;
	} 
	
	
	
	
}
