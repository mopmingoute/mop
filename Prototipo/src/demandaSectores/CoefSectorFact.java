/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CoefSectorFact is part of MOP.
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

package demandaSectores;

import java.util.Calendar;
import java.util.GregorianCalendar;

import tiempo.LineaTiempo;

/**
 * Almacena la forma de curvas de carga de un sector dado, tomando para definir la 
 * estacionalidad las energías mensuales facturadas en un año base 
 * @author ut469262
 * 
 * En datGen está el año para el que se tienen las energías mensuales del sector.
 * 
 * Para cada sector
 * EM(m) es la energía facturada en el año base en el mes m del sector
 * FH(e,t,h) es el coeficiente horario de la estación e, tipo de día t y hora h
 * la suma en las h de los coeficientes da 24.
 * 
 * EAF es la energía anual en el año base
 * EA(a) es la energía anual en el año a
 * 
 * FD(e,t) es el coeficiente diario de la estación e, tipo de dia t
 * FD(e,1)=1, los coeficientes se normalizan al primero.
 * 
 * T(d) es el tipo de día del dia d
 * M(d) es el mes del día d
 * S(d) es la estación del día d
 * 
 * Para cada mes m del año base se calcula:
 * C(m) = suma en d en el mes m [ (t(d)) ] cantidad de días 1 equivalentes en el mes m en el año base
 *
 * La potencia demandada por el sector en la hora h de un día d cualquiera del año a, P(d,h,a) sale de la fórmula
 * 
 * P(d,h) =  (E(m)/C(m)). FD(S(d),T(d)).FH(S(d),T(d),h). (EA(a)/EAF)
 *
 */


public class CoefSectorFact {
	
	private DatosGenSectores datGen;
	private int anioBaseFac;  // año base de facturación elegido
	private double[] enerMesFacMWh; // energías mensuales del sector en el año base elegido 
	private double[][][] coefHora; // primer índice estación, segundo tipo de día, tercero hora del día
								   // en cada estación y tipo de día la suma de coeficientes es 24
	private double[][] coefDia; // primer índice estación, segundo tipo de día

	public CoefSectorFact(DatosGenSectores datGen) {
		this.datGen = datGen;
		anioBaseFac = datGen.getAnioFac();
		coefHora = new double[datGen.getCantEstac()][datGen.getCantTiposDia()][];
	}


	public DatosGenSectores getDatGen() {
		return datGen;
	} 


	public void setDatGen(DatosGenSectores datGen) {
		this.datGen = datGen;
	}

	/**
	 * Normaliza los coeficientes horarios de modo de que sumen 24 en cada día
	 */
	public void normalizaCoefHor() {		
		for(int ie=0; ie<datGen.getCantEstac(); ie++) {
			for(int itip=0; itip<datGen.getCantTiposDia(); itip++) {
				double suma = 0.0;
				for(int ih=0; ih<datGen.getCantHoras(); ih++) {
					suma += coefHora[ie][itip][ih];
				}
				for(int ih=0; ih<datGen.getCantHoras(); ih++) {
					coefHora[ie][itip][ih] = coefHora[ie][itip][ih]*24/suma;
				}
			}
		}
	}
	
	
	
	/**
	 * Normaliza los coeficientes diarios de modo que el primer tipo de día tenga coeficiente 1.
	 * @return
	 */
	public void normalizaCoefDia() {		
		for(int ie=0; ie<datGen.getCantEstac(); ie++) {
			for(int itip=0; itip<datGen.getCantTiposDia(); itip++) {
				coefDia[ie][itip] = coefDia[ie][itip]/coefDia[ie][0];
			}
		}
	}	
	
	




	public double[] getEnerMesFacMWh() {
		return enerMesFacMWh;
	}


	public void setEnerMesFacMWh(double[] enerMesFacMWh) {
		this.enerMesFacMWh = enerMesFacMWh;
	}


	public double[][][] getCoefHora() {
		return coefHora;
	}


	public void setCoefHora(double[][][] coefHora) {
		this.coefHora = coefHora;
	}


	public int getAnioBaseFac() {
		return anioBaseFac;
	}


	public void setAnioBaseFac(int anioBaseFac) {
		this.anioBaseFac = anioBaseFac;
	}


	public double[][] getCoefDia() {
		return coefDia;
	}


	public void setCoefDia(double[][] coefDia) {
		this.coefDia = coefDia;
	}


	
	
}
