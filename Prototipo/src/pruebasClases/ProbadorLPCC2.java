/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProbadorLPCC2 is part of MOP.
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

package pruebasClases;



import utilitarios.DirectoriosYArchivos;

/*
 * Duración de arranques diferente para cada arranque
 *
 */

public class ProbadorLPCC2 {
	
	
	public static int cantHoras = 48;
	public static int nModTG = 2;
	public static int nModCV = 1;
	public static Double costo = 100.0;
	public static Double costoMin = 10.0;
	public static double pmax1TG = 170.0; 
	public static double pmin1TG = 60.0;
	public static double epsilon = 0.01;
	
	public static int cantTipArranques = 4;   // cantidad de tipos de arranques
	public static int[] cantArranquesPorTipo = {1, 1, 1, 1};   // cantidad de arranques de cada tipo en el dóa
	public static int[] durRampa = {4, 2, 4, 3}; 
	public static int xinicial = 1;  // puede ser 0 o 1

	
	public static int iniDem = 3;
	public static int subeDem = 8;
	public static int bajaDem = 15;
	public static int subeDem2 = 19;
	
	public static double coefZTot = 0.001;
	public static double coefZVerdadero = 50;
	
	public static double[] demanda;
	
	/**
	 * Las potencias durante las rampas son potRamp
	 * Las potencias como TG son potTG
	 * Las potencias como CC son potCC
	 * 
	 * xh h=0,..cantHoras, binarias que valen 1 si el CC estó en rógimen normal
	 * ynh, n=1,..cantTipArranques * cantArranquesPorTipo, binarias que valen 1 si se estó en rampa de arranque
	 * zTGh h=0,..cantHoras, enteras que valen la cantidad de módulos de TG  
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		String dirSalidas = "G:/PLA/Pla_datos/Archivos/ModeloOp/ModeladoCicloCombinado/EjemploLPSolve/prob2.lp";
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
		
		// Restricción x y1 y2 sólo una a lo sumo es 1 
		for(int h=0; h<cantHoras; h++){
			sb.append("x"+ h);
			for(int ia=1; ia<=cantTipArranques; ia++){
				sb.append(" + y"+ ia + h);
			}
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
	
		
		// Entrada en rógimen requiere alguna rampa previa 
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
		
		
		// No hay rampas de duración superior a durRampa horas
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
		
		// Potencia
		
		for(int h=0; h<cantHoras; h++){
			sb.append("pot" + h);
			sb.append(" - 400" + " x" + h); 
			for(int ia=1; ia<=cantTipArranques; ia++){				
				sb.append(" - 200" + " y" + ia + h);		
			}
			sb.append(" = 0");
			sb.append(";");
			sb.append("\n");	
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
		
		
		String texto = sb.toString();
		
		DirectoriosYArchivos.agregaTexto(dirSalidas, texto);
		System.out.println("fin de impresión lp");
		
		
	}




	

}
