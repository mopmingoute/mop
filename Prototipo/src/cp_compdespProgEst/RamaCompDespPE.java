/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RamaCompDespPE is part of MOP.
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

import compdespacho.BarraCompDesp;
import compdespacho.RamaCompDesp;
import compdespacho.TermicoCompDesp;
import cp_despacho.BaseRest;
import cp_despacho.BaseVar;
import cp_despacho.ConCP;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import parque.Barra;
import parque.GeneradorTermico;
import parque.Rama;
import utilitarios.Constantes;

public class RamaCompDespPE extends CompDespPE{
	
	private RamaCompDesp compDesp;
	
	private Rama ra;
	
	private String nomPar;
	
		
	
	@Override
	public void crearBasesVar() {
		long instanteIni = this.getCorrida().getInstanteInicial();
		double potM12 = ra.getPotenciaMaxima12().getValor(instanteIni);		
		double potM21 = ra.getPotenciaMaxima21().getValor(instanteIni);		
		String compRama = ra.getCompGeneral().getEvolucionComportamientos().get(Constantes.COMPRAMA).getValor(instanteIni);
		if (compRama.equalsIgnoreCase(Constantes.RAMASIMPLE)) {
			if (compRama.equalsIgnoreCase(Constantes.RAMASIMPLE)) {
				for(int p=0; p<cantPos; p++) {						
					BaseVar bV = new BaseVar(ConCP.POTR12, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false,
							0.0, null, potM12, null, true);
					cargaBaseVar(bV, ConCP.POTR12, nomPar, p);												
					bV = new BaseVar(ConCP.POTR21, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false,
							0.0, null, potM21, null, true);
					cargaBaseVar(bV, ConCP.POTR21, nomPar, p);		
				}
			}
		} else if (compRama.equalsIgnoreCase(Constantes.RAMADC)) {
			for (int p = 0; p < cantPos; p++) {
				BaseVar bV = new BaseVar(ConCP.POTR, nomPar, p, true, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, false,
						-potM21, null, potM12, null, true);
			}
		}
		
	}

	@Override
	public void crearBasesRest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearBaseObj() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completaConstruccion() {
		// TODO Auto-generated method stub
		
	}

	
	
	
	public BaseRest dameAporteABarra(Barra barra, int poste) {
		
		/*
		 * Devuelve la contribución de la rama this a la Barra barra para el Poste
		 * poste. 
		 */
		
		BaseRest br = null;
		
		long instanteIni = this.getCorrida().getInstanteInicial();
		String compRama = ra.getCompGeneral().getEvolucionComportamientos().get(Constantes.COMPRAMA).getValor(instanteIni);
		if (!barra.isUnica()) {
			br = new BaseRest(ConCP.RRAMAPORTA, nomPar, poste,
					      0.0, null, null, null, 0, null);  // Se carga posteSM=0 por compatibilidad, no se  usa
			
//			BaseRest(String nomRDeConCP, String nomPar, Integer ent, 
//					Double fijoSM, String nomVASM, Integer posteSM, Integer tipo)			
			if (compRama.equalsIgnoreCase(Constantes.RAMASIMPLE)) {
				if (ra.getBarra1().getNombre() == barra.getNombre()) {
					// la rama es saliente de la barra
					br.agSumVar(ConCP.POTR12, nomPar, poste, -1.0, null);
					br.agSumVar(ConCP.POTR21, nomPar, poste, 1.0 - ra.getPerdidas21().getValor(instanteIni), null);
					
				} else if (ra.getBarra2().getNombre() == barra.getNombre()) {
					// la rama es entrante de la barra
					br.agSumVar(ConCP.POTR21, nomPar, poste, -1.0, null);
					br.agSumVar(ConCP.POTR12, nomPar, poste, 1.0 - ra.getPerdidas12().getValor(instanteIni), null);
				}
			} else if (compRama.equalsIgnoreCase(Constantes.RAMADC)) {
				for (Barra b : barra.getRedAsociada().listaBarrasConectadasDC(barra)) {
					Double betaPrima = b.getRedAsociada().getCoeficienteBPrima(barra, b);
					br.agSumVar(ConCP.POTR, nomPar, poste,  -betaPrima, null);
				}
			}
		}
		return br;
		
		
		
	}

	public RamaCompDesp getCompDesp() {
		return compDesp;
	}

	public void setCompDesp(RamaCompDesp compDesp) {
		this.compDesp = compDesp;
	}

	public Rama getRa() {
		return ra;
	}

	public void setRa(Rama ra) {
		this.ra = ra;
	}







	
	

}
