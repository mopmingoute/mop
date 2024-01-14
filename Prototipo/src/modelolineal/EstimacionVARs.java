/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimacionVARs is part of MOP.
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

package modelolineal;



import java.util.ArrayList;

import matrices.Oper;

import java.util.Random;

import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;
import matrices.XcargaDatos;

/**
 * !!!!!!!!!!!!    ATENCION   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ESTA CLASE  NO SE DEBE USAR PERO TIENE PEDAZOS DE CODIGO UTILES PARA MEJORAR
 * LA CLASE EstimaVARExo
 * @author 
 * Estima las matrices A y la matriz B de [k x k] en:
 * 
 * yt = v + A1 yt-1 + A2 yt-2 + ...Ap yt-p + C0 xt + C1 xt-1 + ...Cq xt-q + ut
 * u=Be, 
 * E(e) = 0
 * e multivariado normal de dimension k independientes entre s�.
 * v es no nulo s�lo si indep=true.
 * 
 * p lags
 * q lagsExo, con q<=p
 * 
 * ESTO ES UN INTENTO DE AGREGAR EXOGENAS
 * 
 */
public class EstimacionVARs {
    private double[][] datos;  // primer �ndice tiempo creciente, segundo �ndice variable
    private double[][] datosExo; // datos de variables ex�genas, primer �ndice tiempo creciente, segundo �ndice variable							 
    private boolean indep;   // si es true cada ecuaci�n tiene t�rmino independiente
    private int lags;  // cantidad de rezagos de las variables y
    private int lagsExo; // cantidad de rezagos de las variables exógenas
    private int cantDatos;
    private int cantVars;
    private int cantVarsExo;
    private double[][] v;    // matriz [k][1] de t�rminos independientes si existen 
    private ArrayList<double[][]> matsA;  // matrices cuadradas [cantVars, cantvars] de la autoregresi�n
    private ArrayList<double[][]> matsC;  // matrices [cantVars, cantvarsExo] de efectos de variables ex�genas
    private double[][] resids; // residuos de la estimaci�n, primer �ndice tiempo, segundo �ndice variable
    private double[][] sigma;  // matriz de covarianza de residuos
    private double[][] b;  // matriz de cambio de base de residuos
    private double[][] bt;  // transpuesta e inversa de la matriz de cambio de base de residuos    
    private double[][] diag; // matriz de covarianzas en base ortonormal
    
    /**
     * Estructura que almacena los resultados de simulaci�n
     * primer índice escenario
     * segundo índice tiempo
     * tercer índice variable 
     */
    private double[][][] simulacion;  
    
    
    public EstimacionVARs(double[][] datos, double[][] datExo) {
        this.datos = datos;
        cantDatos = datos.length;
        cantVars = datos[0].length;
        matsA = new ArrayList<double[][]>(); 
        matsC = new ArrayList<double[][]>();
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
    
    

    public double[][] getDiag() {
        return diag;
    }
    
    
    
    
    public void estimaVAR(int cantLags, boolean esIndep){
        lags = cantLags;
        indep = esIndep;
        int il, iec, ivar, t;    
        // residuos almacena los residuos de estimaci�n: primer indice tiempo, segundo �ndice ecuacion.
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
        for(il = 0; il<= lagsExo; il++){
            double[][] C = new double[cantVars][cantVars];
            matsC.add(C);
        }        
        double[][] xmat = null;
        for(iec=1; iec<=cantVars; iec++ ){
            /**
             * Llamemos k a la cantidad de variables cantVars
             * Se hace la regresi�n lineal MCO para la variable iec+1-�sima.
             * Estima la fila iec+1 OJOJOJO de cada matriz A, hay lags filas a estimar
             * Se estiman lags*k coeficientes si no hay t�rmino indendiente y si hay
             * t�rmino independiente k adicionales.
             */  

            xmat = new double[cantDatos-lags][intIndep + cantVars*lags + cantVarsExo*lagsExo + 1]; 
            double[] y = new double[cantDatos-lags];
            for(t=lags+1; t<=cantDatos; t++){
                y[t-lags-1]=datos[t-1][iec-1];
                if(indep) xmat[t-lags-1][0]=1.0;
                for(il=1; il<=lags; il++){
                    for(ivar = 1; ivar<=cantVars; ivar++){
                        xmat[t-lags-1][intIndep + (il-1)*cantVars+ivar-1] = datos[t-1-il][ivar-1];
                    }
                } 
                for(il=0; il<=lagsExo; il++){
                    for(ivar = 1; ivar<=cantVarsExo; ivar++){
                        xmat[t-lagsExo-1][intIndep + lags*cantVars + il*cantVarsExo+ivar-1] = datosExo[t-1-il][ivar-1];
                    }                
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
            for(il=0; il<=lags; il++){
                double[][]  C = matsC.get(il);
                for(ivar=1; ivar<=cantVarsExo; ivar++){
                    C[iec-1][ivar-1] = filasIec[intIndep+lags*cantVars + il*cantVars+ivar-1];
                }
            }            
            double[] resIec = el.getResid();
            for(t=lags+1; t<=cantDatos; t++){
                residuos[t-lags-1][iec-1] = resIec[t-lags-1];
            }            
        }
        // estima matriz sigma de covarianzas de residuos u de las ecuaciones reducidas
        // TODO: aca hay que restar los t�rminos independientes
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
     * 
     * @param cantLags
     * @param esIndep
     * @param gama
     * @param r 
     */
    public void estimaVARCRestric(int cantLags, boolean esIndep, double[][] gama, double[][] r){
        
    }
    
    
    /**
     * Genera un String con los resultados de la estimaci�n
     */
    public String toString(){
    	StringBuilder sb = new StringBuilder();
    	sb.append("Cantidad de lags = " + lags + "\n");
    	for(int il=0; il<lags; il++){
    		sb.append("Matriz AR - lag " + (il+1) + "\n");
    		sb.append(Oper.imprime(matsA.get(il)));
    	}
    	sb.append("Cantidad de lags = " + lags + "\n");
    	for(int il=0; il<=lagsExo; il++){
    		sb.append("Matriz EXO - lag " + il + "\n");
    		sb.append(Oper.imprime(matsC.get(il)));
    	}
    	
    	if(indep){
        	sb.append("Vector (matriz de una columna) de coeficientes constantes"+ "\n");
        	sb.append(Oper.imprime(v));
    	}
    	sb.append("Matriz de autocorrelaci�n de residuos ut"+ "\n");
    	sb.append(Oper.imprime(sigma));
    	sb.append("Matriz b que multiplica los residuos independientes et para pasar a los ut"+ "\n");    	
    	sb.append(Oper.imprime(b));
    	sb.append("Matriz diagonal de covariandas de los et"+ "\n");    	
    	sb.append(Oper.imprime(diag));    	
    	return sb.toString();
    }
    
    
    /**
     * Simula escenarios cantEsc escenarios a partir de los valores iniciales valIni
     * @param cantEsc cantidad de escenarios a simula
     * @param largo largo de los escenarios
     * @param valIni valores iniciales de variables y, primer �ndice variable, segundo �ndice lag
     * @param valExo valores de las variables ex�genas, primer �ndice escenario, 
     * segundo �ndice tiempo, tercer �ndice variable.
     * @throws XcargaDatos
     */
    public double[][][] simulaEscenarios(int cantEsc, int largo, double[][] valIni, double[][][] valExo) throws XcargaDatos{
    	/**
    	 * En simulaci�n se colocan los valores valIni (una cantidad lags de datos) y 
    	 * los valores simulados (una cantidad largo).
    	 */
    	simulacion = new double[cantEsc][largo][cantVars];  
    	double[][] et = new double[cantVars][1];
    	double[][] ut = new double[cantVars][1];
    	ArrayList<Random> innov = new ArrayList<Random>();
    	for(int ir = 0; ir<cantVars; ir++){
        	innov.add(new Random());
    	}   	
    	if (valIni.length != cantVars || valIni[0].length != lags){
    		System.out.println("Error en la dimensiones de valIni");
    	}
    	
    	if (valExo.length != cantVarsExo || valIni[0].length != (lagsExo+1)){
    		System.out.println("Error en la dimensiones de valExo");
    	}    	
    	
    	for(int e=0; e<cantEsc; e++){
        	for(int iv = 0; iv<cantVars; iv++){
        		for(int il = 0; il<lags; il++){
        			simulacion[e][lags-il-1][iv] = valIni[iv][il];
        		}       		
        	}    		    		
    		for(int id = lags; id<largo; id++ ){
    			double[][] yt = new double[cantVars][1];
    			if(indep){
    				yt = Oper.suma(v, yt);
    			}
    			for(int il = 0; il<lags; il++){
    				double[][] sumando = new double[cantVars][1];
    				double[][] ytl = new double[cantVars][1];
    				for (int iv = 0; iv<cantVars; iv++){
    					ytl[iv][0] = simulacion[e][id-il-1][iv];
    				}
    				sumando = Oper.prod(matsA.get(il), ytl);
    				yt = Oper.suma(yt,sumando);
    			}
    			for(int il = 0; il<=lagsExo; il++){
    				double[][] sumando = new double[cantVars][1];
    				double[][] yte = new double[cantVars][1];    				
    				for (int iv = 0; iv<cantVarsExo; iv++){
    					if(id-il-1<0){
    						yte[iv][0] = datosExo[cantDatos + id - il - lags - 1][iv];	
    						
    					}else{
    						yte[iv][0] = valExo[e][id-il-1][iv];	
    					}   					
    				}
    				sumando = Oper.prod(matsC.get(il), yte);
    				yt = Oper.suma(yt,sumando);
    			}    			
    			// Genera y suma el residuo aleatorio
    			for(int iv = 0; iv<cantVars; iv++){
    				et[iv][0] = innov.get(iv).nextGaussian()*Math.sqrt(diag[iv][iv]);    				
    			}
    			ut = Oper.prod(b, et);	
    			yt = Oper.suma(yt, ut);
        		for(int iv = 0; iv<cantVars; iv++){
        			simulacion[e][id][iv] = yt[iv][0];
        		}     			
    			
    		}    		
    	}
    	
    	
    	return simulacion;
    }
    
    
    
    
    public double[][] getDatos() {
		return datos;
	}

	public void setDatos(double[][] datos) {
		this.datos = datos;
	}

	public boolean isIndep() {
		return indep;
	}

	public void setIndep(boolean indep) {
		this.indep = indep;
	}

	public int getCantDatos() {
		return cantDatos;
	}

	public void setCantDatos(int cantDatos) {
		this.cantDatos = cantDatos;
	}

	public int getCantVars() {
		return cantVars;
	}

	public void setCantVars(int cantVars) {
		this.cantVars = cantVars;
	}

	public double[][][] getSimulacion() {
		return simulacion;
	}

	public void setSimulacion(double[][][] simulacion) {
		this.simulacion = simulacion;
	}

	public void setLags(int lags) {
		this.lags = lags;
	}

	public void setV(double[][] v) {
		this.v = v;
	}

	public void setMatsA(ArrayList<double[][]> matsA) {
		this.matsA = matsA;
	}

	public void setResids(double[][] resids) {
		this.resids = resids;
	}

	public void setSigma(double[][] sigma) {
		this.sigma = sigma;
	}

	public void setB(double[][] b) {
		this.b = b;
	}

	public void setBt(double[][] bt) {
		this.bt = bt;
	}

	public void setDiag(double[][] diag) {
		this.diag = diag;
	}

//	public static void main(String[] args) throws XcargaDatos{
//        String dirDatos = "C:/Mario/PruebasMatrices/PruebasMatrices/datosVar.txt";
//        if(DirectoriosYArchivos.existeArchivo(dirDatos)) DirectoriosYArchivos.eliminaArchivo(dirDatos);
//        int cantDatos = 1000000;
//        int cantVars = 2;        
//        double[][]  datos = new double[cantDatos][cantVars];
//        datos[0][0] = 0.5;
//        datos[0][1] = 0.5;
//        datos[1][0] = 0.5;
//        datos[1][1] = 0.5;
//        StringBuilder sb;                
//        sb = new StringBuilder();
//        sb.append(datos[0][0]);
//        sb.append("  ");
//        sb.append(datos[0][1]);
////        DirectoriosYArchivos.agregaTexto(dirDatos,sb.toString());         
//        sb = new StringBuilder();
//        sb.append(datos[1][0]);
//        sb.append("  ");
//        sb.append(datos[1][1]);                
////        DirectoriosYArchivos.agregaTexto(dirDatos,sb.toString()); 
//        double[][] vDato = {{0.01},{0.02}};
//        double[][] A1 = {{0.6,0.1},{0.1,0.6}};
////        double[][] A2 = {{0.1,0.0},{0.0,0.2}};        
//        double[][] A2 = {{0.0,0.0},{0.0,0.0}};               
//        double[][] B = {{1.0,0.1},{0.1,1.0}};
//        double[][] Bt = new double[2][2];
//        Bt[0][0] = B[0][0];
//        Bt[1][1] = B[1][1];
//        Bt[0][1] = B[1][0];
//        Bt[1][0] = B[0][1];        
//        double s1 = 1.0;
//        double s2 = 0.5;
//        double[][] diag = new double[2][2];
//        diag[0][0] = s1*s1; 
//        diag[1][1] = s2*s2;         
//        Random n1 = new Random(); 
//        Random n2 = new Random(); 
//        for(int ifil=2; ifil<cantDatos; ifil++){
//            double[][] yl1 = new double[2][1];
//            yl1[0][0] = datos[ifil-1][0];
//            yl1[1][0] = datos[ifil-1][1];            
//            double[][] yl2 = new double[2][1];
//            yl2[0][0] = datos[ifil-2][0];
//            yl2[1][0] = datos[ifil-2][1];                        
//              
//            double e1 = n1.nextGaussian()*s1;
//            double e2 = n2.nextGaussian()*s2;
//            double[][] e = new double[2][1];
//            e[0][0] = e1; 
//            e[1][0] = e2;                 
//            double[][] y = Oper.prod(B, e);
//            y = Oper.suma(Oper.prod(A2, yl2),y);
//            y = Oper.suma(Oper.prod(A1, yl1),y);    
//            y = Oper.suma(vDato,y);
//            datos[ifil][0] = y[0][0];                
//            datos[ifil][1] = y[1][0];             
//            if(Math.IEEEremainder(ifil, 1000)==0)System.out.println(ifil 
//                    + "---" + datos[ifil][0] + "---" +datos[ifil][1]);
////            sb = new StringBuilder();
////            sb.append(y[0][0]);
////            sb.append("  ");
////            sb.append(y[1][0]);
////            DirectoriosYArchivos.agregaTexto(dirDatos,sb.toString());        
//        }
//        VectorAutoreg va1 = new VectorAutoreg(datos);
//        va1.estimaVAR(1, true);
//        double[][]  ve = va1.getV();
//        double[][]  A1e = va1.getMatsA().get(0);
////        double[][]  A2e = va1.getMatsA().get(1);
//        double[][]  Be = va1.getB();
//        double[][]  Bet = va1.getBt();
//        double[][]  diage = va1.getDiag();
//        double[][]  sigmaExacta; // la real a partir de B y diag.
//        sigmaExacta = Oper.prod(diag, Bt);
//        sigmaExacta = Oper.prod(B,sigmaExacta);
//        double[][]  sigmae = va1.getSigma();   // sigmae estimaci�n del m�todo
//        double[][]  sigmaeE = Oper.prod(diage, Bet);   // sigmae verificaci�n de la estimaci�n
//        sigmaeE = Oper.prod(Be, sigmaeE);
//        System.out.println("RESULTADOS");
//        System.out.println("v");
//        System.out.print(Oper.imprime(vDato));
//        System.out.println("ve");
//        System.out.print(Oper.imprime(ve)); 
//        System.out.println("A1");
//        System.out.print(Oper.imprime(A1));
//        System.out.println("A1e");
//        System.out.print(Oper.imprime(A1e));        
////        System.out.println("A2");
////        System.out.print(Oper.imprime(A2));        
////        System.out.println("A2e");
////        System.out.print(Oper.imprime(A2e));        
//        System.out.println("B");
//        System.out.print(Oper.imprime(B));        
//        System.out.println("Be");  
//        System.out.print(Oper.imprime(Be));                
//        System.out.println("diag");
//        System.out.print(Oper.imprime(diag));        
//        System.out.println("diage");      
//        System.out.print(Oper.imprime(diage)); 
//        System.out.println("sigmaExacta");      
//        System.out.print(Oper.imprime(sigmaExacta));         
//        System.out.println("sigma");
//        System.out.print(Oper.imprime(sigmae));        
//        System.out.println("sigmae");      
//        System.out.print(Oper.imprime(sigmaeE)); 
		
		
	public static void main(String[] args) throws XcargaDatos{		
    	String datOrig = "D:/_Migro/PruebaVARs/apnqt.sal";
    	ArrayList<ArrayList<String>> texto = LeerDatosArchivo.getDatos(datOrig);
    	int cantDatos = texto.size() - 1;
    	double[][] datos = new double[cantDatos][3];
    	for(int i=1; i<=cantDatos; i++){
    		for(int j=1; j<=3; j++){
    			datos[i-1][j-1]=Double.parseDouble(texto.get(i).get(j));
    		}	
    	}
    	boolean esIndep = false;
    	int cantLags = 7;
    	
    	EstimacionVARs va = new EstimacionVARs(datos, null);  // ojo el null este de las exogenas
    	va.estimaVAR(cantLags, esIndep);
    	
    	// Verifica la estimaci�n de b
    	double[][] ver = new double[va.getCantVars()][va.getCantVars()];
    	ver = Oper.prod(va.getDiag(),va.getBt());
    	ver = Oper.prod(va.getB(), ver);
    	ver = Oper.suma(ver, Oper.opuesta(va.getSigma()));
    	
    	String str = va.toString();
        String archSal = "D:/_Migro/PruebaVARs/" + "/DatosSalExo.xlt";
        boolean existe = DirectoriosYArchivos.existeArchivo(archSal);
        if(existe) DirectoriosYArchivos.eliminaArchivo(archSal);
        DirectoriosYArchivos.agregaTexto(archSal, str);
        if (Constantes.NIVEL_CONSOLA > 1) System.out.println("TERMIN� LA ESTIMACI�N");
    	double[][] valIni = new double[3][cantLags];
    	va.simulaEscenarios(1, 10000, valIni, null);   // ojo el null este de las exogenas
    	
        String archSimul = "D:/_Migro/PruebaVARs/" + "/ResSimul.xlt";
        existe = DirectoriosYArchivos.existeArchivo(archSimul);
        if(existe) DirectoriosYArchivos.eliminaArchivo(archSimul);
        double[][] salida = new double[va.getSimulacion()[0].length][va.getCantVars()];
        for(int iv = 0; iv<va.getCantVars(); iv++){
        	for(int it = 0; it <va.getSimulacion()[0].length; it++){
        		salida[it][iv] = va.getSimulacion()[0][it][iv];
        	}
        }
        
        String stSimul = Oper.imprime(salida);
        DirectoriosYArchivos.agregaTexto(archSimul, stSimul);    	
    	
        if (Constantes.NIVEL_CONSOLA > 1) System.out.println("TERMIN� LA SIMULACI�N");
    	
        
    }

	public double[][] getDatosExo() {
		return datosExo;
	}

	public void setDatosExo(double[][] datosExo) {
		this.datosExo = datosExo;
	}

	public int getLagsExo() {
		return lagsExo;
	}

	public void setLagsExo(int lagsExo) {
		this.lagsExo = lagsExo;
	}

	public int getCantVarsExo() {
		return cantVarsExo;
	}

	public void setCantVarsExo(int cantVarsExo) {
		this.cantVarsExo = cantVarsExo;
	}


    
}


