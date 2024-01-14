/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResOptimHiperplanos is part of MOP.
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
import java.util.Hashtable;

import control.VariableControlDE;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesResOptim.DatosHiperplano;
import datatypesResOptim.DatosTablaControlesDE;
import datatypesResOptim.DatosTablaHiperplanos;
import datatypesResOptim.DatosTablaVByValRec;
import estado.VariableEstado;
import futuro.AFHiperplanos;
import futuro.ClaveDiscreta;
import futuro.Hiperplano;
import futuro.TablaControlesDEMemoria;
import futuro.TablaHiperplanos;
import futuro.TablaHiperplanosMemoria;
import futuro.TablaVByValRecursosMemoria;
import logica.CorridaHandler;
import persistencia.EscritorHiperplanos;
import pizarron.PizarronRedis;
import utilitarios.AproximadorMinimosCuadros;
import utilitarios.DirectoriosYArchivos;
import utilitarios.EnumeradorLexicografico;

public class ResOptimHiperplanos extends ResOptim{
	
	
	
	private TablaHiperplanos tablaHiperplanos;	
	private ClaveDiscreta claveVEDiscretas;	
	private int[] contadorHiperplanos;	
	

	
	public ResOptimHiperplanos(){}		
	
	public ResOptimHiperplanos(int cantPasos){		
		super(cantPasos);
		tablaHiperplanos = new TablaHiperplanosMemoria(cantPasos);

		setContadorHiperplanos(new int[cantPasos]);
//		super.completaConstruccion();			
	}	
	
	
	
	/**
	 * 
	 * Devuelve los hiperplanos del paso de tiempo, teniendo en cuenta
	 * los valores de las variables de estado discretas, almacenadas en el ResOptim
	 * Cada una de las VE discretas ya deben tener cargado sus valores. 
	 * 
	 * Las VE discretas se toman en el orden en que aparecen en la colección de VE
	 * del ResOptim, para formar la clave de acceso a la TablaHiperplanos
	 * 
	 * 
	 * @param paso
	 */
	public AFHiperplanos devuelveAproxFuturaHiperplanos(int paso){
		int[] enteros = new int[this.getCantVEDisNoInc()];		
		for(int ive=0; ive<this.getCantVEDisNoInc(); ive++){
			enteros[ive]=this.getVarsEstadoDisNoInc().get(ive).getEstado().intValue();
		}		
		claveVEDiscretas = new ClaveDiscreta(enteros);		
		ArrayList<String> nombresVEContEnDespacho = new ArrayList<String>();
		for(VariableEstado vec: this.getVarsEstadoContinuas()){
			String nombreEnDespacho = this.getVariableDespachoDeVEContinua().get(vec.getNombre());
			nombresVEContEnDespacho.add(nombreEnDespacho);
		}
		ArrayList<Hiperplano> alh = tablaHiperplanos.devuelveHiperplanos(paso, claveVEDiscretas);
		return new AFHiperplanos(alh, nombresVEContEnDespacho);
	}
	
	
	
	public void inicializaResOptimParaNuevoPaso(int numpaso, int instante, ArrayList<VariableEstado> varsEstado,
			ArrayList<VariableControlDE> varsControlDE){	
		
		super.inicializaResOptimParaNuevoPaso(numpaso, instante, varsEstado, varsControlDE);
		
	}


	/**
	 * Crea un ónico hiperplano idónticamente nulo para cada estado de las variables discretas 
	 * para ser usado como valor de fin de juego
	 */
	public void cargaHiperplanosFinales() {
		int cantVD = this.getCantVEDisNoInc();
		int pasoFinal = this.getCantPasos()-1;
		int[] cotasInferioresEsta = new int[cantVD];
		int[] cotasSuperioresEsta = new int[cantVD];		
		for(int ic=0; ic<cantVD; ic++){
			cotasInferioresEsta[ic]=0;
			cotasSuperioresEsta[ic]= this.getVarsEstadoDisNoInc().get(ic).cantValoresPosibles(this.getInstanteRef())-1;
		}
		EnumeradorLexicografico enumL = new EnumeradorLexicografico(cantVD, cotasInferioresEsta, cotasSuperioresEsta);		
		int[] vec = enumL.devuelveVector();

		ArrayList<Hiperplano> hiperplanosFinales = new ArrayList<Hiperplano>(); 
		double vmax = 75.0E6; // valor del agua con lago vacóo
		double vmin = 1.0E6;  // valor del agua con lago lleno
		double xmax = 8200.0E6; // stock móximo en hm3
		double xmin = 0.0;    // stock mónimo en hm3
		double alfa = 2.6; // exponente de la función potencial de VB
		int cantHiper = 10; // cantidad de puntos en los que se crearón hiperplanos finales
		for(int i=0; i<cantHiper; i++){
			double xi = xmin + i*(xmax-xmin)/(cantHiper-1);
			double bi = - alfa*vmax*Math.pow((xmax-xi)/xmax, alfa-1)/xmax;
			double vbi = vmax*Math.pow((xmax-xi)/xmax, alfa);
			Hiperplano hip = new Hiperplano(1, this.getPasoCorriente(), i);
			hip.getCoefs()[0] = bi;
			hip.setTind(vbi-bi*xi);
			hip.setvBellman(vbi);
			hip.getPunto()[0]=xi/1e6;
			hiperplanosFinales.add(hip);
		}
	
		while(vec != null){
			ClaveDiscreta cd = new ClaveDiscreta(vec);
			for(Hiperplano hip: hiperplanosFinales){
				tablaHiperplanos.cargaHiperplano(pasoFinal, cd, null, hip);
			}
			vec = enumL.devuelveVector();
		}

	}
	
	
	/**	 
	 * Dado un conjunto de hiperplanos H={hi}, cada uno con su punto de tangencia pi, 
	 * devuelve para cada hiperplano hi la cantidad vi de puntos j de tangencia tales que
	 * hi(pj)>hj(pj)  y el conjunto de los hiperplanos hj que ocasionan ese error del hi
	 * Los hiperplanos no se afectan
	 * Si el hipervolumen por encima de los hiperplanos es convexo, vi debe ser nulo para todo i
	 */
	public static VerificacionConvexidadHiper verificaConvexidad(ArrayList<Hiperplano> listaHips){
		ArrayList<Integer> cuentaErrores = new ArrayList<>();
		ArrayList<ArrayList<Double>> hiMenosHj = new ArrayList<ArrayList<Double>>();
		VerificacionConvexidadHiper verif = new VerificacionConvexidadHiper();
		for(Hiperplano hi: listaHips){
			ArrayList<Double> difs1Hip = new ArrayList<Double>();
			int cuenta1 = 0;
			for(Hiperplano hj: listaHips){
				double dif=0.0;
				if(hi!=hj){
					dif = hi.valor(hj.getPunto())-hj.getvBellman();
					if(dif>0) cuenta1 ++;					
				}
				difs1Hip.add(dif);
			}
			cuentaErrores.add(cuenta1);
			hiMenosHj.add(difs1Hip);
		}		
		verif.setCuentaErroresPorH(cuentaErrores);
		verif.setHiMenosHj(hiMenosHj);
		return verif;
	}
	
	
	
	/**
	 * Carga un hiperplano en la tablaHiperplanos
	 * @param numpaso paso de la tabla en que se carga el hiperplano
	 * @param claveVEDiscretas clave de las VE discretas
	 * @param claveTotal clave de todas las VE del paso
	 * 
	 */
	public void cargaHiperplano(int numpaso, ClaveDiscreta claveVEDiscretas, ClaveDiscreta claveTotal, Hiperplano hiper){
		tablaHiperplanos.cargaHiperplano(numpaso, claveVEDiscretas, claveTotal, hiper);
	}


//	/**
//	 * A partir de la tabla OptimizadorPaso.hipersFinTmenos1 que para cada estado discreto total
//	 * da el ónico hiperplano antes del salto, construye una etapa de la tabla
//	 * tablaHiperplanos, que da todos los hiperplanos de un paso dada la clave de las VE de los PE DE.
//	 * @param numpaso
//	 */
//	public void cargaTablaHiperplanosFinTmenos1(int numpaso, TablaHiperplanos hipersFinTmenos1) {
//		int[] cotasInferiores = new int[this.getCantVEDisNoInc()];
//		int[] cotasSuperiores = new int[this.getCantVEDisNoInc()];
//		int ive=0;
//		for(VariableEstado vd: this.getVarsEstadoDisNoInc()){
//			cotasInferiores[ive]=0;
//			cotasSuperiores[ive]=vd.getEvolDiscretizacion().getValor(this.getInstanteRef()).getCantValores()-1;
//		}
//		EnumeradorLexicografico enumEstadosVEDisc = new EnumeradorLexicografico(this.getCantVEDisNoInc(), cotasInferiores, cotasSuperiores);
//		enumEstadosVEDisc.inicializaEnum();
//		int[] vdis = enumEstadosVEDisc.devuelveVector();
//		while (vdis != null) {
//			ClaveDiscreta clave = new ClaveDiscreta(vdis);
//			ArrayList<Hiperplano> alH = hipersFinTmenos1.devuelveHiperplanos(0, clave);
//			for(Hiperplano hip: alH){
//				tablaHiperplanos.cargaHiperplano(numpaso, clave, null, hip);
//			}
//			vdis = enumEstadosVEDisc.devuelveVector();
//		}
//	}

	


	/**
	 * Graba en el directorio dirSalidas las tablas de valores VB y derivadas y de controles
	 * @param dirSalidas
	 */
	public void guardarTablasResOptimEnDisco(String dirSalidas) {
		
		try {
			String nombreArch = "TablaHiperplanos";
			DatosTablaHiperplanos dt = ((TablaHiperplanosMemoria)this.getTablaHiperplanos()).creaDataType();
			System.out.println("para antes de serializar tabla hiperplanos");
			utilitarios.ManejaObjetosEnDisco.guardarEnDisco(dirSalidas, nombreArch, dt);
			nombreArch = "TablaControlesDE";
			DatosTablaControlesDE dc = ((TablaControlesDEMemoria)this.getTablaControlesDE()).creaDataType();
			utilitarios.ManejaObjetosEnDisco.guardarEnDisco(dirSalidas, nombreArch, dc);			
		} catch (Exception e) {
			System.out.println("Error en la serialización del resoptim");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(0);
		}
	}



	public TablaHiperplanos getTablaHiperplanos() {
		return tablaHiperplanos;
	}



	public void setTablaHiperplanos(TablaHiperplanos tablaHiperplanos) {
		this.tablaHiperplanos = tablaHiperplanos;
	}

	public int[] getContadorHiperplanos() {
		return contadorHiperplanos;
	}

	public void setContadorHiperplanos(int[] contadorHiperplanos) {
		this.contadorHiperplanos = contadorHiperplanos;
	}

	/**
	 * Genera un texto con la descripción de los hiperplanos del paso numpaso
	 * enumerados punto a punto de la discretización de estados
	 * 
	 * @param numpaso
	 * @return
	 */
	public String publicaHiperplanosPorPuntos(int numpaso) {	
		ArrayList<VariableEstado> veTotal = this.getVarsEstadoCorrientes();
		ArrayList<VariableEstado> veCont = this.getVarsEstadoContinuas();
		EnumeradorLexicografico enumL = this.getEnumLexEstados();
		EscritorHiperplanos escritorHip = new EscritorHiperplanos();
		enumL.inicializaEnum();
		StringBuilder sb = new StringBuilder("HIPERPLANOS AL FIN DEL PASO " + numpaso + "\n");
		int[] vec = enumL.devuelveVector();
		boolean inicio = true;
		ArrayList<Hiperplano> hiperplanos = new ArrayList<Hiperplano>();
		while(vec!= null){
			ClaveDiscreta clave = new ClaveDiscreta(vec);
			ClaveDiscreta claveVEDis = this.claveVEDiscretasDeClaveTotal(clave);
			Hiperplano h = tablaHiperplanos.devuelveElHiperplanoDeUnPunto(numpaso, clave);
			hiperplanos.add(h);
			DatosHiperplano dh = h.creaDataType();
			if(inicio==true){
				sb.append(escritorHip.imprimeTitulosHiperplanos(veCont.size(), veTotal, veCont));
				inicio = false;
			}			
			sb.append(escritorHip.imprimeHiperplano(dh, claveVEDis));
			vec = enumL.devuelveVector();
		}
		return sb.toString();
	}



	/**
	 * Genera un texto con la descripción de los hiperplanos del paso numpaso
	 * enumerados a partir de los valores de las VE discretas. Para cada valor de las VE discretas
	 * hay una lista de hiperplanos
	 * 
	 * @param numpaso paso en la tabla
	 * @param pasoImpresion nómero de paso que aparece en el String de salids
	 * @param veTotal lista de todas las VE
	 * @param veContinuas lista de las VE continuas
	 * @return
	 */
	public String publicaHiperplanosPorVEDiscretas(int numpaso, int pasoImpresion, String titulo, TablaHiperplanos tHiper) {
		long instante = this.getInstanteRef();
		ArrayList<VariableEstado> veTotal = this.getVarsEstadoCorrientes();
		ArrayList<VariableEstado> veCont = this.getVarsEstadoContinuas();
		ArrayList<VariableEstado> veDisc = this.getVarsEstadoDisNoInc();
		int cantVEDisc = this.getCantVEDisNoInc();
		int[] cotasInf = new int[cantVEDisc];
		int[] cotasSup = new int[cantVEDisc];
		int i=0;
		for(VariableEstado ved: veDisc){
			cotasInf[i] = 0;
			cotasSup[i] = ved.getEvolDiscretizacion().getValor(instante).getCantValores()-1;
		}
		EnumeradorLexicografico enumL = new EnumeradorLexicografico(cantVEDisc, cotasInf, cotasSup);
		enumL.inicializaEnum();
		EscritorHiperplanos escritorHip = new EscritorHiperplanos();
		StringBuilder sb = new StringBuilder("====================================================\n");
		sb.append(titulo + pasoImpresion + "\n");
		sb.append("====================================================\n");
		int[] vec = enumL.devuelveVector();
		while(vec!= null){
			boolean inicio = true;
			ClaveDiscreta claveVEDis = new ClaveDiscreta(vec);
			ArrayList<Hiperplano> alh = tHiper.devuelveHiperplanos(numpaso, claveVEDis);
			sb.append("CLAVE DISCRETA \t");
			for(int ic=0; ic<vec.length; ic++){
				sb.append(vec[ic] + "\t");
			}
			sb.append("\n");
			for(Hiperplano h: alh){
				DatosHiperplano dh = h.creaDataType();
				if(inicio==true){
					sb.append(escritorHip.imprimeTitulosHiperplanos(veCont.size(), veTotal, veCont));
					inicio = false;
				}
				sb.append(escritorHip.imprimeHiperplano(dh, claveVEDis));
			}
			sb.append("Verificación de convexidad\n");
			sb.append(verificaConvexidad(alh).imprimir());
			sb.append("\n");
			vec = enumL.devuelveVector();
		}
		return sb.toString();
	}



	
	
	
}
