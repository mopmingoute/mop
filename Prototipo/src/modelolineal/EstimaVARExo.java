
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimaVARExo is part of MOP.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modelolineal;

import java.util.ArrayList;
import matrices.Oper;
import java.util.Random;

import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;
import matrices.XcargaDatos;

/**
 *
 * @author Maite
 * Estima las matrices A y la matriz B de [k x k] en:
 * 
 * y(t) = v + A1 y(t-1) + A2 y(t-2) + ...Ap y(t-p) + C ex(t) + u(t)
 * u=Be, 
 * E(e) = 0
 * e multivariado normal de dimension k independientes entre sí.
 * v es no nulo sólo si indep=true.
 * ex vector de m variables exógenas
 * C matriz de k filas y m columnas [k x m]
 * 
 * ATENCIÓN: LOS NOMBRES DE VARIABLES NO SON LOS MISMOS QUE LOS DE LA CLASE ProcesoVARMA
 * 
 */

public class EstimaVARExo {

    private double[][] datos;  // datos de las variables yt, primer índice tiempo creciente, segundo índice variable
    private double[][] datosExo; // datos de las variables exógenas, cada variable tiene tata cantidad de datos como las yt, aunque los primeros lag datos no se usan
    private boolean indep;   // si es true cada ecuación tiene término independiente
    private boolean exo; // true si hay variables exógenas
    private int lags;  // el valor de p de la fórmula de arriba
    private int cantDatos;  
    private int cantVars;
    private int cantExo;  // el papel de m en la fórmula de arriba
    private double[][] v;    // matriz [k][1] de términos independientes si existen 
    private ArrayList<double[][]> matsA;  // matrices cuadradas de la regresión
    private double[][] resids; // residuos de la estimación, primer índice tiempo, segundo índice variable
    private double[][] sigma;  // matriz de covarianza de residuos
    private double[][] b;  // matriz de cambio de base de residuos
    private double[][] bt;  // transpuesta e inversa de la matriz de cambio de base de residuos    
    private double[][] diag; // matriz de covarianzas en base ortonormal
    private double[][] c;  // efectos de las variables exógenes, primer índice variable exógena, segundo ecuación

    /**
     * Si datosExo es null, no hay datos exógenos y exo = false;
     * @param datos
     * @param datosExo 
     */
    public EstimaVARExo(double[][] datos, double[][] datosExo) {
        this.datos = datos;
        this.datosExo = datosExo;
        cantDatos = datos.length;
        cantVars = datos[0].length;
        cantExo = 0;
        exo = false;
        if(datosExo!=null){
            cantExo = datosExo[0].length;
            exo = true;
        }
        
        matsA = new ArrayList<double[][]>();        
    }

    public ArrayList<double[][]> getMatsA() {
        return matsA;
    }

    public int getLags() {
        return lags;
    }

    public double[][] getV() {
        return v;
    }
    
    

    public double[][] getResids() {
        return resids;
    }

    public double[][] getSigma() {
        return sigma;
    }

    public double[][] getB() {
        return b;
    }

    public double[][] getBt() {
        return bt;
    }

    public double[][] getC() {
        return c;
    }

    public void setC(double[][] c) {
        this.c = c;
    }        

    public double[][] getDiag() {
        return diag;
    }
    
       
    /**
     * Estima por mínimos cuadrados ordinarios por separado cada ecuación del VAR
     * @param cantLags
     * @param esIndep 
     */
    public void estimaVAR(int cantLags, boolean esIndep){
        lags = cantLags;
        indep = esIndep;
        int il, iec, ivar, t;    
        // residuos almacena los residuos de estimación: primer indice tiempo, segundo índice ecuacion.
        double[][] residuos = new double[cantDatos-lags][cantVars];
        int intIndep = 0;
        if(indep){
            v = new double[cantVars][1];
            intIndep = 1;
        }
        for(il = 1; il<= lags; il++){
            double[][] A = new double[cantVars][cantVars];
            matsA.add(A);
        }
        c = new double[cantVars][cantExo];
        double[][] xmat = null;
        for(iec=1; iec<=cantVars; iec++ ){
            /**
             * Llamemos k a la cantidad de variables cantVars
             * Se hace la regresión lineal MCO para la variable iec+1-ésima.
             * Estima la fila iec+1 de cada matriz A, hay lags filas a estimar
             * Se estiman lags*k coeficientes si no hay término indendiente y si hay
             * término independiente k adicionales.
             */  

            xmat = new double[cantDatos-lags][intIndep + cantVars*lags + cantExo];
            double[] y = new double[cantDatos-lags];
            for(t=lags+1; t<=cantDatos; t++){
                y[t-lags-1]=datos[t-1][iec-1];
                if(indep) xmat[t-lags-1][0]=1.0;
                for(il=1; il<=lags; il++){
                    for(ivar = 1; ivar<=cantVars; ivar++){
                        xmat[t-lags-1][intIndep + (il-1)*cantVars+ivar-1] = datos[t-1-il][ivar-1];
                    }
                } 
                for(int iex=1; iex<=cantExo; iex++){
                    xmat[t-lags-1][intIndep + lags*cantVars + iex-1] = datosExo[t-1][iex-1];
                }
            }
            EstimaLineal el = new EstimaLineal(xmat, y, false);
            el.calcCoefModelo();
            double[] filasIec;
            filasIec = el.getB();
            if(indep) v[iec-1][0] = filasIec[0];
            for(il=1; il<=lags; il++){
                double[][]  A = matsA.get(il-1);
                for(ivar=1; ivar<=cantVars; ivar++){
                    A[iec-1][ivar-1] = filasIec[intIndep+(il-1)*cantVars+ivar-1];
                }
            }
            for(int iex=1; iex<=cantExo; iex++){                
                c[iec-1][iex-1]=filasIec[intIndep+lags*cantVars+iex-1];                
            }
            double[] resIec = el.getResid();
            for(t=lags+1; t<=cantDatos; t++){
                residuos[t-lags-1][iec-1] = resIec[t-lags-1];
            }            
        }
        // estima matriz sigma de covarianzas de residuos u de las ecuaciones reducidas
        sigma = new double[cantVars][cantVars];
        for(int ifil=0; ifil< cantVars; ifil++){
            for(int icol=0; icol< cantVars; icol++){  
                sigma[ifil][icol] = 0.0;
                for(t=lags+1; t<= cantDatos; t++){
                    // OJOJOJOJO ACA DIVISOR DEL ESTIMADOR
                    sigma[ifil][icol] += residuos[t-lags-1][ifil]*residuos[t-lags-1][icol]/(cantDatos-cantVars);
                }
            }                                      
        }
        // estima la matriz b de cambio de base
        b = new double[cantVars][cantVars]; 
        bt = new double[cantVars][cantVars]; 
        diag = new double[cantVars][cantVars]; 
        Oper.eigenDecomp(sigma, diag, b, bt);               
    }
    
    
    /**
     * Crea los archivos de entrada para CargadorPEVAR a partir del VAR
     * estimado
     * @param arch
     */
    public String devuelveTextoParametrosVAR(String[] nombresVars, String[] nombresExo, String formaEstimacion){

    	StringBuilder sb = new StringBuilder();
    	sb.append("FORMA_ESTIMACION\t" + formaEstimacion);
    	sb.append("\n");
    	sb.append("CANT_VARIABLES\t" + cantVars);
    	sb.append("\n");
    	sb.append("CANT_REZAGOS_AR\t" + lags);    
    	sb.append("\n");
    	sb.append("CANT_REZAGOS_MA\t" + "0");    
    	sb.append("\n");    	
    	sb.append("CANT_INNOVACIONES\t" + cantVars);    
    	sb.append("\n");
    	sb.append("NOMBRES_SERIES\t");    
    	for(String s: nombresVars){
    		sb.append(s + "\t");    		
    	}
    	sb.append("\n");
    	sb.append("NOMBRES_VAR_EXOGENAS\t");        	
    	for(String s: nombresExo){
    		sb.append(s + "\t");    		
    	}
    	sb.append("\n");
    	
    	// imprime las matrices A autoregresivas    	
    	for(int p = 0; p<lags; p++){
    		String etiqueta = "A" + (p+1);
    		sb.append(etiqueta + "\n");
    		sb.append(matrices.Oper.imprime(matsA.get(p)));
    	}
    	// no hay matrices de medias móviles
    	
    	// imprime los vectores de efectos de variables exógenas
    	for(int ive=0; ive<cantExo; ive++){
    		sb.append(nombresExo[ive] + "\n");
    		for(double d1: c[ive]){
    			sb.append(d1 + "\t");
    		}
        	sb.append("\n");
    	}
    	
		// imprime la matriz B de vectores propios para generar u(t) a partir de e(t) distribuido N(0, I)
    	sb.append("B\n");
		sb.append(matrices.Oper.imprime(b));
			
		// imprime la matriz D diagonal de valores propios para generar u(t) a partir de e(t) distribuido N(0, I)
    	sb.append("D   \n");
		sb.append(matrices.Oper.imprime(diag));
		return sb.toString();
    }
    
    
    
    
    /**
     * 
     * @param cantLags
     * @param esIndep
     * @param gama
     * @param r 
     */
    public void estimaVARCRestric(int cantLags, boolean esIndep, double[][] gama, double[][] r){
        
    }
    
    public static void main(String[] args) throws XcargaDatos{
        String dirDatos = "C:/Mario/PruebasMatrices/PruebasMatrices/datosVar.txt";
        if(DirectoriosYArchivos.existeArchivo(dirDatos)) DirectoriosYArchivos.eliminaArchivo(dirDatos);
        int cantDatos = 100000;
        int cantVars = 2;
        int cantExo = 1;
        double[][] datos = new double[cantDatos][cantVars];
        double[][] datosExo = new double[cantDatos][cantExo];
        datos[0][0] = 0.5;
        datos[0][1] = 0.5;
        datos[1][0] = 0.5;
        datos[1][1] = 0.5;
        StringBuilder sb;                
        sb = new StringBuilder();
        sb.append(datos[0][0]);
        sb.append("  ");
        sb.append(datos[0][1]);
//	        DirectoriosYArchivos.agregaTexto(dirDatos,sb.toString());         
        sb = new StringBuilder();
        sb.append(datos[1][0]);
        sb.append("  ");
        sb.append(datos[1][1]);                
//	        DirectoriosYArchivos.agregaTexto(dirDatos,sb.toString()); 
        double[][] vDato = {{0.01},{0.02}};
        double[][] A1 = {{0.6,0.1},{0.1,0.6}};
        double[][] A2 = {{0.1,0.0},{0.0,0.2}};        
//        double[][] A2 = {{0.0,0.0},{0.0,0.0}};               
        double[][] B = {{1.0,0.1},{0.1,1.0}};
        double[][] Bt = new double[2][2];
        Bt[0][0] = B[0][0];
        Bt[1][1] = B[1][1];
        Bt[0][1] = B[1][0];
        Bt[1][0] = B[0][1];
        double[][] C = {{0.4},{-0.5}};
        double s1 = 1.0;
        double s2 = 0.5;
        double[][] diag = new double[2][2];
        diag[0][0] = s1*s1; 
        diag[1][1] = s2*s2;         
        Random n1 = new Random(); 
        Random n2 = new Random(); 
        Random n3 = new Random();
        
        for(int ifil=2; ifil<cantDatos; ifil++){
            double[][] yl1 = new double[2][1];
            yl1[0][0] = datos[ifil-1][0];
            yl1[1][0] = datos[ifil-1][1];            
            double[][] yl2 = new double[2][1];
            yl2[0][0] = datos[ifil-2][0];
            yl2[1][0] = datos[ifil-2][1];                        
              
            double e1 = n1.nextGaussian()*s1;
            double e2 = n2.nextGaussian()*s2;
            double[][] e = new double[2][1];
            e[0][0] = e1; 
            e[1][0] = e2;     
            double valExo = n3.nextGaussian();
            double[][] ex = {{valExo}};
            double[][] y = Oper.prod(B, e);
            y = Oper.suma(Oper.prod(A2, yl2),y);
            y = Oper.suma(Oper.prod(A1, yl1),y);    
            y = Oper.suma(vDato,y);
            y = Oper.suma(Oper.prod(C, ex), y);
            datos[ifil][0] = y[0][0];                
            datos[ifil][1] = y[1][0];
            datosExo[ifil][0] = valExo;
            if(Math.IEEEremainder(ifil, 1000)==0)System.out.println(ifil 
                    + "---" + datos[ifil][0] + "---" +datos[ifil][1]);
//	            sb = new StringBuilder();
//	            sb.append(y[0][0]);
//	            sb.append("  ");
//	            sb.append(y[1][0]);
//	            DirectoriosYArchivos.agregaTexto(dirDatos,sb.toString());        
        }
        EstimaVARExo va1 = new EstimaVARExo(datos, datosExo);
        va1.estimaVAR(2, true);
        double[][]  ve = va1.getV();
        double[][]  A1e = va1.getMatsA().get(0);
        double[][]  A2e = va1.getMatsA().get(1);
        double[][]  Ce = va1.getC();
        double[][]  Be = va1.getB();
        double[][]  Bet = va1.getBt();
        double[][]  diage = va1.getDiag();
        double[][]  sigmaExacta; // la real a partir de B y diag.
        sigmaExacta = Oper.prod(diag, Bt);
        sigmaExacta = Oper.prod(B,sigmaExacta);
        double[][]  sigmae = va1.getSigma();   // sigmae estimación del método
        double[][]  sigmaeE = Oper.prod(diage, Bet);   // sigmae verificación de la estimación
        sigmaeE = Oper.prod(Be, sigmaeE);
        System.out.println("RESULTADOS");
        System.out.println("v");
        System.out.print(Oper.imprime(vDato));
        System.out.println("ve");
        System.out.print(Oper.imprime(ve)); 
        System.out.println("A1");
        System.out.print(Oper.imprime(A1));
        System.out.println("A1e");
        System.out.print(Oper.imprime(A1e));        
        System.out.println("A2");
        System.out.print(Oper.imprime(A2));        
        System.out.println("A2e");
        System.out.print(Oper.imprime(A2e));        
        System.out.println("B");
        System.out.print(Oper.imprime(B));        
        System.out.println("Be");  
        System.out.print(Oper.imprime(Be));                
        System.out.println("C");
        System.out.print(Oper.imprime(C));       
        System.out.println("Ce");
        System.out.print(Oper.imprime(Ce));       
        System.out.println("diag");       
        System.out.print(Oper.imprime(diag));        
        System.out.println("diage");      
        System.out.print(Oper.imprime(diage)); 
        System.out.println("sigmaExacta");      
        System.out.print(Oper.imprime(sigmaExacta));         
        System.out.println("sigma");
        System.out.print(Oper.imprime(sigmae));        
        System.out.println("sigmae");      
        System.out.print(Oper.imprime(sigmaeE)); 
        
        String dirArch = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/salVAR.txt";
        String[] nV = new String[] {"V1" , "V2"};
        String[] nE = new String[] {"EXO1"};
        va1.devuelveTextoParametrosVAR(nV, nE, null);
        DirectoriosYArchivos.siExisteElimina(dirArch);
        DirectoriosYArchivos.agregaTexto(dirArch, sb.toString());

        System.out.println("Termina la prueba de EstimaVARExo");
    }
	
}
