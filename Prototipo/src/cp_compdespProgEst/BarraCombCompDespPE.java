/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BarraCombCompDespPE is part of MOP.
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

import compdespacho.BarraCombustibleCompDesp;
import compdespacho.EolicoCompDesp;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.ConCP;
import datatypesProblema.DatosRestriccion;
import parque.BarraCombustible;
import parque.CicloCombinado;
import parque.Combustible;
import parque.ContratoCombustible;
import parque.ConvertidorCombustible;
import parque.DuctoCombustible;
import parque.GeneradorEolico;
import parque.GeneradorTermico;
import parque.TanqueCombustible;
import utilitarios.Constantes;

public class BarraCombCompDespPE extends CompDespPE{
	

	private BarraCombustibleCompDesp compDesp;
	private BarraCombustible bc;
	private Combustible comb;
	private String nomCom;
	private boolean uninodal;
	
	
	
	@Override
	public void completaConstruccion() {
		// TODO Auto-generated method stub
		bc = (BarraCombustible)participante;
		comb = bc.getComb();
		nomCom = comb.getNombre();
	}	
	

	@Override
	public void crearBasesVar() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void crearBasesRest() {
		/**
		 * Balance de barra de combustible en cada poste.
		 * 
		 * (Se anula la entrada neta de combustible)
		 * (El índice c es el del combustible de la barra)
		 * Los flujos están expresados en unidades de comb. por hora.
		 * Los volúmenes del balance en unidades en el poste.
		 * 
	  	 * suma en ductos entrantes de (flujo ))*durpaso
		 * - suma en ductos salientes ds (flujo ) *durpaso
		 * - suma en tanques t (varVolt)
		 * -suma en generadores g conectados a la barra [suma en postes p(enerTg,p,c)(1/pciMWh) ]
		 * + suma en ContratosComb cc (cantCombcc) *durpaso
		 * + suma en ConvertidoresCombustible entrantes cve ( flujoConv) *durpaso
		 * - suma en ConvertidoresCombustible salientes cvs ( flujoConv) *durpaso
		 * = 0
		 * 
		 * NO SE PROGRAMAN LOS CONVERTIDORES
		 */
		
		for(int p=0; p<cantPos; p++) {
			BaseRest br = new BaseRest(ConCP.RBALBARRACOMB + "_" + nomCom, nomPar, p, 
					cero , null, null, null, p, Constantes.RESTIGUAL);
			
		
			for (DuctoCombustible de : bc.getDuctosEntrantes()) {
				br.agSumVar(ConCP.FLUJO + "_" + nomCom, de.getNombre(), p, (double)dur1Pos/Constantes.SEGUNDOSXHORA, null);
			}
				
			for (DuctoCombustible de : bc.getDuctosSalientes()) {
				br.agSumVar(ConCP.FLUJO + "_" + nomCom, de.getNombre(), p, -(double)dur1Pos/Constantes.SEGUNDOSXHORA, null);
			}
			
			for (TanqueCombustible t : bc.getTanques()) {
				br.agSumVar(ConCP.VARVOL + "_" + nomCom, t.getNombre(), p, -1, null);
			}
					
			for (GeneradorTermico g : bc.getGeneradoresConectados()) {
				br.agSumVar(ConCP.ENERTPC + "_" + comb.getNombre(), g.getNombre(), p, -1 / comb.getPci(), null);			
			}
	
			for (CicloCombinado c : bc.getCiclosCombConectados()) {
				br.agSumVar(ConCP.ENERTPC + "_" + comb.getNombre(), c.getNombre(), p, -1 / comb.getPci(), null);
			}
	
			for (ContratoCombustible cc : bc.getContratos()) {
				br.agSumVar(ConCP.CAUDAL + "_" + nomCom, cc.getNombre(), p, (double)dur1Pos/Constantes.SEGUNDOSXHORA, null);				
			}
	
	//		for (ConvertidorCombustible cve : this.barra.getConvertidoresEntrantes()) {
	//			nr.agregarTermino(cve.getNFlujoConv(), (double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
	//		}
	//
	//		for (ConvertidorCombustible cvs : this.barra.getConvertidoresSalientes()) {
	//			nr.agregarTermino(cvs.getNFlujoOrigen(), -(double) barra.getDuracionPaso() / Constantes.SEGUNDOSXHORA);
	//		}	
	//		this.barra.getRedAsociada().getCompDesp().getRestricciones().put(nr.getNombre(), nr);

			agrega1BR(generaNomBRest(ConCP.RBALBARRACOMB + "_" + nomCom, nomPar, p), br);
		}
	}
	
	@Override
	public void crearBaseObj() {
		// TODO Auto-generated method stub
		
	}


	
	public BarraCombustibleCompDesp getCompDesp() {
		return compDesp;
	}
	public void setCompDesp(BarraCombustibleCompDesp compDesp) {
		this.compDesp = compDesp;
	}
	public BarraCombustible getBc() {
		return bc;
	}
	public void setBc(BarraCombustible bc) {
		this.bc = bc;
	}





	public Combustible getComb() {
		return comb;
	}





	public void setComb(Combustible comb) {
		this.comb = comb;
	}





	public boolean isUninodal() {
		return uninodal;
	}





	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}
	
	

}
