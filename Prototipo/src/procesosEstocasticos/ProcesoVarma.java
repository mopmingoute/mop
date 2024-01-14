/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoVarma is part of MOP.
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

import java.util.ArrayList;
import java.util.Hashtable;

import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEVarma;
import estado.Discretizacion;
import estado.VariableEstado;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocUtilitarios.DistribucionNormal;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.LineaTiempo;
import tiempo.SentidoTiempo;
import utilitarios.Par;

//ATENCI�N: EN AZAR HAY QUE HACER UNA PASADA PARA LINKEAR LOS PE QUE TIENEN VARIABLES EX�GENAS
//CON ESAS VARIABLES Y SUS PROCESOS Y PARA FIJAR LA PRIORIDAD DE THIS.

/**
 * ATENCION: ESTE MÉTODO NO ESTA COMPLETO NI PROBADO
 * 
 * @author ut469262 La clase implementa un proceso VARMA con variables ex�genas
 *         de la forma
 * 
 *         y(t) = A1 y(t-1) + ...+ Ap y(t-p) + X1.x1(t) + ... + XNE.xNE(t) +
 *         u(t) + M1 u(t-1)...MNM u(t-NM)
 * 
 *         y(t) son variables transformadas de las originales, t�picamente
 *         normales est�ndar El m�todo producirRealizacion devuelve las VA
 *         ORIGINALES usando la antitransformaci�n del m�todo de normalizaci�n
 *         utilizado para generar las y(t).
 * 
 *         Las variables ex�genas requeridas SON NORMALES ESTANDAR y se obtienen
 *         invocando otros procesos estoc�sticos que las producen
 * 
 *         Ai, i=1,...,NA son matrices [NVA x NVA] Xi, i=1,...,NE son vectores
 *         [NVA x 1] Mi, i=1,...,NM son matrices [NVA x NVA]
 *
 * 
 * 
 *         xe(t) escalares, para e=1,...,NE son las variables ex�genas
 *         u(t),...,u(t-NM) cada uno [NM x cantInnovaciones] son vectores de
 *         residuos normales correlacionados.
 * 
 *         u(t) = B (D)**(1/2) e(t)
 * 
 *         B y D son las matrices que resultan de la descomposici�n espectral de
 *         la matriz de covarianza de los residuos u(t)
 * 
 *         e(t) son innovaciones N(0 vector, I)
 * 
 * 
 */

public class ProcesoVarma extends ProcesoEstocastico implements AportanteEstado {

	private int NA; // cantidad de rezagos de las variables considerados en la parte AR
	private int NM; // cantidad de rezagos de las innovaciones, adem�s de las innovaciones
					// contempor�neas

	private int NVA; // cantidad de variables aleatorias del proceso.

	/**
	 * Variables de estado asociadas a cada una de las variables aleatorias
	 * normalizadas rezagadas, en la f�rmula de arriba las variables y(t-1) ...
	 * y(t-NA) Si en una posici�n hay null, la respectiva variable aleatoria no es
	 * una variable de estado. Primer indice rezago, hay NA valores Segundo �ndice
	 * variable aleatoria (fila de las Ai, es decir hay NVA valores)
	 */
	private ArrayList<ArrayList<VariableEstado>> varsEstadoVA;

	/**
	 * Variables de estado asociadas a cada una de las normales sorteadas con las
	 * innovaciones rezagadas, en la f�rmula de arriba: las variables u(t-1) + ...+
	 * u(t-NM) Si en una posici�n hay null, la respectiva normal no es una variable
	 * de estado. Primer indice rezago, hay NB valores. El get(0) es el rezago 1.
	 * Segundo �ndice innovaci�n normal, hay NVA valores.
	 */
	private ArrayList<ArrayList<VariableEstado>> varsEstadoU;

	private int cantInno; // cantidad de innovaciones aleatorias, normalmente igual a cantVar

	/**
	 * Cantidad de variables ex�genas
	 * 
	 * Atenci�n, el proceso this al producir realizaciones, har� que los procesos
	 * estoc�sticos de las variables ex�genas tambi�n produzcan realizaciones. Debe
	 * chequearse que si hay dos procesos P1 y P2 con variables ex�genas comunes E,
	 * el proceso P1 no pida realizaciones de los procesos de las variables de E que
	 * pertenezcan al pasado seg�n P2. El chequeo en principio es que el paso de P1
	 * y P2 sea el mismo.
	 */
	private int NE;

	/**
	 * Transformaciones desde la variable original para pasar a las del proceso this
	 * y sus inversas Primer �ndice recorre pasos de tiempo del proceso a lo largo
	 * del a�o. Por ejemplo si el proceso es semanal hay 52. Segundo �ndice recorre
	 * las variables aleatorias del proceso this
	 * 
	 */
	private ArrayList<ArrayList<TransformacionVA>> transformacionesVA;

	/**
	 * Lista de matrices de coeficientes autoregresivos primer �ndice rezagos
	 * i=1,...NA de las variables empezando en 1 segundo �ndice ecuaci�n, o fila de
	 * la matriz Ai tercer �ndice variable, o columna de la matriz Ai
	 */
	private double[][][] A;

	/**
	 * Lista de matrices de coeficientes de medias m�viles primer �ndice rezagos de
	 * los residuos empezando en 0, hasta NM segundo �ndice ecuaci�n tercer �ndice
	 * innovaci�n aleatoria normal de media nula y desv�o 1, independiente de las
	 * otras innovaciones
	 */
	private double[][][] M;

	/**
	 * Matrices para generar los residuos u(t) a partir de e(t)
	 */
//	private double[] B;
//	private double[] D;

	/**
	 * Lista de vectores con coeficientes de efectos de variables ex�genas primer
	 * �ndice variable ex�gena, segundo �ndice variable afectada (1 a NE)
	 */
	private double[][] X;

	private double[] u; // vector de dimensi�n cantInno gen�rico para residuos normales
	private double[] y; // vector de dimensi�n NVAR gen�rico para variables normalizadas rezagadas
	private double[] yt; // vector con la realizaci�n que se est� generando con las nuevas variables
							// normalizadas

	public ProcesoVarma(DatosPEVarma datos) {
		super(datos.getDatGen());

		this.iniciaConstruccion(datos.getDatGen());

		this.setA(datos.getA());
		this.setM(datos.getM());
		this.setNA(datos.getNA());
		this.setNM(datos.getNM());
		this.setNE(datos.getNE());
		this.completaConstruccion();

		// Crea la discretizaci�n de las VE y carga otros datos de las VE

		for (VariableEstado ve : this.getVarsEstado()) {
			Discretizacion d = new Discretizacion(datos.getDatDis().datosDiscDeVE(ve.getNombre()));
			Evolucion<Discretizacion> ev = new EvolucionConstante<Discretizacion>(d, null); // el sentido se carga al
																							// inicializar simulac�n u
																							// optimizaci�n
			ve.setEvolDiscretizacion(ev);
			ve.setDiscreta(false);
		}

	}

	@Override
	public void producirRealizacionSinPronostico(long instante) {
		int cantIn = this.getCantidadInnovaciones();

		if (instante < this.getInstanteCorrienteFinal()) {
			System.out.println("En el proceso " + this.getNombre() + "se pidi� realizacion de instante " + instante
					+ "cuando el instanteCorrienteFinal del proceso era posterior" + this.getInstanteCorrienteFinal());
			if (CorridaHandler.getInstance().isParalelo()) {
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(1);
		}
		long instPaso = this.getInstanteCorrienteFinal() + utilitarios.Constantes.EPSILONSALTOTIEMPO;
		if (instante > this.getInstanteCorrienteFinal()) {
			// el instante es posterior al fin del ultimo paso sorteado del proceso this
			// es necesario producir nuevos valores
			while (this.getInstanteCorrienteFinal() < instante) {
				avanzarUnPasoDelPE(instPaso);
			}
		}

		int iv = 0;
		for (VariableAleatoria va : this.getVariablesAleatorias()) {
			va.setValor(yt[iv]);
			iv++;
		}

	}
	
	
	
	@Override
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		LineaTiempo lt = this.getAzar().getCorrida().getLineaTiempo();
		int ordBloque = lt.getBloqueActual();
		int durIntMuestreo = lt.getBloques().get(ordBloque).getIntervaloMuestreo();

		// Se cargan en los valores de las VE normalizadas para el rezago 1
		// los valores transformados del promedio con el pronóstico que está cargado
		// en la realización en el atributo valor
		int pasoDelAnio = this.pasoDelAnio(instante);		
		for(int iv=0; iv<NVA; iv++){
			VariableAleatoria va = this.getVariablesAleatorias().get(iv);
			TransformacionVA tr = this.dameTrans(va.getNombre(), pasoDelAnio);		
			varsEstadoVA.get(0).get(iv).setEstado(tr.transformar(va.getValor()));	
		}		
	}
	

	/**
	 * Sortea las innovaciones, produce los nuevos valores de las variables
	 * aleatorias del proceso this y modifica los valores de las variables de estado
	 * y, avanzando un paso de tiempo del proceso this.
	 */
	public void avanzarUnPasoDelPE(long instPaso) {

		yt = new double[NVA];

		// Se acumulan los aportes de residuos rezagados
		int in;
		int r;
		int iv;
		int ive;
		for (r = 1; r < NM; r++) {
			for (in = 0; in < cantInno; in++) {
				u[in] = varsEstadoU.get(r - 1).get(in).getEstado();
			}
			matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(M[r], u));
		}

		// Se acumula la contribuci�n de los valores contempor�neos de u
		in = 0;
		for (GeneradorDistUniforme ga : this.getGeneradoresAleatorios()) {
			double unif = ga.generarValor();
			double normal = DistribucionNormal.inversa(unif);
			u[in] = normal;
			in++;
		}
		matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(M[0], u));

		// Se acumula la contribuci�n de los valores rezagados de y
		for (r = 1; r < NA; NA++) {
			for (iv = 0; iv < NVA; iv++) {
				y[iv] = varsEstadoVA.get(r - 1).get(iv).getEstado();
			}
			matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(A[r - 1], y));
		}

		// Se produce la realizaci�n de las variables ex�genas y se acumula su
		// contribuci�n

		for (ProcesoEstocastico pe : this.getProcesosVarsExogenas()) {
			pe.producirRealizacion(instPaso);
		}
		for (ive = 0; ive < NE; ive++) {
			double valEx = this.getVarsExogenas().get(ive).getValor();
			matrices.Oper.sumaUnVector(yt, matrices.Oper.prodVecEscalar(X[ive], valEx));
		}

		// Se rezagan los valores de las variables de estado normalizadas varsEstadoVA
		for (r = 1; r < NA - 1; r++) {
			for (iv = 0; iv < NVA; iv++) {
				varsEstadoVA.get(r).get(iv).actualizarEstado(varsEstadoVA.get(r - 1).get(iv).getEstado());
			}
		}
		// Se rezagan los valores de las variables de estado normalizadas varEstadoU
		for (r = 1; r < NM - 1; r++) {
			for (iv = 0; iv < cantInno; iv++) {
				varsEstadoU.get(r).get(iv).actualizarEstado(varsEstadoU.get(r - 1).get(iv).getEstado());
			}
		}

		this.setInstanteCorrienteInicial(this.getInstanteCorrienteInicial() + this.getDurPaso());
		this.setInstanteCorrienteFinal(this.getInstanteCorrienteInicial() + this.getDurPaso());

	}

//
//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(
//			ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		kk
//		return null;
//	}

	@Override
	public boolean tieneVEOptim() {
		if (this.getCantVE() > 0 & this.isUsaVarsEstadoEnOptim())
			return true;
		return false;
	}

	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub

	}


	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// TODO Auto-generated method stub

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

	public ArrayList<ArrayList<VariableEstado>> getVarsEstadoVA() {
		return varsEstadoVA;
	}

	public void setVarsEstadoVA(ArrayList<ArrayList<VariableEstado>> varsEstadoVA) {
		this.varsEstadoVA = varsEstadoVA;
	}

	public ArrayList<ArrayList<VariableEstado>> getVarsEstadoU() {
		return varsEstadoU;
	}

	public void setVarsEstadoU(ArrayList<ArrayList<VariableEstado>> varsEstadoU) {
		this.varsEstadoU = varsEstadoU;
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

	public ArrayList<ArrayList<TransformacionVA>> getTransformacionesVA() {
		return transformacionesVA;
	}

	public void setTransformacionesVA(ArrayList<ArrayList<TransformacionVA>> transformacionesVA) {
		this.transformacionesVA = transformacionesVA;
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

	public double[] getU() {
		return u;
	}

	public void setU(double[] u) {
		this.u = u;
	}

	public double[] getY() {
		return y;
	}

	public void setY(double[] y) {
		this.y = y;
	}

	public double[] getYt() {
		return yt;
	}

	public void setYt(double[] yt) {
		this.yt = yt;
	}

//	@Override
//	public void actualizarVarsEstadoSimulacion() {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//
//
//	@Override
//	public void actualizarVarsEstadoOptimizacion() {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void contribuirAS0fint() {
		// TODO Auto-generated method stub

	}

	@Override
	public void contribuirAS0fintOptim() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoSimulacion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<VariableEstado> aportarEstadoOptimizacion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cargarValVEOptimizacion() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarValRecursoVESimulacion() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub

	}

	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}


}
