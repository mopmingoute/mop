/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorTiposDeDia is part of MOP.
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import datatypesTiempo.DatosTiposDeDia;
import procesosEstocasticos.ProcesoDemandaEscenarios;
import utilitarios.LeerDatosArchivo;
import utilitarios.Par;

public class CargadorTiposDeDia {
	
	public static DatosTiposDeDia cargaTiposDeDia(String dirEntradas, int anioInicial, int anioFinal) {
		String arch = dirEntradas;
		DatosTiposDeDia dtd = new DatosTiposDeDia();
		ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(arch);
		int i = 0;
    	// Lee tipos de dia de los días de la semana
		if(!texto.get(i).get(0).equalsIgnoreCase("TIPOS_DIA_SEMANA")){
			System.out.println("Error en tipos de día de la semana de PEDemanda - "+ arch);
			System.exit(0);
		}else{	
			int[] tiposDias = new int[7];
			for(int id=0; id< 7; id++){
				tiposDias[id]= Integer.parseInt(texto.get(i).get(id+1));
			}
			dtd.setTiposDiasSemana(tiposDias);
		}    	
    	i++;
    	
    	// Lee feriados que no cambian con al año
		if(!texto.get(i).get(0).equalsIgnoreCase("FERIADOS_COMUNES")){
			System.out.println("Error en feriados - "+ arch);
			System.exit(0);
		}else{
			int cantFeriados = Integer.parseInt(texto.get(i).get(2));
			Hashtable<String, Par> feriados = new Hashtable<String, Par>();
			i++;
			for(int ife=0; ife< cantFeriados; ife++){
				int mesF = Integer.parseInt(texto.get(i).get(0));
				int diaF = Integer.parseInt(texto.get(i).get(1));
				String fe = claveFeriadosComunes(mesF, diaF);
				feriados.put(fe, new Par(mesF, diaF));
				i++;
			}
			dtd.setFeriadosComunes(feriados);
		} 
    	// Lee nombres y tipos de los días especiales
//		if(!texto.get(i).get(0).equalsIgnoreCase("DIAS_ESPECIALES")){
//			System.out.println("Error en DIAS_ESPECIALES en - "+ arch);
//			System.exit(0);
//		}else{
//			int cantDiasEsp = Integer.parseInt(texto.get(i).get(2));
//			Hashtable<String, Integer> diasEsp = new Hashtable<String, Integer>();
//			i++;
//			for(int ide=0; ide< cantDiasEsp; ide++){
//				String nombreDE = texto.get(i).get(0);
//				int tipoDE = Integer.parseInt(texto.get(i).get(1));
//				diasEsp.put(nombreDE, tipoDE);
//				i++;
//			}
//			dtd.setTipoDiaDiasEspeciales(diasEsp);
//		} 
		
		
		// Lee días especiales
		Hashtable<Integer, String> diasEspeciales = new Hashtable<Integer, String>(); 
		Hashtable<String, Integer> ordinalDiasEspecialesEnAnioBase = new Hashtable<String, Integer>();
//		Hashtable<String, Integer> tipoDiaDiasEspeciales = new Hashtable<String, Integer>();
		while(i<texto.size()){
			int anioE = Integer.parseInt(texto.get(i).get(0)); 
			int mesE = Integer.parseInt(texto.get(i).get(1));
			int diaE = Integer.parseInt(texto.get(i).get(2));
			String nombre = texto.get(i).get(3);
			int clave = ProcesoDemandaEscenarios.claveDiasEsp(anioE, mesE, diaE);
			diasEspeciales.put(clave, nombre);			
			if(anioE<= anioFinal && anioE>=anioInicial){
				GregorianCalendar cal = new GregorianCalendar(anioE, mesE-1, diaE);
				ordinalDiasEspecialesEnAnioBase.put(anioE+nombre, cal.get(Calendar.DAY_OF_YEAR)-1 );
//				tipoDiaDiasEspeciales.put(nombre, tipoDiaE);
			}
			i++;
		}
		dtd.setDiasEspecialesHorizonte(diasEspeciales);
		dtd.setOrdinalDiasEspecialesEnAnioBase(ordinalDiasEspecialesEnAnioBase);
//		dtd.setTipoDiaDiasEspeciales(tipoDiaDiasEspeciales);
		return dtd;
	}
	
	/**
	 * Devuelve la clave en feriadosComunes la tabla de feriados que se
	 * repiten todos los años
	 * @param mes del 1 al 12
	 * @param diaMes del 1 al 31
	 */
	public static String claveFeriadosComunes(int mes, int diaMes) {
		String result = mes + "_" + diaMes;
		return result;
		
	}
	

}
