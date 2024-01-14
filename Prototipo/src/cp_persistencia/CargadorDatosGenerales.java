/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorDatosGenerales is part of MOP.
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

package cp_persistencia;

import java.util.ArrayList;

import cp_datatypesEntradas.DatosGeneralesCP;
import datatypesProcEstocasticos.DatosGeneralesPE;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.Constantes;
import utilitarios.LeerDatosArchivo;
import utilitarios.UtilArrays;

public class CargadorDatosGenerales {
	
	public static DatosGeneralesCP devuelveDatosGeneralesCP(String ruta) {
				
		DatosGeneralesCP dat = new DatosGeneralesCP();
		
		String dirArchivo = ruta + "/DatosGenerales.txt";		
        ArrayList<ArrayList<String>> texto;       
        texto = LeerDatosArchivo.getDatos(dirArchivo);	
        
    	AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirArchivo);    	
    	int i=0;
    	String fecha = lector.cargaPalabra(i, "INST_INI_CP"); 	
    	dat.setInstIniCPS(fecha);
    	i++;
    	int cpdia = lector.cargaEntero(i, "CANT_POS_DIA");
    	if(cpdia%Constantes.HORASXDIA!=0) {
    		System.out.println("La cantidad de postes por dia no es múltiplo de " + Constantes.HORASXDIA);
    	}
    	int cpd = lector.cargaEntero(i, "CANT_POS_DIA");
    	dat.setCantPosDia(cpd);

    	dat.setCantPosHora(cpdia/Constantes.HORASXDIA);
    	dat.setDur1Pos(Constantes.SEGUNDOSXDIA/cpdia);
    	i++;
    	int cdias = lector.cargaEntero(i, "CANT_DIAS_HORIZONTE");
    	dat.setCantDias(cdias);
    	dat.setCantPostes(cdias*cpdia);
    	i++;
    	int cetap = lector.cargaEntero(i, "CANT_ETAPAS");
    	dat.setCantEtapas(cetap);
    	i++;
    	int[] cpe = utilitarios.UtilArrays.dameArrayI(lector.cargaListaEnteros(i, "CANT_POSTES_ETAPAS"));
    	if(UtilArrays.suma(cpe)!=cdias*cpdia) {
    		System.out.println("Error en lectura de DatosGeneralesCP en la definición de cantidad de postes por etapa");
    		System.exit(1);
    	}
    	dat.setCantPostesEtapas(cpe);
    	int[] pinie = new int[cetap];
    	int[] pfine = new int[cetap];
    	int ini = 0;
    	for(int ie=0; ie<cetap; ie++) {
    		pinie[ie] = ini;
    		pfine[ie] = ini + cpe[ie]-1;
    		ini+=cpe[ie];
    	}
    	dat.setPosIniEtapa(pinie);
    	dat.setPosFinEtapa(pfine);
    	i++;
    	dat.setUsaSoloEsc0(lector.cargaBooleano(i, "USA_SOLO_ESC0"));
    	i++;  	
		String tipoVBellman = lector.cargaPalabra(i, "TIPO_VBELLMAN_CORRIDA");
		dat.setTipoVBellman(tipoVBellman);
		i++;
    	String resolvedor = lector.cargaPalabra(i, "RESOLVEDOR");
    	dat.setResolvedorLP(resolvedor);
    	i++;
    	ArrayList<String> varEstPrefijadas = lector.cargaLista(i, "VAR_ESTADO_PREFIJADAS");
    	i++;
    	int[] ordinalVEPrefijadas = new int[varEstPrefijadas.size()];
    	Double[] valorVEPrefijadas = new Double[varEstPrefijadas.size()];
    	int j=0;
    	for(String vep: varEstPrefijadas) {
    		if(!texto.get(i).get(0).equalsIgnoreCase(vep)) {
    			System.out.println("Error en lectura de variable de estado prefijada " + vep);
    			System.exit(1);
    			ordinalVEPrefijadas[j] = Integer.parseInt(texto.get(i).get(1));
    			valorVEPrefijadas[j] = Double.parseDouble(texto.get(i).get(1));
    		}
    		i++;
    	}
    	dat.setOrdinalVEPrefijadas(ordinalVEPrefijadas);
    	dat.setValorVEPrefijadas(valorVEPrefijadas);
    	System.out.println("Termina la lectura de datos generales");
		
		return dat;
		
	}

}
