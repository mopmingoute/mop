/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FallaCompDespPE is part of MOP.
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

import compdespacho.DemandaCompDesp;
import compdespacho.FallaCompDesp;
import compdespacho.RedCompDesp;
import cp_datatypesEntradas.DatosCicloCombCP;
import cp_datatypesEntradas.DatosFallaCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;
import parque.Demanda;
import parque.Falla;
import utilitarios.Constantes;

public class FallaCompDespPE extends CompDespPE{
	
	private FallaCompDesp compDesp;
	private DatosFallaCP dCP;
	private Falla falla;
	private int cantEsc;
	private double[] cosFalla;
	private boolean hayProg;
	private int diasProg;  // TODO: A ESTA VARIABLE NO SE LA EMPLEA SE SUPONE QUE EN EL MODELO CP NO SE CAMBIA LAS DECISIONES DE FALLAS PROGRAMADAS
	private ArrayList<Integer> programables;
	private boolean hayForzados;
	private ArrayList<Integer> forzados;
	
	/**
	 * TODO: ATENCIÓN LAS FALLAS INTEMPESTIVAS TIENEN QUE SUMAR 100% DE LA DEMANDA
	 * ESTO DEBE ARREGLARSE AQUÍ Y EN EL MOP CON FALLAS PROGRAMABLES E INTEMPESTIVAS
	 */
	
	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosFallaCP)dpcp;	
	
	}
	

	@Override
	public void completaConstruccion() {
		falla = (Falla)participante;
		cantEsc = falla.getCantEscalones();
		nomPar = falla.getNombre();
		for(int e=0; e<falla.getCantEscalones(); e++) {
			ConCP.FALLAPOTESC.add("POTESC_" + e);
		}
		for(int e=0; e<falla.getCantEscalones(); e++) {
			ConCP.RFALLAFORZESC.add("RFORZESC_" + e);
		}
		cosFalla = new double[cantEsc];
		for(int e=0; e<cantEsc; e++) {
			cosFalla[e]=falla.getEscalones().get(e).second;
		}
		hayProg = dCP.isHayEscProgramables();
		diasProg = dCP.getdProg();
		programables = dCP.getEscProgramables();
		hayForzados = dCP.isHayEscForzados();
		forzados = dCP.getEscForzados();
		
	}
	
	@Override
	public void crearBasesVar() {
		for (int p = 0; p < cantPos; p++) {
			String nom = falla.getNombre();
			for(int ie=0; ie<cantEsc; ie++) {
				DemandaCompDespPE dc = (DemandaCompDespPE)(falla.getDemanda().getCompDespPE());			
				String nomVADem = dc.getNomVADem();
				double profie = falla.getEscalones().get(ie).first/100;
				BaseVar bV = new BaseVar(ConCP.FALLAPOTESC.get(ie), nom, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false, 
						cero, null, profie, nomVADem, true);
				cargaBaseVar(bV,ConCP.FALLAPOTESC.get(ie), nom, p);
			}
		}	
	}
	
	@Override
	public void crearBasesRest() {
		double profie;
		DemandaCompDespPE dc = (DemandaCompDespPE)(falla.getDemanda().getCompDespPE());			
		String nomVADem = dc.getNomVADem();
		ArrayList<String> nomsVA = new ArrayList<String>();
		nomsVA.add(nomVADem);
		for(int ie: programables) {
			if(forzados.contains(ie)) {
				profie = falla.getEscalones().get(ie).first/100; // programable forzado despachado a tope
			}else {
				profie = 0.0;  // programable no forzado tiene despacho nulo
			}
			ArrayList<Double> prof = new ArrayList<Double>();
			prof.add(profie);
			for(int p=0; p<cantPos; p++) {
				BaseRest br = new BaseRest(ConCP.RFALLAFORZESC.get(ie), falla.getNombre(), p, 
						cero, nomsVA, prof, null, p, Constantes.RESTIGUAL);			
				br.agSumVar(ConCP.FALLAPOTESC.get(ie), nomPar, p, 1.0, null);	
				agrega1BR(generaNomBRest(ConCP.RFALLAFORZESC.get(ie), falla.getNombre(), p), br);		
			}
		}		
	}
	
	
	
	
	@Override
	public void crearBaseObj() {
		for(int p=0; p<cantPos; p++) {
			for(int e=0; e<cantEsc; e++) {
				BaseTermino bt = new BaseTermino(ConCP.FALLAPOTESC.get(e), nomPar, p, cosFalla[e]*dur1Pos/Constantes.SEGUNDOSXHORA, null, grafo);
			agrega1BTalObj(bt);	
			}
		}
		
	}
	
	

	
	
	public FallaCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(FallaCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public Falla getFalla() {
		return falla;
	}
	public void setFalla(Falla falla) {
		this.falla = falla;
	}





	
	

}
