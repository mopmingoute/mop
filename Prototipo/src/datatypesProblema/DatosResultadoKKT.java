/*
 * Este es un mensaje de copyright tonto de ejemplo
 */

package datatypesProblema;

import org.gnu.glpk.GLPK;
import utilitarios.Constantes;

public class DatosResultadoKKT {

    // Variables asociadas a la condición PE
    int ae_indPE;
    int re_indPE;
    double ae_maxPE;
    double re_maxPE;

    // Variables asociadas a la condición PB
    int ae_indPB;
    int re_indPB;
    double ae_maxPB;
    double re_maxPB;

    // Variables asociadas a la condición DE
    int ae_indDE;
    int re_indDE;
    double ae_maxDE;
    double re_maxDE;

    //// Variables asociadas a la condición DB
    int ae_indDB;
    int re_indDB;
    double ae_maxDB;
    double re_maxDB;


    public int getAe_indPE() {
        return ae_indPE;
    }

    public void setAe_indPE(int ae_indPE) {
        this.ae_indPE = ae_indPE;
    }

    public int getRe_indPE() {
        return re_indPE;
    }

    public void setRe_indPE(int re_indPE) {
        this.re_indPE = re_indPE;
    }

    public double getAe_maxPE() {
        return ae_maxPE;
    }

    public void setAe_maxPE(double ae_maxPE) {
        this.ae_maxPE = ae_maxPE;
    }

    public double getRe_maxPE() {
        return re_maxPE;
    }

    public void setRe_maxPE(double re_maxPE) {
        this.re_maxPE = re_maxPE;
    }

    public int getAe_indPB() {
        return ae_indPB;
    }

    public void setAe_indPB(int ae_indPB) {
        this.ae_indPB = ae_indPB;
    }

    public int getRe_indPB() {
        return re_indPB;
    }

    public void setRe_indPB(int re_indPB) {
        this.re_indPB = re_indPB;
    }

    public double getAe_maxPB() {
        return ae_maxPB;
    }

    public void setAe_maxPB(double ae_maxPB) {
        this.ae_maxPB = ae_maxPB;
    }

    public double getRe_maxPB() {
        return re_maxPB;
    }

    public void setRe_maxPB(double re_maxPB) {
        this.re_maxPB = re_maxPB;
    }

    public int getAe_indDE() {
        return ae_indDE;
    }

    public void setAe_indDE(int ae_indDE) {
        this.ae_indDE = ae_indDE;
    }

    public int getRe_indDE() {
        return re_indDE;
    }

    public void setRe_indDE(int re_indDE) {
        this.re_indDE = re_indDE;
    }

    public double getAe_maxDE() {
        return ae_maxDE;
    }

    public void setAe_maxDE(double ae_maxDE) {
        this.ae_maxDE = ae_maxDE;
    }

    public double getRe_maxDE() {
        return re_maxDE;
    }

    public void setRe_maxDE(double re_maxDE) {
        this.re_maxDE = re_maxDE;
    }

    public int getAe_indDB() {
        return ae_indDB;
    }

    public void setAe_indDB(int ae_indDB) {
        this.ae_indDB = ae_indDB;
    }

    public int getRe_indDB() {
        return re_indDB;
    }

    public void setRe_indDB(int re_indD) {
        this.re_indDB = re_indD;
    }

    public double getAe_maxDB() {
        return ae_maxDB;
    }

    public void setAe_maxDB(double ae_maxDB) {
        this.ae_maxDB = ae_maxDB;
    }

    public double getRe_maxDB() {
        return re_maxDB;
    }

    public void setRe_maxDB(double re_maxDB) {
        this.re_maxDB = re_maxDB;
    }

    /**
     * Función que revisa si se cumplen las condiciones KKT con el error relativo
     * @return devuelve TRUE si ninguno de los errores relativos asociados a las 4 condiciones KKT es mayor a cierta tolerancia
     */
    public boolean cumpleKKTRelativo(){
        return !(re_maxPE > Constantes.RES_GLP_TOL || re_maxPB > Constantes.RES_GLP_TOL ||
                re_maxDE > Constantes.RES_GLP_TOL || re_maxDB > Constantes.RES_GLP_TOL);
    }

    /**
     * Función que revisa si se cumplen las condiciones KKT con el error absoluto
     * @return devuelve TRUE si ninguno de los errores absolutos asociados a las 4 condiciones KKT es mayor a cierta tolerancia
     */
    public boolean cumpleKKTAbsoluto(){
        return ae_maxPE > Constantes.RES_GLP_TOL || ae_maxPB > Constantes.RES_GLP_TOL ||
                ae_maxDE > Constantes.RES_GLP_TOL || ae_maxDB > Constantes.RES_GLP_TOL;
    }

}
