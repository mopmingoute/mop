/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesoCronicas is part of MOP.
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

import interfacesParticipantes.AportanteEstado;
import logica.CorridaHandler;

import java.util.Hashtable;

import datatypesProcEstocasticos.DatosPECronicas;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import persistencia.CargadorPECronicas;
import pizarron.PizarronRedis;
import tiempo.LineaTiempo;
import estado.VariableEstado;

/**
 *
 * @author ut469262
 * Es un proceso en el que los escenarios futuros están vinculados a las crónicas de un proceso histórico.
 * El primer año del primer escenario del proceso this,
 * se asocia a la primera crónica del proceso histórico asociado
 * El primer año del segundo escenario del proceso this,
 * se asocia a la segunda crónica del proceso histórico asociado,
 * etc.
 * Ejemplo: 
 * primer escenario, a partir del primer año se asocian las crónicas 1909, 1910,....
 * segundo escenario ídem 1910, 1911
 * 
 * NECESARIAMENTE EL ProcesoCronicas DEBE TENER CERO VARIABLES DE ESTADO PROPIAS, PORQUE
 * ESTÁ CONECTADO A UN PROCESO HISTÓRICO, QUE ES EL QUE PUEDE TENER VARIABLES DE ESTADO
 * 
 * ATENCIÓN:
 * Puede ocurrir que en un año bisiesto, a un escenario se asocie una crónica no bisiesta.
 * Eso debió resolverlo el que estimó el proceso:
 * -si el proceso es semanal no hay problema
 * -si el proceso fuese diario u horario, el que estimó el proceso debió haber agregado días u horas.
 * Puede ocurrir que en un año no bisiesto, a un escenario se asocie una crónica bisiesta.
 * Eso debió resolverlo el que estimó el proceso:
 * -si el proceso es semanal no hay problema
 * -si el proceso fuese diario u horario, el que estimó el proceso debió haber eliminado días u horas.
 *
 */


// public class ProcesoCronicas extends ProcesoEscenarios implements AportanteEstado{
	
public class ProcesoCronicas extends ProcesoEscenarios {
	private ProcesoHistorico pOrigen;
	
	
	
	/*
	 * TODO: ATENCIÓN, PRESUPONE QUE EL PROCESO pOrigen ya fue construído
	 * 
	 * 
	 */		
	public ProcesoCronicas(DatosPECronicas dpcron){		
		super(dpcron.getDpEsc());	
		
		pOrigen = (ProcesoHistorico) this.getAzar().devuelveProcesoDeNombre(dpcron.getNombreProcesoOrigen());
		if(pOrigen==null){
			System.out.println("El proceso " + dpcron.getNombreProcesoOrigen() +  "no existe o aún no fue construído");
		}
		// Verifica la coherencia de las etiquetas de cr�nicas le�das con las cr�nicas
		// ProcesoHistorico pOrigen. 
		for(int iesc=0; iesc<this.getCantEsc(); iesc++){
			int cronEsperada = pOrigen.getCronIni() + iesc;
			for(int ian=0; ian<(this.getAnioFinalPE()-this.getAnioFinalPE()); ian++){
				if(this.getEtiquetaCron()[iesc][ian]!=cronEsperada){
					System.out.println("Incoherencia en crónicas del proceso " + this.getNombre()
							+ " con el proceso histórico " + pOrigen.getNombre());
					if (CorridaHandler.getInstance().isParalelo()){
						//PizarronRedis pp = new PizarronRedis();
						//pp.matarServidores();
					}
					System.exit(1);					
				}
				cronEsperada++;
				if(cronEsperada>pOrigen.getCronFin()) cronEsperada = pOrigen.getCronIni();				
			}
		}
	}
	
	
	public void producirRealizacion(int instante) {
		//int anioCorriente = this.getSimuladorPaso().getCorrida().getLineaTiempo().getAnioInic() + this.getIndAnio();  // anio del instante pedido
		
		/*
		 * Se tiene en cuenta que el año inicial del PE, que en el escenario 1 ojojojojoj TODO: del PE corresponde a
		 * la primera crónica del proceso histórico asociado (ej. 1909), puede no coincidir con el
		 * año inicial del proceso estocástico this.
		 * 
		 * rezago = cantidad de años desde el inicio del PE hasta el inicio de la corrida (eventualmente negativa)
		 * 
		 * El proceso repite circularmente sus propios escenarios
		 * Ejemplo: si tiene 105 escenarios del 1 al 105, el escenario 106 repite el 1, y así sucesivamente
		 */
		LineaTiempo lt = this.getSimuladorPaso().getCorrida().getLineaTiempo();
		int rezago = lt.getAnioInic() - this.getAnioInicialPE();
		int iescCron = (this.getEscenario()-1)%this.getCantEsc() - rezago;	
		if(iescCron<0) iescCron = this.getCantEsc() - 1 + iescCron;
		int ip = pasoDelAnio(instante);   // usa el método pasoDelAnio de la clase padre ProcesoEstocastico
		int anCron = this.anioDeInstante(instante) + rezago;
		if(anCron< lt.getAnioInic()){
			System.out.println("Se pidió un instante anterior al inicio del proceso " + this.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}else if(anCron>=lt.getAnioFin()){
			System.out.println("Se pidió un instante posterior al fin del proceso " + this.getNombre());
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
			System.exit(1);
		}
		int ianCron = anCron - lt.getAnioInic(); 
		int iva;
		int ive;
		for(VariableAleatoria va: this.getVariablesAleatorias()){
			iva = this.getIndiceVA().get(va.getNombre());
			va.setValor(this.getDatos()[iescCron][ianCron][ip][iva]);				
		}
		// carga los valores de las variables de estado
		for(VariableEstado ve: this.getVarsEstado()){
			ive = this.getIndiceVE().get(ve.getNombre());
			ve.setEstado(this.getValoresVE()[iescCron][ianCron][ip][ive]);				
		}		
	}
	


	public static void main(String[] args) { 
		
//		String nombre = "Precios Brasil";
//		String tipo = "";
//		String tipoSoporte = "";
//		String ruta = "D:/Proyectos/modelopadmin/resources/cmargBrasil";
//		
//		DatosProcesoEstocastico dpe = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, false, false, null);
//				
//		DatosPECronicas dpcron = CargadorPECronicas.devuelveDatosPECronicas(dpe);
//		
//		ProcesoCronicas pcron = new ProcesoCronicas(dpcron);
//		System.out.println("Terminó lectura ProcesoCronicas");
		
		
	}	
	
	

}
