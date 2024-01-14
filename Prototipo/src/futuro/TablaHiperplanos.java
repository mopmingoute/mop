/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaHiperplanos is part of MOP.
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

import estado.VariableEstado;
import logica.CorridaHandler;
import optimizacion.ResOptimHiperplanos;
import optimizacion.ResOptimIncrementos;
import pizarron.PizarronRedis;
import utilitarios.EnumeradorLexicografico;

public abstract class TablaHiperplanos implements Serializable{
	
	private int cantPasos;
	
	
	/**
	 * ATENCIóN AL USO DE ESTOS MóTODOS, PORQUE LA CLAVE TIENE UN SENTIDO DISTINTO SEGóN EL USO.
	 * EN EL COMPORTAMIENTO GLOBAL HIPERPLANOS.
	 * 
	 * En la simulación, se usa devuelveHiperplanos y la clave contiene solo las VE de los PE DE 
	 * y se busca en la tabla tablaHiperplanos, ResOptimHiperplanos, para obtener la colección
	 * de todos los hiperplanos de un paso dada la clave de las VE de los PE DE.
	 * 
	 * En la optimización, en la clase OptimizadorEstado, el uso es anólogo.
	 * 
	 * En la optimización, en la clase OptimizadorPaso
	 * en el mótodo optimizarPaso(),
	 *  - se carga en las tablas DE UN SOLO PASO hipersIniT e hipersFinTmenos1 
	 *    un hiperplano por cada clave de TODAS LAS VE.
	 * 
	 * En la optimización, en la clase OptimizadorPaso:
	 * en el mótodo actualizarParaPasoAnterior()
	 * 	- se usa el mótodo devuelveHiperplanos con la clave total (con el código discreto de todas las VE),
	 * 	  para traer UN HIPERPLANO, que es el ónico en la colección dada la clave.
	 * 
	 *  - se usa el mótodo cargaHiperplano con la clave total para cargar hipersFinTmenos 1
	 *  
	 * 	- se usa el mótodo cargaHiperplano con la clave SóLO DE LAS VE PE DE para cargar tablaHiperplanos
	 *    al fin del paso t-1 (antes del salto de las VE de los procesos estocósticos discretos exhaustivos (PE DE)).  
	 */
	
	
	
	/**
	 * Mótodo para obtener los hiperplanos
	 * en un punto de la grilla de estados al fin del paso.
	 * @param paso
	 * @param clave es el array de enteros con los ordinales del estado de cada variable de estado discreta.
	 * 
	 * El orden de las variables de estado discretas es determinado por el ResOptimHiperplanos
	 * @return
	 */
	public abstract ArrayList<Hiperplano> devuelveHiperplanos(int paso, ClaveDiscreta clave);
	
	
	
	/**
	 * Devuelve el hiperplano que en el paso paso, corresponde al punto de discretización clave,
	 * ATENCIóN: LA CLAVE ES LA CLAVE ENTERA CON TODAS LAS VARIABLES DE ESTADO
	 * Este mótodo se emplea para obtener el hiperplano por un punto (clave de todas las VE) en particular,
	 * entre la colección de todos los hiperplanos con la misma clave de las VE de los PEDE. 
	 */
	public abstract Hiperplano devuelveElHiperplanoDeUnPunto(int paso, ClaveDiscreta clave);
	
	
	
	
	/**
	 * Carga el hiperplano hiper en el paso y le asigna un nómero de hiperplano empezando en cero.
	 * Hay un juego de nómeros diferente para cada claveVEDiscretas
	 * @param paso
	 * @param claveVEDiscretas es la clave entera con los ordinales de las VE discretas.
	 * ATENCIóN: las variables de estado discretas contienen a las VE de procesos estocósticos DE (VE PE DE) pero son un
	 * conjunto mayor, por ejemplo una variable discreta puede ser si la falla con estado estó despachada
	 * los procesos estocósticos DE.
	 * @param claveTotal es el array de enteros con los ordinales de TODAS las VE 
	 * @param hiper el hiperplano a cargar.
	 * 
	 * Si alguna de las claves es nula, el hiperplano no se almacena en la sub-tabla respectiva y no
	 * se podró recuperar por esa clave.
	 * 
	 * En los mótodos para buscar en la tabla
	 * - devuelveHiperplanos usa la primera clave, claveVEDE
	 * - devuelveElHiperplanoDeUnPunto usa la segunda clave, claveTotal
	 */
	public abstract void cargaHiperplano(int paso, ClaveDiscreta claveVEDiscretas, ClaveDiscreta claveTotal, Hiperplano hiper);



	
	

	
	
	/**	
	 * Genera un String con el contenido de la tabla de valores 
	 * @param paso paso del resoptim a imprimir
	 * @param pasoImpresion nómero de paso que debe a aparecer en la impresión
	 * @param resoptim debe el del paso paso, cargado con toda la información
	 * @param varR si es null devuelve valores de Bellman, si no es null devuelve valores del recurso asociado a varR
	 * @return null si se pidió valor de recurso asociado a variable discreta que no es discreta incremental
	 */
	public String publicaUnPasoValoresHiperplanos(int paso, int pasoImpresion, long instanteRef, ResOptimHiperplanos resoptim, VariableEstado varR){
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
			Hiperplano hip = this.devuelveElHiperplanoDeUnPunto(paso, clave);
			if(varR==null){
				// se carga el valor de Bellman
				sb.append(hip.getvBellman());
			}else{
				if(!varR.isDiscreta()){
					// la variable es continua se carga el coeficiente del hiperplano con signo negativo
					sb.append(-hip.getCoefs()[ordinalCont]);
				}else{
					System.out.println("Error: se pidió el valor del recurso de " + varR.getNombre() + " que no es continua");
					if (CorridaHandler.getInstance().isParalelo()){
						//PizarronRedis pp = new PizarronRedis();
						//pp.matarServidores();
					}
					System.exit(1);
				}				
			}
			sb.append("\n");
			vt = enuml.devuelveVector();	
		}
		return sb.toString();
	}
	

	public int getCantPasos() {
		return cantPasos;
	}




	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}
	
	
	


}