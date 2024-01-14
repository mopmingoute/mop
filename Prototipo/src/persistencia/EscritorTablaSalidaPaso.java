/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorTablaSalidaPaso is part of MOP.
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

package persistencia;

import java.util.ArrayList;

import utilitarios.Constantes;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesResOptim.DatosHiperplano;
import datatypesSalida.DatosAcumuladorSP;
import datatypesSalida.DatosBarraCombSP;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosCicloCombSP;
import datatypesSalida.DatosConstructorHiperplanosSP;
import datatypesSalida.DatosContratoCombSP;
import datatypesSalida.DatosContratoEnergiaSP;
import datatypesSalida.DatosDemandaSP;
import datatypesSalida.DatosFallaSP;
import datatypesSalida.DatosFotovoltaicoSP;
import datatypesSalida.DatosHidraulicoSP;
import datatypesSalida.DatosImpactoSP;
import datatypesSalida.DatosImpoExpoSP;
import datatypesSalida.DatosPaso;
import datatypesSalida.DatosProveedorElecSP;
import datatypesSalida.DatosRedCombustibleSP;
import datatypesSalida.DatosRedSP;
import datatypesSalida.DatosSalidaPaso;
import datatypesSalida.DatosTermicoSP;
import datatypesSalida.DatosEolicoSP;

/**
 * Crea un String que tiene la tabla de resultados de un paso de simulación con
 * campos separados por comas
 * 
 * @author ut469262
 *
 */
public class EscritorTablaSalidaPaso {

	private EscritorHiperplanos escritorH;

	public EscritorTablaSalidaPaso() {
		escritorH = new EscritorHiperplanos();
	}

	public String escribeSalidaPaso(DatosSalidaPaso dsp) {

		// Escribe datos generales del paso
		StringBuilder sb = new StringBuilder();
		DatosPaso dpaso = dsp.getPaso();

		sb.append("PASO NUMERO :" + dpaso.getNumPaso());
		sb.append("\n");
		sb.append("Instante inicial\t");
		sb.append(dpaso.getfYHInicial());
		sb.append("\n");
		sb.append("Instante final\t");
		sb.append(dpaso.getFyHFinal());
		sb.append("\n");
		sb.append("ESCENARIO NUMERO ");
		sb.append(dpaso.getEscenario());
		sb.append("\n");

		sb.append(" \t");
		int cantPos = dpaso.getCantPostes();
		for (int j = 1; j <= cantPos; j++) {
			sb.append("Poste");
			sb.append(j);
			sb.append("\t");
		}
		sb.append("\n");
		sb.append("Duracion del poste (s)\t");
		for (int j = 1; j <= cantPos; j++) {
			sb.append(dpaso.getDurPostes()[j - 1]);
			sb.append("\t");
		}
		sb.append("\n");

		double enerMWh;
		// Escribe los balances de barras y datos de los recursos asociados a la barra
		DatosRedSP dr = dsp.getRed();
		ArrayList<DatosBarraSP> listaBarras = dr.getBarras();

		for (DatosBarraSP db : listaBarras) {

			sb.append("DATOS BARRA \t");
			sb.append(db.getNombre());
			sb.append("\n");
			// Para cada barra escribe balance
			sb.append("Balance de potencia activa en MW - barra\t");
			sb.append(db.getNombre());
			sb.append("\n\t");
			for (int j = 1; j <= cantPos; j++) {
				sb.append("Pot.Poste");
				sb.append(j);
				sb.append("\t");
			}
			sb.append("Energia(MWh)");
			sb.append("\n");

			// Térmicos: potencia
			for (DatosTermicoSP dt : db.getTermicos()) {
				sb.append(dt.getNombre());
				sb.append("\t");
				enerMWh = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					sb.append(dt.getPotencias()[j - 1]);
					sb.append("\t");
					enerMWh += dt.getPotencias()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
			}

			// Ciclos combinados: potencia
			for (DatosCicloCombSP dt : db.getCiclosCombinados()) {
				sb.append(dt.getNombre());
				sb.append("\t");
				enerMWh = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					sb.append(dt.getPotencias()[j - 1]);
					sb.append("\t");
					enerMWh += dt.getPotencias()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
			}
			
			// Hidróulicos: potencia
			for (DatosHidraulicoSP dh : db.getHidraulicos()) {
				sb.append(dh.getNombre());
				sb.append("\t");
				enerMWh = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					sb.append(dh.getPotencias()[j - 1]);
					sb.append("\t");
					enerMWh += dh.getPotencias()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
			}

			// Acumuladores: potencia
			double enerAcu;
			for (DatosAcumuladorSP ac : db.getAcumuladores()) {
				sb.append(ac.getNombre());
				sb.append("\t");
				enerMWh = 0.0;
				enerAcu = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					sb.append(ac.getPotenciasIny()[j - 1]);
					sb.append("\t");
					enerMWh += ac.getPotenciasIny()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
				sb.append(ac.getNombre() + "Acum");
				sb.append("\t");
				for (int j = 1; j <= cantPos; j++) {
					sb.append(ac.getPotenciasAlm()[j - 1]);
					sb.append("\t");
					enerAcu += ac.getPotenciasAlm()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerAcu / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
			}

			// Eólicas: potencia
			for (DatosEolicoSP de : db.getEolicos()) {
				sb.append(de.getNombre());
				sb.append("\t");
				enerMWh = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					sb.append(de.getPotencias()[j - 1]);
					sb.append("\t");
					enerMWh += de.getPotencias()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
			}
			// Fotovoltaicas: potencia
			for (DatosFotovoltaicoSP df : db.getFotovoltaicos()) {
				sb.append(df.getNombre());
				sb.append("\t");
				enerMWh = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					sb.append(df.getPotencias()[j - 1]);
					sb.append("\t");
					enerMWh += df.getPotencias()[j - 1] * dpaso.getDurPostes()[j - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n");
			}

		
			
			// ImpoExpo: potencia
			for (DatosImpoExpoSP de : db.getImpoExpos()) {
				int signo = 1;
				if (de.getOperacionCompraVenta().equalsIgnoreCase(Constantes.PROVVENTA))
					signo = -1;
				for (int ib = 0; ib < de.getPotencias().length; ib++) {
					sb.append(de.getNombre() + "bloque" + ib);
					sb.append("\t");
					enerMWh = 0.0;
					for (int j = 1; j <= cantPos; j++) {
						sb.append(de.getPotencias()[ib][j - 1] * signo);
						sb.append("\t");
						enerMWh += de.getPotencias()[ib][j - 1] * dpaso.getDurPostes()[j - 1];
					}
					sb.append(signo* enerMWh / Constantes.SEGUNDOSXHORA);
					sb.append("\n");
				}
			}

			// Fallas: potencia
			for (DatosDemandaSP d : db.getDemandas()) {
				sb.append("Falla de demanda ");
				sb.append(d.getNombre());
				DatosFallaSP df = d.getFalla();
				sb.append("\n");
				int cantEsc = df.getPotencias()[0].length;
				for (int e = 0; e < cantEsc; e++) {
					sb.append("Escalon" + Integer.toString(e) + "\t");
					enerMWh = 0.0;
					for (int ip = 1; ip <= cantPos; ip++) {
						sb.append(df.getPotencias()[ip - 1][e]);
						sb.append("\t");
						enerMWh += df.getPotencias()[ip - 1][e] * dpaso.getDurPostes()[ip - 1];
					}
					sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
					sb.append("\n");
				}
				sb.append("\n\n");
			}

			// Demandas: potencia

			for (DatosDemandaSP d : db.getDemandas()) {
				sb.append("Demanda ");
				sb.append(d.getNombre() + "\t");
				enerMWh = 0.0;
				for (int ip = 1; ip <= cantPos; ip++) {
					sb.append(d.getPotencias()[ip - 1]);
					sb.append("\t");
					enerMWh += d.getPotencias()[ip - 1] * dpaso.getDurPostes()[ip - 1];
				}
				sb.append(enerMWh / Constantes.SEGUNDOSXHORA);
				sb.append("\n\n");
			}

			// Costos marginales

			sb.append("Costos marginales (USD/MWh) \t");
			for (int ip = 1; ip <= cantPos; ip++) {
				sb.append(db.getCostoMarginal()[ip - 1]);
				sb.append("\t");
			}
			sb.append("\n\n");

			// Hiperplanos: otros datos
			DatosConstructorHiperplanosSP dch = dsp.getConsHip();
			if (dch != null) {
				int cantVC = dch.getHiperplanosActivos().get(0).getCoefs().length;
				sb.append("Hiperplanos activos y sus valores duales\n");
				sb.append("Numero\tGeneracion\t\t");
				sb.append("punto\t");
				for (int i = 0; i < cantVC - 1; i++) {
					sb.append("\t");
				}
				sb.append("\t");
				sb.append("coefs\t");
				for (int i = 0; i < cantVC - 1; i++) {
					sb.append("\t");
				}

				sb.append("tind\t");
				sb.append("vBellman\t");
				sb.append("var.dual\t");
				sb.append("\n");

				int ia = 0;
				for (DatosHiperplano dh : dch.getHiperplanosActivos()) {
					sb.append(escritorH.imprimeHiperplano(dh, null));
					ia++;
				}
			}
			sb.append("\n\n");

//			// Falla: otros datos
//			for(DatosDemandaSP d: db.getDemandas()){
//				sb.append("Falla de demanda ");
//				sb.append(d.getNombre());			
//				DatosFallaSP df = d.getFalla();
//				sb.append("\t Profundidad (MW)\n");				
//				int cantEsc = df.getPotencias()[0].length;
//				for(int e=0; e<cantEsc; e++){
//					sb.append("Escalon "+ Integer.toString(e)+"\t");					
//					for(int ip=1; ip<=cantPos; ip++){
//						sb.append(df.getProfMW()[ip-1][e]);
//						sb.append("\t");															
//					}	
//					sb.append("\n");						
//				}				
//				sb.append("\n\n");	
//			}			
//			

			// Hidróulica: otros datos
			for (DatosHidraulicoSP dh : db.getHidraulicos()) {
				sb.append(dh.getNombre());
				sb.append("\n");
				sb.append("Vol.inicial.paso (hm3)\t");
				sb.append(dh.getVolIni());
				sb.append("\n");
				sb.append("Vol.final.paso (hm3)\t");
				sb.append(dh.getVolFin()/utilitarios.Constantes.M3XHM3);
				sb.append("\n");
				sb.append("Dual de rest. volumen (USD/m3)\t");
				sb.append(dh.getDualVol());
				sb.append("\n");
				sb.append("Cota inicial a.arriba (m)\t");
				sb.append(dh.getCotaArribaIni());
				sb.append("\n");
				sb.append("Aporte (m3/s)\t");
				sb.append(dh.getAporte());
				sb.append("\n");
				sb.append("Turb.max (m3/s)\t");
				sb.append(dh.getQturMax());
				sb.append("\n");
				sb.append("Vert.max (m3/s)\t");
				sb.append(dh.getQverMax());
				sb.append("\n");
				sb.append("Erog.min (hm3)\t");
				sb.append(dh.getVolEroMin());
				sb.append("\n");
				sb.append("Valor del agua (USD/m3)\t");
				sb.append(dh.getValAgua());
				sb.append("\n");
				sb.append("Cant.mod.disp\t");
				sb.append(dh.getCantModDisp());
				sb.append("\n");
				sb.append("Turbinados (m3/s)\t");
				for (int j = 1; j <= cantPos; j++) {
					sb.append(dh.getQturb()[j - 1]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("Coef.energ (MW/(m3/s)\t");
				for (int j = 1; j <= cantPos; j++) {
					sb.append(dh.getCoefEnergMWm3s()[j - 1]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("vertido (m3/s)\t");
				for (int j = 1; j <= cantPos; j++) {
					sb.append(dh.getQvert()[j - 1]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("Erogado (hm3)\t");
				double erog = 0.0;
				for (int j = 1; j <= cantPos; j++) {
					erog += (dh.getQvert()[j - 1] + dh.getQturb()[j - 1]) * dpaso.getDurPostes()[j - 1]
							/ Constantes.M3XHM3;
				}
				sb.append(erog);
				sb.append("\n");
				sb.append("Penal.Caudal Eco.(USD)\t");
				sb.append(dh.getCostoPenalEco());
				sb.append("\n\n");
			}

			// Acumulador: otros datos
			for (DatosAcumuladorSP ac : db.getAcumuladores()) {
				sb.append(ac.getNombre());
				sb.append("\n");
				sb.append("Energia almacenada.inicial.paso MWh\t");
				sb.append(ac.getEnergAlmacIni());
				sb.append("\n");
				sb.append("Barra");
				sb.append(ac.getNombreBarra());
				sb.append("\n");
				sb.append("Valor energia acumulada USD/MWh\t");
				sb.append(ac.getValEnerg());
				sb.append("\n");
				sb.append("Potencia maxima que inyecta (MW) \t");
				sb.append(ac.getPotMax());
				sb.append("\n");
				sb.append("Potencia maxima que acumula (MW) \t");
				sb.append(ac.getPotMaxAlmac());
				sb.append("\n\n");
			}

			// Eólica: otros datos
			for (DatosEolicoSP de : db.getEolicos()) {
				sb.append(de.getNombre());
				sb.append("\n");
				sb.append("Potencia instalada\t");
				sb.append(de.getPotmax());
				sb.append("\n");
				sb.append("Factores de potencia \t");
				for (int ip = 0; ip < cantPos; ip++) {
					sb.append(de.getPotencias()[ip] / de.getPotmax());
					sb.append("\t");
				}
				sb.append("\n\n");

			}

			// Térmicos: otros datos
			for (DatosTermicoSP dt : db.getTermicos()) {
				sb.append(dt.getNombre());
				sb.append("\n");
				sb.append("Rend.minimo\t");
				sb.append(dt.getRendMin());
				sb.append("\n");
				sb.append("Rend.maximo\t");
				sb.append(dt.getRendMax());
				sb.append("\n");
				sb.append("Cant.mod.disponibles\t");
				sb.append(dt.getCantModDisp());
				sb.append("\n");
				sb.append("Costo variable medio mintec.\t");
				sb.append(dt.getCostoVarMintec());
				sb.append("\n");

				int cantComb = dt.getEnerEC().length;
				sb.append("Combustible\tCosto Variable por encima del minimo técnico");
				sb.append("\n");
				int ic = 0;
				for (String sc : dt.getListaCombustibles()) {
					sb.append(sc);
					sb.append("\t");
					sb.append(dt.getCostoVarPropC()[ic]);
					sb.append("\n");
					ic++;
				}
				
				sb.append("Energias termicas (MWh)\n");
				for (ic = 0; ic < cantComb; ic++) {
					sb.append("Comb.ordinal" + Integer.toString(ic) + "\t");
					for (int ip = 0; ip < cantPos; ip++) {
						sb.append(dt.getEnerTPC()[ip][ic]);
						sb.append("\t");
					}
					sb.append("\n");
				}
				sb.append("\n");
			}

		
		
			// Ciclos combinados: otros datos
			for (DatosCicloCombSP dt : db.getCiclosCombinados()) {
				sb.append(dt.getNombre());
				sb.append("\n");
				sb.append("Rend.minimo TG ciclo abierto\t");
				sb.append(dt.getRendMinTG());
				sb.append("\n");
				sb.append("Rend.maximo TG ciclo abierto\t");
				sb.append(dt.getRendMaxTG());
				sb.append("\n");
				sb.append("Rend.minimo TG ciclo combinado\t");
				sb.append(dt.getRendMinCC());
				sb.append("\n");
				sb.append("Rend.maximo TG ciclo combinado\t");
				sb.append(dt.getRendMaxCC());
							
				
				sb.append("\n");
				sb.append("Cant.mod.disponibles TG\t");
				sb.append(dt.getCantModTGDisp());
				sb.append("\n");
				
				sb.append("Cant.mod.disponibles CV\t");
				sb.append(dt.getCantModCVDisp());
				sb.append("\n");
				
				sb.append("Potencia TGs (abiertas y comb.)\t");
				for (int ip = 0; ip < cantPos; ip++) {
					sb.append(dt.getPotTGs()[ip]);
					sb.append("\t");
				}
				sb.append("\n");
				sb.append("Potencia ciclos vapor\t");
				for (int ip = 0; ip < cantPos; ip++) {
					sb.append(dt.getPotTVs()[ip]);
					sb.append("\t");
				}
				sb.append("\n");
				
				sb.append("Potencia de TGs en ciclo abierto\t");
				for (int ip = 0; ip < cantPos; ip++) {
					sb.append(dt.getPotAb()[ip]);
					sb.append("\t");
				}
				sb.append("\n");			
				
				sb.append("Potencia de TGs en ciclo comb. incluso la parte de ciclo de vapor\t");
				for (int ip = 0; ip < cantPos; ip++) {
					sb.append(dt.getPotComb()[ip]);
					sb.append("\t");
				}
				sb.append("\n");	
				
				sb.append("Potencia total\t");
				for (int ip = 0; ip < cantPos; ip++) {
					sb.append(dt.getPotencias()[ip]);
					sb.append("\t");
				}
				sb.append("\n");
				
	//			sb.append("Costo variable medio mintec.\t");
	//			sb.append(dt.getCostoVarMintec());
	//			sb.append("\n");
	
				int cantComb = dt.getEnerEC().length;
	//			sb.append("Combustible\tCosto Variable por encima del minimo técnico");
	//			sb.append("\n");
	//			int ic = 0;
	//			for (String sc : dt.getListaCombustibles()) {
	//				sb.append(sc);
	//				sb.append("\t");
	//				sb.append(dt.getCostoVarPropC()[ic]);
	//				sb.append("\n");
	//				ic++;
	//			}
				
				sb.append("Energias termicas (MWh)\n");
				for (int ic = 0; ic < cantComb; ic++) {
					sb.append("Comb.ordinal" + Integer.toString(ic) + "\t");
					for (int ip = 0; ip < cantPos; ip++) {
						sb.append(dt.getEnerTPC()[ip][ic]);
						sb.append("\t");
					}
					sb.append("\n");
				}
				sb.append("\n");
			}

		}

		// Combustible: otros datos
		sb.append("Datos combustibles\n\n");
		ArrayList<DatosRedCombustibleSP> redesComb = dsp.getRedesComb();
		for (DatosRedCombustibleSP drc : redesComb) {
			sb.append(drc.getNombre());
			sb.append("\n");
			for (DatosBarraCombSP dbc : drc.getBarras()) {
				// Datos de una barra del combustible
				sb.append("Barra:\t" + dbc.getNombre());
				sb.append("\n");
				sb.append("Costo marginal comb. USD/unid\t" + dbc.getCostoMarg());
				sb.append("\n");
				for (DatosContratoCombSP dcc : dbc.getContratos()) {
					sb.append("Contrato " + dcc.getNombreContrato());
					sb.append("\n");
					sb.append("CaudalMax (u/hora)\t" + dcc.getCaudalMax());
					sb.append("\n");
					sb.append("Caudal del paso\t" + dcc.getCaudal());
					sb.append("\n");
					sb.append("Costo (USD/unidad)\t" + dcc.getCostoUnit());
					sb.append("\n");
					sb.append("P.Calor�fico (MWh/unidad)\t" + drc.getCombustible().getPCI());
					sb.append("\n\n");
				}
			}
		}

		// Impactos: costos
		for (DatosImpactoSP i : dsp.getImpactos()) {
			boolean porPoste = false;
			if (i.getMagnitudPoste()!=null) porPoste = true; 
			sb.append("Impacto ambiental:\t ");

			sb.append(i.getNombre());
			sb.append("\n");			
			sb.append("Costo " + i.getCostoTotalPaso());
			sb.append("\n");			
			sb.append("Magnitud " + i.getMagnitudTotal());
			sb.append("\n\n");	
			
			if (porPoste) {
				sb.append("Costo Poste: \t"); 
				for (int j = 1; j <= cantPos; j++) {
					sb.append(i.getCostoPoste()[j - 1]);
					sb.append("\t");
					
				}
				sb.append("Magnitud Poste: \t"); 
				for (int j = 1; j <= cantPos; j++) {
					sb.append(i.getMagnitudPoste()[j - 1]);
					sb.append("\t");					
				}
				
				sb.append("\n");	
			}
			
		}
		sb.append("\n\n");

		
		// Contratos: energia, valor, energia acumulada anual 
		for (DatosContratoEnergiaSP i : dsp.getContratosEnergia()) {
			sb.append("ContratoEnergia:\t ");
			sb.append(i.getNombre());
			sb.append("\n");			
			sb.append("Valor MUSD\t" + i.getValorPasoUSD()/utilitarios.Constantes.USDXMUSD);
			sb.append("\n");			
			sb.append("Energia del paso (GWh)\t" + i.getEnergiaPasoGWh());
			sb.append("\n");
			sb.append("Potencias\t");
			for (int ip = 0; ip < cantPos; ip++) {
				sb.append(i.getPotencias()[ip]);
				sb.append("\t");
			}
			sb.append("\n");
			sb.append("Valor medio en el paso (USD/MWh)\t" + i.getValorPasoUSD()/i.getEnergiaPasoGWh()/utilitarios.Constantes.MWHXGWH);
			sb.append("\n");			
			sb.append("Energía acumulada fin paso (GWh)\t" + i.getEnergiaAcumAnioGWh());
			sb.append("\n\n");
		}
		sb.append("\n\n");		

		
		return sb.toString();
	}

}

