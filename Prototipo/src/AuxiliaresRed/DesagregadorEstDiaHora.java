package AuxiliaresRed;

import java.util.ArrayList;
import java.util.Hashtable;

import datatypesProcEstocasticos.DatosDesagregadorEstDiaHora;
import datatypesTiempo.DatosTiposDeDia;
import modelolineal.EstimaLineal;
import tiempo.AsignadorDiasEnAnioBase;
import tiempo.LineaTiempo;
import utilitarios.ParReales;
import utilitarios.UtilArrays;

/**
 * Reparte la demanda total de un instante a nivel de generación entre un conjunto de barras, restando unas pérdidas a la demanda total
 * y usando coeficientes de reparto estimados a partir de datos de un año base
 *  
 */
public class DesagregadorEstDiaHora extends DesagregadorPorBarras{
	
	
	/**
	 * Clave: Año
	 * Valor: potencia en MW por intervalo de muestreo y barra.
	 * primer índice cada intervalo de muestreo (en principio hora) del año
	 * segundo índice barra
	 */
	private Hashtable<Integer, double[][]> potenciasBarras;
	
	/**
	 * Clave: año
	 * Valor: para cada intervalo de muestreo del año la potencia total
	 */
	private Hashtable<Integer, double[]> potenciaTotal;
	
	/**
	 * Valor: para cada intervalo de muestreo del año base las pérdidas totales 
	 */
	private double[] perdidasAnioBase;
	
	/**
	 * Coeficientes (a , b) de la estimación para el año base de:
	 * perd(t)/potmedia = a + b (pot(t)/potmedia)**2
	 * donde potmedia es la potencia media del año
	 */
	private double[] coefsPerdidas;
	
	private static double ERR_REL_MAX; // error relativo máximo admisible de las suma de pérdidas estimadas respecto a reales en el año base
	
	/**
	 * Duración en segundos del intervalo de muestreo de la demanda o variables a repartir, 
	 * en principio 3600 segundos por hora.
	 */
	private static int durIM = utilitarios.Constantes.SEGUNDOSXHORA;
	private static int cantIMPorDia;
	
	private int cantBarras;
			
	private int anioBase;   // año base elegido
	private int anioSimInicial;   // primer año a simular 
	private int anioSimFinal;   // ultimo año a simular
	private int cantAniosSim;
	
	private LineaTiempo lt;
	
	AsignadorDiasEnAnioBase asig;
	
	private DatosTiposDeDia datosTD;
	
	private double[] fraccionesBarras; // los valores resultantes de la última invocación del método devuelveFracsBarras
	
	private double[][] factoresCrecimiento; // primer índice año, segundo índice barra

	/**
	 * Clave: nombre de la barra
	 * Valor: los coeficientes a0, a1 de la recta que da la evolución de la demanda de energía anual en MWh:  a0 + a1 (año)  año por ejemplo 2025.
	 */
	private Hashtable<String, ParReales> coefCrecimiento;
	
	public DesagregadorEstDiaHora(Hashtable<Integer, double[][]> potenciasBarras, int anioBase,
			int anioSimInicial, int anioSimFinal, LineaTiempo lt, DatosTiposDeDia datosTD, Hashtable<Integer, double[]> potenciaTotal,
			ArrayList<String> nombresBarras, Hashtable<String, ParReales> coefCrecimiento) {
		super();
		this.potenciasBarras = potenciasBarras;
		this.anioBase = anioBase;
		this.anioSimInicial = anioSimInicial;
		this.anioSimFinal = anioSimFinal;
		this.lt = lt;
		this.datosTD = datosTD;
		this.potenciaTotal = potenciaTotal;
		this.nombresBarras = nombresBarras;
		this.coefCrecimiento = coefCrecimiento;
		cantBarras = nombresBarras.size();
		fraccionesBarras = new double[cantBarras];
		if(utilitarios.Constantes.SEGUNDOSXDIA%durIM != 0) {
			System.out.println("El día no contiene una cantidad exacta de intervalos de muestreo en el desagregador por barras");
		}
		cantIMPorDia = utilitarios.Constantes.SEGUNDOSXDIA/durIM;
		
		asig = new AsignadorDiasEnAnioBase(anioBase, anioBase, anioSimInicial, anioSimFinal, datosTD);
		asig.cargaOrdinalesEnAniosBase(null, false);
		cantAniosSim = anioSimFinal-anioSimInicial;	
		factoresCrecimiento = new double[cantAniosSim][cantBarras];
		
		calculaFacCrec();
		calculaPerdidasAnioBase();
	}
	
	
	/**
	 * Calcula los factores de crecimiento de cada barra entre el año base cada año anio
	 * a partir de los coeficientes lineales de coefCrecimiento
	 */
	private void calculaFacCrec() {
		for(int ib=0; ib<cantBarras; ib++) {
			String nomb = nombresBarras.get(ib);
			ParReales pr = coefCrecimiento.get(nomb);
			for(int anio=anioSimInicial; anio<=anioSimFinal; anio++) {
				int ian = anio-anioSimInicial;			
				double factor = pr.getReal1() + pr.getReal2()*anio;
				factoresCrecimiento[ian][ib] = factor;
			}			
		}
	}

	/**
	 * Calcula pérdidas totales del año base a partir de los datos de potencia total y por barras
	 * y estima la función lineal:
	 * 
	 * perd(t)/potmedia = a + b (pot(t)/potmedia)**2
	 * donde potmedia es la potencia media del año
	 */
	private void calculaPerdidasAnioBase() {
		
		perdidasAnioBase = (double[]) potenciaTotal.get(anioBase).clone();
		int cantIManio = potenciaTotal.get(anioBase).length;
		for(int ib=0; ib<cantBarras; ib++) {
			for(int im=0; im<cantIManio; im++) {
				double pot1b = potenciasBarras.get(anioBase)[im][ib];
				perdidasAnioBase[im] -= pot1b;
			}
		}
		
		double[] aux = potenciaTotal.get(anioBase).clone();
		double potMed = utilitarios.UtilArrays.promedio(aux);
		double escalar = 1/potMed;
		double[] potNormaliz = utilitarios.UtilArrays.prodNumero(aux, escalar);
		
		double[] aux2 = perdidasAnioBase.clone();
		double perdMed = utilitarios.UtilArrays.promedio(aux2);
		escalar = 1/perdMed;
		double[] perdNormaliz = utilitarios.UtilArrays.prodNumero(aux2, perdMed);
		double[] perdNormaliz2 = utilitarios.UtilArrays.elevaALaAlfa(perdNormaliz, 2);
		
		/**
		 * Calcula coeficientes a y b 
		 */
		double [][] xmat = matrices.Oper.creaMatAPartirDeVecColumna(potNormaliz);
		
		EstimaLineal elin = new EstimaLineal(xmat, perdNormaliz2, true);
		coefsPerdidas = elin.calcCoefModelo();
		
		double enerPerdRealBaseMWh = utilitarios.UtilArrays.suma(aux2)*durIM/utilitarios.Constantes.SEGUNDOSXHORA;
		
		double[] perdsEstim = utilitarios.UtilArrays.prodNumero(elin.devuelveYEstimados(), potMed);		
		double enerPerdEstBaseMWh = utilitarios.UtilArrays.suma(perdsEstim)*durIM/utilitarios.Constantes.SEGUNDOSXHORA;
		
		System.out.println("ATENCION: Desagregador por barras: " + this.getNombre());
		System.out.println("Perdidas reales en el año base  " + enerPerdRealBaseMWh + " MWh");
		System.out.println("Perdidas estimadas en el año base  " + enerPerdEstBaseMWh + " MWh");
		double errRel = Math.abs((enerPerdEstBaseMWh-enerPerdRealBaseMWh)/enerPerdRealBaseMWh);
		System.out.println("Error relativo en valor absoluto:  |est-reales|/reales = " + errRel);
		if(errRel>ERR_REL_MAX) {
			System.out.println("ERROR NO ADMISIBLE");
			System.exit(1);
		}
	}

	@Override
	public double[] devuelveFracsBarras(long instante) {
		
		int anio = lt.anioDeInstante(instante);  // anio del instante pedido
		int ian = anio - anioSimInicial;
		int diaDelAnio = lt.diaDelAnioDeInstante(instante); // ordinal en el día simulado
		int imDelDia = lt.intMuestreoAnioDeInstante(instante, durIM);	
		if(anio>anioSimFinal) {
			System.out.println("Se pidió al desagregador " + nombre + " un año posterior al último admisible");
			System.exit(1);
		}else if(anio<anioSimInicial) {
			System.out.println("Se pidió al desagregador " + nombre + " un año anterior al primero admisible");
			System.exit(1);
		}		
		double[] fracsBarrasBase = new double[cantBarras];		
		double[] fracsBarras = new double[cantBarras];
		double[] factoresCrecBarras = factoresCrecimiento[ian];
		
		String clave = asig.claveEnAniosBase(anioBase, anio);		
		int ordinalDiaAnioBase = asig.getOrdinalesEnAniosBase().get(clave)[diaDelAnio];		
		int ordinalIMAnioBase = ordinalDiaAnioBase * cantIMPorDia + imDelDia;
		
		double[] pots1IMBarrasBase = potenciasBarras.get(anioBase)[ordinalIMAnioBase];		
		double sumaPotsBase = UtilArrays.suma(pots1IMBarrasBase);
		for(int ib=0; ib<cantBarras; ib++) {
			fracsBarrasBase[ib] = pots1IMBarrasBase[ib]/sumaPotsBase;
		}
		
		double sumaProd = 0;
		for(int ib=0; ib<cantBarras; ib++) {
			sumaProd += fracsBarrasBase[ib]*factoresCrecBarras[ib];
		}
		for(int ib=0; ib<cantBarras; ib++) {
			fracsBarras[ib] = fracsBarrasBase[ib]*factoresCrecBarras[ib]/sumaProd;
		}
		return fracsBarras;
	}


	public String claveAnioBarra(int anio, String nombreBarra) {
		String clave = anio + "-" + nombreBarra;
		return clave;
	}



	@Override
	public double devuelveFracBarra(String nombreBarra, long instante) {
		// TODO Auto-generated method stub
		int lugar = nombresBarras.indexOf(nombreBarra);
		return fraccionesBarras[lugar];
	}

}
