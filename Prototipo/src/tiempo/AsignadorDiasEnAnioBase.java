/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AsignadorDiasEnAnioBase is part of MOP.
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

package tiempo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import datatypesTiempo.DatosTiposDeDia;
import persistencia.CargadorTiposDeDia;
import utilitarios.DirectoriosYArchivos;
import utilitarios.Par;

/**
 * Contiene métodos que permiten asignar a un día de un horizonte de tiempo a simular
 * un día asociado de un año base, por cercanía dentro del año y por ser del mismo tipo de día.
 * 
 * El resultado queda cargado en el atributo Hashtable<String, int[]> ordinalesEnAniosBase; 
 * @author UT469262
 *
 */
public class AsignadorDiasEnAnioBase {
	
	private int anioBaseInicial;   // primer año base posible, ejemplo: 2016.
	private int anioBaseFinal;   // último año base posible.
	private int anioSimInicial;   // primer año a simular para el que se quiere asociar días del año base
	private int anioSimFinal;
	
	private boolean corrigeTiposDia; // False: elige el dia de igual ordinal; True: elige el día del mismo tipo más cercano en ordinal
	
	/**
	 * 
	 * Clave: construida a partir de anioBase y anioSim con el método claveEnAniosBase
	 * Valor: para cada día del anioSim el ordinal del dia asociado en el anioBase asociado
	 */
	private Hashtable<String, int[]> ordinalesEnAniosBase; 
	
	private DatosTiposDeDia tiposDeDia;
	
	
	
	
	public AsignadorDiasEnAnioBase(int anioBaseInicial, int anioBaseFinal, int anioSimInicial, int anioSimFinal,
			DatosTiposDeDia tiposDeDia) {
		super();
		this.anioBaseInicial = anioBaseInicial;
		this.anioBaseFinal = anioBaseFinal;
		this.anioSimInicial = anioSimInicial;
		this.anioSimFinal = anioSimFinal;
//		this.corrigeTiposDia = corrigeTiposDia;
		this.tiposDeDia = tiposDeDia;
	}



	/**
	 * Carga la tabla 	ordinalesEnAniosBase
	 * Clave: construida a partir de anioBase y anioSim con el método claveEnAniosBase
	 * Valor: para cada día del anioSim el ordinal del anioBase asociado
	 *
	 * @param archOrdinales archivo de salida de resultados si imprime == true
	 * @param imprime
	 */
	public void cargaOrdinalesEnAniosBase(String archOrdinales, boolean imprime){
		ordinalesEnAniosBase = new Hashtable<String, int[]>();
		if(imprime){
			boolean existe = DirectoriosYArchivos.existeArchivo(archOrdinales);
	        if(existe) DirectoriosYArchivos.eliminaArchivo(archOrdinales);
	        DirectoriosYArchivos.agregaTexto(archOrdinales, "ORDINALES DE DIAS ASOCIADOS EN EL ANIO BASE ");
		// si se está creando el proceso desde el MOP se sobreescriben los parámetros anioHorizonteIni y cantAnios
		}
		
		// Recorre todos los años base posibles y todos los anioSim posibles
		for(int anioBase=anioBaseInicial; anioBase<=anioBaseFinal; anioBase++) {
			for(int anioSim = anioSimInicial; anioSim<=anioSimFinal; anioSim++) {
				if(anioSim==anioBase || (anioBase==anioBaseFinal && anioSim>anioBaseFinal) || (anioSim<anioBaseInicial && anioBase==anioBaseInicial)) {
			        DirectoriosYArchivos.agregaTexto(archOrdinales, "ANIO SIMULADO\t" + anioSim + "\tANIO BASE\t" + anioBase);
					int[] diaAnioBaseAsociado = new int[366];
					StringBuilder sb = new StringBuilder();
					String clave = claveEnAniosBase(anioBase, anioSim);
					// Recorre todos los días del año simulado anioSim	

					GregorianCalendar cal = new GregorianCalendar(anioSim, Calendar.JANUARY, 1);
					int cantDias = 365;
					if(cal.isLeapYear(anioSim)) cantDias = 366;
					for(int idia=0; idia<cantDias; idia++){	
						int ordinal = -1;		
						int mes = cal.get(Calendar.MONTH)+1;
						int diaMes = cal.get(Calendar.DAY_OF_MONTH);
						int diaDelAnio = cal.get(Calendar.DAY_OF_YEAR);										
						if(anioBase==anioBaseFinal && anioSim>anioBaseFinal) {
							int claveDia = claveDiasEsp(anioSim, mes, cal.get(Calendar.DAY_OF_MONTH));
							String nombreDiaEspecial = tiposDeDia.getDiasEspecialesHorizonte().get(claveDia);						
							if(nombreDiaEspecial!=null){
								// El día es una fecha especial del horizonte
								ordinal = tiposDeDia.getOrdinalDiasEspecialesEnAnioBase().get(anioBase+nombreDiaEspecial);
							}else{
								// El dia es un feriado que no cambia de fecha o un día común
								String claveFe = CargadorTiposDeDia.claveFeriadosComunes(mes, diaMes);
								Par pfe = tiposDeDia.getFeriadosComunes().get(claveFe); 
								int mesB = mes;  // mes en el año base
								int diaMesB = diaMes;  // día del mes en el año base
								GregorianCalendar calBase = new GregorianCalendar(anioBase, mesB-1, diaMesB);
								if(pfe != null){
									// El dia es un feriado que no cambia de fecha
									mesB = pfe.getInt1();
									diaMesB = pfe.getInt2();
									GregorianCalendar diaFer = new GregorianCalendar(anioBase, mesB-1, diaMesB);
									ordinal = diaFer.get(Calendar.DAY_OF_YEAR)-1;	
								}else {
									// Es un día común porque no se encuentra en las tablas
									// de días especiales ni de feriados que no cambian
									int codigoDia = cal.get(Calendar.DAY_OF_WEEK)-1; 
									int tipoDia = tiposDeDia.getTiposDiasSemana()[codigoDia];
									// Busca el día más cercano de igual ordinal o posterior en el año base, que no sea feriado y tenga el mismo tipoDia				
									int claveDiaEsp = claveDiasEsp(anioBase, mes, cal.get(Calendar.DAY_OF_MONTH));								
									int desplazamiento = 1;   
									while(tiposDeDia.getDiasEspecialesHorizonte().get(claveDiaEsp)!=null 
											|| tiposDeDia.getTiposDiasSemana()[calBase.get(Calendar.DAY_OF_WEEK)-1]!=tipoDia
											|| tiposDeDia.getFeriadosComunes().contains(new Par(calBase.get(Calendar.MONTH)+1, calBase.get(Calendar.DAY_OF_MONTH)))){
										// sigue iterando porque el dia de calBase es especial o no es del tipoDia adecuado o es un feriado
										if(calBase.get(Calendar.DAY_OF_YEAR)==cantDias) desplazamiento = -1; // si se le acaba el año base empieza a retroceder
										calBase.add(Calendar.DAY_OF_YEAR, desplazamiento);
										mesB = calBase.get(Calendar.MONTH)+1;
										diaMesB = calBase.get(Calendar.DAY_OF_MONTH);
										claveDiaEsp = claveDiasEsp(anioBase, mesB, diaMesB);
									}
									ordinal = calBase.get(Calendar.DAY_OF_YEAR)-1;						
								}
							}
						diaAnioBaseAsociado[idia] = ordinal;
						}else if(anioSim==anioBase) {
							diaAnioBaseAsociado[idia] = idia;
						}				
						
						cal.add(Calendar.DAY_OF_YEAR, 1);
						ordinalesEnAniosBase.put(clave, diaAnioBaseAsociado);
						if(imprime){						
							sb.append(anioSim);
							sb.append("\t");
							sb.append(mes);
							sb.append("\t");
							sb.append(diaMes);
							sb.append("\tOrdinal\t");
							sb.append(idia);				
							sb.append("\tOrdinalEnAnioBase\t");
							sb.append(diaAnioBaseAsociado[idia]);
							sb.append("\n");
						}						
					}
					DirectoriosYArchivos.agregaTexto(archOrdinales, sb.toString());
				}
			}
		}
	}
	
	
	
	/**
	 * Devuelve un String que sirve de clave para encontrar las listas de ordinales en un anioBase
	 * de un año anioSim de simulación del proceso
	 * @param anioBase
	 * @param anioSim
	 * @return
	 */
	public String claveEnAniosBase(int anioBase, int anioSim) {
		return Integer.toString(anioBase) + "-" + Integer.toString(anioSim);
	}
	
	
	public static int claveDiasEsp(int anioE, int mesE, int diaE) {
		int clave = anioE*10000+mesE*100+diaE;
		return clave;
	}



	public Hashtable<String, int[]> getOrdinalesEnAniosBase() {
		return ordinalesEnAniosBase;
	}


	
}
