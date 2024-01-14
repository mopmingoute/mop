/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * SimuladorAproximada is part of MOP.
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

package simulacion;

import java.util.Calendar;

import datatypesResOptim.DatosResOptimIncrementos;
import datatypesSalida.DatosEPPResumen;
import logica.CorridaHandler;
import optimizacion.ResOptim;
import persistencia.CargadorResOptimEDF;
import pizarron.ClienteHandler;
import pizarron.ServidorHandler;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

public class SimuladorAproximada<T extends Simulable> {
	private LineaTiempo ltiempo;
	
	private int cantEscenarios;
	private long instanteInicial;
	private long instanteFinal;
	private long instanteInicialEstudio;
	private boolean encadenado;
	private int numpaso;
	private boolean resoptimExterno;
	private String dirSalidasSim; // directorio de las salidas de la simulación
	
	  
	private ResOptim resoptim;
	//Cuando se haga la paralelización es probable que se tenga una colección de simulables	
	private Simulable simulable;
	
	
	DatosEPPResumen datosResumen;


	

	/**
	 * El comportamiento en un paso corresponde al vólido en el instante inicial de ese paso segón las evoluciones
	 * de los comportamientos. En el instante inicial del paso ya estó hecha la reducción o ampliación del conjunto de variables de estado.
	 * @param resoptim2 
	 */
	public SimuladorAproximada() {	
	
	}

	public void inicializarSimulador(LineaTiempo lt, ResOptim resoptim2, int cantEscenarios, T simulable,
			boolean encadenado, boolean resoptimExterno, String rutaSals) {

		this.simulable = simulable;
		this.cantEscenarios = cantEscenarios;
		this.instanteInicial = lt.getInicial();
		this.instanteFinal = lt.getLinea().get(lt.getCantidadPasos()-1).getInstanteFinal();
		this.ltiempo = lt;
		this.resoptimExterno = resoptimExterno;
		if (resoptimExterno) {			
			DatosResOptimIncrementos droi = new DatosResOptimIncrementos();
			droi.setNombre("Prueba de cargador EDF");
			droi.setRuta("G:/PLA/Pla_datos/Archivos/ModeloOp/ValaguaEDF");
			droi.setTipoSoporte(" ");
			this.resoptim = CargadorResOptimEDF.devuelveResOptimIncrementos(droi);
			
		} else {
			this.resoptim = resoptim2;	
		}
		this.simulable.setResOptim(resoptim);
		ltiempo.reiniciar();
		
		inicializarSalidasSim(rutaSals);
		
		
	}

	public void simular(int[] escenarios) {
		
		if (encadenado)
			simularEncadenado();
		else
			simularEnHaz(escenarios);
		
		System.out.println("TERMINó SIMULACIóN");
	}
	
	/**
	 * Tomamos como hipótesis que la simulación encadenada mantiene comportamientos y espacio de estados constante.
	 */
	public void simularEncadenado() {
		
		
	}
	
	public void simularEnHaz(int [] escenarios) {	
			
		simulable.inicializarSimulable();
						
		ltiempo.setSentidoTiempo(1);
	
		
		for (int i = 0; i< escenarios.length; ++i) {			
			
			ltiempo.reiniciar();
			
			simulable.inicializarEscenario(escenarios[i]);
			simulable.inicializarAzarParaUnEscenario(escenarios[i]);		
			PasoTiempo paso = ltiempo.devuelvePasoActual();
			numpaso = ltiempo.getNumPaso();
					
			while ( paso != null) {
				
				simulable.cargarPasoCorriente(numpaso, paso);
				

				simulable.inicializarPEPaso();
			//	simulable.sortearProcEstDE(paso.getInstanteInicial()+Constantes.EPSILONSALTOTIEMPO); //htDE toma el valor luego del sorteo, lo guarda el simulable	
				
				/**
				 * 		ESTOS SON LOS MONTECARLITOS, EVENTUALMENTE PODRóAMOS HACERLO DENTRO DEL SIMULADOR PASO
				 * 		PARA TENER LA POSIBILIDAD DE QUE LOS CONTROLES DE INCIDAN EN LOS SORTEOS				 * 
				 */
				simulable.sortearProcEstNODE(paso.getInstanteInicial()+Constantes.EPSILONSALTOTIEMPO);
							
				simulable.simularPaso(CorridaHandler.getInstance().isParalelo());	
				simulable.guardarResultadoPaso();
							
				simulable.actualizarParaProximoPasoAproximada();
								
				ltiempo.avanzarPaso();
				paso = ltiempo.devuelvePasoActual();				
				numpaso++;				
				
				
			}
			
			//simulable.finalizarEscenario();			
		}
				
		simulable.finalizarSimulable();

	}

	public void simularEnHazServidor(int[] escenarios) {
		
		simulable.inicializarSimulable();

		ltiempo.setSentidoTiempo(1);
	
		// Los escenarios empiezan en 1 !!!!
		// i es un óndice en un vector de nómeros de escenarios

		for (int i = 0; i < escenarios.length; ++i) {

			/**
			 * ATENCIóN MANOLO Para simulacion encadenadas La lónea de tiempo tiene que
			 * empezar el 1 de enero de un aóo
			 */

			ltiempo.reiniciar();

			/*
			 * ATENCION MANOLO Los dos mótodos siguientes hay que agregarle parómetro
			 * ENCADENADO
			 * 
			 */
			simulable.inicializarEscenario(escenarios[i]);
			simulable.inicializarAzarParaUnEscenario(escenarios[i]);
			PasoTiempo paso = ltiempo.devuelvePasoActual();
			numpaso = ltiempo.getNumPaso();

			while (paso != null) {

				simulable.cargarPasoCorriente(numpaso, paso);

				simulable.inicializarPEPaso();
				// simulable.sortearProcEstDE(paso.getInstanteInicial()+Constantes.EPSILONSALTOTIEMPO);
				// //htDE toma el valor luego del sorteo, lo guarda el simulable

				/**
				 * ESTOS SON LOS MONTECARLITOS, EVENTUALMENTE PODRóAMOS HACERLO DENTRO DEL
				 * SIMULADOR PASO PARA TENER LA POSIBILIDAD DE QUE LOS CONTROLES DE INCIDAN EN
				 * LOS SORTEOS *
				 */
				simulable.sortearProcEstNODE(paso.getInstanteInicial() + Constantes.EPSILONSALTOTIEMPO);

			
				simulable.simularPaso(CorridaHandler.getInstance().isParalelo());
				simulable.guardarResultadoPaso();
			
				simulable.actualizarParaProximoPaso();

				ltiempo.avanzarPaso();
				paso = ltiempo.devuelvePasoActual();
				numpaso++;

			}

			simulable.finalizarEscenarioServidor();
		}
	

	}
	public LineaTiempo getLtiempo() {
		return ltiempo;
	}


	public void setLtiempo(LineaTiempo ltiempo) {
		this.ltiempo = ltiempo;
	}


	public int getCantEscenarios() {
		return cantEscenarios;
	}


	public void setCantEscenarios(int cantEscenarios) {
		this.cantEscenarios = cantEscenarios;
	}


	public long getInstanteInicial() {
		return instanteInicial;
	}


	public void setInstanteInicial(long instanteInicial) {
		this.instanteInicial = instanteInicial;
	}


	public long getInstanteFinal() {
		return instanteFinal;
	}


	public void setInstanteFinal(int instanteFinal) {
		this.instanteFinal = instanteFinal;
	}


	public long getInstanteInicialEstudio() {
		return instanteInicialEstudio;
	}


	public void setInstanteInicialEstudio(int instanteInicialEstudio) {
		this.instanteInicialEstudio = instanteInicialEstudio;
	}


	public boolean isEncadenado() {
		return encadenado;
	}


	public void setEncadenado(boolean encadenado) {
		this.encadenado = encadenado;
	}


	public ResOptim getResoptim() {
		return resoptim;
	}


	public void setResoptim(ResOptim resoptim) {
		this.resoptim = resoptim;
		this.simulable.setResOptim(resoptim);
	}


	public Simulable getSimulable() {
		return simulable;
	}


	public void setSimulable(Simulable simulable) {
		this.simulable = simulable;
	}
	
	public boolean isResoptimExterno() {
		return resoptimExterno;
	}

	public void setResoptimExterno(boolean resoptimExterno) {
		this.resoptimExterno = resoptimExterno;
	}
	
	
	public String getDirSalidasSim() {
		return dirSalidasSim;
	}

	public void setDirSalidasSim(String dirSalidasSim) {
		this.dirSalidasSim = dirSalidasSim;
	}

	/**
	 * Reinicializa los archivos de salida
	 *  salidaDetalladaSP.txt   - tabla con el despacho por paso y poste en formato tabular
	 *  salidaAvisosPaso.txt    - avisos de resultados raros por paso, por ejemplo destrucción de agua
	 *  modeloInfactible.lp  - salida del resolvedor lineal LPsolve u otro con el problema infactible para investigar
	 * @param rutaSals: si es null, se emplea el directorio de salidas ya existente en el atributo dirSalidasSim
	 */
	public void inicializarSalidasSim(String rutaSals){
		
		//Fecha y hora actual
        Calendar fecha = Calendar.getInstance();
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);
        int hora = fecha.get(Calendar.HOUR_OF_DAY);
        int minuto = fecha.get(Calendar.MINUTE);
        int segundo = fecha.get(Calendar.SECOND);
        String dirCompleto = null;
        String dirRaiz = null;
        String dirNuevo = null;
        
        if(rutaSals!= null){
        	// Se crea el directorio de Salidas
	        dirRaiz = rutaSals;
	        dirNuevo = anio + "-" + mes + "-" + dia + "-" + hora  + "-"  + minuto + "-" + segundo +  "-SIM";
	        dirCompleto = dirRaiz + "/" + dirNuevo;
	        dirSalidasSim = dirCompleto;
	        DirectoriosYArchivos.creaDirectorio(dirRaiz, dirNuevo);
        }else{
        	// Se toma el directorio de Salidas ya existente
        	dirCompleto = dirSalidasSim;
        }
        
        this.simulable.setDirSalidas(dirCompleto);
        
        
		
		String nombreArchivoSalida = dirCompleto + "salidaDetalladaSP.xlt";
		boolean existe = DirectoriosYArchivos.existeArchivo(nombreArchivoSalida);
		if (existe) DirectoriosYArchivos.eliminaArchivo(nombreArchivoSalida);

		String nombreArchivoPL = dirCompleto + "salidaAvisosPaso.txt";
		existe = DirectoriosYArchivos.existeArchivo(nombreArchivoPL);
		if (existe) DirectoriosYArchivos.eliminaArchivo(nombreArchivoPL);	
		
		String nombreArchInfactible = dirCompleto + "modeloInfactible.lp";
		existe = DirectoriosYArchivos.existeArchivo(nombreArchInfactible);
		if (existe) DirectoriosYArchivos.eliminaArchivo(nombreArchInfactible);			
		
		/**
		 * ATENCIóN MANOLO ACó SE CREA LA ESTRUCTURA DE RESUMEN GENERAL		 
		 */
		DatosEPPResumen datosResumen = new DatosEPPResumen(); 

	}


	private void borrarSalidasParalelo() {
//		for (int i = 1; i <= CorridaHandler.getInstance().getCorridaActual().getCantEscenarios(); ++i) {
//			DirectoriosYArchivos.eliminaArchivo(Constantes.RUTA_SALIDA_SIM_PARALELA+"\\escenario"+Integer.toString(i));
//		}
//		
	}
	

}
