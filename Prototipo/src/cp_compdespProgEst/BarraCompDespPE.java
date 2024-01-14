/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCompDespPE is part of MOP.
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

package cp_compdespProgEst;

import java.util.ArrayList;

import compdespacho.AcumuladorCompDesp;
import compdespacho.BarraCompDesp;
import compdespacho.DemandaCompDesp;
import compdespacho.FallaCompDesp;
import compdespacho.GeneradorCompDesp;
import compdespacho.ImpoExpoCompDesp;
import compdespacho.RedCompDesp;
import cp_datatypesEntradas.DatosContIntCP;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseSM;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_nuevosParticipantesCP.ContratoIntSist;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;
import parque.Acumulador;
import parque.Barra;
import parque.Demanda;
import parque.Generador;
import parque.ImpoExpo;
import parque.Participante;
import parque.Rama;
import parque.RedElectrica;
import tiempo.PasoTiempo;
import utilitarios.Constantes;

public class BarraCompDespPE extends CompDespPE{


	private BarraCompDesp compDesp;
	private Barra barra;
	private String nomPar;
	private boolean uninodal;
	
	@Override
	public void completaConstruccion() {
		barra = (Barra)participante;
		nomPar = barra.getNombre();
		
	}
	
	@Override
	public void crearBasesVar() {
		boolean uninodal = corrida.getRed().isRedUninodal(corrida.getInstanteInicial());
		if (!uninodal) {
			// Genera ángulo de barra
			for (int p = 0; p < cantPos; p++) {
				String nom = barra.getNombre();
				BaseVar bV = new BaseVar(ConCP.DELTA, nom, p, true, Constantes.VCCONTINUA, Constantes.VCLIBRE, false, 
						null, null, null, null, true);
				cargaBaseVar(bV, ConCP.DELTA, nom, p);		
			}
		}	
	}
	
	
	@Override
	public void crearBasesRest() {
		/**
		 * Restriccion de balance de potencia en la Barra
		 * 
		 * suma de potencias de generadores y falla 
		 * + potencia entrante por las ramas 
		 * + inyeccion neta de acumuladores en la barra
		 * - potencia entregada en contratos interrumpibles
		 * 	= demanda
		 *
		 */
		
		GeneradorCompDespPE gc;
		double coef;
		String nom = barra.getNombre();
		for (int p = 0; p < cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RBALBARRA, nom, p,
					cero, null, null, null, p, Constantes.RESTIGUAL);  // ATENCION: EL SEGUNDO MIEMBRO QUE SE CREA CON ESTOS DATOS SE SOBREEESCRIBE

			for (Rama r : this.barra.getRamas()) {
				RamaCompDespPE rcdPE = (RamaCompDespPE)r.getCompDespPE();
				br.contribuir(rcdPE.dameAporteABarra(this.barra, p));
			}
			

			for (Generador g : this.barra.getGeneradores()) {
				gc = (GeneradorCompDespPE) g.getCompDespPE();
				coef = 1.0;
				br.agSumVar(ConCP.POT, g.getNombre(), p, coef, null);
				
				if (g instanceof Acumulador) {
					Acumulador acum = (Acumulador) g;
					AcumuladorCompDespPE acd = (AcumuladorCompDespPE)acum.getCompDespPE();
					br.agSumVar(ConCP.POTALM, g.getNombre(), p, -coef, null);
					// TODO  ATENCION REVISAR ESTO
				}
			}
			
			ArrayList<String> auxNom = new ArrayList<String>();
			ArrayList<Double> auxCoef = new ArrayList<Double>();
			BaseSM bsm = new BaseSM(cero, auxNom, auxCoef, null, p);
			for (Demanda d : this.barra.getDemandas()) {
				DemandaCompDespPE dc = (DemandaCompDespPE) d.getCompDespPE();
				FallaCompDespPE fc = (FallaCompDespPE) d.getFalla().getCompDespPE();
				for (int e = 0; e < d.getFalla().getCantEscalones(); e++) {
					br.agSumVar(ConCP.FALLAPOTESC.get(e), fc.getNomPar(), p, 1.0, null);
				}
				bsm.getNomsVASMSum().add(dc.getNomVADem());
				bsm.getCoefsVASMSum().add(1.0);
			}
			br.setSegM(bsm);
			
			for (ImpoExpo ie : this.barra.getImpoExpos()) {
				ImpoExpoCompDespPE iec = (ImpoExpoCompDespPE)ie.getCompDespPE();
				br.agSumVar(ConCP.POTIETOTENT, ie.getNombre(), p, 1.0, null);
	
			} 
			
			for (Participante par: participantes) {
				if(par instanceof ContratoIntSist) {
					ContratoIntCompDespPE ci = (ContratoIntCompDespPE)par.getCompDespPE();
					DatosContIntCP dci = ci.getdCP();
					if(dci.getBarra().equalsIgnoreCase(barra.getNombre())) {
						BaseVar bV = dameBaseVar(ConCP.POT, dci.getNombrePart(), p);
						br.agSumVar(ConCP.POT, dci.getNombrePart(), p, -1.0, null);
					}
				}

			}	
			agrega1BR(generaNomBRest(ConCP.RBALBARRA, barra.getNombre(), p), br);
		}
		// TODO setear flotante
		
	}
	
	
	
	
	
	@Override
	public void crearBaseObj() {
		// TODO Auto-generated method stub
		
	}

	
	
	
	

	
	public BarraCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(BarraCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public Barra getBarra() {
		return barra;
	}
	public void setBarra(Barra barra) {
		this.barra = barra;
	}

	public String getNomPar() {
		return nomPar;
	}

	public void setNomPar(String nomPar) {
		this.nomPar = nomPar;
	}

	public boolean isUninodal() {
		return uninodal;
	}

	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}

	


	
	
	
	
}
