/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Serie is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import utilitarios.DirectoriosYArchivos;

/**
 * Almacena anio, paso y dato, para cada tiempo de una serie de tiempo de una variable aleatoria.
 * @author ut469262
 *
 */
public class Serie {

	private String nombre; 
	private String nombrePaso;
	private int cantMaxPasos;
	private int cantDatos;
	double[] datos;
	int[] anio;
	int[] paso; // número de paso dentro del año empezando en 1, ej. 1 a 52 si nombrePaso es semana.
	// datos, anio y paso tiene la misma longitud
	
	public Serie(String nombrePaso){
		this.nombrePaso = nombrePaso;
		this.cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
	}

	public Serie(String nombre, String nombrePaso, double[] datos, int[] anio, int[] paso) {
		super();
		this.nombre = nombre;
		this.nombrePaso = nombrePaso;
		this.datos = datos;
		this.anio = anio;
		this.paso = paso;
		this.cantMaxPasos = utilitarios.Constantes.CANTMAXPASOS.get(nombrePaso);
		this.cantDatos = datos.length;
	}
		
	/**
	 * Dado un ordinal de tiempo t de los datos, empezando en cero devuelve
	 * el paso del año correspondiente
	 * @param t
	 * @return
	 */	
	public int pasoDeT(int t){
		return this.getPaso()[t];
	}
	
	/**
	 * Devuelve los datos separados por paso del año, en todos los años,
	 * ordenados por el año
	 * @return datPorPaso primer índice paso, segundo índice recorre los datos del paso
	 */
	public double[][] datosPorPaso(){
		ArrayList<ArrayList<Double>> dat = new ArrayList<ArrayList<Double>>();
		for(int ip=0; ip<cantMaxPasos; ip++){
			ArrayList<Double> aux = new ArrayList<Double>();
			dat.add(aux);
		}
		for(int t=0; t<this.datos.length; t++){
			dat.get(this.paso[t] -1).add(this.datos[t]);
		}
		double[][] datPorPaso = new double[cantMaxPasos][];
		for(int ip=0; ip<cantMaxPasos; ip++){
			datPorPaso[ip] = utilitarios.UtilArrays.dameArrayD(dat.get(ip));
		}
		return datPorPaso;		
	}

	
	
	/**
	 * Crea una serie nueva que elimina los n pasos iniciales
	 * de la serie this 
	 * @return
	 */
	public Serie truncaNIniciales(int n){
		int[] anioN = utilitarios.UtilArrays.truncaNInicialesI(this.getAnio(), n);
		int[] pasoN = utilitarios.UtilArrays.truncaNInicialesI(this.getPaso(), n);
		double[] datosN = utilitarios.UtilArrays.truncaNInicialesD(this.getDatos(), n);
		Serie result = new Serie(this.getNombre(), this.getNombrePaso(),
				datosN, anioN, pasoN);		
		return result;
	}
	
	
	/**
	 * Crea una serie nueva que elimina los n pasos finales
	 * de la serie s1 
	 * @return
	 */
	public Serie truncaNFinales(int n){
		int[] anioN = utilitarios.UtilArrays.truncaNFinalesI(this.getAnio(), n);
		int[] pasoN = utilitarios.UtilArrays.truncaNFinalesI(this.getPaso(), n);
		double[] datosN = utilitarios.UtilArrays.truncaNInicialesD(this.getDatos(), n);
		Serie result = new Serie(this.getNombre(), this.getNombrePaso(),
				datosN, anioN, pasoN);		
		return result;
	}	
	
	/**
	 * Devuelve el número de paso del año empezando en 1, si a partir de paso 
	 * se avanzan avance pasos, pudiendo avance ser negativo.
	 * @param paso
	 * @param avance
	 * @param cantMaxPasos cantidad total de pasos del año
	 * @return
	 */
	public int avancePasoCircular(int paso, int avance){
		if(avance==0) return paso;
		if(avance>0) return posteriorCircular(paso, avance);
		return anteriorCircular(paso, -avance);
	}
	
	
	/**
	 * Devuelve el número de paso del año empezando en 1, si a partir de paso 
	 * se retrasan retraso pasos, con un orden circular.
	 * @param paso
	 * @param retraso >= 0
	 * @param cantMaxPasos cantidad total de pasos del año
	 * @return
	 */
    public int anteriorCircular(int paso, int retraso){
    	int result = paso - retraso;
    	if(result<1) result = cantMaxPasos + result; 
    	return result;
    }
    
	/**
	 * Devuelve el número de paso del año empezando en 1, si a partir de paso 
	 * se avanzan avance pasos, con un orden circular.
	 * @param paso
	 * @param avance >= 0
	 * @param cantMaxPasos cantidad total de pasos del año
	 * @return
	 */    
    public int posteriorCircular(int paso, int avance){
    	int result = paso + avance;
    	if(result>cantMaxPasos) result = result - cantMaxPasos;
    	return result;    
	}	
    
    /**
     * Devuelve para cada paso del año la población de valores de la serie en ese paso
     * @param radioEntorno
     * @param periodo
     * @param cantPeriodos, si es par se toma el impar inmediato mayor.
     * @return poblaciones primer índice paso del año, paso 1 en el get(0)
     * 			segundo índice recorre la poblacion de los valores del paso 
     * EJEMPLO:
     * 1  2  3  4  5  6  7  …….24 25 26 27 ……….48 49 50 51 
	 * Para la observación 25, si se toma radioEntorno =1, periodo=24, cantPeriodos=3, 
	 * las observaciones que constituyen una población son la 1,2,3, 24,25,26,48,49,50.
	 * Como radioEntorno=1, se toma la observación 24 y 26 además de la 25. 
	 * periodo es la cantidad de observaciones entre el centro de dos entornos sucesivos.
	 * Como cantPeriodos=3, se toman entornos de observaciones en tres períodos, el que contiene la observación 25, el anterior y el posterior.
     */
    public ArrayList<ArrayList<Double>> devuelvePoblacionesPasos(DefPoblacionSerie dpob){
    	int radioEntorno = dpob.getRadioEntorno();
    	int periodo = dpob.getPeriodo();
    	int cantPeriodos = dpob.getCantPeriodos();
    	int cantDatos = datos.length;
    	int radioPeriodos = cantPeriodos/2;
    	ArrayList<ArrayList<Double>> poblaciones = new ArrayList<ArrayList<Double>>();
    	for(int ip=0; ip<cantMaxPasos; ip++){
    		poblaciones.add(new ArrayList<Double>());
    	}
    	int tau=0;
    	for(int t=0; t<cantDatos; t++){
    		for(int p=-radioPeriodos; p<=radioPeriodos; p++){
    			for(int e=-radioEntorno; e<=radioEntorno; e++){
    				tau = t + p*periodo + e; 
    				if(tau>=0 && tau<cantDatos) poblaciones.get(paso[t]-1).add(datos[tau]);
    			}
    		}	
    	}
    	return poblaciones;
    }
    
    /**
     * Sustituye los ceros por un valor aleatorio uniforme entre 0 y valor
     * @return
     */
    public Serie sustituyeCeros(double valor){
    	cantDatos = this.datos.length; 
    	Serie result = new Serie(this.nombre, this.nombrePaso, new double[this.cantDatos], this.anio, this.paso);
    	for(int t=0; t<cantDatos; t++){
    		if(this.datos[t]==0){
    			result.getDatos()[t] = Math.random()*valor;
    		}else{
    			result.getDatos()[t] = this.getDatos()[t];
    		}
    	}    	
    	return result;    	
    }
    
    /**
     * Llena todos los datos de la serie this con valor
     * @param valor
     * @return
     */
    public void inicializa(double valor){
    	for(int t=0; t<cantDatos; t++){
    		this.datos[t]=valor;
    	}
    }
    
    
    /**
     * Devuelve el dia de la semana de una fecha
     * @param int anio por ejemplo 2025
     * @param int dia, ordinal del día en el año, empezando en 1
     * @return int dia de la semana empezando en 1 para el lunes y terminando en 7 domingo
     */
    public static int devuelveDiaSem(int anio, int dia) {
    	GregorianCalendar g = new GregorianCalendar(anio, 0, 1);  // se setea el primer día del año
    	g.add(Calendar.DAY_OF_MONTH, dia-1);
    	int diaS = g.get(GregorianCalendar.DAY_OF_WEEK);
    	if(diaS==0) return 7;  // es domingo
    	if(diaS>0) return diaS-1;
    	return diaS;    	
    }
    
    /**
     * Devuelve el mes del año con enero = 1
     * @param int anio por ejemplo 2025
     * @param int dia del año (ordinal del día), empezando en 1
     * @return mes del año empezando en 1
     */
    public static int devuelveMes(int anio, int dia) {
    	GregorianCalendar g = new GregorianCalendar(anio, 0, 1);  // se setea el primer día del año
    	g.add(Calendar.DAY_OF_MONTH, dia-1);
    	return g.get(GregorianCalendar.MONTH) + 1;    	    	
    }
    
    
    
    /**
     * Devuelve la semana del año empezando en 1
     * @param int anio por ejemplo 2025
     * @param int dia del año (ordinal del día), empezando en 1
     * @return semana del año empezando en 1. Los días 365 y 366 pertenecen a semana 52
     */   
    public static int devuelveSemana(int anio, int dia) {
    	int result = Math.min((dia-1)/7 + 1, 52);
    	return result;
    }
    
    
    /**
     * Devuelve el dia del mes empezando en 1
     * @param int anio por ejemplo 2025
     * @param int paso, ordinal del día en el año, empezando en 1
     * @return mes del año
     */
    public static int devuelveDiaDelMes(int anio, int dia) {
    	GregorianCalendar g = new GregorianCalendar(anio, 0, 1);  // se setea el primer día del año
    	g.add(Calendar.DAY_OF_MONTH, dia-1);
    	return g.get(GregorianCalendar.DAY_OF_MONTH);    	    	
    }   
    
    /**
     * Devuelve un entero año*10000 + mes*100 + dia del mes , a partir del año y el ordinal del dia en el año
     * @param anio
     * @param dia, ordinal del día en el año empezando en 1
     * @return
     */
    public static int devuelveStringFecha(int anio, int dia){  	
    	int result = anio*10000;
    	result = result + devuelveMes(anio, dia)*100 + devuelveDiaDelMes(anio, dia);
    	return result;
    }
    
    
    
    /**
     * Estandariza un vector double[] de datos a 0,1
     * @param datos
     * @return
     */
    public static double[] estandarizaMed0Var1(double[] datos){
    	int ndatos = datos.length;
    	double[] result = new double[ndatos]; 
    	double media = 0.0;
    	double sigma2 = 0.0;
    	int t;
    	for(t=0; t<ndatos; t++){
    		media += datos[t];
    	}
    	media = media/ndatos;
    	for(t=0; t<ndatos; t++){
    		sigma2 += Math.pow(datos[t]-media, 2);
    	}    	
    	sigma2 = sigma2/ndatos;
    	double sigma = Math.pow(sigma2, 0.5);
    	for(t=0; t<ndatos; t++){
    		result[t] = (datos[t] - media)/sigma;
    	}
    	return result;
    }
    
    
    /**
     * Calcula la media de una serie
     */
    public double media(){
    	return media(this.getDatos());
    }
    
    /**
     * Calcula el desvío estándar de una serie
     */
    public double desvioStd(){
    	return desvio(this.getDatos());
    }
    
 	
    /**
     * Calcula la media de un vector double[] datos 	
     * @param datos
     * @return
     */
    public static double media(double[] datos){ 
	  	int ndatos = datos.length;
	    double media = 0.0;
		for(int t=0; t<ndatos; t++){
			media += datos[t];
		}
		media = media/ndatos;
    	return media;
    }

    
    /**
     * Calcula el desvio de un vector double[] datos 	
     * @param datos
     * @return
     */
    public static double desvio(double[] datos){ 
	  	int ndatos = datos.length;
	  	double media = media(datos);
	  	double var= 0.0;
	    double desvio = 0.0;
    	for(int t=0; t<ndatos; t++){
    		var += Math.pow(datos[t]-media, 2);
    	}    	
    	var = var/ndatos;
    	desvio = Math.pow(var, 0.5);
		return desvio;
    }
    
    /**
     * Si nl>= 0 devuelve una nueva serie que resulta de rezagar nl pasos de tiempo
     * la serie this. Si nl<0 adelanta la serie -nl pasos
     * 
     * Si nl>0 los nl pasos de tiempo finales de la serie original se pierden
     * Si nl<0 los -nl pasos de tiempo iniciales de la serie original se pierden
     * 
     * Ejemplo si la serie s es
     * paso  1   2   3   4   5
     * dat0  10  11  14  15  16
     * 
     * la serie rezagada 1 es.
     * paso  2   3   4   5   6
     * dato  10  11  14  15  16
     * 
     * La nueva serie tiene el nombre de this, seguido de "-L"nl
     * Ejemplo this tiene nombre APORTES, la serie rezagada 2 se llama APORTES-L2
     * 
     * @param nl
     * @return
     */
    public Serie rezagaL(int nl){
    	int nlp = Math.abs(nl);
    	int cantDatos = this.getDatos().length;
    	double[] datos = new double[cantDatos-nlp];
    	int[] anio = new int[cantDatos-nlp];
       	int[] paso = new int[cantDatos-nlp]; 
       	// s recorre lugares en los arrays de la serie nueva
    	if(nl>=0){
    		for(int s=0; s<cantDatos-nl; s++){
    			datos[s]=this.getDatos()[s];
    			anio[s]=this.getAnio()[s+nl];
    			paso[s]=this.getPaso()[s+nl];
    		}
    	}else{
    		for(int s=0; s<cantDatos-nlp; s++){
    			datos[s]=this.getDatos()[s+nlp];
    			anio[s]=this.getAnio()[s];
    			paso[s]=this.getPaso()[s];
    		}     		
    	}
    	Serie sl = new Serie(this.getNombre()+"-L"+nl, this.getNombrePaso(), datos, anio, paso);
    	return sl;   	
    }
    
    /**
     * Devuelve una nueva serie con el mismo contenido de this cuyo contenido
     * está en nuevos espacios de memoria.
     * @return
     */
    public Serie clona(){
    	int[] anio2 = new int[cantDatos];
    	int[] paso2 = new int[cantDatos];
    	double[] datos2 = new double[cantDatos];
    	for(int t=0; t<cantDatos; t++){
    		anio2[t]=anio[t];
    		paso2[t]=paso[t];
    		datos2[t]=datos[t];
    	}
    	return new Serie(this.nombre, this.nombrePaso, datos2, anio2, paso2);   	
    }
    
    /**
     * Devuelve una nueva serie que resulta de aplicar a la original this
     * cotas inferior y superior.
     * 
     */
    public Serie acota(double cotaInf, double cotaSup){
    	Serie result = this.clona();
    	double valor;
    	for(int t=0; t<cantDatos; t++){
    		valor = Math.min(cotaSup, this.datos[t]);
    		valor = Math.max(valor, cotaInf);
    		result.getDatos()[t] = valor;
    	}
    	return result;
    }
    
 	
    
    @Override
    public String toString(){
    	StringBuilder sb = new StringBuilder("anio  paso  dato\n");
    	for(int t=0; t<datos.length; t++){
    		sb.append(anio[t] + "  " + paso[t] + "  " + datos[t] + "\n");
    	}
    	return sb.toString();
    }
    
	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	public double[] getDatos() {
		return datos;
	}

	public void setDatos(double[] datos) {
		this.datos = datos;
	}

	public int[] getAnio() {
		return anio;
	}

	public void setAnio(int[] anio) {
		this.anio = anio;
	}

	public int[] getPaso() {
		return paso;
	}

	public void setPaso(int[] paso) {
		this.paso = paso;
	}
	
	
	/**
	 * Devuelve el paso del año de una depurado del efecto de años bisiestos 
	 * Para paso dia en el paso 366 devuelve 365
	 * Para paso hora en las horas de 8761 al final resta 24
	 * 
	 * @paso el paso del año empezando en 1
	 */
	public int pasoDepB(int paso) {
		String nombrePaso = this.getNombrePaso();
		int cantPasosMax = 0;
		int bisiesto = 0;
		if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOSEMANA)){
			return paso;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASODIA)){
			cantPasosMax = 365;
			bisiesto = 1;
		}else if(nombrePaso.equalsIgnoreCase(utilitarios.Constantes.PASOHORA)){
			cantPasosMax = 8760;
			bisiesto = 24;
		}
		if(paso>cantPasosMax) {
			return paso - bisiesto;
		}else {
			return paso;
		}
		
	}
	
    public int getCantMaxPasos() {
		return cantMaxPasos;
	}

	public void setCantMaxPasos(int cantMaxPasos) {
		this.cantMaxPasos = cantMaxPasos;
	}


	public void setCantDatos(int cantDatos) {
		this.cantDatos = cantDatos;
	}

	
	public int getCantDatos() {
		return cantDatos;
	}

	public static void main(String[] args) {    
		int dia = devuelveDiaSem(2021, 71);
		System.out.println(dia);
    }
	
	
}



