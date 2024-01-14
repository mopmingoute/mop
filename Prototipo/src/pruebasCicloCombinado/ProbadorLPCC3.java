/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProbadorLPCC3 is part of MOP.
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

package pruebasCicloCombinado;

import utilitarios.DirectoriosYArchivos;

public class ProbadorLPCC3 {
	

	
	public static double potMin1TG = 60; // potencia mínima de un módulo TG en MW
	public static double potMax1TG = 176 ; // potencia máxima de un m�dulo TG en MW, se supone igual para todos los combustibles
	public static double potMin1CC = 90; // potencia minima de un módulo de TG en el CC en MW
	public static double[] potMax1CC = {270, 265}; // para cada combustible potencia máxima de un módulo CC en MW, se supone igual para todos los combustibles
	
	/*
	 * potencia térmica en MW para el mínimo técnico de un módulo 
	 * (es igual al mínimo técnico en MW dividido el rendimiento en el mínimo técnico) de TG y de CC. 
	 * Se supone que es igual para todos los combustibles.
	 */
	public static double potTerMintec1TG = 220;  // para 60 de potencia eléctrica 
	
	public static int cantComb = 2; // cantidad de combustibles, lugar 0 gas natural, 1 gasoil
	
	/*
	 * potencia térmica en MW por cada MW eléctrico generado por encima del mínimo técnico de la TG (adimensionado)
	 * para cada uno de los combustibles.
	 */
	public static double[] potTerPropTGComb = {3, 3};
	public static double[] potTerPropCCComb = {2, 2.2};  // lo mismo para el ciclo combinado.
			
	public static int cantHoras = 24;
	public static int nModTG = 2;
	public static int nModCV = 1;
	public static Double costo = 100.0;
	public static Double costoMin = 10.0;

	public static double epsilon = 0.01;
	
	public static int cantTipArranques = 1;   // cantidad de tipos de arranques
	public static int[] cantArranquesPorTipo = {1};   // cantidad de arranques de cada tipo en el día
	public static int[] durRampa = {4}; 
	public static double[][] potRampa = {{30, 50, 60, 80}};
	public static int xinicial = 1;  // puede ser 0 o 1

	
	public static int iniDem = 3;
	public static int subeDem = 8;
	public static int bajaDem = 15;
	public static int subeDem2 = 19;
	
	
	public static double[] demanda;
	
	/**
	 * Las potencias eléctricas durante las rampas son potRh
	 * Las potencias eléctricas como TG son potTGh
	 * Las potencias eléctricas como CC son potCCh
	 * 
	 * emin1h,.....eminKh potencias térmicas de cada combustible 1 al K destinadas a cubrir en la hora h la 
	 * energía térmica del mínimo técnico, tanto en ciclo abierto como cerrado
	 * 
	 * eprop1h,....epropKh potencias térmicas de cada combustible por encima de la energéa térmica
	 * del mínimo técnico
	 * 
	 * etot1h,....etotKh potencias térmicas totales de cada combustible
	 * 
	 * 
	 * xh h=0,..cantHoras, binarias que valen 1 si el CC está en régimen normal
	 * ynh, n=1,..cantTipArranques * cantArranquesPorTipo, binarias que valen 1 si se está en rampa de arranque
	 * zTGh h=0,..cantHoras, enteras que valen la cantidad de módulos de TG arrancados 
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		String dirSalidas = "G:/PLA/Pla_datos/Archivos/ModeloOp/ModeladoCicloCombinado/EjemploLPSolve/prob3.lp";
		if(DirectoriosYArchivos.existeArchivo(dirSalidas)) DirectoriosYArchivos.eliminaArchivo(dirSalidas);
		
		StringBuilder sb = new StringBuilder();
		
		// Carga demanda
		double[] demanda = new double[cantHoras];
		
		
		for(int h=0; h<iniDem; h++){
			demanda[h] = 0;			
		}
		
		for(int h=iniDem; h<subeDem; h++){
			demanda[h] = 150;
		}
		
		for(int h=subeDem; h<bajaDem; h++){
			demanda[h] = 250;
		}

		for(int h=bajaDem; h<subeDem2; h++){
			demanda[h] = 0;
		}

		for(int h=subeDem2; h<24; h++){
			demanda[h] = 300;
		}		
		
		for(int h=24; h<iniDem+24; h++){
			demanda[h] = 0;			
		}
		
		for(int h=iniDem+24; h<subeDem+24; h++){
			demanda[h] = 150;
		}
		
		for(int h=subeDem+24; h<bajaDem+24; h++){
			demanda[h] = 250;
		}

		for(int h=bajaDem+24; h<subeDem2+24; h++){
			demanda[h] = 0;
		}

		for(int h=subeDem2+24; h<cantHoras; h++){
			demanda[h] = 300;
		}				
		
		// Objetivo
		
		sb.append("min: ");
		for(int h=0; h<cantHoras; h++){
			sb.append(costo.toString() + " x"+ h );
			sb.append(" + ");	
		}	
		for(int ia=1; ia<=cantTipArranques; ia++){
			for(int h=0; h<cantHoras; h++){
				sb.append(costoMin.toString() + " y"+ ia + h );
				if(h<cantHoras-1 || ia!=cantTipArranques) sb.append(" + ");	
			}			
		}
		sb.append(";");	
		sb.append("\n");	
		
		// Restricci�n x y1 .... yn xTG s�lo una a lo sumo es 1 
		for(int h=0; h<cantHoras; h++){
			sb.append("x"+ h);
			for(int ia=1; ia<=cantTipArranques; ia++){
				sb.append(" + y"+ ia + h);
			}
			sb.append((1/nModTG) + " + x"+ h);
			sb.append(" <= 1 ;");
			sb.append("\n");

		}
		
		// Estado inicial en primeras durRampa[0] horas
		sb.append("x0 = " + xinicial + ";");
		sb.append("\n");
		for(int ih=1; ih<durRampa[0]; ih++){
			sb.append("x" + ih + " - x" + (ih-1) + " <= 0");
			sb.append(";");
			sb.append("\n");
		}	
	
		
		// Entrada en r�gimen requiere alguna rampa previa 
		double coef;
		for(int h=0; h<cantHoras; h++){
			boolean haycoef = false;
			for(int ia = 1; ia<=cantTipArranques; ia++){
				int dur = durRampa[ia-1];
				if(h>=dur){
					haycoef = true;					
					for(int j=h-dur; j<=h-1; j++){					
						coef = (double)1/dur;
						sb.append(coef + " ");
						sb.append("y"+ ia + j);
						sb.append(" + ");					
					}
				}
			}
			if(haycoef==true){
				sb.append(" x" + (h-1) + " - x" + h);
				sb.append(">=0");
				sb.append(";");
				sb.append("\n");	
			}
		}
		
		
		// No hay rampas de duraci�n superior a durRampa horas
		for(int ia=1; ia<=cantTipArranques; ia++){
			int dur = durRampa[ia-1];
			for(int h=0; h<cantHoras-1-dur; h++){				
				for(int j=0; j<=dur; j++){
					sb.append("y"+ ia + (h+j));
					if(j<dur) sb.append(" + ");
				}
				sb.append(" <= " + dur);
				sb.append(";");
				sb.append("\n");	
			}
		}

		// Cantidad de rampas de cada tipo de arranque
		for(int ia=1; ia<=cantTipArranques; ia++){
			for(int ih = 0; ih<cantHoras; ih++){
				sb.append("y" + ia + ih);
				if(ih != cantHoras-1) sb.append(" + ");
			}
			sb.append(" <= " + (cantArranquesPorTipo[ia-1]*durRampa[ia-1] + epsilon));
			sb.append(";");
			sb.append("\n");
		}

		// Las rampas de un tipo de arranque no empiezan hasta que no se han usado las del tipo anterior
		for(int ia=2; ia<=cantTipArranques; ia++){
			double dur = durRampa[ia-1];
			coef = (double)1/dur;
			for(int h=1; h<cantHoras; h++){
				for(int j=0; j<=h-1; j++){
					sb.append(coef + " y" + (ia-1) + j);
					if(j != h-1) sb.append("+");
				}
				sb.append("- y" + ia + h);
				sb.append(">=0");
				sb.append(";");
				sb.append("\n");
			}			
			
		}
		
		
		// Potencias t�rmicas
		for(int ic=1; ic<= cantComb; ic++){
			for(int h=0; h<cantHoras; h++){
				sb.append("potTerTotC" + ic + h + "-potTerMinC" + ic + h + "-potTerPropC" + ic + h + " = 0" );				
			}			
		}
		sb.append(";");
		sb.append("\n");		
		
		for(int ic=1; ic<= cantComb; ic++){
//			sb.append("potTerMintec1);
		}
		sb.append(";");
		sb.append("\n");
		
		// Potencia rampa
		
		for(int h=0; h<cantHoras; h++){
				
		}

		// Restricciones de demanda
		
		for(int h=0; h<cantHoras; h++){
			sb.append("pot" + h + " >= " + demanda[h]);	
			sb.append(";");
			sb.append("\n");	
		}			
		
	
		
		// Declara variables binarias x y1 y2
		sb.append("bin ");
		for(int h=durRampa[0]-1; h<cantHoras; h++){
			sb.append("x"+ h +" ,");
			for(int ia=1; ia<=cantTipArranques; ia++){
				sb.append("y"+ ia + h);
				if(ia!=(cantTipArranques) || h!= cantHoras-1) sb.append(" ,");
			}
		}
		sb.append(";");
		sb.append("\n");	
		
		
		// Declara variables enteras xTG con cantidad de m�dulos de TG en paralelo
		sb.append("int ");
		for(int h=0; h<cantHoras; h++){
			sb.append("xTG"+ h );
			if(h!= cantHoras-1) sb.append(" , ");			
		}		
		
		String texto = sb.toString();
		
		DirectoriosYArchivos.agregaTexto(dirSalidas, texto);
		System.out.println("fin de impresi�n lp");
		

	}
	

}
