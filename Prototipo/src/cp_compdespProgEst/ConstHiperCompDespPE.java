package cp_compdespProgEst;

import compdespacho.ConstructorHiperplanosCompDesp;
import compdespacho.EolicoCompDesp;
import cp_datatypesEntradas.DatosConstHipCP;
import cp_datatypesEntradas.DatosHidroCP;
import cp_datatypesEntradas.DatosHiperplanoCP;
import cp_datatypesEntradas.DatosPartCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseTermino;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import cp_despacho.GrafoEscenarios;
import cp_despacho.NombreBaseVar;
import cp_nuevosParticipantesCP.ConstructorHiperplanosPE;
import futuro.Hiperplano;
import parque.ConstructorHiperplanos;
import parque.GeneradorEolico;
import utilitarios.Constantes;

public class ConstHiperCompDespPE extends CompDespPE{
	
	
	private ConstructorHiperplanosPE cHPE;
	
	private DatosConstHipCP dCP;
	
	
	@Override
	public void cargaDPCPYPart(DatosPartCP dpcp) {
		dCP = (DatosConstHipCP)dpcp;	
	}
	
	
	@Override
	public void completaConstruccion() {
		cHPE = (ConstructorHiperplanosPE)participante;
		nomPar = dCP.getNombrePart();
		usoHip = dCP.isUsoHip();
	}
	
	
	@Override
	public void crearBasesVar() {
		if(usoHip) {
			int dominio = utilitarios.Constantes.VCLIBRE;
			if(dCP.isVbnoneg()) dominio = utilitarios.Constantes.VCPOSITIVA;
			BaseVar bV = new BaseVar(ConCP.ZVB, nomPar, cantPos-1, true, Constantes.VCCONTINUA, dominio, false, 
					null, null, null, null, true);
			cargaBaseVar(bV, ConCP.ZVB, nomPar, cantPos-1);	
		}
	}
	
	
	/**
	 * Cada hiperplano tiene la forma
	 * 
	 * zvb >= tindi + suma en ve (coefve * nombre_variable_estado_ve(cantPos-1))
	 * 
	 * zvb - suma en ve (coefve * nombre_variable_estado_ve(cantPos-1)) >= tindi 
	 * 
	 */
	@Override
	public void crearBasesRest() {
		if(usoHip) {
			int ih=0;
			for(DatosHiperplanoCP h: dCP.getHiperplanos()) {			
				Double tind = h.getCoeficientes()[0]/ConCP.ESCALADOR_DIVISOR;
				BaseRest br = new BaseRest(ConCP.RHIP + "_" + ih , nomPar, cantPos-1, 
						tind, null, null, null, cantPos-1, Constantes.RESTMAYOROIGUAL);	
				int icoef = 1;
				br.agSumVar(ConCP.ZVB, nomPar, cantPos-1, 1.0/ConCP.ESCALADOR_DIVISOR, null);
				for(String nvhip: h.getNombresVE()) {
					NombreBaseVar nbv = this.getcHPE().getNombresVEParticipantes().get(nvhip);
					br.agSumVar(nbv.getNomVar() , nbv.getNomPar(), nbv.getEntero(), -h.getCoeficientes()[icoef]/ConCP.ESCALADOR_DIVISOR, null);
					icoef++;
				}
				agrega1BR(generaNomBRest(ConCP.RHIP + "_" + ih, nomPar, cantPos-1), br);	
				ih++;
			}
		}
	}
	
	
	
	@Override
	public void crearBaseObj() {
		if(usoHip) {
			BaseTermino bt = new BaseTermino(ConCP.ZVB, nomPar, cantPos-1, 1.0, null, grafo);
			agrega1BTalObj(bt);	
		}		
	}



	public ConstructorHiperplanosPE getcHPE() {
		return cHPE;
	}


	public void setcHPE(ConstructorHiperplanosPE cHPE) {
		this.cHPE = cHPE;
	}


	public DatosConstHipCP getdCP() {
		return dCP;
	}


	public void setdCP(DatosConstHipCP dCP) {
		this.dCP = dCP;
	}
	
	

	

}
