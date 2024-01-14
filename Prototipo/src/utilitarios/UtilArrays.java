package utilitarios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.util.ArrayUtilities;

/**
 * Utilitarios que operan sobre arrays []
 * @author ut469262
 *
 */
public class UtilArrays {
	
	/**
	 * Devuelve un nuevo array ordenado en forma creciente
	 * @param a1
	 * @return
	 */
	public static double[] ordenaDouble(double[] a1){
		double[] ord = new double[a1.length];
		ArrayList<Double> lista = new ArrayList<Double>();
		for(Double d: a1){
			lista.add(d);
		}
		Collections.sort(lista);
		int i=0;
		for(Double d: lista){
			ord[i] = lista.get(i);
			i++;
		}
		return ord;
	}
	
	/**
	 * Devuelve un String[] a partir de un ArrayList<String>
	 * @param al
	 * @return
	 */
	public static String[] dameArrayS(ArrayList<String> al){
		String[] result = new String[al.size()];
        result = al.toArray(result);
		return result;
	}
	
	/**
	 * Devuelve un int[] a partir de un ArrayList<Integer>
	 * @param al
	 * @return
	 */
	public static int[] dameArrayI(ArrayList<Integer> al){
		int[] result = new int[al.size()];
		for(int i=0; i<al.size(); i++){
        	result[i] = al.get(i);
        }
		return result;
	}
	

	/**
	 * Devuelve un double[] a partir de un ArrayList<Double>
	 * @param al
	 * @return
	 */
	public static double[] dameArrayD(ArrayList<Double> al){
		double[] result = new double[al.size()];
		for(int i=0; i<al.size(); i++){
        	result[i] = al.get(i);
        }
		return result;
	}
	
	
	/**
	 * Devuelve un ArrayList<Double> a partir de un double[]
	 */
	public static ArrayList<Double> dameAListDDeArray(double[] ad){
		ArrayList<Double> result = new ArrayList<Double>();
		for(double d: ad){
			result.add(d);
		}
		return result;
	}
	
	
	/**
	 * Devuelve un double[][] a partir de un ArrayList<double[]>
	 * el primer [] del double[][] y el ArrayList recorren la misma dimension
	 */
	public static double[][] dameArrayDobleD(ArrayList<double[]> alA){
		int largo = alA.size();
		double[][] result = new double[largo][];
		for(int t=0; t<largo; t++){
			result[t] = alA.get(t);
		}
		return result;		
	}
	
	/**
	 * Devuelve un ArrayList<String> a partir de un String[]
	 */
	public static ArrayList<String> dameArrayListString(String[] aS){
		int largo = aS.length;
		ArrayList<String> result = new ArrayList<String>();
		for(int t=0; t<largo; t++){
			result.add(aS[t]);
		}
		return result;				
	}
	
	
	/**
	 * Devuelve un ArrayList<Double> a partir de un ArrayList<String>
	 * @return
	 */
	public static ArrayList<Double> dameALDouble(ArrayList<String> als){
		ArrayList<Double> result = new ArrayList<Double>();
		for(String s: als){
			result.add(Double.parseDouble(s));
		}
		return result;
	}
	
	/**
	 * Devuelve el mínimo de un array double[]
	 */
	public static double minimo(double[] ad){
		double min = Double.MAX_VALUE;
		for(double d: ad){
			if(d<min) min = d;
		}
		return min;
	}
	
	/**
	 * Devuelve el mínimo de un array double[]
	 */
	public static double maximo(double[] ad){
		double max = Double.MIN_VALUE;
		for(double d: ad){
			if(d>max) max = d;
		}
		return max;
	}
	
	/**
	 * Devuelve el máximo de un array int[]
	 */
	public static int maximo(int[] ad){
		int max = Integer.MIN_VALUE;
		for(int d: ad){
			if(d>max) max = d;
		}
		return max;
	}

	/**
	 * Devuelve el promedio de un array double[]
	 */
	public static double promedio(double[] ad){
		double suma = 0.0;
		for(double d: ad){
			suma += d;
		}
		double cantd = (double)ad.length;
		return suma/cantd;
	}
	
	
	
	/**
	 * Devuelve la suma de los elementos un array double[]
	 */
	public static double suma(double[] ad){
		double suma = 0.0;
		for(double d: ad){
			suma += d;
		}
		return suma;
	}

	/**
	 * Devuelve la suma de los elementos un array int[]
	 */
	public static int sumaArrayInt(int[] ad){
		int suma = 0;
		for(int d: ad){
			suma += d;
		}
		return suma;
	}
	
	
	/**
	 * Devuelve la suma de los elementos un array int[]
	 */
	public static double suma(int[] ad){
		int suma = 0;
		for(int d: ad){
			suma += d;
		}
		return suma;
	}
	
	/**
	 * Devuelve un nuevo array double[] que no tiene los N primeros valores de a1
	 * @param a1
	 * @return
	 */
	public static double[] truncaNInicialesD(double[] a1, int n){
		double[] result = new double[a1.length-n];
		for(int i=n; i<a1.length; i++){
			result[i-n]=a1[i];
		}
		return result;
	}
	
	/**
	 * Devuelve un nuevo array double[] que no tiene los N últimos valores de a1
	 * @param a1
	 * @return
	 */
	public static double[] truncaNFinalesD(double[] a1, int n){
		double[] result = new double[a1.length-n];
		for(int i=0; i<a1.length-n; i++){
			result[i]=a1[i];
		}
		return result;
	}
	
	/**
	 * Devuelve un nuevo array int[] que no tiene los N primeros valores de a1
	 * @param a1
	 * @return
	 */
	public static int[] truncaNInicialesI(int[] a1, int n){
		int[] result = new int[a1.length-n];
		for(int i=n; i<a1.length; i++){
			result[i-n]=a1[i];
		}
		return result;
	}
	
	/**
	 * Devuelve un nuevo array int[] que no tiene los n últimos valores de a1
	 * @param a1
	 * @return
	 */
	public static int[] truncaNFinalesI(int[] a1, int n){
		int[] result = new int[a1.length-n];
		for(int i=0; i<a1.length-n; i++){
			result[i]=a1[i];
		}
		return result;
	}
	
	/**
	 * Devuelve el producto escalar, elemento a elemento de dos arrays del mismo largo
	 * @throws Exception 
	 */
	public static double prodEscalar(double[] a1, double[] a2){
		if(a1.length != a2.length){
			System.out.println("ERROR: Error en producto escalar de arrays double[]");
			int a = 1/0;  // sirve para cortar la ejecución y que trace los llamados.
		}
		double suma = 0.0;
		for(int t=0; t<a1.length; t++){
			suma+= a1[t]*a2[t];
		}
		return suma;
	}
	
	/**
	 * Devuelve el producto del array por un único número
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double[] prodNumero(double[] a1, double v){
		double[] result = new double[a1.length];
		for(int t=0; t<a1.length; t++){
			result[t] = a1[t]*v;
		}
		return result;
	}
	
	
	/**
	 * Devuelve un nuevo array con una combinación lineal de Arrays
	 */
	public double[] combilin(ArrayList<double[]> vectores, ArrayList<Double> escalares) {
		if(vectores.size()!=escalares.size()) {
			System.out.println("Cantidad de vectores distinta de cantidad de escalares en método combilin");
			System.exit(1);
		}
		int dim = vectores.get(0).length;
		double[] result = new double[dim];
		for(int i=0; i<vectores.size(); i++) {
			double[] ad = vectores.get(i);
			if(ad.length!=dim) {
				System.out.println("Dimensión de un array equivocada en método combilin");
				System.exit(1);
			}
			i++;
			for(int j=0; j<dim; j++) {
				result[j] = result[j] + ad[j]*escalares.get(i);
			}
		}
		return result;
	}
	
	/**
	 * Devuelve el producto del array de  int por un único número
	 * @return
	 */
	public static int[] truncarArray(double[] a1){
		int[] result = new int[a1.length];
		for(int t=0; t<a1.length; t++){
			result[t] = (int) Math.floor(a1[t]);
		}
		return result;
	}
	
	
	/**
	 * Devuelve la distancia euclídea entre dos arrays
	 */
	public static double distEuclid(double[] v1, double[] v2) {
		if(v1.length != v2.length){
			System.out.println("ERROR: Error en producto escalar de arrays double[]");
			int a = 1/0;  // sirve para cortar la ejecución y que trace los llamados.
		}
		int dim = v1.length;
		double result = 0;
		for(int i=0; i<dim; i++) {
			result += (v1[i] - v2[i])*(v1[i] - v2[i]);
		}
		result = Math.pow(result,0.5);
		return result;
	}
	
	
	/**
	 * Devuelve un array suma posición a posición de otros dos
	 */
	public static double[] suma2Arrays(double[] a1, double[] a2) {
		if(a1.length != a2.length) {
			System.out.println("Se esta sumando dos arrays de distinto tamaño");
			System.exit(1);
		}
		double[] result = new double[a1.length];
		for(int i=0; i<a1.length; i++) {
			result[i] = a1[i] + a2[i];
		}
		return result;
	}
	
	
	
	/**
	 * Devuelve otro vector donde cada componente es la respectiva del vector inicial vec
	 * elevada a la potencia alfa.
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double[] elevaALaAlfa(double[] vec, double alfa) {
		int dim = vec.length;
		double[] result = new double[dim];
		for(int i=0; i<dim; i++) {
			result[i] = Math.pow(vec[i], alfa);
		}
		return result;		
	}
	
	
	/**
	 * Concantena o yuxtapone dos String[]
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static String[] yuxtaS(String[] a1, String[] a2){
		String[] result = new String[a1.length+a2.length];
		for(int i=0; i<a1.length; i++){
			result[i]=a1[i];
		}
		for(int i=0; i<a2.length; i++){
			result[i+a1.length]=a2[i];
		}
		return result;
	}
	
	/**
	 * Concantena o yuxtapone una lista de String[]
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static String[] yuxtaS(ArrayList<String[]> lista){
		int dimtot = 0;
		for(String[] s: lista) {
			dimtot += s.length;
		}
		String[] result = new String[dimtot];
		int ind = 0;
		for(String[] s: lista){
			for(int j=0; j<s.length; j++) {
				result[ind] = s[j];
				ind++;
			}
		}
		return result;
	}
	

	
	/**
	 * Concantena o yuxtapone dos double[]
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double[] yuxtad(double[] a1, double[] a2){
		double[] result = new double[a1.length+a2.length];
		for(int i=0; i<a1.length; i++){
			result[i]=a1[i];
		}
		for(int i=0; i<a2.length; i++){
			result[i+a1.length]=a2[i];
		}
		return result;
	}
	
	/**
	 * Concantena o yuxtapone una lista de double[]
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static double[] yuxtad(ArrayList<double[]> lista){
		int dimtot = 0;
		for(double[] s: lista) {
			dimtot += s.length;
		}
		double[] result = new double[dimtot];
		int ind = 0;
		for(double[] s: lista){
			for(int j=0; j<s.length; j++) {
				result[ind] = s[j];
				ind++;
			}
		}
		return result;
	}
	
	
	public static double[] sumaUnEscalar(double[] vec, double esc) {
		double[] result = new double[vec.length];
		for(int i=0; i<vec.length; i++) {
			result[i] = vec[i] + esc;
		}
		return result;		
		
	}
	
	/**
	 * Devuelve un ArrayList<Integer> a partir de un int[]
	 */
	public static ArrayList<Integer> dameAListI(int[] aint) {
		ArrayList<Integer> al = new ArrayList<Integer>();
		for(int i=0; i<aint.length; i++) {
			al.add(aint[i]);
		}
		return al;
		
	}
	
	
	/**
	 * Devuelve una matriz transpuesta de otra de reales double[][]
	 */
	public static double[][] devuelveTranspuesta(double[][] mat){
		int nfil = mat.length;
		int ncol = mat[0].length;
		double[][] mattr = new double[ncol][nfil];
		for(int i=0; i<nfil; i++) {
			for(int j=0; j<ncol; j++) {
				mattr[j][i] = mat[i][j];
			}
		}
		return mattr;
	}
	
	/**
	 * Devuelve una matriz transpuesta de otra de reales Double[][]
	 */
	public static Double[][] devuelveTranspuesta(Double[][] mat){
		int nfil = mat.length;
		int ncol = mat[0].length;
		Double[][] mattr = new Double[ncol][nfil];
		for(int i=0; i<nfil; i++) {
			for(int j=0; j<ncol; j++) {
				mattr[j][i] = mat[i][j];
			}
		}
		return mattr;
	}
	
	/**
	 * Devuelve boleano indicando si dos arrays son iguales
	 */
	public static boolean sonIguales(int[] arr1, int[] arr2)
	{
		for(int i=0; i<arr1.length; i++) {
			if(arr1[i] != arr2[i]){
				return false;
			}
		}
		return true;
	}

	/**
	 * Devuelve el producto del array de  int por un único número
	 * @return
	 */
	public static double[] arrayIntProdNumero(int[] a1, double v){
		double[] result = new double[a1.length];
		for(int t=0; t<a1.length; t++){
			result[t] = a1[t]*v;
		}
		return result;
	}

	/**
	 * Devuelve un ArrayList<Integer> a partir de un int[]
	 */
	public static boolean estaIntEnArray(int[] arr, int num) {
		boolean ret = false;
		for(int i=0; i<arr.length; i++) {
			if(arr[i]== num){
				ret = true;
			}
		}
		return ret;

	}
}
