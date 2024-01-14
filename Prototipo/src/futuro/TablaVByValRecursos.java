/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaVByValRecursos is part of MOP.
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

package futuro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import estado.VariableEstado;
import optimizacion.ResOptim;
import optimizacion.ResOptimIncrementos;
import parque.Corrida;
import pizarron.Paquete;
import tiempo.LineaTiempo;
import utilitarios.EnumeradorLexicografico;

/**
 * @author ut469262
 * 
 * Almacena los valores de Bellman y los valores de los recursos
 * LOS VALORES DE LOS RECURSOS SON POSITIVOS, ES DECIR SON LOS 
 * OPUESTOS DE LAS DERIVADAS PARCIALES.
 *
 */
public abstract class TablaVByValRecursos implements Serializable {
	
	
	private int cantPasos;
	
	
	/**
	 * Mótodo para obtener los datos de VBellman, derivadas parciales e incrementos
	 * en un punto de la grilla de estados al fin del paso.
	 * @param paso
	 * @param clave contiene array de enteros con los ordinales del estado discretizado de cada variable de estado.
	 * @return
	 */
	public abstract InformacionValorPunto devuelveInfoValoresPunto(int paso, ClaveDiscreta clave);
	
	
	
	/**
	 * Mótodo para obtener los datos de VBellman en un punto de la grilla de estados al fin del paso.
	 * @param paso
	 * @param clave es el array de enteros con los ordinales del estado discretizado de cada variable de estado.
	 * @return
	 */
	public abstract double devuelveValorVBPunto(int paso, ClaveDiscreta clave);
	
	
	
	/**
	 * Carga la InformacionValorPunto infvp (que tiene un valor de Bellman, las derivadas parciales y los incrementos)
	 * asociadas al código discreto clave, en el paso paso.
	 * Si ya existe un InformacionValorPunto con esa clave lo sobreescribe.
	 * @param paso
	 * @param clave
	 * @param infvp
	 */
	public abstract void cargaInfoValoresPunto(int paso, ClaveDiscreta clave, InformacionValorPunto infvp);



	public int getCantPasos() {
		return cantPasos;
	}



	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}
		
		
	/**	
	 * Genera un String con el contenido de la tabla de valores 
	 * @param paso paso del resoptim a imprimir
	 * @param pasoImpresion nómero de paso que debe a aparecer en la impresión
	 * @param resoptim debe el del paso paso, cargado con toda la información
	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
	 * @param auxiliar 
	 * @return null si se pidió valor de recurso asociado a variable discreta que no es discreta incremental
	 */
	public String publicaUnPasoValoresIncrementos(int paso, int pasoImpresion, int instanteRef, ResOptimIncrementos resoptim, VariableEstado varR, boolean auxiliar){
		String titulo;
		int ordinalCont = -1;
		int ordinalDE = -1;
		ArrayList<VariableEstado> varsEstado;
		varsEstado = resoptim.getVarsEstadoCorrientes();
		EnumeradorLexicografico enuml = resoptim.getEnumLexEstados();
		if(varR!=null && varR.isDiscreta() && !varR.isDiscretaIncremental()){
			// varR es discreta no incremental no se hace nada
			System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
					+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
			return null;
		}
	
		if(varR==null){
			titulo = "Valores de Bellman \n";
		}else{
			int ordinalVE = resoptim.getOrdinalDeVEEnVarsEstadoCorrientes().get(varR.getNombre());	
			titulo = "Valores del recurso asociado a " + varR.getNombre(); 
			if(!varR.isDiscreta()){
				// la variable es continua, se halla su ordinal entre las continuas
				ordinalCont = resoptim.getOrdinalEnEnumDeContinuas().get(ordinalVE);
			}else if(varR.isDiscretaIncremental()){
				// la variable es discreta incremental, se halla su ordinal entre las discretas incrementales 
				ordinalDE = resoptim.getOrdinalEnInfoPuntoDeDiscretasIncr().get(ordinalVE);
			}else{
				System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
						+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
			}
		}
		int cantVE = varsEstado.size();
		StringBuilder sb = new StringBuilder();
		sb.append(titulo);
		sb.append("\n");
		sb.append("PASO = " + pasoImpresion + "\n");	
		sb.append("NOMBRES VE");
		sb.append("\t");
		for(int ive=0; ive<cantVE; ive++){
			sb.append(varsEstado.get(ive).getNombre());
			sb.append("\t");
		}
		sb.append("\n");
		sb.append("CANT.VALORES");
		sb.append("\t");
		for(int ive=0; ive<cantVE; ive++){
			sb.append(varsEstado.get(ive).devuelveDiscretizacion(instanteRef).getCantValores());
			sb.append("\t");
		}		
		sb.append("\n");
		sb.append("Código estado");
		sb.append("\n");
		
		enuml.inicializaEnum();
		int[] vt = enuml.devuelveVector();
		while(vt!=null){
			for(int ive=0; ive<cantVE; ive++){
				sb.append(vt[ive]);
				sb.append("\t");
			}
			ClaveDiscreta clave = new ClaveDiscreta(vt);
			InformacionValorPunto ivp;
			if (auxiliar)
				ivp = this.devuelveInfoValoresPuntoAuxiliar(paso, clave);
			else
				ivp = this.devuelveInfoValoresPunto(paso, clave);
			if(varR==null){
				// se carga el valor de Bellman
				sb.append(ivp.getValorVB());
			}else{
				if(!varR.isDiscreta()){
					// la variable es continua se carga la derivada parcial
					sb.append(ivp.getDerivadasParciales()[ordinalCont]);
				}else if(varR.isDiscretaIncremental()){
					double[] incrementos = ivp.getIncrementosValor()[ordinalDE];
					// la variable es discreta incremental se cargan los incrementos
					for(int id=0; id<incrementos.length; id++){
						sb.append(incrementos[id]);
						sb.append("\t");
					}
				}				
			}
			sb.append("\t");
			vt = enuml.devuelveVector();	
		}
		return sb.toString();
	}

	/**
	 * Genera un String con el encabezado de una tabla de valores de Bellman o tabla de recursos
	 * con el formato de una línea por paso con todos los estados en la línea (V2)  
	 * @param resoptim
	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
	 * @param unidad la unidad de los valores que se publican
	 * @param instanteRef el instante usado para determinar la discretización de las variables de estado.
	 * @return
	 */
	
	public String publicaEncabezadoTablaValoresIncrementosV2(ResOptimIncrementos resoptim, VariableEstado varR, String unidad, long instanteRef){
		StringBuilder sb = new StringBuilder();
		
		if(varR==null){
			sb.append("Valores de Bellman en USD");
		}else{
			sb.append("Valores del recurso " + varR.getNombre() + " en " + unidad);
		}
		sb.append("\n");
		EnumeradorLexicografico enumLex = resoptim.getEnumLexEstados();
		enumLex.inicializaEnum();
		int cantEst = enumLex.getCantTotalVectores();
		sb.append("Cantidad de vars. estado\t");
		sb.append(resoptim.getCantVE());
		sb.append("\n");
		sb.append("Cantidad de estados\t");
		sb.append(cantEst);
		ArrayList<StringBuilder> lineas = new ArrayList<StringBuilder>();
		ArrayList<VariableEstado> variables = resoptim.getVarsEstadoCorrientes();
		for(int i=0; i<resoptim.getCantVE(); i++){
			lineas.add(new StringBuilder(variables.get(i).getNombre()+"\t"+variables.get(i).getEvolDiscretizacion().getValor(instanteRef).getCantValores()+"\t"));
		}
		int[] vt = enumLex.devuelveVector();
		while(vt!= null){
			for(int i=0; i<resoptim.getCantVE(); i++){				
				lineas.get(i).append(vt[i]);
				lineas.get(i).append("\t");
			}
			vt = enumLex.devuelveVector();
		}
		StringBuilder sbtot = new StringBuilder(sb);
		sbtot.append("\n");
		for(int i=0; i<resoptim.getCantVE(); i++){
			sbtot.append(lineas.get(i).toString());
			sbtot.append("\n");
		}
		return sbtot.toString();
	}
	

	/**	
	 * Genera un String con el contenido de la tabla de valores de un paso
	 * con la forma de una línea por paso V2
	 * @param paso paso del resoptim a imprimir
	 * @param pasoImpresion nómero de paso que debe a aparecer en la impresión
	 * @param resoptim debe el del paso paso, cargado con toda la información
	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
	 * @param auxiliar 
	 * @return null si se pidió valor de recurso asociado a variable discreta que no es discreta incremental
	 */
	public String publicaUnPasoValoresIncrementosV2(int paso, int pasoImpresion, long instanteRef, ResOptimIncrementos resoptim, VariableEstado varR, boolean auxiliar, Corrida corrida, String unidad){
		String encabezado = "";
		if(resoptim.isCambioCantVE() || resoptim.isInicio()){
			encabezado = publicaEncabezadoTablaValoresIncrementosV2(resoptim, varR, unidad, instanteRef);			
		}
		int ordinalCont = -1;
		int ordinalDE = -1;
		ArrayList<VariableEstado> varsEstado;
		varsEstado = resoptim.getVarsEstadoCorrientes();
		EnumeradorLexicografico enuml = resoptim.getEnumLexEstados();
		if(varR!=null && varR.isDiscreta() && !varR.isDiscretaIncremental()){
			// varR es discreta no incremental no se hace nada
			System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
					+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
			return null;
		}
	
		if(varR!=null){			
			int ordinalVE = resoptim.getOrdinalDeVEEnVarsEstadoCorrientes().get(varR.getNombre());	
			if(!varR.isDiscreta()){
				// la variable es continua, se halla su ordinal entre las continuas
				ordinalCont = resoptim.getOrdinalEnEnumDeContinuas().get(ordinalVE);
			}else if(varR.isDiscretaIncremental()){
				// la variable es discreta incremental, se halla su ordinal entre las discretas incrementales 
				ordinalDE = resoptim.getOrdinalEnInfoPuntoDeDiscretasIncr().get(ordinalVE);
			}else{
				System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
						+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
			}
		}
		
		StringBuilder sb = new StringBuilder(encabezado);
		LineaTiempo lt = corrida.getLineaTiempo();
		sb.append(pasoImpresion + "\t");
		sb.append(lt.fechaYHoraDeInstante(instanteRef));
		sb.append("\t");

		enuml.inicializaEnum();
		int[] vt = enuml.devuelveVector();
		while(vt!=null){
			ClaveDiscreta clave = new ClaveDiscreta(vt);
			InformacionValorPunto ivp;
			if (auxiliar)
				ivp = this.devuelveInfoValoresPuntoAuxiliar(paso, clave);
			else
				ivp = this.devuelveInfoValoresPunto(paso, clave);
			if(varR==null){
				// se carga el valor de Bellman
				sb.append(ivp.getValorVB());
			}else{
				if(!varR.isDiscreta()){
					// la variable es continua se carga la derivada parcial
					sb.append(ivp.getDerivadasParciales()[ordinalCont]);
				}else if(varR.isDiscretaIncremental()){
					double[] incrementos = ivp.getIncrementosValor()[ordinalDE];
					// la variable es discreta incremental se cargan los incrementos
					for(int id=0; id<incrementos.length; id++){
						sb.append(incrementos[id]);
						sb.append("\t");
					}
				}				
			}			
			vt = enuml.devuelveVector();
			sb.append("\t");
		}
		return sb.toString();
	}
	
	public String publicaUnPasoValoresIncrementosV2ML(int paso, int pasoImpresion, long instanteRef, ResOptimIncrementos resoptim, VariableEstado varR, boolean auxiliar, Corrida corrida, String unidad){
//		String encabezado = "";
//		if(resoptim.isCambioCantVE() || resoptim.isInicio()){
//			encabezado = publicaEncabezadoTablaValoresIncrementosV2(resoptim, varR, unidad, instanteRef);			
//		}
		int ordinalCont = -1;
		int ordinalDE = -1;
		ArrayList<VariableEstado> varsEstado;
		varsEstado = resoptim.getVarsEstadoCorrientes();
		EnumeradorLexicografico enuml = resoptim.getEnumLexEstados();
		if(varR!=null && varR.isDiscreta() && !varR.isDiscretaIncremental()){
			// varR es discreta no incremental no se hace nada
			System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
					+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
			return null;
		}
	
		if(varR!=null){			
			int ordinalVE = resoptim.getOrdinalDeVEEnVarsEstadoCorrientes().get(varR.getNombre());	
			if(!varR.isDiscreta()){
				// la variable es continua, se halla su ordinal entre las continuas
				ordinalCont = resoptim.getOrdinalEnEnumDeContinuas().get(ordinalVE);
			}else if(varR.isDiscretaIncremental()){
				// la variable es discreta incremental, se halla su ordinal entre las discretas incrementales 
				ordinalDE = resoptim.getOrdinalEnInfoPuntoDeDiscretasIncr().get(ordinalVE);
			}else{
				System.out.println("Se pidió en TablaVByValRecursos el valor asociado "
						+ "a la variable de estado discreta exhaustiva " + varR.getNombre());
			}
		}
		
		StringBuilder sb = new StringBuilder();
		LineaTiempo lt = corrida.getLineaTiempo();
		String pasoAnio = Integer.toString(paso-lt.dameNumeroPasoInicioAnio(lt.getAnioPaso(paso)));
		
		if (paso == lt.getLinea().size()-2) {
			if (varR==null) {
				sb.append("VBellman,Paso,PasoAnio");
			} else {
				sb.append("VRecurso,Paso,PasoAnio");
			}
			
			ArrayList<VariableEstado> variables = resoptim.getVarsEstadoCorrientes();
			for(int i=0; i<resoptim.getCantVE(); i++){
				sb.append("," +variables.get(i).getNombre());
			}
			sb.append("\n");
		}
		enuml.inicializaEnum();
		int[] vt = enuml.devuelveVector();
		while(vt!=null){
			ClaveDiscreta clave = new ClaveDiscreta(vt);
			InformacionValorPunto ivp;
			if (auxiliar)
				ivp = this.devuelveInfoValoresPuntoAuxiliar(paso, clave);
			else
				ivp = this.devuelveInfoValoresPunto(paso, clave);
			if(varR==null){
				// se carga el valor de Bellman
				sb.append(ivp.getValorVB()+",");
				sb.append(pasoImpresion + ",");				
				sb.append(pasoAnio +",");
				for (int i =0 ; i<clave.getEnterosIndices().length; i++ ) {
					sb.append(clave.getEnterosIndices()[i]);
					if (i<clave.getEnterosIndices().length-1) {
						sb.append(",");
					}
				}
				
			}else{
				if(!varR.isDiscreta()){
					// la variable es continua se carga la derivada parcial
					sb.append(ivp.getDerivadasParciales()[ordinalCont]+",");
					sb.append(pasoImpresion + ",");				
					sb.append(pasoAnio +",");
					//System.out.println(pasoAnio);
					for (int i =0 ; i<clave.getEnterosIndices().length; i++ ) {
						sb.append(clave.getEnterosIndices()[i]);
						if (i<clave.getEnterosIndices().length-1) {
							sb.append(",");
						}
					}
				}else if(varR.isDiscretaIncremental()){
					double[] incrementos = ivp.getIncrementosValor()[ordinalDE];
					// la variable es discreta incremental se cargan los incrementos
					for(int id=0; id<incrementos.length; id++){
						sb.append(incrementos[id]);
						sb.append(pasoImpresion + ",");				
						sb.append(pasoAnio +",");
						for (int i =0 ; i<clave.getEnterosIndices().length; i++ ) {
							sb.append(clave.getEnterosIndices()[i]);
							if (i<clave.getEnterosIndices().length-1) {
								sb.append(",");
							}
						}
					}
				}				
			}			
			vt = enuml.devuelveVector();
			sb.append("\n");			
		}
		if(sb.length() > 0 )
	          sb.deleteCharAt( sb.length() - 1 );
		return sb.toString();
	}
	
	
	public abstract void cargaInfoValoresPuntoAuxiliar(int i, ClaveDiscreta clave, InformacionValorPunto ivp);



	public abstract InformacionValorPunto devuelveInfoValoresPuntoAuxiliar(int i, ClaveDiscreta clave);
	

	public abstract void cargaTablaAuxiliar(int paso);
	
	public abstract void cargaTabla(int paso);
	
	public abstract void devuelveTablaAuxiliar(int paso, int cantPaquetes, int cantEstados);
	
	public abstract void devuelveTabla(int paso);



	public abstract boolean compatiblePaquete(Paquete paq, ResOptim resoptim);


	


}
