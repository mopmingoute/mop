
/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimaLineal is part of MOP.
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

import org.ejml.simple.SimpleMatrix;

import matrices.Oper;

/**
 *
 * @author Maite
 * 
 * El modelo es y = xmat . b + sigma.U
 * 
 * U vector de cantDatos normales estándar N(0,1) independientes entre sí  
 * 
 * Usa textos tomados de G:\PLA\Pla_datos\Archivos\ModeloOp\VAR modelos\EstimacionYAnalisisModelosLineales
 * en particular lesso12_multregression.pdf
 * 
 * 
 * 
 */
public class EstimaLineal {
    private double[][] xmat;  // matriz de variables independientes. primer indice datos, segundo indice variables.
    private double[] y; // variable dependiente
    private double[] yest; // valores estimados con el modelo
    private boolean tind;  // true si hay tórmino independiente
    
    /**
     * coeficientes estimados, si hay tórmino independiente es b[0]
     * si no hay tórmino independiente b[0] es el coef de la primera variable
     */
    private double[] b; 
    private double[] resid; //residuos de estimacion
    private int cantDatos; // n = cantidad de datos
    private int p; // cantidad de parámetros, la dimensión del vector b, incluso el término independiente de la regresión
    private int cantVarInd;
    private SimpleMatrix xSM;
    private SimpleMatrix xSMt;
    private SimpleMatrix xSMtxSM_inv;  // inversa del producto de xmat transpuesta por xmat, en formato SM
    private double[][] xtx_imv;  // lo mismo en formato double[][]
    private SimpleMatrix ySM;
    private SimpleMatrix bSM;
    private double R2; // R cuadrado de la regresión
    private double R2ajust; // R cuadrado ajustado por los grados de libertad
    private double sigmaEst; // desvío estándar estimado de la normal de los residuos de regresión.
    // t es el índice que recorre los datos
    // y(t) es el valor de la variable dependiente para el dato t
    // yest(t) es el valor estimado por el modelo para el dato t
    // ymedia es la media de los valores de y
    // y - yest = resid
    
    private double sse;  // suma de cuadrados de errores (residuos) del modelo: suma en t [ (y(t) - yest(t))**2 ]
    private double sst;  // suma de cuadrados totales:    suma en t [ (y(t) - ymedia)**2 ] 
    private double ssr;  // suma de cuadrados explicados por la regresión: suma en t [ (yest(t) - ymedia)**2 ]
    
    //  ||sst||**2 = ||sse||**2 + ||sst||**2 
    
    private double ftest;  // estadístico del test f con hipótesis nula que todos los parámetros excepto el término independiente son nulos
    private String gradosF;  // grados de libertad de la distribución del F   (cantidad de variables independientes(k) - 1, observaciones(n) - k)
    
    private double[] desviosCoefs;  // desvíos estándar de los coeficientes estimados
    private double[] testsTCoefs;   // estadísticos t de los coeficientes
    private int gradosT; // grados de libertad de la distribución de t
    
    
    // varb = sigma**2 (Xt X)**-1
    private double[][] varb; // matriz de covarianza de los estimadores de los coeficientes b
    
    private String textoResultados;
    
    
   
    public double[][] getXmat() {
        return xmat;
    }

    public void setXmat(double[][] xmat) {
        this.xmat = xmat;
    }

    public double[] getY() {
        return y;
    }

    public void setY(double[] y) {
        this.y = y;
    }

    public double[] getB() {
        return b;
    }

    public void setB(double[] b) {
        this.b = b;
    }

    public double[] getResid() {
        return resid;
    }

    public void setResid(double[] resid) {
        this.resid = resid;
    }

    public int getCantDatos() {
        return cantDatos;
    }

    public void setCantDatos(int cantDatos) {
        this.cantDatos = cantDatos;
    }

    
    
    public int getCantVarInd() {
        return cantVarInd;
    }

    public void setCantVarInd(int cantVarInd) {
        this.cantVarInd = cantVarInd;
    }

    public SimpleMatrix getxSM() {
        return xSM;
    }

    public void setxSM(SimpleMatrix xSM) {
        this.xSM = xSM;
    }

    public SimpleMatrix getySM() {
        return ySM;
    }

    public void setySM(SimpleMatrix ySM) {
        this.ySM = ySM;
    }

    public SimpleMatrix getbSM() {
        return bSM;
    }

    public void setbSM(SimpleMatrix bSM) {
        this.bSM = bSM;
    }

    /**
     * Crea un modelo lineal
     * @param xmat  matriz de datos de variables independientes, no incluye el vector de 1s del término independiente
     * @param y     vector de variables dependientes
     * @param tind  si es true se agrega tórmino independiente a la regresión
     */
    public EstimaLineal(double[][] xmat, double[] y, boolean tind) {
        this.xmat = xmat;
        this.y = y;
        this.tind = tind;
        this.p = xmat[0].length;
        if(tind) this.p++;
    }
       
    /**
     * Calcula los coeficientes del modelo y los residuos
     * @return 
     */
    public double[] calcCoefModelo(){
        cantDatos = xmat.length; // cantidad de filas  
        cantVarInd = xmat[0].length;        
        if (tind){ 
            // inserta en xmat una primera columna de unos
            double[][] xaux = new double[cantDatos][cantVarInd+1];
            for(int ifil=0; ifil<cantDatos; ifil++){
                xaux[ifil][0] = 1.0;
                for(int icol=0; icol<cantVarInd; icol++){
                    xaux[ifil][icol+1]=xmat[ifil][icol];
                }
            }
            cantVarInd++;
            xmat = xaux;
        }

        double[][] ymat = new double[cantDatos][1];
        for(int ifil=0; ifil<cantDatos; ifil++){
            ymat[ifil][0] = y[ifil];
        }
        xSM = new SimpleMatrix(xmat);
        ySM = new SimpleMatrix(ymat);
        b = new double[cantVarInd];
        xSMt = xSM.transpose();
        bSM = xSMt.mult(xSM);        
        bSM = bSM.invert();
        xSMtxSM_inv = bSM;
        bSM = bSM.mult(xSMt);
        bSM = bSM.mult(ySM);
        for(int ib=0; ib<cantVarInd; ib++){
            b[ib]=bSM.get(ib, 0);
        }
        SimpleMatrix yestSM = xSM.mult(bSM);
        yest = new double[cantDatos];
        for(int t=0; t<cantDatos; t++) {
        	yest[t] = yestSM.get(t, 0);
        }
        resid = residuos();
        return b;
    }
    
    /**
     * Calcula los residuos de la estimación
     * @return 
     */
    public double[] residuos(){
        double[] res = new double[cantDatos];
        SimpleMatrix resSM = ySM.minus(xSM.mult(bSM));
        for(int id=0; id<cantDatos; id++){
            res[id]=resSM.get(id,0);
        }
        return res;                
    }
    
    
    /**
     * Devuelve los valores estimados según el modelo
     */
    public double[] devuelveYEstimados() {
    	return yest;
    }
    
    /**
     * Calcula otros indicadores
     * Requiere que esté estimado el modelo y calculados los residuos.
     * @param args
     */
    public void calculaIndicadores() {
    	sse = matrices.Oper.normaEuCuad(resid);
    	double ymedEscalar = utilitarios.UtilArrays.promedio(y);     	
    	// sst  suma de cuadrados totales:    suma en t [ (y(t) - ymedia)**2 ] 
    	double[] ymenosYmedia = utilitarios.UtilArrays.sumaUnEscalar(y, -ymedEscalar);
    	sst = matrices.Oper.normaEuCuad(ymenosYmedia);
    	// ssr suma de cuadrados explicados por la regresión: suma en t [ (yest(t) - ymedia)**2 ]
    	double[] yestmenosYmedia = utilitarios.UtilArrays.sumaUnEscalar(yest, -ymedEscalar);
    	ssr = matrices.Oper.normaEuCuad(yestmenosYmedia);
    	R2 = ssr/sst;
    	int n = cantDatos;
    	R2ajust = 1 - ((n-1)/(n-p))*(1-R2);
    	sigmaEst = Math.pow(sse/(n-p), 0.5);
    	ftest = (ssr/(p-1))/(sse/(n-p));
    	gradosF = "(" + (cantVarInd-1) + "," +  (n-cantVarInd) + ")";
    	
    	// varb = sigma**2 (Xt X)**-1
    	varb = new double[p][p];
    	for(int i=0; i<p; i++) {
    		for(int j=0; j<p; j++) {
    			varb[i][j] = Math.pow(sigmaEst, 2) * xSMtxSM_inv.get(i,j);
    		}
    	}
    	
        // desviosCoefs    	desvíos estándar de los coeficientes estimados
        // testsTCoefs      	estadísticos t de los coeficientes
        // gradosT			grados de libertad de la distribución de t    
    	desviosCoefs = new double[p];
    	testsTCoefs =  new double[p];
    	for(int i=0; i<p; i++) {
    		desviosCoefs[i] = Math.pow(varb[i][i], 0.5);
    		testsTCoefs[i] = b[i]/desviosCoefs[i];
    	}
    	
    }
    
    
    /**
     * Crea texto de salidas 
     * @return
     */
    public String creaSalidasTexto() {
    	StringBuilder sb = new StringBuilder();
        sb.append("Coeficientes estimados\t");
        for(int ivar=0; ivar<b.length; ivar++){
            sb.append(b[ivar]);
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Estadístico t\t");
        for(int ivar=0; ivar<b.length; ivar++){
            sb.append(testsTCoefs[ivar]);
            sb.append("\t");
        }        
        sb.append("R2 " + this.getR2());
        sb.append("\n");
        sb.append("R2ajust " + this.getR2ajust());
        sb.append("\n");
        sb.append("Desvío estándar de los residuos " + this.getSigmaEst());
        sb.append("\n");
        sb.append("Test f =" + this.getFtest() + " con grados de libertad " + this.getGradosF());
        return sb.toString();    	
    }
    
    
    public double[] getYest() {
		return yest;
	}

	public void setYest(double[] yest) {
		this.yest = yest;
	}

	public boolean isTind() {
		return tind;
	}

	public void setTind(boolean tind) {
		this.tind = tind;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public double getR2() {
		return R2;
	}

	public void setR2(double r2) {
		R2 = r2;
	}

	public double getR2ajust() {
		return R2ajust;
	}

	public void setR2ajust(double r2ajust) {
		R2ajust = r2ajust;
	}

	public double getSigmaEst() {
		return sigmaEst;
	}

	public void setSigma(double sigmaEst) {
		this.sigmaEst = sigmaEst;
	}

	public double getSse() {
		return sse;
	}

	public void setSse(double sse) {
		this.sse = sse;
	}

	public double getSst() {
		return sst;
	}

	public void setSst(double sst) {
		this.sst = sst;
	}

	public double getSsr() {
		return ssr;
	}

	public void setSsr(double ssr) {
		this.ssr = ssr;
	}
	
	

	public double getFtest() {
		return ftest;
	}

	public void setFtest(double ftest) {
		this.ftest = ftest;
	}
	

	public String getGradosF() {
		return gradosF;
	}

	public void setGradosF(String gradosF) {
		this.gradosF = gradosF;
	}

	public void setSigmaEst(double sigmaEst) {
		this.sigmaEst = sigmaEst;
	}

	
	
	
	public SimpleMatrix getxSMt() {
		return xSMt;
	}

	public void setxSMt(SimpleMatrix xSMt) {
		this.xSMt = xSMt;
	}

	public SimpleMatrix getxSMtxSM_inv() {
		return xSMtxSM_inv;
	}

	public void setxSMtxSM_inv(SimpleMatrix xSMtxSM_inv) {
		this.xSMtxSM_inv = xSMtxSM_inv;
	}

	public double[][] getXtx_imv() {
		return xtx_imv;
	}

	public void setXtx_imv(double[][] xtx_imv) {
		this.xtx_imv = xtx_imv;
	}

	public double[][] getVarb() {
		return varb;
	}

	public void setVarb(double[][] varb) {
		this.varb = varb;
	}

	public static void main(String[] args){
        
        double[][] mat = new double[6][3];
        double[] f1 = {1,2,3};
        double[] f2 = {2,2,2};
        double[] f3 = {1,2,1};
        double[] f4 = {2,0.5, 1};        
        double[] f5 = {2,0.5, 1.5};
        double[] f6 = {2,1.5, 1};        
        mat[0]=f1;
        mat[1]=f2;
        mat[2]=f3;        
        mat[3]=f4;
        mat[4]=f5;
        mat[5]=f6;        
        double[] y = {1, 2 , 2.3 , 2.4 , 2 ,  3.1};
        EstimaLineal est = new EstimaLineal(mat, y, true);
        double[] b = est.calcCoefModelo();
        est.calculaIndicadores();
 
        System.out.println("Coeficientes");
        for(int ivar=0; ivar<b.length; ivar++){
            System.out.println(b[ivar]);
        }
        System.out.println("R2 " + est.getR2());
        System.out.println("R2ajust " + est.getR2ajust());
        for(int t=0; t<est.getCantDatos(); t++){
            System.out.println(y[t] + "  " + est.getYest()[t]);
        }
        System.out.println("sigma estimado " + est.getSigmaEst());
        System.out.println("test f =" + est.getFtest() + " con grados de libertad " + est.getGradosF());
        System.out.println("Matriz de varianzas de coeficientes estimados");
        for(int i=0; i<est.getP(); i++) {
        	StringBuilder linea = new StringBuilder();
        	for(int j=0; j<est.getP(); j++) {
        		linea.append(est.getVarb()[i][j] + "  ");
        	}
        	System.out.println(linea.toString());
        }
        System.out.println("\n\n\n");
        System.out.println(est.creaSalidasTexto());

    }
    
}

