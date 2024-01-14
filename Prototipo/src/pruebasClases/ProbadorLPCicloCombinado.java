/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProbadorLPCicloCombinado is part of MOP.
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
 * Duración de arranques igual para todos los arranques del dóa
 *
 */
public class ProbadorLPCicloCombinado {
	
	
	
	public static int cantHoras = 24;
	public static int nModTG = 2;
	public static int nModCV = 1;
	public static Double costo = 100.0;
	public static Double costoMin = 50.0;
	public static double pmax1TG = 170.0;
	public static double pmin1TG = 60.0;
	
	public static int xinicial = 1;  // puede ser 0 o 1
	public static int iniDem = 3;
	public static int subeDem = 8;
	public static int bajaDem = 15;
	public static int subeDem2 = 19;
	

	
	public static double[] demanda;
	
	
	
	public static void main(String[] args) {
		
		String dirSalidas = "G:/PLA/Pla_datos/Archivos/ModeloOp/ModeladoCicloCombinado/EjemploLPSolve/prob1.lp";
		if(DirectoriosYArchivos.existeArchivo(dirSalidas)) DirectoriosYArchivos.eliminaArchivo(dirSalidas);
		
		StringBuilder sb = new StringBuilder();
		
		// Carga demanda
		double[] demanda = new double[24];
		
		
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
		
		
		// Objetivo
		
		sb.append("min: ");
		for(int h=0; h<24; h++){
			sb.append(costo.toString() + " x"+ h );
			sb.append(" + ");	
		}		
		for(int h=0; h<24; h++){
			sb.append(costoMin.toString() + " y"+ h );
			if(h<23) sb.append(" + ");	
		}	
		sb.append(";");
		sb.append("\n");	
		
		
		// Relación zTot zVerdadero
		
		sb.append("zTot - zVerdadero >= 0");
		sb.append(";");
		sb.append("\n");	
		
		// Restricción x y no son ambas uno y son binarias
		for(int h=0; h<24; h++){
			sb.append("x"+ h + " + y"+ h + " <=1");
			sb.append(";");
			sb.append("\n");		
		}
		
		// Estado inicial en primeras 4 horas
		sb.append("x0 = " + xinicial + ";");
		sb.append("\n");
		for(int ih=1; ih<4; ih++){
			sb.append("x" + ih + " - x" + (ih-1) + " <= 0");
			sb.append(";");
			sb.append("\n");
		}		
		
		// Entrada en rógimen requiere rampa previa de 4 horas
		for(int h=0; h<20; h++){
			for(int j=0; j<=3; j++){
				sb.append("y"+ (h+j));
				if(j<3) sb.append(" + ");
			}
			sb.append(" - 4 x" + (h+4) + " + 4 x" + (h+3));
			sb.append(" >= 0");
			sb.append(";");
			sb.append("\n");	
		}	
		
		// No hay rampas de 5 horas
		for(int h=0; h<19; h++){
			for(int j=0; j<=4; j++){
				sb.append("y"+ (h+j));
				if(j<4) sb.append(" + ");
			}
			sb.append(" <= 4");
			sb.append(";");
			sb.append("\n");	
		}			

		// Potencia
		for(int h=0; h<24; h++){
			sb.append("pot" + h + " - 400" + " x" + h + " - 200" + " y" + h +" = 0");		
			sb.append(";");
			sb.append("\n");	
		}					

		// Restricciones de demanda
		
		for(int h=0; h<24; h++){
			sb.append("pot" + h + " >= " + demanda[h]);	
			sb.append(";");
			sb.append("\n");	
		}			
		
		
		// Hora del óltimo encendido
		for(int h=0; h<24; h++){
			sb.append("z" + h + " - " + h + " x" + h + " = 0" );
			sb.append(";");
			sb.append("\n");				
		}			
		
		
		for(int h=0; h<24; h++){			
			sb.append("zTot - z" + h + " >=0" );	
			sb.append(";");
			sb.append("\n");	
		}					
		
		// Declara variables binarias x y
		sb.append("bin ");
		for(int h=4; h<24; h++){
			sb.append("x"+ h +" ,");
			sb.append("y"+ h);
			if(h<23) sb.append(" ,");			
		}
		sb.append(";");
		
		
		String texto = sb.toString();
		
		DirectoriosYArchivos.agregaTexto(dirSalidas, texto);
		System.out.println("fin de impresión lp");
		
		
	}

}
