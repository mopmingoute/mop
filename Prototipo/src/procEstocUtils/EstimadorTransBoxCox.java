/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorTransBoxCox is part of MOP.
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
package procEstocUtils;

import java.util.ArrayList;

import procesosEstocasticos.ConjuntoDeSeries;
import procesosEstocasticos.DefPoblacionSerie;
import procesosEstocasticos.MetodosSeries;
import procesosEstocasticos.Serie;
import procesosEstocasticos.TransBoxCox;
import utilitarios.DirectoriosYArchivos;
import utilitarios.UtilArrays;

/**
 *
 * Transformaci�n de Box-Cox de normalizaci�n
 * @author 
 */
public class EstimadorTransBoxCox {
    
    private double[] datos;
    private double[] datosTrans; // los datos transformados con el lambda hallado y estandarizados media 0, varianza 1
    private double lambda;
    private int cantDatos;

    
    
    public EstimadorTransBoxCox(double[] datos) {
		super();
		this.datos = datos;
	}

	public double[] getDatos() {
        return datos;
    }

    public void setDatos(double[] datos) {
        this.datos = datos;
    }
        

    public double[] getDatosTrans() {
		return datosTrans;
	}

	public void setDatosTrans(double[] datosTrans) {
		this.datosTrans = datosTrans;
	}

	public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }
    

    /**
     * Calcula los valores transformados, hallando un lambda que maximiza
     * la función de verosimilitud
     * El lambda resultante queda en el atributo del mismo nombre.
     * @param dat serie de datos
     * @param lamInf valor inicial mínimo de lambda probado
     * @param lamSup idem máximo
     * @param nGrilla cantidad de valores en la grilla en cada etapa
     * @param etapas
     * @return 
     */
    public void estimaLambda(double[] dat, double lamInf, double lamSup, int nGrilla, int etapas){
        datos = dat;
        cantDatos = datos.length;
        int iter = etapas;
        double linf = lamInf;
        double lsup = lamSup;
        double lamMax = 0;
        double lam;
        double verosim;
        double veroMax = -Double.MAX_VALUE;
        double delta = 0.0;
        int indMax = 0;
        while(iter>0){
            delta = (lsup-linf)/nGrilla;
            for(int il = 0; il<=nGrilla; il++){
                lam = linf + il*delta;
                verosim = logVeroBoxCox(lam);
                if(verosim>veroMax){
                    lamMax = lam;
                    veroMax = verosim;
                }                
            }
            linf = lamMax - delta;
            lsup = lamMax + delta;
            System.out.println("iteracion " + (etapas - iter) + " lambda " + lamMax + " veromax " + veroMax);
            iter--;
        }
        lambda = lamMax;        
        datosTrans = new double[cantDatos];
        if(lambda!=0.0){
            for(int id = 0; id<cantDatos; id++){
                datosTrans[id] = (Math.pow(datos[id],lambda)-1)/lambda;
            }         
        }else{
            for(int id = 0; id<cantDatos; id++){            
                datosTrans[id] = Math.log(datos[id]);            
            }
        }
        System.out.println("Fin de estimación");
    }
    
    
    /**
     * A partir de un vector de datos dat estima la transformacion de normalizacion
     * BoxCox que maximiza la verosimilitud.
     * Para estimar empieza por hacer una traslación de los tados de modo que el menor valor
     * de los datos originales más la traslacion valga 0
     * @param dat
     * @return TransBoxCox tr es la transformacion Box-Cox que maximiza la verosimilitud.
     */
    public TransBoxCox estimaBoxCox(){
    	double medDat = UtilArrays.promedio(datos);
    	double minDat = UtilArrays.minimo(datos);
    	double traslacion = 0.0;
    	if(minDat<0.0 || (minDat>0.0 && minDat>medDat*utilitarios.Constantes.EPSITRAS_BOXCOX))
    			traslacion = - UtilArrays.minimo(datos) + medDat*utilitarios.Constantes.EPSIMIN_BOXCOX;
    	estimaLambda(datos, utilitarios.Constantes.LIMINF, utilitarios.Constantes.LIMSUP,
    			utilitarios.Constantes.NGRILLA, utilitarios.Constantes.ETAPAS);
    	double media = Serie.media(datosTrans);
    	double desvio = Serie.desvio(datosTrans);
    	datosTrans = Serie.estandarizaMed0Var1(datosTrans);
    	TransBoxCox tr = new TransBoxCox(lambda, media, desvio, traslacion);
    	return tr;
    }
      
    
    /**
     * Devuelve el valor de la función de verosimilitud
     * para el valor lam del parámetro
     * 
     * Tomada de la documentación de R de la función boxcox
     * https://www.rdocumentation.org/packages/EnvStats/versions/2.4.0/topics/boxcox
     * Esta página está copiada en:
     * G:\PLA\Pla_datos\Archivos\ModeloOp\Bibliografía\Box-Cox\boxcox function _ R Documentation.html
     * 
     * 
     */
    public double logVeroBoxCox(double lam){
    	double vero=0;
    	int i;
    	double[] ytr = new double[datos.length];  // los datos transformados con lam
    	double mediaYtr = 0.0;
    	double sumLogX = 0.0;
    	if(lam!=0.0){
    		for(i=0; i<cantDatos; i++){
    			ytr[i] = (Math.pow(datos[i], lam) - 1)/lam;
    			mediaYtr += ytr[i];
    			sumLogX += Math.log(datos[i]);
    		}

    	}else{
    		for(i=0; i<cantDatos; i++){
    			ytr[i] = Math.log(datos[i]);
    			mediaYtr += ytr[i];
    			sumLogX += Math.log(datos[i]);
    		}            
    	}  
    	mediaYtr = mediaYtr/cantDatos;
    	double sigmaYtr = 0.0;    	
    	for(i=0; i<cantDatos; i++){
    		sigmaYtr += Math.pow(ytr[i]-mediaYtr, 2);
    	}
    	sigmaYtr = Math.pow(sigmaYtr/cantDatos, 0.5);
    	vero = -(cantDatos/2)*Math.log(2*Math.PI)
    	       -(cantDatos/2)*Math.log(Math.pow(sigmaYtr,2))
    	       - cantDatos/2
    	       + (lam-1)*sumLogX;
    	return vero;    	    	
    }
    
    
    
    
    public static void main(String[] args){
    	String dirDatos = "G:/PLA/Pla_datos/Archivos/ModeloOp/Aportes/datosProcHistorico-ConjuntoDeSeries.xlt";
    	ConjuntoDeSeries conj = MetodosSeries.leeConjuntoDeSeries(dirDatos);
    	String nom = "APORTE-PALMAR";
    	Serie sap = conj.getSeries().get(nom);
    	Serie sap1 = sap.sustituyeCeros(2.0);
    	DefPoblacionSerie dp = new DefPoblacionSerie(1, 0, 0);
    	ArrayList<ArrayList<Double>> pob = sap1.devuelvePoblacionesPasos(dp);
    	int paso = 4;
    	double lamMin = -10;
    	double lamMax = 10;
    	int nGrilla = 200;
    	int etapas = 8;
    	double[] dat = new double[pob.get(paso).size()];
    	int i = 0;
    	for(Double d: pob.get(paso)){
    		dat[i] = d;
    		i++;
    	}    	
    	EstimadorTransBoxCox eBC = new EstimadorTransBoxCox(dat);
    	TransBoxCox tr = eBC.estimaBoxCox();
    	double[] datTransStd = eBC.getDatosTrans();
    	Graficador graf = new Graficador(datTransStd);
    	double valorMin = -4;
    	double valorMax = 4;
    	int cantDiv = 32;
    	double[][] histograma = graf.histogramaStd(valorMin, valorMax, cantDiv);
    	String shist = graf.textoHistograma("TR-BOX-COX" + nom + paso, histograma);
    	String dirArch = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/TRANS-BC-" + nom + paso + ".xlt";
    	if(DirectoriosYArchivos.existeArchivo(dirArch)) DirectoriosYArchivos.eliminaArchivo(dirArch);
    	DirectoriosYArchivos.agregaTexto(dirArch, shist);
    }    
}
