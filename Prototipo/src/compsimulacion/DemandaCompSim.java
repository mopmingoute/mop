/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DemandaCompSim is part of MOP.
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

package compsimulacion;

import java.util.ArrayList;
import java.util.Hashtable;

import compdespacho.DemandaCompDesp;
import compgeneral.DemandaComp;
import parque.Demanda;
import simulacion.ValPostizador;
import utilitarios.Constantes;
import datatypesProblema.DatosSalidaProblemaLineal;
import interfacesParticipantes.AportantePost;

public class DemandaCompSim extends CompSimulacion implements AportantePost{
	private Demanda demanda;
	private DemandaCompDesp dcd;
	private DemandaComp compG;
	
	public DemandaCompSim() {
		super();
		this.demanda = (Demanda)this.getParticipante();
		this.dcd = (DemandaCompDesp)this.getCompdespacho();
	}

	public DemandaCompSim(Demanda demanda, DemandaCompDesp compD, DemandaComp compG) {
		super();
		this.demanda = demanda;
		this.dcd = compD;
		this.setCompG(compG);
	}

	@Override
	public ArrayList<Double> aportaParaPost(int sorteo) {
		ArrayList<Double> resultado = new ArrayList<Double>();
		double [] muestra;
		
		if (!demanda.getSimPaso().isSimulando()) {
			muestra = demanda.getDemanda().getUltimoMuestreoOptim()[sorteo];
		} else {
			muestra = demanda.getDemanda().getUltimoMuestreo();
		}
				
		for (int i = 0; i < muestra.length;i++) {
			resultado.add(muestra[i]);
			//resultado.add(350.0);
		}

		return resultado;
	}

	@Override
	public void actualizarVariablesCompDespacho(int iter) {
		// ESTA PRONTO, DEBE ESTAR VACóO		
		
	}

	@Override
	public void cargarDatosParaUnaIteracion(int iter,
			DatosSalidaProblemaLineal salidaIter) {
		// ESTA PRONTO, DEBE ESTAR VACóO		
		
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// ESTA PRONTO, DEBE ESTAR VACóO		
		
	}

	@Override
	public boolean aceptaDetenerIteracion(int iter,DatosSalidaProblemaLineal salidaIter) {
		// ESTA PRONTO		
		return true;
	}

	@Override
	public void inicializarParaEscenario() {
		// ESTA PRONTO, DEBE ESTAR VACóO		
		
	}

	@Override
	public void actualizarVariablesCompGlobal(Hashtable<String,String> comps) {
		// TODO MAS ADELANTE CUANDO HAYA DEMANDA RESIDUAL
		
	}

	
	
	public void cargarDatosCompDespacho(long instante) {
		ValPostizador valP = demanda.getSimPaso().getValPostizador();
		cargarDatosCompDespachoAuxiliar(instante, valP);
	}
	
	
//	@Override
//	public void cargarDatosCompDespacho(int instante) {
//		dcd.setPotActivaPorPoste(new double [demanda.getCantPostes()]);
//		//DATOS QUE VIENEN DE VARIABLES ALEATORIAS
//		double[] listaPots; 
//		ValPostizador valP = demanda.getSimPaso().getValPostizador();
//		if (valP.isExterna()) {
//			listaPots = valP.valPostizar(demanda.getDemanda().getPe(), demanda.getDemanda().getNombre());
//		} else {
//			listaPots = valP.valPostizar(demanda.getDemanda().getUltimoMuestreo(), Constantes.VALPALEAT);
//		}	
//		dcd.setPotActivaPorPoste(listaPots);		
//	}
	
	
	/**
	 * Mótodo auxiliar que es llamado tanto por cargarDatosCompDespacho como por
	 * cargarDatosCompDespachoOptim, las llamadas difieren en el origen del Valpostizador
	 * @param instante
	 * @param valP
	 */
	public void cargarDatosCompDespachoAuxiliar(long instante, ValPostizador valP){
		dcd.setPotActivaPorPoste(new double [demanda.getCantPostes()]);
		//DATOS QUE VIENEN DE VARIABLES ALEATORIAS
		
		double[] listaPots = null;
		if (valP.isExterna()) {
			listaPots = valP.valPostizar(demanda.getDemanda().getPe(), demanda.getNombreVA());
		} else {
			listaPots = valP.valPostizar(demanda.getDemanda().getUltimoMuestreo(), Constantes.VALPPROMEDIO,demanda.getDemanda().getValor());
		}	
//		/**
//		 * TODO IMPRESIÓN TRUCHA
//		 */
//		String salida = "potencias\r";
//		for(double d: listaPots) {
//			salida = salida + d + "\r";
//		}
//		System.out.println(salida);
		
		dcd.setPotActivaPorPoste(listaPots);			
		
	}

	public Demanda getDemanda() {
		return demanda;
	}

	public void setDemanda(Demanda demanda) {
		this.demanda = demanda;
	}

	public DemandaCompDesp getDcd() {
		return dcd;
	}

	public void setDcd(DemandaCompDesp dcd) {
		this.dcd = dcd;
	}

	public DemandaComp getCompG() {
		return compG;
	}

	public void setCompG(DemandaComp compG) {
		this.compG = compG;
	}

	//////////////////////// METODOS USADOS SóLO EN LA OPTIMIZACIóN ////////////////	
	
	@Override
	public void actualizarOtrosDatosIniciales() {
		// No hace nada deliberadamente
	}

	@Override
	public double calculaCostoPaso(DatosSalidaProblemaLineal salidaUltimaIter) {
		// Los costos de falla los calcula el participante falla
		return 0.0;
	}

//	@Override
//	public void cargarVEfinPasoOptim(DatosSalidaProblemaLineal salidaUltimaIter) {
//		// No hace nada deliberadamente
//		
//	}
	
	
	@Override
	public void actualizarVariablesCompDespachoOptim(int iter) {
		actualizarVariablesCompDespacho(iter);
		
	}
	
	
	public void cargarDatosCompDespachoOptim(long instante) {
		ValPostizador valP = demanda.getOptimPaso().getValPostizador();
		cargarDatosCompDespachoAuxiliar(instante, valP);
	}



	/////////////////////////////////////////////////////////////////////////////////
}
