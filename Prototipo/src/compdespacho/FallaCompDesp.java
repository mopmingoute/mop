/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FallaCompDesp is part of MOP.
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

package compdespacho;

import java.util.ArrayList;
import java.util.Hashtable;
import datatypes.Pair;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;
import datatypesSalida.DatosCurvaOferta;
import datatypesSalida.DatosEPPUnEscenario;
import parque.Falla;
import utilitarios.Constantes;

/**
 * Clase encargada de modelar el comportamiento de la falla en el problema de despacho
 * @author ut602614
 *
 */

public class FallaCompDesp extends CompDespacho{

	private Falla falla;

	private static String compFallaPorDefecto; //Hacer todo lo de los comportamientos
	
	private int cantEscForzados; // Cantidad de escalones programados que estón forzados
	
	/**
	 * En los comportamientos FALLA_CONESTADO_SINDUR y FALLA_CONESTADO_CONDUR, Los escalones programados que no estón
	 * forzados por variables de control DE, no se despachan en absoluto.
	 * Los escalones no programados pueden despacharse siempre. 
	 * Es decir que los escalones programados no forzados son los que no aparecen en el despacho.
	 */
	
	private String compFalla;
	
//	private int escalonesForzadosIni; //cantidad de escalones forzados al inicio del paso
	
	private double [] costosArranque; //costos de arranque en USD de cada escalón
	
//	private String narranques; //cantidad de escalones de falla que se arrancan por encima de los iniciales
//	private String nparadas; //cantidad de escalones de falla que se eliminan por debajo de los iniciales
//	private String nescalonesforzadosfin; //cantidad de escalones de falla al fin del paso
	private String[][] npotpe; //nombres de las variables de control potencia de falla por poste y escalón
							   //se crean los nombres de todos los escalones incluso los programados no forzados	
	private String[] nxe; //nombre de las variables binarias auxiliares para forzamiento con arranques


	public FallaCompDesp() {
		super();
	
		this.parametros = new Hashtable<String, String>();
		falla = (Falla)this.participante;
		
	}
	
	public FallaCompDesp(Falla falla2) {
		this.falla = falla2;
	}

	/**
	 * Devuelve la potencia móxima de la falla en el poste p y escalon e
	 * @param p 
	 * @param e
	 * @return
	 */
	private Double damePotenciaPosteEscalon(int p, int e) {
		Pair<Double,Double> par = this.falla.getEscalones().get(e); 
		Double poruno = par.first/100;
		Double potActiva = this.falla.dameComportamientoDemanda().getPotActivaPorPoste()[p]; 
		return poruno*potActiva;
		
	}

	@Override
	public void crearVariablesControl() {		 
		Falla falla = (Falla)this.getParticipante();
		
		for (int p = 0; p < participante.getCantPostes(); p++) {
			this.npotpe[p] = new String [falla.getCantEscalones()];
			for (int e = 0; e < falla.getCantEscalones(); e++) {				
				this.npotpe[p][e] = generarNombre("potpe_", p + "_" + e);						
				DatosVariableControl dv = new DatosVariableControl(this.npotpe[p][e] , Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null, damePotenciaPosteEscalon(p, e));
				this.variablesControl.put(dv.getNombre(), dv);
			}
			
		}
		

		
//		if (compFalla.equalsIgnoreCase(Constantes.FALLACONARRANQUE)) {
//			// ESTE COMPORTAMIENTO FALLA CON ARRANQUE QUE EN TEXTOS VIEJOS DE LAS ECUACIONES SE LLAMA
//			// FALLA ESTRUCTURADA, PARECE NO TENER SENTIDO			
//			this.narranques = generarNombre("arranques");
//			this.nparadas = generarNombre("paradas");
//			this.nescalonesforzadosfin= generarNombre("escalonesForzadosFin");
//			DatosVariableControl dv = new DatosVariableControl(this.narranques, Constantes.VCENTERA, Constantes.VCPOSITIVA, null, (double)falla.getCantEscalones()-this.escalonesForzadosIni);
//			this.variablesControl.put(dv.getNombre(), dv);
//			
//			dv = new DatosVariableControl(this.nparadas, Constantes.VCENTERA, Constantes.VCPOSITIVA, null, (double)this.escalonesForzadosIni);
//			this.variablesControl.put(dv.getNombre(), dv);			
//			
//			dv  = new DatosVariableControl(this.nescalonesforzadosfin, Constantes.VCENTERA, Constantes.VCPOSITIVA, null, (double)falla.getCantEscalones());
//			this.variablesControl.put(dv.getNombre(), dv);
//			for (int e = 0; e < falla.getCantEscalones(); e++){
//				this.nxe[e] = generarNombre("nxe", Integer.toString(e));
//				dv = new DatosVariableControl(this.nxe[e], Constantes.VCBINARIA, null, null, Constantes.INFNUESTRO);				
//				this.variablesControl.put(dv.getNombre(), dv);				
//			}
//		}		
	}

	@Override
	public void cargarRestricciones() {
		String compFalla = parametros.get(Constantes.COMPFALLA);	
		Falla falla = (Falla)this.participante;
		if(!compFalla.equalsIgnoreCase(Constantes.FALLASINESTADO)){
			for (int p = 0; p < participante.getCantPostes(); p++) {
				for (int e = 0; e < this.cantEscForzados; e++) {
					DatosRestriccion dr = new DatosRestriccion();
					dr.agregarTermino(this.npotpe[p][e], 1.0);
					dr.setSegundoMiembro(damePotenciaPosteEscalon(p, e));
					dr.setTipo(Constantes.RESTIGUAL);
					dr.setNombre(generarNombre("forzamientopotpe_", p + "_" + e));
					this.restricciones.put(dr.getNombre(), dr);					
				}
				for (int e = this.cantEscForzados; e < falla.getCantEscProgram(); e++) {
					DatosRestriccion dr = new DatosRestriccion();
					dr.agregarTermino(this.npotpe[p][e], 1.0);
					dr.setSegundoMiembro(0.0);
					dr.setTipo(Constantes.RESTIGUAL);
					dr.setNombre(generarNombre("forzamientopotpe_", p + "_" + e));
					this.restricciones.put(dr.getNombre(), dr);					
				}			
			}
		}	
			
//		} else if (compFalla.equalsIgnoreCase(Constantes.FALLACONARRANQUE)) {
//			// ESTE COMPORTAMIENTO FALLA CON ARRANQUE QUE EN TEXTOS VIEJOS DE LAS ECUACIONES SE LLAMA
//			// FALLA ESTRUCTURADA, PARECE NO TENER SENTIDO
//			DatosRestriccion drxe = new DatosRestriccion();
//			DatosRestriccion dr;
//
//			for (int e = 0; e < falla.getCantEscalones(); e++) { 					
//				drxe.agregarTermino(this.nxe[e], 1.0);		
//				for(int p = 0; p < participante.getCantPostes(); p++) {
//					double sumaprof = 0.0;
//					dr = new DatosRestriccion();					
//					for (int esub = 0; esub <= e; esub++) {
//						dr.agregarTermino(this.npotpe[p][esub], 1.0);
//						sumaprof += damePotenciaPosteEscalon(p, esub);
//					}
//					dr.agregarTermino(this.nxe[e], -sumaprof);
//					dr.setTipo(Constantes.RESTMAYOROIGUAL);
//					dr.setNombre(generarNombre("limitesPotencia",  p + "_" + e));
//					dr.setNombre(generarNombre("forzamientopotpe_", p + "_" + e));
//					this.restricciones.put(dr.getNombre(), dr);					
//				}
//			}
//			drxe.agregarTermino(this.narranques, -1.0);
//			drxe.agregarTermino(this.nparadas, 1.0);
//			drxe.setSegundoMiembro((double)this.escalonesForzadosIni);
//			drxe.setTipo(Constantes.RESTIGUAL);
//			drxe.setNombre(generarNombre("binariasForzado"));
//			this.restricciones.put(drxe.getNombre(), drxe);
//			
//			//define cantidad de forzados final
//			dr = new DatosRestriccion();
//			dr.agregarTermino(this.nescalonesforzadosfin, 1.0);
//			dr.agregarTermino(this.narranques, -1.0);
//			dr.agregarTermino(this.nparadas, 1.0);
//			dr.setSegundoMiembro((double)this.escalonesForzadosIni);
//			dr.setNombre(generarNombre("cantforzadosfinal"));
//			dr.setTipo(Constantes.RESTIGUAL);
//			this.restricciones.put(dr.getNombre(), dr);			
//		}
		
	}

	/**
	 * Devuelve el costo unitario en el poste p escalón e en USD/MJ
	 * @param p
	 * @param e
	 * @return
	 */
	private Double dameCostoUnitarioPosteEscalon(int p, int e) {
		return this.falla.getEscalones().get(e).second;
	}
	
	@Override
	public void contribuirObjetivo() {
		Falla falla = (Falla) this.participante;
		ArrayList<Double> variablesPoste = new ArrayList<Double>();
		ArrayList<Double> potsPoste = new ArrayList<Double>();
		boolean variables = this.falla.getSimPaso().getCorrida().isCostosVariables();
		if (this.falla.getSimPaso().isSimulando() && variables) {
			variablesPoste = new ArrayList<Double>();
			potsPoste = new ArrayList<Double>();
		}
		
		for (int e = 0; e < falla.getCantEscalones(); e++) {					
//			if (compFalla.equalsIgnoreCase(Constantes.FALLACONARRANQUE)) {
//				this.objetivo.agregarTermino(this.nxe[e], this.costosArranque[e]);
//			} //Ojo que puede ser necesario un epsilon
			
			for(int p = 0; p < falla.getCantPostes(); p++) {
				if (this.falla.getSimPaso().isSimulando() && variables) {
					variablesPoste.add(dameCostoUnitarioPosteEscalon(p, e)*falla.getDuracionPostes(p)/Constantes.SEGUNDOSXHORA);
					potsPoste.add(damePotenciaPosteEscalon(p, e));
				}
				this.objetivo.agregarTermino(this.npotpe[p][e], dameCostoUnitarioPosteEscalon(p, e)*falla.getDuracionPostes(p)/Constantes.SEGUNDOSXHORA);
				
			}
			if (this.falla.getSimPaso().isSimulando() && variables) {
				DatosEPPUnEscenario des = this.falla.getSimPaso().getDatosEscenario();
				DatosCurvaOferta dco = des.getCurvOfertas().get(falla.getSimPaso().getPaso());
				dco.agregarVariablesMaquinaPaso(falla.getNombre() + "_" + Integer.toString(e), variablesPoste);
				dco.agregarPotsDispMaquinaPaso(falla.getNombre() + "_" + Integer.toString(e), potsPoste);			
			}	
		}		 
	}
	
	


	public static String getCompFallaPorDefecto() {
		return compFallaPorDefecto;
	}

	public static void setCompFallaPorDefecto(String compFallaPorDefecto) {
		FallaCompDesp.compFallaPorDefecto = compFallaPorDefecto;
	}

	public String getCompFalla() {
		return compFalla;
	}

	public void setCompFalla(String compFalla) {
		this.compFalla = compFalla;
	}


//	public String getNarranques() {
//		return narranques;
//	}
//
//	public void setNarranques(String narranques) {
//		this.narranques = narranques;
//	}
//
//	public String getNparadas() {
//		return nparadas;
//	}
//
//	public void setNparadas(String nparadas) {
//		this.nparadas = nparadas;
//	}

	public String[][] getNpotpe() {
		return npotpe;
	}

	public void setNpotpe(String[][] npotpe) {
		this.npotpe = npotpe;
	}

	public double [] getCostosArranque() {
		return costosArranque;
	}

	public void setCostosArranque(double [] costosArranque) {
		this.costosArranque = costosArranque;
	}

	/**
	 * Devuelve el resultado de potencia en MW del poste p, del escalon esc de la falla dada una salida del problema lineal
	 * @param p
	 * @param esc
	 * @return
	 */
	public Double getPotEsc(DatosSalidaProblemaLineal resultados, int p, int esc) {
		return resultados.getSolucion().get(npotpe[p][esc]);				
	}
	public Falla getFalla() {
		return falla;
	}

	public void setFalla(Falla falla) {
		this.falla = falla;
	}

//	public String getNescalonesforzadosfin() {
//		return nescalonesforzadosfin;
//	}
//
//	public void setNescalonesforzadosfin(String nescalonesforzadosfin) {
//		this.nescalonesforzadosfin = nescalonesforzadosfin;
//	}

	public String[] getNxe() {
		return nxe;
	}

	public void setNxe(String[] nxe) {
		this.nxe = nxe;
	}

	public int getCantEscForzados() {
		return cantEscForzados;
	}

	public void setCantEscForzados(int cantEscForzados) {
		this.cantEscForzados = cantEscForzados;
	}
	
	

}
