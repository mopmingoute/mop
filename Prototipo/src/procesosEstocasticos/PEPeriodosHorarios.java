/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PEPeriodosHorarios is part of MOP.
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

import datatypesProcEstocasticos.DatosPEHistorico;
import datatypesProcEstocasticos.DatosPEPorPeriodos;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import estado.VariableEstado;
import persistencia.CargadorPEPeriodosHorarios;
import utilitarios.Constantes;
import utilitarios.LeerDatosArchivo;

/**
 * Cada variable aleatoria del proceso base tiene los datos de la variable aleatoria
 * del proceso this en un bloque o per�odo horario del paso de tiempo, que no tiene ninguna relaci�n
 * en principio con los postes de la postizaci�n interna o externa.
 * 
 * El paso de tiempo est� dividido en "horas", pueden ser en realidad medias horas, etc.
 * Las horas pertenecen a per�odos horarios.
 * 
 * El paso de tiempo de this es el mismo que el del proceso base.
 * 
 * TODO: ATENCI�N El proceso this tiene las mismas variables de estado del proceso base.
 * 
 * El proceso this es siempre muestreado.
 * El proceso procBase no es muestreado, porque cada variable aleatoria corresponde
 * a un conjunto de "horas" del paso de tiempo del PE en el mismo per�odo horario, no contiguas.
 * 
 * @author ut469262
 *
 */
public class PEPeriodosHorarios extends ProcesoEstocastico{
	private int cantHoras;  // las "horas" se enumeran de 0 a cantHoras-1
	private int cantPeriodos;  // los per�odos se enumeran de 0 a cantPeriodos-1
	private int[] periodoDeHoras;   // periodoDeHoras[i] indica a qu� per�odo pertenece la hora i.
	/**
	 * La variable alatoria del proceso this es �nica y tiene el mismo nombre del proceso this.
	 * Las variables aleatorias del procBase deben tener nombres de la forma:
	 * nombre del proceso this + i  , donde i=0,...cantPeriodos 
	 */
	private ProcesoEstocastico procBase;   // cada variable del proceso estoc�stico base da el valor en un per�odo horario
	private int durHora;
	

	/*
	 * TODO: ATENCI�N, PRESUPONE QUE EL PROCESO procBase ya fue constru�do
	 * 
	 * 
	 */	
	public PEPeriodosHorarios(DatosPEPorPeriodos dpp){
		super();
		this.setNombre(dpp.getNombre());
		this.setDiscretoExhaustivo(dpp.isDiscretoExhaustivo());
		this.setRuta(dpp.getRuta());
		ProcesoEstocastico pBase = this.getAzar().devuelveProcesoDeNombre(dpp.getNombreProcesoBase());
		if(pBase==null){
			System.out.println("El proceso " + dpp.getNombreProcesoBase() +  "no existe o aun no fue construido");
		}	
		ArrayList<String> nombreVA = new ArrayList<String>();
		nombreVA.add(dpp.getNombre());  // la �nica variable de estado tiene el mismo nombre del proceso
		this.setNombresVarsAleatorias(nombreVA);
		this.setNombresVarsEstado(new ArrayList<String>());	// el proceso no tiene variables de estado adicionales	
		this.setCantVA(1);
		
		this.setCantVE(pBase.getCantVE());
		this.setNombrePaso(pBase.getNombrePaso());

		this.completaConstruccion();	
		this.setCantidadInnovaciones(0);  // no se le sortean innovaciones porque sus realizaciones dependen de las de procBase.
		this.setPrioridadSorteo(pBase.getPrioridadSorteo()+1);
		this.durHora = this.getDurPaso()/this.cantHoras;
		this.setMuestreado(true);
		
	}
	

	@Override
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		// Deliberadamente en blanco
	}
	
	
	@Override
	public void producirRealizacionSinPronostico(long instante) {		
		/**
		 * Se supone que por el orden del sorteo de los procesos estoc�sticos
		 * ya se ha sorteado las realizaciones del proceso procBase en el instante inicial del paso 
		 * de tiempo, y est�n disponibles sus valores
		 * 
		 * El proceso procBase no es muestreado por lo que se va a buscar
		 * el valor de la VA en el atributo valor.
		 */
		int paso = this.pasoDelAnio(instante);
		long instIniAnio = this.instanteInicialAnioDeInstante(instante);
		long instIniPaso = instIniAnio + paso*this.getDurPaso(); 
		int hora = (int)((instante - instIniPaso)/durHora);
		int periodo = periodoDeHoras[hora];
		String nombreVABase = this.getNombre()+periodo; // nombre de la VA del procBase.
		this.getVariablesAleatorias().get(0).setValor(procBase.devuelveVADeNombre(nombreVABase).getValor());
	}
	
//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		/**
		 * Este m�todo se invoca si el procBase tiene variables de estado.
		 * 
		 * Se supone que por el orden del sorteo de los procesos estoc�sticos
		 * ya se ha sorteado las realizaciones del proceso procBase en el instante inicial del paso 
		 * de tiempo, y est�n disponibles sus valores
		 * 
		 * El proceso procBase no es muestreado por lo que se va a buscar
		 * el valor de la VA en el atributo valor.
		 */
		for(int im=0; im<instantesMuestreo.length; im++){
			long instante = instantesMuestreo[im]; 
			int paso = this.pasoDelAnio(instante);
			long instIniAnio = this.instanteInicialAnioDeInstante(instante);
			long instIniPaso = instIniAnio + paso*this.getDurPaso(); 
			int hora = (int)((instante - instIniPaso)/durHora);
			int periodo = periodoDeHoras[hora];
			String nombreVABase = this.getNombre()+periodo; // nombre de la VA del procBase.
			// El isort es irrelevante porque no se emplean innovaciones sino el valor de las VA del procBase que s� uso las innovaciones del sorteo isort
			this.getVariablesAleatorias().get(0).getUltimoMuestreo()[im]=procBase.devuelveVADeNombre(nombreVABase).getValor();
		}
	}
	
	
	@Override
	public boolean tieneVEOptim() {
		return procBase.tieneVEOptim();
	}
	
	

	
	@Override
	public void prepararPasoOptim(int cantSortMontecarlo) {
		// Deliberadamente vac�o
		
	} 
	

	
	
	
	public int getCantHoras() {
		return cantHoras;
	}




	public void setCantHoras(int cantHoras) {
		this.cantHoras = cantHoras;
	}




	public int getCantPeriodos() {
		return cantPeriodos;
	}




	public void setCantPeriodos(int cantPeriodos) {
		this.cantPeriodos = cantPeriodos;
	}




	public int[] getPeriodoDeHoras() {
		return periodoDeHoras;
	}




	public void setPeriodoDeHoras(int[] periodoDeHoras) {
		this.periodoDeHoras = periodoDeHoras;
	}




	public ProcesoEstocastico getProcBase() {
		return procBase;
	}




	public void setProcBase(ProcesoEstocastico procBase) {
		this.procBase = procBase;
	}




	public int getDurHora() {
		return durHora;
	}




	public void setDurHora(int durHora) {
		this.durHora = durHora;
	}




	public static void main(String[] args) {
		
		
//		String ruta = "D:/Proyectos/modelopadmin/resources/cmargBrasil";
//		String nombre = "cmargBrasil";
//		String tipo = "periodosHorarios";
//		String tipoSoporte = null;
//		Boolean discretoExhaustivo = false;
//		Boolean muestreado = true;
//		Hashtable<String, Double> estadosIniciales = new Hashtable<String, Double>();
//		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, discretoExhaustivo, muestreado, estadosIniciales);
//		
//		
//		DatosPEPorPeriodos dph = CargadorPEPeriodosHorarios.devuelveDatosPEPorPeriodo(dpe);
//
//		PEPeriodosHorarios peph = new PEPeriodosHorarios(dph);
//		System.out.println("TERMIN� CREACI�N");		
		
		
		
	}

}
