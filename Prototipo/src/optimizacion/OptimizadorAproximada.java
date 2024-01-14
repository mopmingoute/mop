/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * OptimizadorAproximada is part of MOP.
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

import java.lang.management.ManagementFactory;
import java.util.Calendar;

import pizarron.ClienteHandler;
import pizarron.ServidorHandler;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.ProfilerBasicoTiempo;

public class OptimizadorAproximada<T extends Optimizable> {

	private LineaTiempo ltiempo;
	private int numpaso;
	private Optimizable optimizable;

	
	public OptimizadorAproximada() {
		
	}
	
	public void inicializarOptimizador(LineaTiempo ltiempo, Optimizable optimizable, String dirSal) {
	
		this.ltiempo = ltiempo;
//		this.instanteInicial = ltiempo.getInicial();
//		this.instanteFinal = ltiempo.getLinea().get(ltiempo.getCantidadPasos()-1).getInstanteFinal();
		this.optimizable = optimizable;
// El resoptim se volvóa a crear en inicializar optimizable de OptimizadorPaso
// se saca de acó
//		resoptim = new ResOptimIncrementos(Constantes.PROBINCREMENTOS);
//		ordinalInnovacionesPE = new Hashtable<String, Integer>();
//		cantInnovacionesPE =  new Hashtable<String, Integer>();
//		peMontNoEstadoOptim = new ArrayList<ProcesoEstocastico>(); 
//		peMontEstadoOptim = new ArrayList<ProcesoEstocastico>();
		if (dirSal!=null) {
			inicializarSalidasOpt(dirSal);
		}
	}

	

	public ResOptim optimizar() {	
		
		ltiempo.setSentidoTiempo(-1);
		// Lleva la lónea de tiempo al óltimo paso
		ltiempo.llevarAlFinal();
		
		optimizable.inicializarOptimizable(); 
				
		optimizable.inicializarAzarParaOptimizacion();
						
		ResOptim resoptim = optimizable.devuelveResOptim();	
		
		PasoTiempo paso = ltiempo.devuelvePasoActual();

		numpaso = ltiempo.getNumPaso();
		
		while (numpaso>=0) {
			

			paso = ltiempo.devuelvePasoActual();
			
			optimizable.cargarPasoCorriente(numpaso, paso);
			
			optimizable.determinarInstantesMuestreo();
			
			/**
			 * Prepara los PE para los procesos
			 * Carga instante inicial del paso de la optimización
			 * Carga indAnio del paso
			 */
			optimizable.inicializarPEPasoOptim();
			
			optimizable.sortearInnovPEDE();   	// sortea innovaciones para los PE con VE discretas exhaustivas
												// ATENCION: las innovaciones se usan para sortear valores de las VA
												// no para sortear transiciones, ya que son exhaustivas.
			
			optimizable.sortearInnovMontPEVE();   // sortea innovaciones para PE con VE en la optimización
			
			
			
			optimizable.sortearVAMontPENoVE();  // sortea valores VA para PE sin VE en la optimización				
						
			optimizable.optimizarPasoAproximada();
			
			if (Constantes.NIVEL_CONSOLA > 1) System.out.println("Terminó llamada a optimizable paso " + numpaso);		
			if(numpaso>0){											
				optimizable.actualizarParaPasoAnterior();
				numpaso = ltiempo.getNumPaso();
				// NO HAY QUE LLAMAR retrocederPaso porque eso ya lo hace actualizarParaPasoAnterior()							
			}else{
				numpaso = -1;
			}
			
		}
		
		/**
		 * Se serializan las tablas de valores de Bellman y de control
		 * en disco si estón en memoria
		 */
		
		optimizable.guardarTablasResOptimEnDisco();
		if (Constantes.NIVEL_CONSOLA > 1) System.out.println("Se generó resoptim serializado en disco");
		

		System.out.println("TERMINO LA OPTIMIZACIóN");
		return resoptim; 
	}


//	private ArrayList<double[]> creaGrillaEstados() {
//		// TODO Auto-generated method stub
//		return null;
//	}


//	public Hashtable<String, Integer> getOrdinalDePEDEEnVarsEstadoOptimizacion() {
//		return ordinalDePEDEEnVarsEstadoOptimizacion;
//	}
//
//
//	public void setOrdinalDePEDEEnVarsEstadoOptimizacion(Hashtable<String, Integer> ordinalDePEDEEnVarsEstadoOptimizacion) {
//		this.ordinalDePEDEEnVarsEstadoOptimizacion = ordinalDePEDEEnVarsEstadoOptimizacion;
//	}


	/**
	 * Crea el directorio de salidas de la optimización
	 * @param rutaSals 
	 */
	public void inicializarSalidasOpt(String rutaSals){
		
		//Fecha y hora actual
        Calendar fecha = Calendar.getInstance();
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minuto = fecha.get(Calendar.MINUTE);
        int segundo = fecha.get(Calendar.SECOND);
        

        String dirRaiz = rutaSals;
        String dirNuevo = anio + "-" + mes + "-" + dia + "-" + hora  + "-"  + minuto + "-" + segundo +  "-OPT" + ManagementFactory.getRuntimeMXBean().getName()  ;

        String dirCompleto = dirRaiz + "/" + dirNuevo;
        this.optimizable.setDirSalidas(dirCompleto);
        
        DirectoriosYArchivos.creaDirectorio(dirRaiz, dirNuevo);
	
	}
	
}
