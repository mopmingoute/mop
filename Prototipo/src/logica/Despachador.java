
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Despachador is part of MOP.
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

package logica;

import java.util.Collection;
import java.util.Hashtable;

import compdespacho.CompDespacho;
import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import optimizacion.Optimizador;
import parque.Corrida;
import parque.Participante;
import problema.ProblemaHandler;
import utilitarios.Par;
//import utilitarios.ProfilerBasicoTiempo;

/**
 * Clase encargada de realizar el despacho de un paso de tiempo
 * 
 * @author ut602614
 *
 */

public class Despachador {

	DatosEntradaProblemaLineal entrada;
	DatosSalidaProblemaLineal salida;

	public Despachador() {
		entrada = new DatosEntradaProblemaLineal();
	}

	/**
	 *  
	 * @param corridaActual
	 * @param paso
	 * @param escenario
	 * @param dirInfactible
	 *            nombre del archivo donde se guarda el problema si es
	 *            infactible
	 * @return dirEntradasPL si es !=null es el nombre del archivo donde se
	 *         guarda la entrada del problema (volcado del objeto Java)
	 * @return dirSalidasPL si es !=null es el nombre del archivo donde se
	 *         guarda la salida del resolvedor.
	 */
	public DatosSalidaProblemaLineal despachar(Corrida corridaActual, int paso, int escenario, String dirInfactible,
			String dirEntradasPL, String dirSalidasLP) {
		
		Collection<Participante> participantes = corridaActual.getParticipantesDirectos();
	//	Optimizador.prof.iniciarContador("cargado_rest_y_objetivo_fuera_resolvedor");
		 
		entrada = new DatosEntradaProblemaLineal();
		for (Participante p : participantes) {
			CompDespacho c = p.getCompDesp();
			if (c != null) {
				c.resetearVariablesControl();
				c.crearVariablesControl();
			}
		}

		for (Participante p : participantes) {
			CompDespacho c = p.getCompDesp();
			if (c != null) {
				c.resetearRestricciones();
				c.resetearObjetivo();
				c.cargarRestricciones();
				c.contribuirObjetivo();

			
				Hashtable<String, DatosRestriccion> rest = c.getRestricciones();
				entrada.agregarRestricciones(rest);
				entrada.contribuirObjetivo(c.getContribucionObjetivo());
				entrada.agregarVariables(c.getVariablesControlArray());
			}
		}
	//	entrada.imprimir();
		int itCambioRes=0;
		do {
			

			// TODO: if corrida.tipoResolucion == hiperplanos entonces llamar
			// hiperplanista para que devuelva restricciones y contribución al
			// objetivo

			ProblemaHandler ph = ProblemaHandler.getInstance();

			
//			if (dirEntradasPL != null){
//				entrada.guardar(dirEntradasPL);
//				entrada.imprimir();
//				entrada.imprimirVariables();
//			}
			
			
		
			
			//Optimizador.prof.pausarContador("cargado_rest_y_objetivo_fuera_resolvedor");
			//Optimizador.prof.iniciarContador("resolvedor");
		//	entrada.imprimir();
			salida = ph.resolver(entrada, escenario, paso, dirInfactible, dirSalidasLP);
			//Optimizador.prof.pausarContador("resolvedor");
			//Optimizador.prof.iniciarContador("cargado_rest_y_objetivo_fuera_resolvedor");
			for (Participante p: participantes) {
				p.setUltimoInfactible(salida.isInfactible());
			} 

//			if(escenario==110) {
//				entrada.imprimir("d:/salidasProb/entradaProblemaImpacto + " + paso +".lp");
//				salida.printSolucion("d:/salidasProb/problemaImpacto"+ paso + ".lp");
//			} 
			if (salida.isInfactible()) { 
				System.out.println("SE RELAJAN LOS BALANCES DEL HIDRÁULICO PASO "+ paso + ", ESCENARIO: " + escenario);
				//entrada.imprimir(); 
				if(itCambioRes>0){
					System.out.println("CAMBIO DE RESOLVEDOR EN PASO: "+ paso + ", ESCENARIO: " + escenario);
					ph.cambiarResolvedor();
					itCambioRes=0;
				}
				itCambioRes++;
			}

		} while (salida.isInfactible());
		
		ProblemaHandler ph = ProblemaHandler.getInstance();
		ph.resetearResolvedor();
		//Optimizador.prof.pausarContador("cargado_rest_y_objetivo_fuera_resolvedor");
		return salida;

	}

	public DatosEntradaProblemaLineal getEntrada() {
		return entrada;
	}

	public void setEntrada(DatosEntradaProblemaLineal entrada) {
		this.entrada = entrada;
	}

	public DatosSalidaProblemaLineal getSalida() {
		return salida;
	}

	public void setSalida(DatosSalidaProblemaLineal salida) {
		this.salida = salida;
	}
	
	
	

}
