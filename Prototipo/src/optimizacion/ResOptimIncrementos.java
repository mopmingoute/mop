/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResOptimIncrementos is part of MOP.
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

package optimizacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import control.VariableControlDE;
import datatypesResOptim.DatosTablaControlesDE;
import datatypesResOptim.DatosTablaVByValRec;
import utilitarios.Constantes;
import utilitarios.EnumeradorLexicografico;
import estado.Discretizacion;
import estado.VariableEstado;
import estado.VariableEstadoPE;
import estado.VariableEstadoPar;
import futuro.AFIncrementos;
import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;
import futuro.TablaControlesDE;
import futuro.TablaControlesDEMemoria;
import futuro.TablaVByValRecursos;
import futuro.TablaVByValRecursosMemoria;
import futuro.TablaVByValRecursosRedis;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocasticos.ProcesoEstocastico;

/**
 * Clase que representa el resultado obtenido de la optimización que debe ser utilizado en la simulación
 * @author ut469262
 * 
 * Se supone que las VE y la discretización de las mismas empleadas en el despacho del paso t, y que forman el 
 * s0fint son las mismas que existen durante todo el paso.
 * 
 * La optimización tiene que dar cuenta de los cambios en las VE o en su discretización, cuando se
 * estó haciendo la recursión hacia atrós desde el paso t+1 al t. Por ejemplo si en t+1 no existe una variable
 * de estado ve* que só existe en t, el ResOptim debe tener una heuróstica que asigne valores de la aproximación 
 * futura para todos los valores de la ve* al fin del paso t. 
 * 
 * CUANDO SE MENCIONAN LAS DERIVADAS PARCIALES DEL VALOR DE BELLMAN , SE ENTIENDE QUE ES LA DERIVADA CON SIGNO NEGATIVO
 *                                                                                -------------------------------------
 * ES DECIR QUE LA LLAMADA DERIVADA PUEDE USARSE COMO UN COSTO EN EL DESPACHO.
 *                                                                                
 *                                                                
 * CUANDO SE MENCIONAN LOS INCREMENTOS DEL VALOR DE BELLMAN POR LA VARIACIóN DE UNA VARIABLE DISCRETA, SE ENTIENDE
 * QUE ES EL AUMENTO DE VALOR DE BELLMAN PORQUE LA VARIABLE DISCRETA TENGA UN VALOR MAYOR AL FIN DEL PASO.
 *           ---------------------------
 */
public class ResOptimIncrementos extends ResOptim{

	/* 
	 * valRecPrecalculados es:
	 * true si los valores de recursos ya estón precalculados en los puntos de la grilla 
	 * y se trata sólo de interpolar en esas tablas precalculadas
	 * 
	 * false si hay que hacer las consultas en la tabla de valores de Bellman y calcular los valores de recursos
	 * en cada punto de la grilla que sea necesario
	 * 
	 * 
	 */
	private boolean valRecPrecalculados;  // OJO ESTA VARIABLE NO SE USA SE SUPONE TRUE LOS VALORES ESTAN PRECALCULADOS
	
	
	/*
	 * Tabla que almacena toda la información de valores de la optimización, en cada punto de discretización
	 * -valor de Bellman
	 * -valor de las derivadas parciales respecto a variables de estado continuas CON SIGNO NEGATIVO
	 * -lista de valores de los incrementos de valor para cada variable de estado 
	 *  con controles discretos incrementales CON SIGNO NEGATIVO
	 * -controles óptimos de las variables discretas exhaustivas
	 */
	private TablaVByValRecursos tablaValores;	
	

	
	/*
	 * Enumerador lexicogrófico para los poliedros en la interpolación de las variables continuas
	 */
	private EnumeradorLexicografico enumLexPoliedro; 
	private int[] cotasInferioresPoli;  // del enumerador lexicogrófico, siempre iguales a 0
	private int[] cotasSuperioresPoli;  // del enumerador lexicogrófico, siempre iguales a 1
	private int cantVertices;  // la cantidad de vórtices del poliedro = 2^cantVECont;
	
	
	/*
	 * Variables auxiliares
	 */
	private int[] codigoUnVertice;
	private ClaveDiscreta clave;
	private double x0, x1;
	private int pasoInf;
	private ArrayList<Double> ponderadoresVertices;
	private ArrayList<Double> ponderadoresVerticesVB;
	

	
	public ResOptimIncrementos(int cantPasos){		
		super(cantPasos);
		if (CorridaHandler.getInstance().isParalelo()) {
			tablaValores = new TablaVByValRecursosRedis(cantPasos);
		} else {
			tablaValores = new TablaVByValRecursosMemoria(cantPasos);
		}
		
		super.completaConstruccion();			
	}

	public ResOptimIncrementos(){		
		super.completaConstruccion();			
	}	
	

	public void completaConstruccion(){	
		clave = new ClaveDiscreta();
	}
	
	
	

	
	
	/**
	 * Devuelve en forma sucesiva los códigos enteros de las VE corrientes de los estados posibles
	 * de la grilla de estados en el paso t.
	 * 
	 * Se emplea el enumerador lexicogrófico enumLexEstados que cuando se crea en el mótodo 
	 * inicializaParaNuevasVE() se inicializa en el primer valor
	 * @return
	 */	
	public int[] devuelveCodigoEstadoDeLaGrilla(){		
		int[] vector = this.getEnumLexEstados().devuelveVector();
		return vector;		
	}
	
	
	

	
	/**
	 * Devuelve la aproximación futura en el S0fint que está almacenado en las
	 * variables de estado varsEstadoCorrientes. 
	 *  
	 * @param paso paso en el que se estó simulando u optimizando 
	 * @param calculaVB si es true, interpola el VB, de lo contrario no
	 * El valor del estado s0fint, estado al fin del paso, estó contenido en el atributo
	 * de las variables de estado.
	 * 
	 */
	public AFIncrementos devuelveAproxS0fint(int paso, boolean calculaVB) {	

		double[] valores = new double[this.getCantVE()];
		for(int ive=0; ive<this.getVarsEstadoCorrientes().size(); ive++){	
			VariableEstado ve = this.getVarsEstadoCorrientes().get(ive);
			valores[ive] = ve.getEstadoS0fint();
		}
		return devuelveAprox(paso, valores, calculaVB);
	}

	/**
	 * SE USA EN LA OPTIMIZACIóN
	 * Devuelve la aproximación futura en el estado final que estó almacenada en las
	 * variables de estado varsEstadoCorrientes en el atributo estadoFinalOptim. 
	 * Se emplea luego de terminar las iteraciones para optimizar el estado.
	 * 
	 *  
	 * @param paso paso en el que se estó simulando u optimizando 
	 * @param calculaVB si es true, interpola el VB, de lo contrario no
	 * El valor del estado s0fint, estado al fin del paso, estó contenido en el atributo
	 * de las variables de estado.
	 * 
	 */
	public AFIncrementos devuelveAproxEstadoFinal(int paso, boolean calculaVB) {	
		double[] valores = new double[this.getCantVE()];
		for(int ive=0; ive<this.getVarsEstadoCorrientes().size(); ive++){	
			VariableEstado ve = this.getVarsEstadoCorrientes().get(ive);
			valores[ive] = ve.getEstadoFinalOptim();
		}
		return devuelveAprox(paso, valores, calculaVB);
	}	
	
	/**
	 * Corrige los valores de las derivadas parciales de la variable de estado ve en el paso paso.
	 * Si el valor de la VE ve es menor (mayor) que valorLim, impone la cota inferior o superior cota.
	 * al valor del recurso.
	 * 
	 * @param paso
	 * @param ve es la variable de estado para la que se corrige la derivada
	 * @param valorLim es el valor lómite de los valores de la ve en los que se corrige la derivada
	 * @param menor es true si se corrigen los estados menores a valorLim, si es falso se corrigen los mayores
	 * @param cota cota a imponer a los valores de la derivada.
	 * @param cotaInf es true si cota es una cota inferior y false si es una cota superior.
	 */
	public void corrigeValRecursos (int paso, VariableEstadoPar ve, double valorLim, boolean menor, double cota, boolean cotaInf){
		int indiceVE = this.getVarsEstadoCorrientes().indexOf(ve);
		int indiceVEEnContinuas = this.getOrdinalEnEnumDeContinuas().get(indiceVE);
		int[] cotasInferiores = new int[this.getCantVE()];
		int[] cotasSuperiores = new int[this.getCantVE()];
		int ind = 0;
		Discretizacion discVE = ve.getEvolDiscretizacion().getValor(this.getInstanteRef());
		for(VariableEstado v: this.getVarsEstadoCorrientes()){
			cotasSuperiores[ind]=v.getEvolDiscretizacion().getValor(this.getInstanteRef()).getCantValores()-1;			
			ind++;
		}
		EnumeradorLexicografico enumEst = new EnumeradorLexicografico(this.getCantVE(), cotasInferiores, cotasSuperiores);
		enumEst.inicializaEnum();
		int[] codEst = enumEst.devuelveVector();
		while(codEst!=null){
			double valorVE = discVE.devuelveValorOrdinal(codEst[indiceVE]); 
			if(valorVE <= valorLim && menor || valorVE >= valorLim && !menor){
				ClaveDiscreta clave = new ClaveDiscreta(codEst);
				InformacionValorPunto ivp = tablaValores.devuelveInfoValoresPunto(paso, clave);
				double deriv = ivp.getDerivadasParciales()[indiceVEEnContinuas];
				if(deriv<cota && cotaInf || deriv>cota && !cotaInf) {
					ivp.getDerivadasParciales()[indiceVEEnContinuas]=cota;
					tablaValores.cargaInfoValoresPunto(paso, clave, ivp);
				}
			}				
		}		
	}
	
	
	@Override
	public void inicializaResOptimParaNuevoPaso(int numpaso, long instante, ArrayList<VariableEstado> varsEstado,
			ArrayList<VariableControlDE> varsControlDE){			
		super.inicializaResOptimParaNuevoPaso(numpaso, instante, varsEstado, varsControlDE);
		codigoUnVertice = new int[this.getCantVE()];
		ponderadoresVerticesVB = new ArrayList<Double>();
		ponderadoresVertices = new ArrayList<Double>();
		// Crea el enumerador lexicogrófico para los poliedros de interpolación de las variables continuas
		cotasInferioresPoli = new int[this.getCantVECont()];
		cotasSuperioresPoli = new int[this.getCantVECont()];		
		for(int ic=0; ic<this.getCantVECont(); ic++){
			cotasInferioresPoli[ic]=0;
			cotasSuperioresPoli[ic]=1;
		}
		enumLexPoliedro = new EnumeradorLexicografico(this.getCantVECont(), cotasInferioresPoli, cotasSuperioresPoli);
		enumLexPoliedro.creaTablaYListaOrdinales();   // Crea la lista de vectores binarios que se usarón
		cantVertices = enumLexPoliedro.getListaVectorDeOrdinal().size();	
		
	}
	
	
	/**
	 *  
	 * 
	 * Devuelve la aproximación futura para los valores de las VE que estón contenidos en 
	 * el parómetro valores, en el mismo orden en que aparecen las VE en varsEstadoCorrientes.
	 *  
	 * @param paso paso en el que se estó simulando u optimizando. La aproximación se hace al fin del paso 
	 * 
	 * @param valores es el conjunto de valores de las VE al fin del paso, para el que hay que devolver la aproximación
	 * 				en el mismo orden en que estan las VE en varsEstadoCorrientes
	 * 
	 * @param calculaVB es true si hay que devolver el valor de Bellman interpolado
	 * 
	 */
	public AFIncrementos devuelveAprox(int paso, double[] valores, boolean calculaVB) {
		
		
		AFIncrementos aproxF = new AFIncrementos();

		double[] alfaivb = new double[this.getCantVECont()]; 
		double[] alfai = new double[this.getCantVECont()]; 
		/**
		 * Codigos enteros de todas las VE, las VE continuas en su valor inferior
		 * para S0fint o estadoFinalOptim segón el caso
		 */
		int[] codigoTotalInf = new int[this.getCantVE()];   

		/*
		 *  Calcula el código discreto del estado, para las VE continuas el código inferior
		 *  Calcula los alfai
		 * 
		 *  alfas son los ponderadores del valor en pasoInf + 1
		 *  1 - alfas son los ponderadores del valor en pasoInf 
		 */
	
		int ic=0;
		for(int ive=0; ive<this.getVarsEstadoCorrientes().size(); ive++){	
			VariableEstado ve = this.getVarsEstadoCorrientes().get(ive);
			double valor = valores[ive];
			Discretizacion disc = ve.devuelveDiscretizacion(this.getInstanteRef());
			pasoInf = disc.devuelvePasoInferiorParaInterpolar(valor);
			codigoTotalInf[ive] = pasoInf;
			if(!ve.isDiscreta()){
				// La variable es continua y se calcula el alfai
				x0 = disc.devuelveValorOrdinal(pasoInf);
				x1 = disc.devuelveValorOrdinal(pasoInf+1);
				double alfa = (valor-x0)/(x1-x0);
				alfai[ic] = alfa;
				// se chequea por recurso inútil por encima del valor superior para interpolar el valor de Bellman
				if(alfa>1 && ve instanceof VariableEstadoPar 
						&& pasoInf==disc.getCantValores()-2){    // cantValores-2 es el penóltimo paso, ya que empiezan en 0
					VariableEstadoPar vep = (VariableEstadoPar)ve;
					if(vep.isHayValorSuperior() && vep.getValorRecursoSuperior().getValor(this.getInstanteRef())<Constantes.EPSILONCOEF){
						/**
						 * Se calcula como si el recurso estuviera exactamente en
						 * el valor superior de modo de no extrapolar el valor de Bellman
						 */
						alfaivb[ic] = 0;
					}
				}else{
					alfaivb[ic]= alfa;
				}
				ic++;
			}			
		}	
		/*
		 * ponderadores tiene los ponderadores de cada vórtice del poliedro de interpolación
		 * infoVPs tiene la información de los vórtices del poliedro.
		 */
		ponderadoresVertices.clear();
		ponderadoresVerticesVB.clear();
		ArrayList<InformacionValorPunto> infoVPs = new ArrayList<InformacionValorPunto>();
		
		/*
		 * vertices tiene la lista de vectores binarios que permite generar los puntos 
		 * de interpolación
		 */
		ArrayList<int[]> vertices = enumLexPoliedro.getListaVectorDeOrdinal();
		
		/*
		 * Lee la información de los puntos de interpolación
		 * Calcula los ponderadores de los vórtices para la interpolación	
		 */		
		for(int[] ver: vertices){
			for(int ive=0; ive<this.getCantVE(); ive++){
				if(!this.getVarsEstadoCorrientes().get(ive).isDiscreta()){
					// La variable de estado ive-ósima es continua y se requiere interpolar respecto a ella
					codigoUnVertice[ive] = codigoTotalInf[ive]+ver[this.getOrdinalEnEnumDeContinuas().get(ive)];
				}else{
					// La variable es discreta y el código no se altera
					codigoUnVertice[ive] = codigoTotalInf[ive];
				}				
			}
			
			// Lee la información de un vórtice
			clave.setEnterosIndices(codigoUnVertice);
			infoVPs.add(tablaValores.devuelveInfoValoresPunto(paso, clave));
			
			// Calcula el ponderador del vórtice
			double aux = 1.0;
			double auxVB = 1.0;
			for(ic=0; ic<this.getCantVECont(); ic++){
				aux = aux*(ver[ic]==0? (1-alfai[ic]) : alfai[ic]);
				auxVB = auxVB*(ver[ic]==0? (1-alfaivb[ic]) : alfaivb[ic]);
			}
			ponderadoresVertices.add(aux);
			ponderadoresVerticesVB.add(auxVB);
		}
				
		// Interpola los valores de Bellman
		if(calculaVB){		
			double valorVB = 0.0;
			int ivert = 0;
			for(double pond: ponderadoresVerticesVB){
				valorVB = valorVB + pond*infoVPs.get(ivert).getValorVB();
				ivert++;
			}
			aproxF.setValorBellman(valorVB);			
		}
				
		// Interpola las derivadas parciales de las VE continuas
		if(this.getCantVECont()>0){
			double valorDer;
			for(int ivc=0; ivc<this.getCantVECont(); ivc++){
				// Recorre las variables de estado continuas
				ArrayList<Double> aux = new ArrayList<Double>();  // El ArrayList se llenaró con un ónico valor de la derivada parcial				
				int ordEnVE = this.getOrdinalEnVarsEstadoDeContinuas().get(ivc);
				VariableEstado ve = this.getVarsEstadoCorrientes().get(ordEnVE);
				double valor = valores[ordEnVE];
				String nombreVE = ve.getNombre();
				Discretizacion disc = ve.devuelveDiscretizacion(this.getInstanteRef());
				if(ve instanceof VariableEstadoPar){
					VariableEstadoPar veP = (VariableEstadoPar)ve;
					if(valor<=disc.getValMin() && veP.isHayValorInferior()){
						valorDer = veP.getValorRecursoInferior().getValor(this.getInstanteRef());
					}else if(valor>=disc.getValMax()  && veP.isHayValorSuperior() ){
						valorDer = veP.getValorRecursoSuperior().getValor(this.getInstanteRef());						
					} else {
						valorDer = 0;
						int ivert = 0;
						for(double pond: ponderadoresVertices){	
							valorDer = valorDer + pond*infoVPs.get(ivert).getDerivadasParciales()[ivc];
							ivert++;
						}
					}										
				}else{
					valorDer = 0;
					int ivert = 0;
					for(double pond: ponderadoresVertices){
						valorDer = valorDer + pond*infoVPs.get(ivert).getDerivadasParciales()[ivc];
						ivert++;
					}
					
				}
				if (valorDer<0) {
					valorDer = 0;
				}
				aux.add(valorDer);
				aproxF.getIncrementosYDerivadasParciales().put(nombreVE, aux);					
			}
		}
			
	    // Interpola los incrementos de las VE discretas incrementales
		if(this.getCantVEDisInc()>0){
			for(int ivde=0; ivde<this.getCantVEDisInc(); ivde++){
				int ordEnVE = this.getOrdinalEnVarsEstadoDeContinuas().get(ivde);
				VariableEstado ve = this.getVarsEstadoCorrientes().get(ordEnVE);
				String nombreVE = ve.getNombre();
				ArrayList<Double> aux = new ArrayList<Double>();				
				for(int iIncr=0; iIncr<ve.devuelveDiscretizacion(this.getInstanteRef()).getCantValores(); iIncr++){
					// iIncr recorre los posibles valores de la variable al fin del paso t, que son los de la discretización
					double valorInc = 0;
					int ivert = 0;
					for(double pond: ponderadoresVertices){
						valorInc = valorInc + pond*infoVPs.get(ivert).getIncrementosValor()[ivde][iIncr];
						ivert++;
					}
					aux.add(valorInc);					
				}
				aproxF.getIncrementosYDerivadasParciales().put(nombreVE, aux);				
			}			
					
			
		}	
		return aproxF;
	}	
	


	
	
	
	/**
	 * Carga en la tabla de VB un valor obtenido en la optimización.
	 * Para eso crea un objeto InformacionValorPunto en el que carga el VB, las derivadas
	 * e incrementos quedan vacóos.
	 * @param paso paso dela optimización
	 * @param codigoEnt es un int[] con los códigos o claves de los pasos discretos de un estado
	 * @param valor es el valor a cargar
	 * 
	 */
	public void creaIVPyCargaVBEnTabla(int paso, int[] codigoEnt, double valorVB){
		InformacionValorPunto ivp = new InformacionValorPunto();
		ivp.setValorVB(valorVB);
		ClaveDiscreta clave = new ClaveDiscreta(codigoEnt);
		tablaValores.cargaInfoValoresPunto(paso, clave, ivp);		
	}
	
	
	/**
	 * Lee de la tabla la InformacionValorPunto asociada a un código entero
	 */	
	public InformacionValorPunto leeIVPdeCodigo(int paso, int[] codigoEnt){
		ClaveDiscreta clave = new ClaveDiscreta(codigoEnt);
		InformacionValorPunto ivp = tablaValores.devuelveInfoValoresPunto(paso, clave); 
		return ivp;
			
	}
	
	
	/**
	 * Dado que estón calculados los VB al fin del paso paso, calcula las derivadas parciales 
	 * SIGNO NEGATIVO PARA REPRESENTAR EL VALOR DE LOS RECURSOS
	 * respecto a las variables continuas e incrementos del VB respecto a las variables discretas 
	 * @param paso
	 */
	public void calculaDerivadasParcEIncrementos(int paso){
		this.getEnumLexEstados().inicializaEnum();
		int[] vt = this.getEnumLexEstados().devuelveVector();
		while(vt!=null){
			if (Constantes.NIVEL_CONSOLA > 1) {
			StringBuilder sb = new StringBuilder("Calcula derivada estado");
			for(int it=0; it<vt.length; it++){
				sb.append(vt[it]);
				sb.append(" ");
			}
			System.out.println(sb.toString());
			}
			InformacionValorPunto ivp = new InformacionValorPunto(leeIVPdeCodigo(paso, vt));
			double[] aux = new double[this.getCantVECont()];
			ivp.setDerivadasParciales(aux);
			double[][] aux2 = new double[this.getCantVEDisInc()][];
			ivp.setIncrementosValor(aux2);
			
			for(int ive=0; ive<this.getCantVE(); ive++){
//				if(vt[0]==0 && vt[1]==0 && vt[2]==1 && ive==2) {
//					int pp = 0;
//				}
				double[] X = new double[3];
				double[] Y = new double[3];
				int indPunto;
				VariableEstado ve = this.getVarsEstadoCorrientes().get(ive); 
				if(!ve.isDiscreta()){
					// La variable de estado es continua hay que calcular la derivada parcial
					int[] vtaux = vt.clone();
					Discretizacion disc = ve.getEvolDiscretizacion().getValor(this.getInstanteRef());
					if(vt[ive]==0){						
						// la VE continua estó en su menor valor posible
						indPunto = 0;
						X[0] = disc.devuelveValorOrdinal(0);
						Y[0] = ivp.getValorVB();
						vtaux[ive] = vtaux[ive]+1;
						InformacionValorPunto ivpmed = leeIVPdeCodigo(paso, vtaux);
						X[1] = disc.devuelveValorOrdinal(1);
						Y[1] = ivpmed.getValorVB();
						vtaux[ive] = vtaux[ive]+1;
						InformacionValorPunto ivpmax = leeIVPdeCodigo(paso, vtaux);
						X[2] = disc.devuelveValorOrdinal(2);
						Y[2] = ivpmax.getValorVB();						
//						System.out.println("X0: " + X[0] + " PASO " + paso);
//						System.out.println("X1: " + X[1] + " PASO " + paso);
//						System.out.println("X2: " + X[2] + " PASO " + paso);
//						System.out.println("Y0: " + Y[0] + " PASO " + paso);
//						System.out.println("Y1: " + Y[1] + " PASO " + paso);
//						System.out.println("Y2: " + Y[2] + " PASO " + paso);
//						System.out.println("PASO DISCRETIZACION: " + (X[1]-X[0]));
					}else if(vt[ive]==ve.cantValoresPosibles(this.getInstanteRef())-1){
						// la VE continua estó en su mayor valor posible
						indPunto = 2;
						int cantMax = disc.getCantValores();
						X[2] = ve.getEvolDiscretizacion().getValor(this.getInstanteRef()).devuelveValorOrdinal(cantMax-1);
						Y[2] = ivp.getValorVB();
						vtaux[ive] = vtaux[ive]-1;
						InformacionValorPunto ivpmed = leeIVPdeCodigo(paso, vtaux);
						X[1] = ve.getEvolDiscretizacion().getValor(this.getInstanteRef()).devuelveValorOrdinal(cantMax-2);
						Y[1] = ivpmed.getValorVB();
						vtaux[ive] = vtaux[ive]-1;
						InformacionValorPunto ivpmin = leeIVPdeCodigo(paso, vtaux);
						X[0] = ve.getEvolDiscretizacion().getValor(this.getInstanteRef()).devuelveValorOrdinal(cantMax-3);
						Y[0] = ivpmin.getValorVB();		
//						System.out.println("X0: " + X[0] + " PASO " + paso);
//						System.out.println("X1: " + X[1] + " PASO " + paso);
//						System.out.println("X1: " + X[2] + " PASO " + paso);
//						System.out.println("Y0: " + Y[0] + " PASO " + paso);
//						System.out.println("Y1: " + Y[1] + " PASO " + paso);
//						System.out.println("Y2: " + Y[2] + " PASO " + paso);
//						System.out.println("PASO DISCRETIZACION: " + (X[1]-X[0]));
					}else{
						// la VE continua no estó ni en el mónimo ni en el móximo
						indPunto = 1;
						X[1] = disc.devuelveValorOrdinal(vt[ive]);
						Y[1] = ivp.getValorVB();
						vtaux[ive] = vtaux[ive]+1;
						InformacionValorPunto ivpmax = leeIVPdeCodigo(paso, vtaux);
						X[2] = disc.devuelveValorOrdinal(vt[ive]+1);
						Y[2] = ivpmax.getValorVB();
						vtaux[ive] = vtaux[ive]-2;
						InformacionValorPunto ivpmin = leeIVPdeCodigo(paso, vtaux);
						X[0] = disc.devuelveValorOrdinal(vt[ive]-1);
						Y[0] = ivpmin.getValorVB();	
//						System.out.println("X0: " + X[0] + " PASO " + paso);
//						System.out.println("X1: " + X[1] + " PASO " + paso);
//						System.out.println("X1: " + X[2] + " PASO " + paso);
//						System.out.println("Y0: " + Y[0] + " PASO " + paso);
//						System.out.println("Y1: " + Y[1] + " PASO " + paso);
//						System.out.println("Y2: " + Y[2] + " PASO " + paso);
//						System.out.println("PASO DISCRETIZACION: " + (X[1]-X[0]));
					}
					
					double deriv = calculaDerivada3PCuadConvex(X, Y, indPunto);
					if(deriv>0){
						deriv=0; // por la aproximación cuadrótica apareció una estimación de VB creciente
						if (Constantes.NIVEL_CONSOLA > 1) {
							System.out.println("Derivada de VB negativa variable " + ve.getNombre());
							String texto = "Estado ";
							for(int ie=0; ie<vt.length; ie++){
								texto += vt[ie];
								texto += " ";
							}
						}
					}

					/**
					 * CAMBIA EL SIGNO DE TODAS LAS DERIVADAS PARA QUE SEA EL VALOR DE UN RECURSO
					 */
					ivp.getDerivadasParciales()[this.getOrdinalEnEnumDeContinuas().get(ive)] = -deriv;
					
				}else if(ve.isDiscretaIncremental()){
					/**
					 * La variable de estado es discreta incremental hay que calcular los incrementos
					 * El incremento es igual a (VB con VE DE incrementada) menos (VB base)
					 */
					int[] vtaux = vt.clone();
//					ivp = leeIVPdeCodigo(paso, vtaux);
					double vBBase = ivp.getValorVB(); 
					Discretizacion disc = ve.getEvolDiscretizacion().getValor(this.getInstanteRef());
					int cantValoresDisc = disc.getCantValores();
					/**
					 * valorVBInc tiene los valores de Bellman para todos los valores posibles
					 * de la variable de estado discreta incremental ve
					 */
					double[] valorVBInc = new double[cantValoresDisc];
					for(int id=0; id<cantValoresDisc; id++){
						vtaux[ive]=id;
						InformacionValorPunto ivp2 = leeIVPdeCodigo(paso, vtaux);
						double vBIncr = ivp2.getValorVB();
						/**
						 * CAMBIA EL SIGNO PARA QUE SEA EL VALOR DE UN RECURSO
						 */							
						valorVBInc[id]= -(vBIncr-vBBase);
					}									
					ivp.getIncrementosValor()[ive] = valorVBInc;					
					
				}
			}
			
			tablaValores.cargaInfoValoresPunto(paso, new ClaveDiscreta(vt), ivp);
			vt = this.getEnumLexEstados().devuelveVector();
		}
	
	}
	
	
	/**
	 * Dado que estón calculados los VB al fin del paso paso, completa las derivadas parciales
	 * e incrementos con valores nulos. Se usa para inicializar las tablas al fin del horizonte. 
	 * @param paso
	 */
	public void creaNulosDerivadasParcEIncrementos(int paso){
		
		int[] vt = this.getEnumLexEstados().devuelveVector();
		while(vt!=null){
			InformacionValorPunto ivp = new InformacionValorPunto(leeIVPdeCodigo(paso, vt));
			for(int ive=0; ive<this.getCantVE(); ive++){
				VariableEstado ve = this.getVarsEstadoCorrientes().get(ive); 
				if(!ve.isDiscreta()){
					// La variable de estado es continua hay que calcular la derivada parcial						
					ivp.getDerivadasParciales()[this.getOrdinalEnEnumDeContinuas().get(ive)] = 0.0;						
				}else if(ve.isDiscretaIncremental()){
					/**
					 * La variable de estado es discreta incremental hay que calcular los incrementos
					 * El incremento es igual a (VB con VE DE incrementada) menos (VB base)
					 */
					Discretizacion disc = ve.getEvolDiscretizacion().getValor(this.getInstanteRef());
					int cantValoresDisc = disc.getCantValores();
					/**
					 * valorVBInc tiene los valores de Bellman para todos los valores posibles
					 * de la variable de estado discreta incremental ve
					 */
					double[] valorVBInc = new double[cantValoresDisc];
					for(int id=0; id<cantValoresDisc; id++){
						valorVBInc[id]= 0.0;
					}
					ivp.getIncrementosValor()[ive] = valorVBInc;											
				}
			}
			tablaValores.cargaInfoValoresPunto(paso, new ClaveDiscreta(vt), ivp);
			vt = this.getEnumLexEstados().devuelveVector();
		}		
	}
	
	
	
	
	

	
	
	
	/*
	 * Calcula derivada parcial en un punto por el mótodo de Lagrange (aproximación cuadrática por 
	 * tres puntos de la función), CORRIGIENDO en caso de que los tres puntos no determinen
	 * una función convexa. En ese caso se toma como derivada la pendiente entre los dos puntos extremos.
	 * 
	 * @param valoresX son los valores del argumento respecto al cual se deriva, en tres puntos
	 * @param valoresY son los valores de la función a derivar, para los tres argumentos de valoresX
	 * @param indPunto vale 0 si se quiere estimar la derivada en el menor argumento, valoresX[0]
	 *        indPunto vale 1 si se quiere estimar la derivada en el punto central, valoresX[1]
	 *        indPunto vale 2 si se quiere estimar la derivada en el mayor argumento, valoresX[2]
	 */	
	public double calculaDerivada3PCuadConvex(double[] valoresX, double[] valoresY, int indPunto){		
		double g1 = (valoresY[1]-valoresY[0])/(valoresX[1]-valoresX[0]);
		double g2 = (valoresY[2]-valoresY[1])/(valoresX[2]-valoresX[1]);
		double der;
		if(g1>g2){
			der = (valoresY[2] - valoresY[0])/((valoresX[2] - valoresX[0])) ;
			
		}else{
			der = calculaDerivada3PCuad(valoresX, valoresY, indPunto);
		}
		return der;
	}
	
	
	/*
	 * Calcula derivada parcial en un punto por el mótodo de Lagrange (aproximación cuadrótica por 
	 * tres puntos de la función).
	 * 
	 * @param valoresX son los valores del argumento respecto al cual se deriva, en tres puntos
	 * @param valoresY son los valores de la función a derivar, para los tres argumentos de valoresX
	 * @param indPunto vale 0 si se quiere estimar la derivada en el menor argumento, valoresX[0]
	 *        indPunto vale 1 si se quiere estimar la derivada en el punto central, valoresX[1]
	 *        indPunto vale 2 si se quiere estimar la derivada en el mayor argumento, valoresX[2]
	 */
	public double calculaDerivada3PCuad(double[] valoresX, double[] valoresY, int indPunto){	
		double l1 = valoresX[1]-valoresX[0]; 
		double l2 = valoresX[2]-valoresX[1]; 
		double g1 = (valoresY[1]-valoresY[0])/l1;
		double g2 = (valoresY[2]-valoresY[1])/l2;		
		if(indPunto==0) {
			// Derivada en el primer punto, menor argumento		
			return ((2*l1+l2)*g1-l1*g2)/(l1+l2);
		}else if(indPunto==1){
			// Derivada en el punto central		
			return (l2*g1+l1*g2)/(l1+l2);
		}else {
			// Derivada en el tercer punto, mayor argumento
			return ((2*l2+l1)*g2-l2*g1)/(l1+l2);
		}
	}
		

	public boolean isValRecPrecalculados() {
		return valRecPrecalculados;
	}

	public void setValRecPrecalculados(boolean valRecPrecalculados) {
		this.valRecPrecalculados = valRecPrecalculados;
	}

	public TablaVByValRecursos getTablaValores() {
		return tablaValores;
	}

	public void setTablaValores(TablaVByValRecursos tablaValores) {
		this.tablaValores = tablaValores;
	}



//	/*
//	 * Para cada variable de estado de varsEstadoCorrientes
//	 * - si la VE es discreta incremental (es decir se calculan incrementos respecto a sus variaciones),
//	 *   da una posición empezando en cero en InformacionValorPunto
//	 * - si la VE no es discreta incremental da el móximo entero
//	 *   
//	 */
//	public ArrayList<Integer> getOrdinalEnInfoPuntoDeDiscretasIncr() {
//		return ordinalEnInfoPuntoDeDiscretasIncr;
//	}
//
//	public void setOrdinalEnInfoPuntoDeDiscretasInc(
//			ArrayList<Integer> ordinalEnInfoPuntoDeDiscretasInc) {
//		this.ordinalEnInfoPuntoDeDiscretasIncr = ordinalEnInfoPuntoDeDiscretasInc;
//	}
//
//	
//	/*
//	 * Para cada lugar en InformacionValorPunto para las variables discretas incrementales, devuelve
//	 * el ordinal empezando en cero en el vector varsEstadoCorrientes
//	 */	
//	public ArrayList<Integer> getOrdinalEnVarsEstadoDeDiscretasIncr() {
//		return ordinalEnVarsEstadoDeDiscretasIncr;
//	}
//
//	public void setOrdinalEnVarsEstadoDeDiscretasIncr(
//			ArrayList<Integer> ordinalEnVarsEstadoDeDiscretasIncr) {
//		this.ordinalEnVarsEstadoDeDiscretasIncr = ordinalEnVarsEstadoDeDiscretasIncr;
//	}

	public int getCantVertices() {
		return cantVertices;
	}

	public void setCantVertices(int cantVertices) {
		this.cantVertices = cantVertices;
	}



	public EnumeradorLexicografico getEnumLexPoliedro() {
		return enumLexPoliedro;
	}

	public void setEnumLex(EnumeradorLexicografico enumLexPoliedro) {
		this.enumLexPoliedro = enumLexPoliedro;
	}


	public int[] getCotasInferioresPoli() {
		return cotasInferioresPoli;
	}


	public void setCotasInferioresPoli(int[] cotasInferioresPoli) {
		this.cotasInferioresPoli = cotasInferioresPoli;
	}


	public int[] getCotasSuperioresPoli() {
		return cotasSuperioresPoli;
	}


	public void setCotasSuperioresPoli(int[] cotasSuperioresPoli) {
		this.cotasSuperioresPoli = cotasSuperioresPoli;
	}




	public void setEnumLexPoliedro(EnumeradorLexicografico enumLexPoliedro) {
		this.enumLexPoliedro = enumLexPoliedro;
	}

	/**
	 * Carga cero en todos los valores de Bellman, derivadas parciales e incrementos
	 * en los valores al fin del óltimo paso.
	 */
	public void cargaVBFinales() {
		this.getEnumLexEstados().inicializaEnum();
		int[] vt = this.getEnumLexEstados().devuelveVector();
		while(vt!=null){			
			creaIVPyCargaVBEnTabla(this.getCantPasos()-1, vt, 0.0);
			vt = getEnumLexEstados().devuelveVector();
		}	
		creaNulosDerivadasParcEIncrementos(this.getCantPasos()-1);
	}


	
	/**
	 * Graba en el directorio dirSalidas las tablas de valores VB y derivadas y de controles
	 * @param dirSalidas
	 */
	public void guardarTablasResOptimEnDisco(String dirSalidas) {
		try {
			String nombreArch = "TablaVByValRecursos";
			DatosTablaVByValRec dtv = ((TablaVByValRecursosMemoria)this.getTablaValores()).creaDataType();
			utilitarios.ManejaObjetosEnDisco.guardarEnDisco(dirSalidas, nombreArch, dtv);
			nombreArch = "TablaControlesDE";
			DatosTablaControlesDE dc = ((TablaControlesDEMemoria)this.getTablaControlesDE()).creaDataType();
			utilitarios.ManejaObjetosEnDisco.guardarEnDisco(dirSalidas, nombreArch, dc);			
		} catch (Exception e) {
			System.out.println("Error en la serialización del resoptim");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(0);
		}
		
	}

	public void actualizarTabla(int numpaso) {
		tablaValores.cargaTabla(numpaso);
	}	
	
	public void obtenerTabla(int numpaso) {
		tablaValores.devuelveTabla(numpaso);
	}

	public void actualizarTablaControles(int numpaso) {
		this.tablaControlesDE.cargaTabla(numpaso);
		
	}
//	public void obtenerTablaControles(int numpaso,int cantPaquetes, int cantEstados) {
//		tablaControlesDE.devuelveTabla(numpaso,cantPaquetes,cantEstados);
//	}

}
