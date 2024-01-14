/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BoxCox is part of MOP.
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
package transnorm;

/**
 *
 * Transformación de Box-Cox de normalización
 * @author 
 */
public class BoxCox {
    
    private static double[] datos;
    private static double lambda;
    private static int cantDatos;
    private static double mediaG; // media geomótrica de los datos

    public double[] getDatos() {
        return datos;
    }

    public void setDatos(double[] datos) {
        this.datos = datos;
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
     * @param lamMin valor inicial mónimo de lambda probado
     * @param lamMax idem móximo
     * @param nGrilla cantidad de valores en la grilla en cada etapa
     * @param etapas
     * @return 
     */
    public static double[] transforma(double[] dat, double lamMin, double lamMax, int nGrilla, int etapas){
        datos = dat;
        cantDatos = datos.length;
        int iter = etapas;
        double lmin = lamMin;
        double lmax = lamMax;
        double lam;
        double verosim;
        double veroMax;
        double delta = 0.0;
        int indMax = 0;
        mediaG = 1;
        for(int id = 0; id<cantDatos; id++){
            mediaG = mediaG*datos[id];
        }
        mediaG = Math.pow(mediaG, 1/cantDatos);                
        while(iter>0){
            veroMax = -Double.MAX_VALUE;
            delta = (lmax-lmin)/nGrilla;
            for(int il = 0; il<nGrilla; il++){
                lam = lmin + il*delta;
                verosim = veroBoxCox(lam);
                if(verosim>veroMax){
                    indMax = il;
                    veroMax = verosim;
                }                
            }
            if(indMax==0){
                lmax = lmin+delta;
            }else if(indMax==nGrilla){
                lmin = lmax - delta;
            }else{
                lmin = lmin+delta*(indMax-1);
                lmax = lmin+delta*(indMax+1);                
            }
        }
        lambda = lmin = indMax*delta;
        double[] datosTrans = new double[cantDatos];
        if(lambda!=0.0){
            for(int id = 0; id<cantDatos; id++){
                datosTrans[id] = (Math.pow(datos[id],lambda)-1)/lambda;
            }         
        }else{
            for(int id = 0; id<cantDatos; id++){            
                datosTrans[id] = Math.log(datos[id]);            
            }
        }
        return datosTrans;                               
    }
    
    /**
     * Devuelve el valor de la función L de verosimilitud
     * para el valor lam del parómetro
     * @param lam
     * @return 
     */
    public static double veroBoxCox(double lam){
        
        double vero=0;
        int iu;
        double[] u = new double[datos.length];
        double mediaU = 0;
        if(lam!=0.0){
            for(iu=0; iu<cantDatos; iu++){
                u[iu] = (Math.pow(datos[iu], lam) - 1)/(lam*Math.pow(mediaG, lam-1));
                mediaU += u[iu];
            }
            
        }else{
            for(iu=0; iu<cantDatos; iu++){
                u[iu] = mediaG*Math.log(datos[iu]);
                mediaU += u[iu];                
            }            
        }        
        for(iu=0; iu<cantDatos; iu++){
            vero += Math.pow(u[iu]-mediaU, 2);
        }
        vero = -(cantDatos/2)*Math.log(vero);
        return vero;
    }
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
}
