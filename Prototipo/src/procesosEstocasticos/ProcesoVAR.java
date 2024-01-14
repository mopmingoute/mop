/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoVAR is part of MOP.
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
import java.util.Random;
import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProcEstocasticos.DatosPEVAR;
import estado.Discretizacion;
import estado.VariableEstado;
import futuro.AFIncrementos;
import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;
import matrices.Oper;
import pizarron.PizarronRedis;
import procEstocUtils.DistribucionNormal;
import tiempo.BloqueTiempo;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.LineaTiempo;
import utilitarios.DirectoriosYArchivos;


//ATENCION: EN AZAR HAY QUE HACER UNA PASADA PARA LINKEAR LOS PE QUE TIENEN VARIABLES EXOGENAS
//CON ESAS VARIABLES Y SUS PROCESOS Y PARA FIJAR LA PRIORIDAD DE THIS.

/**
 * 
 * @author ut469262
 * La clase implementa un proceso VAR con variables exógenas de la forma
 * 
 * y(t) = A1 y(t-1) + ...+ Ap y(t-p) + X1.x1(t) + ... + XNEX.xNEX(t) + u(t) 
 * 
 * y(t) son variables transformadas de las originales, típicamente normales estándar
 * El método producirRealizacion devuelve las VA ORIGINALES usando la antitransformaci�n
 * del método de normalización utilizado para generar las y(t).
 * 
 * Las variables exógenas requeridas SON NORMALES ESTANDAR y se obtienen invocando 
 * otros procesos estoc�sticos que las producen.
 * 
 * ATENCION: VA A FUNCIONAR MAL SI LOS PROCESOS EXOGENOS REQUIEREN QUE SE LOS AVANCE
 * MAS DE UN PASO 
 * 
 * 
 * Ai, i=1,...,NA son matrices [NVA x NVA]
 * Xi, i=1,...,NE son vectores [NVA x 1]
 *
 * 
 * 
 * xe(t) escalares, para e=1,...,NEX son las variables exógenas
 * u(t),...,u(t-NM) cada uno [NM x cantInnovaciones] son vectores de residuos normales correlacionados.
 * 
 * u(t) = B (D)**(1/2) e(t)
 * 
 * B y D son las matrices que resultan de la descomposición espectral de la matriz de covarianza
 * de los residuos u(t)
 * 
 * e(t) son innovaciones N(0 vector, I)
 * 
 * 
 */

//ATENCION QUE EN LA OPCION SOLO_VAR EN OPTIMIZACION, EL PROCESO DEBE TENER MAS
//INNOVACIONES, PARA SORTEAR EL ESTADO COMPLETO A PARTIR DEL ESTADO AGREGADO.
//OTRO ASUNTO ES SI DEBE TENER TANTAS INNOVACIONES COMO VARIABLES DE ESTADO DEL VAR
//O MENOS, PORQUE SE DETECTAN LOS VALORES PROPIOS CERO EN LA GENERACION DE ESTADOS COMPLETOS

public class ProcesoVAR extends ProcesoEstocastico implements AportanteEstado{
	
	private String formaEstimacion;   // SOLO_VAR o VAR_Y_PVA
	private int nA; // cantidad de rezagos de las variables considerados en la parte AR
	private int nM; // cantidad de rezagos de las innovaciones, además de las innovaciones contemporáneas

	private int nVA; // cantidad de variables aleatorias del proceso.
	
	/**
	 * SE USAN SOLO EN SIMULACIÓN
	 * Estas colecciones reordenan las variables de estado de la clase padre
	 * asociadas a cada una de las variables aleatorias vectoriales normalizadas rezagadas. 
	 * En la fórmula de arriba son las variables vectoriales y(t-1) ... y(t-NA)
	 * Si en una posición hay null, la respectiva variable aleatoria no es una variable de estado.
	 * Primer indice rezago, hay NA valores
	 * Segundo índice variable aleatoria (fila de las Ai, es decir hay NVA valores)
	 * 
	 * Estas colecciones se cargan al inicio de cada paso con las variables de estado 
	 * respectivas. Son una forma de referenciar las VE de manera más sencilla.
	 */
	private ArrayList<ArrayList<VariableEstado>> varsEstadoVA;
	
	
	
	/**
	 * SE USAN SOLO EN OPTIMIZACION
	 * Estas variables se usan en producirRealizacionPEEstadoOptim cumpliendo
	 * el mismo papel en los cálculos que los valores de varsEstadoVA en la simulación
	 * Primer indice rezago, hay NA valores
	 * Segundo índice variable aleatoria (fila de las Ai, es decir hay NVA valores)
	 */
	private ArrayList<ArrayList<Double>> espejoVarsEstadoVA;
	
	private int cantIn; // cantidad de innovaciones aleatorias
	
	/**
	 * Cantidad de variables exógenas 
	 * 
	 * Atención, el proceso this al producir realizaciones, hará que los procesos estocásticos de las
	 * variables exógenas también produzcan realizaciones. Debe chequearse que si hay dos
	 * procesos P1 y P2 con variables exógenas comunes E, el proceso P1 no pida realizaciones de los procesos
	 * de las variables de E que pertenezcan al pasado según P2. El chequeo en principio es que el paso de P1 y P2 sea
	 * el mismo.
	 */
	private int nEx; 
	
	/**
	 * Lista de matrices de coeficientes autoregresivos
	 * primer índice rezagos i=1,...NA de las variables empezando en 1
	 * segundo  índice ecuación, o fila de la matriz Ai
	 * tercer  índice variable, o columna de la matriz Ai
	 */
	private double[][][] A;
	
	
	/**
	 * Matrices para generar los residuos u(t) a partir de e(t)
	 */
	private double[][] B;
	private double[][] D;
	private double[][] DRaiz;   // D^(1/2)
	
	/**
	 * Lista de vectores con coeficientes de efectos de variables exógenas
	 * primer índice variable exógena, segundo �ndice variable afectada (1 a NE)
	 */
	private double[][] X; 
	
	private double[] u; // vector de dimensión cantInno genérico para residuos normales
	private double[] y; // vector de dimensión NVAR genérico para variables normalizadas rezagadas
	private double[] yt; // vector con la realización que se está generando con las nuevas variables normalizadas

	/**
	 * Matrices para sortear estados completos V del VAR, dado un valor del estado agregado 
	 *
	 * ME para esperanza condicional dado el estado
	 * E(V│H )= Σ_V T * (T Σ_V T* )^(-1)  H = ME H
	 */
	private double[][] matrizEspCondicional;	
	/**
	 * MV para varianza condicional dado el estado
	 * Var(V│H) = Σ_V - Σ_V T* [ (T Σ_V T*)^(-1) ] T Σ_V  =: MV
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
	
	
	/**
	 * En el método producirRealizacionPEOptim invocado al inicio de 
	 * la optimización de un estado desde OptimizadorEstado.optimizarEstado, 
	 * se carga en este atributo para cada sorteo de Montecarlo,
	 * Los estados finales agregados que son las VE de la optimización.
	 * Esto evita recalcularlos en contribuirAS0fintOptim que se invoca después en cada sorteo.
	 * Primer indice sorteo
	 * Segundo indice variable de estado agregado
	 */
	private double[][] estadosS0fintOptimSorteados;
	
	/**
	 * Cotas superiores e inferiores de las realizaciones de cada VA del proceso
	 * clave nombre de VA, valor cota inferior o superior
	 */
	private Hashtable<String, Double> cotaInfRealiz;
	private Hashtable<String, Double> cotaSupRealiz;
	
	
	
	public ProcesoVAR(DatosPEVAR datos) {
		super(datos.getDatGen());
		this.setFormaEstimacion(datos.getFormaEstimacion());
		this.setA(datos.getA());
		this.setnVA(datos.getNVA());
//		this.setM(datos.getM());
		this.setnA(datos.getNA());
		this.setnM(datos.getNM());
		this.setNEx(datos.getNEx());
		this.setB(datos.getB());
		this.setD(datos.getD());
		DRaiz = new double[nVA][nVA];
		for(int iv=0; iv<nVA; iv++){
			DRaiz[iv][iv] = Math.sqrt(D[iv][iv]);
		}
		
		this.setMatrizEspCondicional(datos.getMatrizEspCondicional());
		this.setMatrizVarCondicional(datos.getMatrizVarCondicional());
		this.setdVC(datos.getdVC());
		this.setdVCC(datos.getdVCC());
		this.setbVC(datos.getbVC());
		this.setCotaInfRealiz(datos.getCotaInfRealiz());
		this.setCotaSupRealiz(datos.getCotaSupRealiz());
		this.completaConstruccion();
		this.varsEstadoVA = new ArrayList<ArrayList<VariableEstado>>();
		this.espejoVarsEstadoVA = new ArrayList<ArrayList<Double>>();
		nVA = this.getCantVA();
		cantIn = this.getCantVA();
		if(this.isUsoOptimizacion()){
			estadosS0fintOptimSorteados = new double[this.getCantSorteos()][this.getCantVE()];
			for(int ir=1; ir<=nA; ir++){
				ArrayList<Double> auxD = new ArrayList<Double>();
				for(int iva=0; iva<nVA; iva++){
					auxD.add(0.0);
				}
				espejoVarsEstadoVA.add(auxD);
			}
		}else{
			for(int ir=1; ir<=nA; ir++){
				ArrayList<VariableEstado> auxVE = new ArrayList<VariableEstado>();
				for(int iva=0; iva<nVA; iva++){
					auxVE.add(this.getVarsEstado().get((ir-1)*nVA+iva));
				}
				varsEstadoVA.add(auxVE);
			}			
		}

		if(this.isUsoOptimizacion()){
			if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.SOLO_VAR)){
				/**
				 * ATENCIÓN
				 * 
				 * LAS INNOVACIONES DEBEN SORTEAR EL ESTADO COMPLETO DADO EL ESTADO AGREGADO H
				 * Las primeras nVA*NA innovaciones producen el estado completo dado el estado agregado
				 * Las ultimas nVA innovaciones sortean los residuos aleatorios U del VAR
				 * 
				 */			
				cantIn = nVA + nVA*nA;
			}else if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
				// TODO
			}
		}
		this.setCantidadInnovaciones(cantIn);
		// Crea la discretización de las VE y carga otros datos de las VE
		
		if(this.isUsoOptimizacion()){
			for(VariableEstado ve: this.getVarsEstado()){
				Discretizacion d = new Discretizacion(datos.getDatDis().datosDiscDeVE(ve.getNombre()));
				Evolucion<Discretizacion> ev = new EvolucionConstante<Discretizacion>(d, null); // el sentido se carga al inicializar simulción u optimización
				ve.setEvolDiscretizacion(ev);
				ve.setDiscreta(false);
				d.setVarAsoc(ve);
			}
		}
		
		if(datos.getDatGen().getDatAgregadorEstados()!=null){
			AgregadorLineal ag = new AgregadorLineal(datos.getDatGen().getDatAgregadorEstados());
			this.setAgregadorEstados(ag);
		}
		
		// TODO: ASEGURARSE QUE HAGA ESTA VERIFICACIÓN
//		// Verifica que intervalo de muestreo >= paso del proceso
//		LineaTiempo lt = this.getAzar().getCorrida().getLineaTiempo();
//		for(BloqueTiempo b: lt.getBloques()) {
//		int durIntMuestreo = b.getIntervaloMuestreo();
//			if(this.getDurPaso()<durIntMuestreo) {
//				System.out.println("SE EMPLEA EL ProcesoVAR " + this.getNombre() 
//				+ "QUE TIENE DURACION DEL PASO MENOR QUE EL INTERVALO DE MUESTREO - NO ES ADMISIBLE");
//				System.exit(1);
//			}
//		}
	
	}

	
	// TODO METODO PARA PROBAR TRANSFORMACIONES INVERSAS
	public void probarTransformaciones(){
		String dirArch = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/transInversas.xlt";
		DirectoriosYArchivos.siExisteElimina(dirArch);
		Random r = new Random();
		int cantSorteos = 10000;
		int cantPasos = utilitarios.Constantes.CANTMAXPASOS.get(this.getNombrePaso());
		for(int ip=0; ip<cantPasos; ip++){
			System.out.println("sortea transf paso " + ip);
			double suma = 0;
			String nombreVA = this.getNombresVarsAleatorias().get(0);
			TransformacionVA tr = this.getTransformaciones().dameTrans(nombreVA, ip);
			for(int is=0; is<cantSorteos; is++){
				suma += tr.inversa(r.nextGaussian());
			}
			double media = suma/cantSorteos;
			String texto = "Paso " + ip + " media = " + media; 
			DirectoriosYArchivos.agregaTexto(dirArch, texto);
		}
		System.out.println("TERMINA SORTEOS TRANSFORMACIONES");
		String nombreVA = this.getNombresVarsAleatorias().get(0);
		TransformacionVA tr = this.getTransformaciones().dameTrans(nombreVA, 0);
		double val = 2000.0;
		double valtr = tr.transformar(val);
		double inv = tr.inversa(valtr);
		System.out.println();
		
	}
	
	/**
	 * Se normaliza el estado inicial leído de los datos, en su valor normalizado que
	 * es el que se emplea internamente en this.
	 */	
	@Override	
	public double devuelveEstadoInicial(VariableEstado ve, double estadoInicial, int pasoPEInicial) {
		String nombreVE = ve.getNombre();
		String nombreOriginal = this.nombreSerieOriginal(nombreVE);
		int lag = this.lagAPartirDeNombre(nombreVE);
		int pasoTR = 0;
		if(pasoPEInicial-lag >= 0) {
			pasoTR = pasoPEInicial - lag;
		}else {
			pasoTR = this.getCantPasosAnio() +  pasoPEInicial - lag;
		}
		TransformacionVA tr = this.getTransformaciones().dameTrans(nombreOriginal, pasoTR);
		// el paso del año que hay que usar es el paso lag pasos anterior al paso inicial de la simulación
		return tr.transformar(estadoInicial);
	}

	@Override
	public void producirRealizacionSinPronostico(long instante) {
				
		int pasoDelAnio = this.pasoDelAnio(instante);

		if (instante > this.getInstanteCorrienteFinal()) {
			// el instante es posterior al fin del ultimo paso sorteado del proceso this
			// es necesario producir nuevos valores
			long instPaso = this.getInstanteCorrienteFinal() + utilitarios.Constantes.EPSILONSALTOTIEMPO;
			while(this.getInstanteCorrienteFinal()<instante){				
				avanzarUnPasoDelPE(instPaso);
			}
		}		

		
		int iv = 0;
		for(VariableAleatoria va: this.getVariablesAleatorias()){
			TransformacionVA tr = this.dameTrans(va.getNombre(), pasoDelAnio);
			double valor = tr.inversa(yt[iv]);
			double cota = cotaInfRealiz.get(va.getNombre());
			if(valor<cota) valor = cota;
			cota = cotaSupRealiz.get(va.getNombre());
			if(valor>cota) valor = cota;
			va.setValor(valor);
			iv ++;	
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
		for(int iv=0; iv<nVA; iv++){
			VariableAleatoria va = this.getVariablesAleatorias().get(iv);
			TransformacionVA tr = this.dameTrans(va.getNombre(), pasoDelAnio);		
			varsEstadoVA.get(0).get(iv).setEstado(tr.transformar(va.getValor()));	
		}		
	}
	
	
	/**
	 * Sortea las innovaciones, produce los nuevos valores de las variables aleatorias 
	 * del proceso this y modifica los valores de las variables de estado y, avanzando un paso
	 * de tiempo del proceso this.
	 */
	public void avanzarUnPasoDelPE(long instPaso){

		yt = new double[nVA];
		y = new double[nVA];
		int in;
		int r;
		int iv;
		int ive;
				
//		Se acumulan los aportes de residuos rezagados
//      ESTO ES DE LA PARTE MA QUE SE SACÓ  

//		for(r = 1; r<NM; r++){
//			for(in = 0; in<cantInno; in++){
//				u[in] = varsEstadoU.get(r-1).get(in).getEstado();
//			}
//			matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(M[r], u)); 			
//		}
		
		// Se acumula la contribución de los valores contemporáneos de u
		// u(t) = B (D)**(1/2) e(t), donde e es vector de normales N(0,1) independientes
		double[] e = new double[cantIn];
		in = 0;
		for(GeneradorDistUniforme ga: this.getGeneradoresAleatorios()){
			double unif = ga.generarValor();			
			double normal = DistribucionNormal.inversacdf2(unif);			
			e[in] = normal;
			in++;			
		}
		double[] aux = matrices.Oper.matPorVector(DRaiz,e);
		matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(B,aux)); 		

		// Se acumula la contribución de los valores rezagados de y
		for(r=1; r<=nA; r++){
			for(iv = 0; iv<nVA; iv++){
				y[iv] = varsEstadoVA.get(r-1).get(iv).getEstado();
			}			
			matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(A[r-1], y)); 	
		}
		
		// Se produce la realización de las variables exógenas y se acumula su contribución		
		for(ProcesoEstocastico pe: this.getProcesosVarsExogenas()){
			pe.producirRealizacionSinPronostico(instPaso);
		}
		
		for(ive = 0; ive<nEx; ive++){
			double valEx = this.getVarsExogenas().get(ive).getValor();
			matrices.Oper.sumaUnVector(yt, matrices.Oper.prodVecEscalar(X[ive], valEx));			
		}		
		
		// Se rezagan los valores de las variables de estado normalizadas varsEstadoVA para rezagos 2 hasta NA 
		for(r=1; r<nA; r++){
			for(iv=0; iv<nVA; iv++){
				varsEstadoVA.get(r).get(iv).actualizarEstado(varsEstadoVA.get(r-1).get(iv).getEstado());
			}
		}
		// Se cargan los nuevos valores de las VE normalizadas para el rezago 1
		for(iv=0; iv<nVA; iv++){
			varsEstadoVA.get(0).get(iv).actualizarEstado(yt[iv]);
		}
		
//		// Se rezagan los valores de las variables de estado normalizadas varEstadoU 	
//		for(r=1; r<NM-1; r++){
//			for(iv=0; iv<cantInno; iv++){
//				varsEstadoU.get(r).get(iv).actualizarEstado(varsEstadoU.get(r-1).get(iv).getEstado());
//			}
//		}	
				
		this.setInstanteCorrienteInicial(this.getInstanteCorrienteInicial()+this.getDurPaso());
		this.setInstanteCorrienteFinal(this.getInstanteCorrienteFinal()+this.getDurPaso());
		
	}




	@Override
	public boolean tieneVEOptim() {
		if(this.getCantVE()>0 & this.isUsaVarsEstadoEnOptim()) return true;
		return false;
	}
	
	/**
	 * Muestrea y carga los valores de las variables aleatorias de procesos que tienen estado 
	 * en la optimización para ser empleados en 
	 * UNO DE LOS SORTEOS Montecarlo en la optimización, para cada instante de muestreo, o para 
	 * el único valor si la VA no es muestreada.
	 * Lo mismo para los valores de las VA de PE discretos exhaustivos.
	 * 
	 * Las innovaciones ya fueron sorteadas en OptimizadorPaso
	 * 
	 * Este método carga 
	 * - valor para las VA que no son muestreadas
	 * - ultimoMuestreo[] para las VA que son muestreadas
	 * 
	 * @instantesMuestreo vector con los instantes de muestreo
	 * @innovaciones1Sort innovaciones a emplear en el sorteo
	 * 	primer indice recorre índices de innovación,
	 * 	segundo índice recorre intervalos de muestreo 
	 * @isort índice del sorteo que se está generando y se cargará en la VA
	 * 
	 * ATENCION: NO DEBE MODIFICARSE LAS VARIABLES DE ESTADO DEL PROCESO
	 */
	// @Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {		
	
		if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
			System.out.println("Se pidió producirRealizacionPEEstadoOptim y no se programó para VAR_Y_PVA");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(1);
		}
		// Las primeras cantVA*NA innovaciones producen el estado completo dado el estado agregado
		// solo se emplean las del primer intervalo de muestreo. Las otras no se usan
		// Las ultimas cantVA innovaciones de cada intervalo de muestreo sortean los residuos aleatorios U del VAR
		// NA es la cantidad de rezagos del VAR, NVA la cantidad de variables autoregresivas

		// Construye el estado completo del VAR a partir de las variables de estado
		double[][] varsEstado = new double[this.getCantVE()][1];
		int ive=0;
		for(VariableEstado ve: this.getVarsEstado()){
			varsEstado[ive][0] = ve.getEstado();
			ive++;
		}
		double[][] esperanzaCond;
		esperanzaCond = matrices.Oper.prod(matrizEspCondicional,varsEstado);
		
		double[][] inECompNorm = new double[nVA*nA][1]; 
		for(int ii=0; ii<nVA*nA; ii++){
			double uii = procEstocUtils.DistribucionNormal.inversacdf2(innovaciones1Sort[ii][0]);
			inECompNorm[ii][0] = uii*Math.sqrt(dVCC[ii][ii]);			
		}
		double[][] m1 = matrices.Oper.prod(bVC, inECompNorm);
		
		double[][] ecMat = matrices.Oper.suma(m1, esperanzaCond);

		// En espejoVarsEstadoVA
		// Primer indice rezago, hay NA valores
		// Segundo índice variable aleatoria (fila de las Ai, es decir hay NVA valores)
		int inE = 0;
		for(int ir=0; ir<nA; ir++){
			for(int iva=0; iva<nVA; iva++){
				espejoVarsEstadoVA.get(ir).set(iva,ecMat[inE][0]);
				inE++;
			}
		}			
		int cantIm = instantesMuestreo.length;
		long instIniPasoOp = this.getOptimizadorPaso().getInstIniPaso();
		// recorre los instantes de muestreo
		for(int im=0; im<cantIm; im++){
			long instante = instantesMuestreo[im];
			int pasoDelAnio = this.pasoDelAnio(instante);
			// Procesa un instante de muestreo
			if (instante > this.getInstanteCorrienteFinal()) {
				// hay que sortear una realización nueva para un paso del VAR			
				double[] innov1Muestreo = new double[this.getCantidadInnovaciones()];
				// carga todas las innovaciones para el intervalo de muestreo im
				for(int in=0; in<cantIn; in++){
					innov1Muestreo[in] = innovaciones1Sort[in][im];
				}
				while(this.getInstanteCorrienteFinal()<instante){	
					avanzarUnPasoDelPEOptim(instante, innov1Muestreo);
				}				
			}
			
			for(int iv=0; iv<nVA; iv++){
				VariableAleatoria va = this.getVariablesAleatorias().get(iv);
				TransformacionVA tr = this.dameTrans(va.getNombre(), pasoDelAnio);
				double valor = tr.inversa(yt[iv]);
				double cota = cotaInfRealiz.get(va.getNombre());
				if(valor<cota) valor = cota;
				cota = cotaSupRealiz.get(va.getNombre());
				if(valor>cota) valor = cota;
				if(this.isMuestreado()){
					this.getVariablesAleatorias().get(iv).getUltimoMuestreoOptim()[isort][im]=valor;
				}else{
					this.getVariablesAleatorias().get(iv).setValor(valor);
				}
			}	
			// Carga el estado final agregado del sorteo Montecarlo
			
			double[] valoresVESimul = new double[nA*nVA];
			double[] valoresVarExo = new double[nEx];
			ive = 0;		
			for(int ir=0; ir<nA; ir++){
				for(int iva=0; iva<nVA; iva++){
					valoresVESimul[ive]=espejoVarsEstadoVA.get(ir).get(iva);
					ive++;
				}
			}
			for(int iex = 0; iex <nEx; iex++){
				valoresVarExo[iex] = this.getVarsExogenas().get(iex).getValor(); 
				ive++;
			}
			double[] estOptim = this.getAgregadorEstados().devuelveEstadoOptim(valoresVESimul, valoresVarExo);
			estadosS0fintOptimSorteados[isort] = estOptim;  
			
			
//			String dirArchChan = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/observPorPaso.xlt";
//			StringBuilder sb = new StringBuilder();
//			sb.append("VEinicial\t" + this.getVarsEstado().get(0).getEstado() + "\t");
//			sb.append("EsperanzaCond\t");
//			for(int i=0; i<esperanzaCond.length;i++){
//				sb.append(esperanzaCond[i][0]+"\t");
//			}
//			sb.append("ApartamientoAleat\t");
//			for(int i=0; i<m1.length;i++){
//				sb.append(m1[i][0]+"\t");
//			}
//			sb.append("EstadoAmpliado\t");
//			for(int i=0; i<ecMat.length;i++){
//				sb.append(ecMat[i][0]+"\t");
//			}			
//			sb.append("NuevasVA\t");
//			for(int iv=0; iv<nVA; iv++){
//				sb.append(this.getVariablesAleatorias().get(iv).getValor()+"\t");
//			}				
//			sb.append("VEFinal\t" + estOptim[0]);
//			DirectoriosYArchivos.agregaTexto(dirArchChan, sb.toString());
		
		}	
	}
	
	
//	@Override
	public void producirRealizacionPEEstadoOptimNUEVOENCONTRUCCION(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {		
	
		if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
			System.out.println("Se pidió producirRealizacionPEEstadoOptim y no se programó para VAR_Y_PVA");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		int cantCarac = utilitarios.Constantes.cantCarac;
		if(!this.durPasoPEMayorPasoOptim() || this.durPasoPEMayorPasoOptim() && this.esUltimoPasoDelPasoPE()){
			String claveEst = this.claveEstSort(cantCarac, isort);
//			kk no se como vaciar los valores cuando se termina el paso del PE
//			HAY UN LIO A RESOLVER PARA EL PARALELISMO
			ArrayList<Double> valoresPasoFinal = new ArrayList<Double>();				
			
			
			// Las primeras cantVA*NA innovaciones producen el estado completo dado el estado agregado
			// solo se emplean las del primer intervalo de muestreo. Las otras no se6s aleatorios U del VAR
			// NA es la cantidad de rezagos del VAR, NVA la cantidad de variables autoregresivas
	
			// Construye el estado completo del VAR a partir de las variables de estado
			double[][] varsEstado = new double[this.getCantVE()][1];
			int ive=0;
			for(VariableEstado ve: this.getVarsEstado()){
				varsEstado[ive][0] = ve.getEstado();
				ive++;
			}
			double[][] esperanzaCond;
			esperanzaCond = matrices.Oper.prod(matrizEspCondicional,varsEstado);
			
			double[][] inECompNorm = new double[nVA*nA][1]; 
			for(int ii=0; ii<nVA*nA; ii++){
				double uii = innovaciones1Sort[ii][0]*Math.sqrt(dVCC[ii][ii]);
				inECompNorm[ii][0] = procEstocUtils.DistribucionNormal.inversacdf2(uii);			
			}
			double[][] m1 = matrices.Oper.prod(bVC, inECompNorm);
			
			double[][] ecMat = matrices.Oper.suma(m1, esperanzaCond);
	
			// En espejoVarsEstadoVA
			// Primer indice rezago, hay NA valores
			// Segundo índice variable aleatoria (fila de las Ai, es decir hay NVA valores)
			int inE = 0;
			for(int ir=0; ir<nA; ir++){
				for(int iva=0; iva<nVA; iva++){
					espejoVarsEstadoVA.get(ir).set(iva,ecMat[inE][0]);
					inE++;
				}
			}			
			int cantIm = instantesMuestreo.length;
			long instIniPasoOp = this.getOptimizadorPaso().getInstIniPaso();
			// recorre los instantes de muestreo
			for(int im=0; im<cantIm; im++){
				long instante = instantesMuestreo[im];
				int pasoDelAnio = this.pasoDelAnio(instante);
				// Procesa un instante de muestreo
				if (instante > this.getInstanteCorrienteFinal()) {
					// hay que sortear una realización nueva para un paso del VAR			
					double[] innov1Muestreo = new double[this.getCantidadInnovaciones()];
					// carga todas las innovaciones para el intervalo de muestreo im
					for(int in=0; in<cantIn; in++){
						innov1Muestreo[in] = innovaciones1Sort[in][im];
					}
					while(this.getInstanteCorrienteFinal()<instante){	
						avanzarUnPasoDelPEOptim(instante, innov1Muestreo);
					}				
				}
				
				for(int iv=0; iv<nVA; iv++){
					VariableAleatoria va = this.getVariablesAleatorias().get(iv);
					TransformacionVA tr = this.dameTrans(va.getNombre(), pasoDelAnio);
					double valor = tr.inversa(yt[iv]);
					double cota = cotaInfRealiz.get(va.getNombre());
					if(valor<cota) valor = cota;
					cota = cotaSupRealiz.get(va.getNombre());
					if(valor>cota) valor = cota;
					if(this.isMuestreado()){
						this.getVariablesAleatorias().get(iv).getUltimoMuestreoOptim()[isort][im]=valor;
					}else{
						this.getVariablesAleatorias().get(iv).setValor(yt[iv]);
					}
				}	
				// Carga el estado final agregado del sorteo Montecarlo
				double[] valoresVESimul = new double[nA*nVA];
				double[] valoresVarExo = new double[nEx];
				ive = 0;		
				for(int ir=0; ir<nA; ir++){
					for(int iva=0; iva<nVA; iva++){
						valoresVESimul[ive]=espejoVarsEstadoVA.get(ir).get(iva);
						ive++;
					}
				}
				for(int iex = 0; iex <nEx; iex++){
					valoresVarExo[iex] = this.getVarsExogenas().get(iex).getValor(); 
					ive++;
				}
				double[] estOptim = this.getAgregadorEstados().devuelveEstadoOptim(valoresVESimul, valoresVarExo);
				estadosS0fintOptimSorteados[isort] = estOptim;  
			}
		}	
	}
	
	
	
	public void avanzarUnPasoDelPEOptim(long instante, double[] innov1Muestreo){		
		yt = new double[nVA];
		int in;
		int r;
		int iv;
		int ive;	
		double[] y = new double[nVA];
		// innov1Muestreo tiene al principio cantVA*NA innovaciones producen el estado completo dado el estado agregado
		// y que acá no se usan
					
		// Se acumula la contribución de los valores contemporáneos de u
		// u = B D e, donde e es vector de normales N(0,1) independientes
		double[] e = utilitarios.UtilArrays.truncaNInicialesD(innov1Muestreo, nVA*nA);
		for(int ie=0; ie<e.length; ie++){
			e[ie] = procEstocUtils.DistribucionNormal.inversacdf2(e[ie]);			
		}
		double[] aux = matrices.Oper.matPorVector(DRaiz,e);
		matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(B,aux)); 

		// Se acumula la contribución de los valores rezagados de y
		for(r=1; r<=nA; r++){
			for(iv = 0; iv<nVA; iv++){
				y[iv] = espejoVarsEstadoVA.get(r-1).get(iv);
			}			
			matrices.Oper.sumaUnVector(yt, matrices.Oper.matPorVector(A[r-1], y)); 	
		}
		
		// Se produce la realización de las variables exógenas y se acumula su contribución		
		for(ProcesoEstocastico pe: this.getProcesosVarsExogenas()){
			pe.producirRealizacion(instante);
		}
		for(ive = 0; ive<nEx; ive++){
			double valEx = this.getVarsExogenas().get(ive).getValor();
			matrices.Oper.sumaUnVector(yt, matrices.Oper.prodVecEscalar(X[ive], valEx));			
		}		
				
		// Se rezagan los valores de las variables de estado normalizadas varsEstadoVA 
		for(r=2; r<nA; r++){
			for(iv=0; iv<nVA; iv++){
				espejoVarsEstadoVA.get(r).set(iv, espejoVarsEstadoVA.get(r-1).get(iv));
			}
		}
		// Se carga el nuevo valor producido como primer rezago para el siguiente paso.
		for(iv=0; iv<nVA; iv++){
			espejoVarsEstadoVA.get(0).set(iv, yt[iv]);
		}
		
//		// Se rezagan los valores de las variables de estado normalizadas varEstadoU 	
//		for(r=1; r<NM-1; r++){
//			for(iv=0; iv<cantInno; iv++){
//				varsEstadoU.get(r).get(iv).actualizarEstado(varsEstadoU.get(r-1).get(iv).getEstado());
//			}
//		}		

		this.setInstanteCorrienteInicial(this.getInstanteCorrienteInicial()+this.getDurPaso());
		this.setInstanteCorrienteFinal(this.getInstanteCorrienteInicial()+this.getDurPaso());						
	}

	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		estadosS0fintOptimSorteados = new double[cantSortMontecarlo][];	
	}




	public int getnA() {
		return nA;
	}


	public void setnA(int nA) {
		this.nA = nA;
	}


	public int getnM() {
		return nM;
	}


	public void setnM(int nM) {
		this.nM = nM;
	}


	public int getnVA() {
		return nVA;
	}


	public void setnVA(int nVA) {
		this.nVA = nVA;
	}


	public ArrayList<ArrayList<VariableEstado>> getVarsEstadoVA() {
		return varsEstadoVA;
	}

	public void setVarsEstadoVA(ArrayList<ArrayList<VariableEstado>> varsEstadoVA) {
		this.varsEstadoVA = varsEstadoVA;
	}

//	public ArrayList<ArrayList<VariableEstado>> getVarsEstadoU() {
//		return varsEstadoU;
//	}
//
//	public void setVarsEstadoU(ArrayList<ArrayList<VariableEstado>> varsEstadoU) {
//		this.varsEstadoU = varsEstadoU;
//	}

	public int getCantIn() {
		return cantIn;
	}

	public void setCantInno(int cantIn) {
		this.cantIn = cantIn;
	}

	public int getNEx() {
		return nEx;
	}

	public void setNEx(int nEx) {
		nEx = nEx;
	}


	public ArrayList<ArrayList<Double>> getEspejoVarsEstadoVA() {
		return espejoVarsEstadoVA;
	}


	public void setEspejoVarsEstadoVA(ArrayList<ArrayList<Double>> espejoVarsEstadoVA) {
		this.espejoVarsEstadoVA = espejoVarsEstadoVA;
	}


	public int getnEx() {
		return nEx;
	}


	public void setnEx(int nEx) {
		this.nEx = nEx;
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

	public double[][] getDRaiz() {
		return DRaiz;
	}

	public void setDRaiz(double[][] dRaiz) {
		DRaiz = dRaiz;
	}


	public double[][] getEstadosS0fintOptimSorteados() {
		return estadosS0fintOptimSorteados;
	}

	public void setEstadosS0fintOptimSorteados(double[][] estadosS0fintOptimSorteados) {
		this.estadosS0fintOptimSorteados = estadosS0fintOptimSorteados;
	}


	public void setCantIn(int cantIn) {
		this.cantIn = cantIn;
	}


	public double[][][] getA() {
		return A;
	}

	public void setA(double[][][] a) {
		A = a;
	}

//	public double[][][] getM() {
//		return M;
//	}
//
//	public void setM(double[][][] m) {
//		M = m;
//	}


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

	public String getFormaEstimacion() {
		return formaEstimacion;
	}

	public void setFormaEstimacion(String formaEstimacion) {
		this.formaEstimacion = formaEstimacion;
	}

	public double[][] getMatrizEspCondicional() {
		return matrizEspCondicional;
	}

	public void setMatrizEspCondicional(double[][] matrizEspCondicional) {
		this.matrizEspCondicional = matrizEspCondicional;
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


	public double[][] getMatrizVarCondicional() {
		return matrizVarCondicional;
	}

	public void setMatrizVarCondicional(double[][] matrizVarCondicional) {
		this.matrizVarCondicional = matrizVarCondicional;
	}



	@Override
	public void contribuirAS0fint() {
		/**
		 * Este metodo se usa en la simulación.
		 * Se supone que en la simulación se invoca DESPUÉS de producirRealizacion()
		 * es decir que se han actualizado para el proximo paso los valores de las VE.
		 * 
		 * varsEstadoVA:
		 * Variables de estado asociadas a cada una de las variables aleatorias normalizadas rezagadas, 
		 * en la fórmula de arriba las variables y(t-1) ... y(t-NA)
		 * Si en una posición hay null, la respectiva variable aleatoria no es una variable de estado.
		 * Primer indice rezago, hay NA valores
		 * Segundo índice variable aleatoria (fila de las Ai, es decir hay NVA valores)
		 */

		for(int ir=0; ir<nA; ir++){
			int iva = 0;
			for(VariableEstado ve: varsEstadoVA.get(ir)){				
				ve.setEstadoS0fint(ve.getEstado());
				iva++;
			}
		}
	}


	@Override
	public void contribuirAS0fintOptim() {
		if(formaEstimacion.equalsIgnoreCase(utilitarios.Constantes.VAR_Y_PVA)){
			System.out.println("Se pidió contribuirAS0fintOptim() y no se programó para VAR_Y_PVA");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		int isort = this.getOptimizadorPaso().getOpEst().getIsort();
		int ive = 0;
		for(VariableEstado ve: this.getVarsEstado()){
			ve.setEstadoS0fint(estadosS0fintOptimSorteados[isort][ive]);
			ive++;
		}		
	}

// HAY UN METODO DE LA CLASE PADRE QUE HACE ESTO
//	@Override
//	public void cargarValVEOptimizacion() {
//		// TODO Auto-generated method stub
//		AgregadorLineal al = (AgregadorLineal)this.getAgregadorEstados();	
//		double[][] matT = al.getMatrizAgregacion();
//		double[][] v = new double[nA*nVA+nEx][1];   // vector v de estado completo
//		int cantVEOptim = matT.length;
//		double[][] h = new double[cantVEOptim][1];
//		h = matrices.Oper.prod(matT, v);
//		kk
//	}


	@Override
	public void cargarValRecursoVEOptimizacion(AFIncrementos aproxFuturaOpt) {
		// Deliberadamente en blanco porque no es un participante con un recurso sino un PE		
	}


	@Override
	public void cargarValRecursoVESimulacion() {
		// Deliberadamente en blanco porque no es un participante con un recurso sino un PE 		
	}


	@Override
	public void actualizaVESimPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// Deliberadamente en blanco porque no es un participante con un recurso sino un PE 
		
	}

	@Override
	public void actualizaVEOptPorControlesDE(long instInicioPaso, ArrayList<VariableControlDE> varsControlDE) {
		// Deliberadamente en blanco porque no es un participante con un recurso sino un PE 
		
	}


	@Override
	public void cargaParVEContinuaVDespacho(Hashtable<String, String> tabla) {
		// Deliberadamente en blanco porque no es un participante con un recurso sino un PE 	
	}



	@Override
	public double devuelveVarDualVEContinua(VariableEstado vec, DatosSalidaProblemaLineal resultado) {
		// TODO  POR EL MOMENTO NO SE USA, EVENTUALMENTE SE USARIA EN LA PD DUAL
		return 0;
	}


	/**
	 * Ejemplo sencillo de generación de estados V completos a partir del valor de las 
	 * variables de estado, que implementa lo de 54-Vnn-IMPLEMENTACION DEL VAR ...
	 * punto 2.3.
	 * Se tiene V normal de media nula y covarianza identidad
	 * H = TV , H vector de estado de dimensión 1.
	 * T = [2 , 1]
	 * Se busca generar V condicionado a que H = [0.5]
	 * Los parámetros de la distribución de V condicionado a H = [0.5]
	 * son media y sigma, cargados en el código abajo
	 * La innovaciona aleatoria es el vector e normal 0, 1 bivariado.
	 * Se calcula la matriz b tal que be normal tiene varianza sigma
	 * Se suma la media
	 * v = media + be
	 * 
	 * SE TRATA DE VERIFICAR QUE H = TV es siempre igual a 0.5
	 * 
	 */
	public static void ejemploSorteosEstadoCompleto (){
		double[][] t = {{2 , 1}};
		double[][] sigma = {{0.2 , -0.4},{-0.4, 0.8}};
		double[][] media = {{0.2},{0.1}};
        double[][] b = new double[2][2]; 
        double[][] bt = new double[2][2]; 
        double[][] diag = new double[2][2]; 
        Oper.eigenDecomp(sigma, diag, b, bt);    
		System.out.println("Llegó acá");
		double s1 = Math.sqrt(diag[0][0]);
		double s2 = Math.sqrt(diag[1][1]);
		System.out.println("Valores propios - s1=" + s1 + "  -  s2=" + s2);
        Random n1 = new Random(); 
        Random n2 = new Random(); 
		// genera normal bivariada V=(v1,v2) con media y sigma
		int nsort=100;
        double[][] e = new double[2][1];
        double[][] v = new double[2][1];
		double v1, v2;
		double[][] h;
		for(int i=0; i<nsort; i++){			
            double e1 = n1.nextGaussian()*s1;
            double e2 = n2.nextGaussian()*s2;
            e[0][0] = e1; 
            e[1][0] = e2;   
            v = Oper.prod(b, e);
            v = Oper.suma(v, media);
            h = Oper.prod(t, v);
            StringBuilder sb = new StringBuilder();
            sb.append("v1 = " + v[0][0] + " - ");
            sb.append("v2 = " + v[1][0] + " - ");
            sb.append("h = " + h[0][0] + "\n");
            System.out.print(sb.toString());
		}
		
		
		
		
	}


	@Override
	public void actualizaValoresVEDiscretizacionesVariables(long instante) {
		// TODO Auto-generated method stub
		
	}


	
}
