/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimaCoefs is part of MOP.
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

import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocasticos.Serie;
import tiempo.LineaTiempo;
import utilitarios.Par;



/**
 * Estima coeficientes fijos para reconstruir curvas de carga horarias
 * 
 * @author ut469262
 * 
 * Se describe el cálculo si se dispone de energías semanales
 * 
 * Sea
 * cantPer cantidad de semanas del año
 * s	el índice que recorre semanas del año s=1,...52
 * d    el índice que recorre días consecutivos de la semana d=1,...7 
 * 		o bien 8 o 9 para la última semana del año
 * T(d,s) el tipo de día del día d-ésimo de la semana s
 * E(s)	la estación del año de la semana s
 * 
 * ENs	la energía de la semana s del año 
 * 
 * f(s)	el factor semanal de la semana s
 * 
 * f(t,e) el factor diario del tipo de día t, de la estación e del año.
 * 		Se conviene que el tipo de día 1 tiene factor diario igual a 1 en cada semana
 * 
 * alfa es la tasa de crecimiento semanal de la energía, tomada como dato.
 * 1+alfa = (1+tasaAnual)^(1/cantPer)
 *
 * 
 * La energía semanal Es de la semana s cumple
 * 
 * Es = edp . fs . (1+alfa)^(s-26). suma_en_d[f(T(d,E(s)),s)]
 * 
 * suma_en_d[f(T(d,E(s)),s)] puede definirse como la cantidad de día tipo 1 equivalentes de la semana s
 * 
 * edp es una constante que puede interpretarse como la energía de un día "promedio".
 * 
 * Se impone que:	suma en s [fs] = 52
 * 
 * fs = (Es/edp) / [(1+alfa)^(s-26). suma_en_d[f(T(d,E(s)),s)] 
 *  
 * De esa fórmula se saca la forma de calcular los fs, que son proporcionales
 * a: 	Es / [(1+alfa)^(s-26). suma_en_d[f(T(d,E(s)),s)]
 * 
 * La constante edp resulta de: 
 * edp = (1/52). suma_en_s{ Es / [(1+alfa)^(s-26). suma_en_d[f(T(d,E(s)),s)]] }
 *
 */

public class EstimaCoefs {
	
	private DatosGenSectores dat;
	
	
	
	/**
	 * Recibe una serie de energías (u otra variable) diarias y calcula
	 * los coeficientes diarios para cada tipo de día y estación
	 * 
	 * @param datoEnerDiarios
	 * @return los coeficientes diarios: primer índice estación, segundo tipo de día
	 * 
	 * El coeficiente diario de la estación e tipo de día t, resulta de dividir la energía promedio de
	 * los días de esa estación y tipo entre la energía promedio de los días tipo 1 de la estación e.
	 */
	public double[][] estimaCoefDia(Serie enerDiaria){
		int cantEst = dat.getCantEstac();
		int cantTDia = dat.getCantTiposDia();
		double[][] coefDias = new double[cantEst][cantTDia];
		int cantTiposDia = dat.getCantTiposDia();
    	if(!enerDiaria.getNombrePaso().equalsIgnoreCase(utilitarios.Constantes.PASODIA)) {
    		System.out.println("Se pidió mes de una serie que no es diaria");
    		if (CorridaHandler.getInstance().isParalelo()){
				////PizarronRedis pp = new PizarronRedis();
				//pp.matarServidores();
			}
    		System.exit(1);
    	}
		double[][] sumaE = new double[cantEst][cantTDia]; // energía sumada por estacion y tipo de día
		int[][] cantD = new int[cantEst][cantTDia]; // cantidad de días por estación y tipo de día 
		int cantDat = enerDiaria.getCantDatos();
		for(int t=0; t<cantDat; t++){
			int anio = enerDiaria.getAnio()[t];
			int paso = enerDiaria.getPaso()[t];  // dia del año
			int sem = enerDiaria.devuelveSemana(anio, paso); // semana del año
			int estac = dat.getDefEstac()[sem-1];
			int tipoD = tipoDiaDeAnioYDiaAnio(dat, enerDiaria, anio, paso);		
			sumaE[estac-1][tipoD-1]+= enerDiaria.getDatos()[t];
			cantD[estac-1][tipoD-1]+= 1;
		}
		for(int ie=0; ie<cantEst; ie++) {
			for(int itip=0; itip<cantTiposDia; itip++) {
				if(cantD[ie][itip]!=0) coefDias[ie][itip] = sumaE[ie][itip]/cantD[ie][itip];
			}
		}
		for(int ie=0; ie<cantEst; ie++) {
			for(int itip=1; itip<cantTiposDia; itip++) {
				coefDias[ie][itip] = coefDias[ie][itip]/coefDias[ie][0];
				coefDias[ie][0]=1.0;
			}
		}		
		return coefDias;
	}
	
	
	
	/**
	 * Devuelve el tipo de día, dado el año y el dia del año
	 * 
	 * @param anio Ejemplo 2022
	 * @param paso día del año empezando de 1 hasta 366 en años bisiestos
	 */
	public int tipoDiaDeAnioYDiaAnio(DatosGenSectores dat, Serie s1, int anio, int paso) {	
		int mes = s1.devuelveMes(anio, paso);
		int sem = s1.devuelveSemana(anio, paso);
		int estac = dat.getDefEstac()[sem-1];
		int diaDelMes = s1.devuelveDiaDelMes(anio, paso);
		int tipoD = devuelveTipoFeriadoYEspecial(dat, anio, mes, diaDelMes);
		if(tipoD==0) tipoD = s1.devuelveDiaSem(anio, paso);	
		return tipoD;
	}
	
	/**
	 * Devuelve tipo de dia si es un día feriado común o día especial (mayor o igual a 1)
	 * o cero si no es feriado ni día especial
	 */
	public int devuelveTipoFeriadoYEspecial(DatosGenSectores dat, int anio, int mes, int diaDelMes) {
		int clave = anio*10000+mes*100+diaDelMes;
		if(dat.getDiasEspecialesHorizonte().get(clave)!=null) 
			return dat.getDiasEspecialesHorizonte().get(clave);
		Par mesDia = new Par(mes,diaDelMes);
		if(dat.getFeriadosComunes().get(mesDia)!= null)
			return dat.getFeriadosComunes().get(mesDia);
		return 0;		
	}

	
	
	/**
	 * Estima coeficientes semanales.
	 * Recibe una serie de energías mensuales o semanales y produce coeficientes semanales
	 * LA SERIE DEBE TENER UNA CANTIDAD DE AÑOS COMPLETOS.
	 * Las semanas comienzan en el primer día del año y tienen 7 días excepto la última
	 * que tiene 8 o 9 si el año es bisiesto.
	 * 
	 * Requiere que estén calculados previamente los coeficientes diarios.
	 * 
	 * Se calcula la cantidad de días tipo 1 equivalentes en cada mes o semana.
	 * El coeficiente semanal de cada semana o mes es el cociente entre
	 * (suma de energías de la semana o mes en toda la serie)/(cantidad de días tipo 1 equivalente en toda la serie)
	 * 
	 * @param serie es la serie de energías mensuales o semanales
	 * @param coefDia es la matriz de coeficientes diarios (primer índice estación, segundo tipo de día)
	 * @return double[] coefSem, un coeficiente para cada una de las 52 semanas del año
	 * 
	 */
	public double[] estimaCoefSem(Serie serie, double[] tasaAnual, double[][] coefDias) {
		int cantDatos = serie.getCantDatos();
		int anioIni = serie.getAnio()[0];
		int anioFin = serie.getAnio()[cantDatos-1];
		int cantAnios = anioFin-anioIni+1;
		int cantAniosTasa = tasaAnual.length;
		if(cantAnios!=cantAniosTasa) {
			System.out.println("La cantidad de datos de tasa anual no coincide con la cantidad de años de la serie " + serie.getNombre());
		}
		double[] alfa = new double[cantAnios];
		for(int ian=0; ian<cantAnios; ian++) {
			
		}
		double[] coefSem = new double[52];
		boolean esMes = false;
		int cantPer = 52; // cantidad de meses o semanas del año según el caso
		if(serie.getNombrePaso().equalsIgnoreCase(utilitarios.Constantes.PASOMES)){
			esMes = true;
			cantPer = 12;
		}else if(!serie.getNombrePaso().equalsIgnoreCase(utilitarios.Constantes.PASOSEMANA)) {
			System.out.println("Error: se está estimando coeficientes semanales a partir de serie que no es mensual ni semanal");
			if (CorridaHandler.getInstance().isParalelo()){
				////PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}

		int itip = 0;
		double[][] cantDias1Equiv = new double[cantAnios][cantPer]; 
		for(int anio=anioIni; anio<=anioFin; anio++) {
			int cantDiasAnio = 365;
			if(tiempo.LineaTiempo.bisiesto(anio)==true) cantDiasAnio = 366;
			for(int idia=0; idia<cantDiasAnio; idia++) {
				int iper = serie.devuelveSemana(anio, idia);
				if(esMes) {
					iper = serie.devuelveMes(anio, idia);
				}
				int paso = idia+1;
				itip = tipoDiaDeAnioYDiaAnio(dat, serie, anio, paso);
				cantDias1Equiv[anio-anioIni][iper-1]++;
			}
			
		}
		
		
		// Los factores semanales son proporcionales a: Es / [(1+alfa)^(s-26). suma_en_d[f(T(d,E(s)),s)]
		
		return coefSem;
	}
	
	
	/**
	 * Estima la energía diaria para cada mes del año para los certificados
	 * Se toman las energías el año anioEstim
	 * 
	 * @param double[] enermes datos mensuales de energía
	 * @param anioEstim el año para el que se estiman energías diarias
	 * @return ener1diaMensual
	 */
	public double[] estimaEnerDiariaCert(double[] enerMes, int anioEstim) {
		double[] enerDiariaMes = new double[12];
		double[] diasPorMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		if(LineaTiempo.bisiesto(anioEstim)) diasPorMes[1] = 29;
		for(int im=1; im<=12; im++) {
			enerDiariaMes[im-1] = enerMes[im-1]/diasPorMes[im-1];
		}
		return enerDiariaMes;
	}
	
	
	/**
	 * Calcula un vector de 8760 o bien 8784 potencias horarias
	 * 
	 */
	public double[] potSectorCert(String nombreSector, double[] enerMesSector) {
		
		return new double[2];
		
	}
	
	
	
	


}
