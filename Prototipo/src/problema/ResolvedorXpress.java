/*
 * Este es un mensaje de copyright tonto de ejemplo
 */

package problema;

import com.dashoptimization.*;
import datatypesProblema.*;
import utilitarios.Constantes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class ResolvedorXpress implements ResolvedorLineal{

    /**
     * Referencia al problema en BCL
     */
    XPRBprob lp;

    /**
     * Mapa que relaciona el nombre de las variables con sus respectivas representaciones en BLC
     */
    private Hashtable<String, XPRBvar> variables;

    /**
     * Mapa que relaciona el nombre de las restricciones con sus respectivas representaciones en BLC
     */
    private Hashtable<String, XPRBctr> restricciones;

    /**
     * Expresion del objetivo
     */
    private XPRBexpr exprObj;

    /**
     * Mapa que relaciona el nombre de las restricciones con sus respectivas expresiones dentro de BLC
     * Estas expresiones representan la expresión matemática de la resticción
     */
     private Hashtable<String, XPRBexpr> expresiones;

    private int tipoProblema = Constantes.TP_LP;

    private int msgLevel = 0;

    private int numPaso = 0;

    private XPRBbasis baseVieja = null;

    private XPRB bcl;

     public ResolvedorXpress(){
         bcl = new XPRB();
         lp = bcl.newProb("problemaDespaho");
         lp.setMsgLevel(0);
         variables = new Hashtable<>();
         restricciones = new Hashtable<>();
         expresiones = new Hashtable<>();
         setMsgLevel(1);
     }

    /**
     * Función que crea el problema dentro de BCL
     * @param entrada datatype que contiene todos los parámetros del problema
     */
    private void cargarProblema(DatosEntradaProblemaLineal entrada){
        if(lp == null){
            lp = bcl.newProb("");
            lp.setMsgLevel(0);
        }
        // TODO: ver que nombre darle al problema
        tipoProblema = Constantes.TP_LP;
        cargarVariables(entrada.getVariables());

        cargarRestricciones(entrada.getRestricciones(), entrada.getVariables());
        cargarObjetivo(entrada.getObjetivo(), entrada.getVariables());


    }

    /**
     * Carga las variables del problema lineal
     * @param vars lista con la informacion de las variables
     */
    private void cargarVariables(ArrayList<DatosVariableControl> vars){
        for(DatosVariableControl vc: vars){
            // TODO: en algùn futuro puede ser interesante investigar las variables semi continuas para modelar mínimos técnicos
            XPRBvar nuevaVariable;
            if(vc.getTipo() == Constantes.VCCONTINUA){
                XPRBvar var = variables.get(vc.getNombre());
                if(var != null) {
                    nuevaVariable = var;
                    var.setType(XPRB.PL);
                } else {
                    nuevaVariable = lp.newVar(vc.getNombre(), XPRB.PL);
                }

            }else if(vc.getTipo() == Constantes.VCENTERA){
                tipoProblema = Constantes.TP_MIP;
                XPRBvar var = variables.get(vc.getNombre());
                if(var != null) {
                    nuevaVariable = var;
                    var.setType(XPRB.UI);
                } else {
                    nuevaVariable = lp.newVar(vc.getNombre(), XPRB.UI);
                }
            } else if(vc.getTipo() == Constantes.VCBINARIA){
                tipoProblema = Constantes.TP_MIP;
                XPRBvar var = variables.get(vc.getNombre());
                if(var != null) {
                    nuevaVariable = var;
                    var.setType(XPRB.BV);
                } else {
                    nuevaVariable = lp.newVar(vc.getNombre(), XPRB.BV);
                }
            } else {
                // se asume semi-continua
                tipoProblema = Constantes.TP_MIP;
                XPRBvar var = variables.get(vc.getNombre());
                if(var != null) {
                    nuevaVariable = var;
                    var.setType(XPRB.SC);
                } else {
                    nuevaVariable = lp.newVar(vc.getNombre(), XPRB.SC);
                }
            }

            Double cotaInferior = vc.getCotaInferior();
            if(vc.getDominio() == Constantes.VCPOSITIVA){
                if(cotaInferior == null){
                    cotaInferior = 0.0;
                }else{
                    cotaInferior = Double.max(cotaInferior, 0);
                }
            }
            nuevaVariable.setLB(-Constantes.INFNUESTRO);
            nuevaVariable.setUB(Constantes.INFNUESTRO);
            if(cotaInferior != null){
                nuevaVariable.setLB(cotaInferior);
            }
            if(vc.getCotaSuperior() != null){
                nuevaVariable.setUB(vc.getCotaSuperior());
            }
            if(cotaInferior != null && cotaInferior.equals(vc.getCotaSuperior())){
                nuevaVariable.fix(cotaInferior);
            }
            // TODO: quizas haya que elimiar las varaibles que ya no se usen
            if (!variables.containsKey(vc.getNombre())) {
                variables.put(vc.getNombre(), nuevaVariable);
            }

        }
    }

    /**
     * Carga las variables del problema lineal
     * Precondicion: Deben de haberse cargado las variables
     * @param rest lista con la informacion de las restricciones
     * @param vars lista con la informacion de las variables
     */
    private void cargarRestricciones(ArrayList<DatosRestriccion> rest, ArrayList<DatosVariableControl> vars){
        for(DatosRestriccion dr: rest){
            XPRBexpr resExpr =  new XPRBexpr();
            Hashtable<String, Double> terminos = dr.getTerminos();
            for(String nomVar: terminos.keySet()){
                Double coef = terminos.get(nomVar);
                if(coef != null){
                    // TODO: resExpr.setTerm()??
                    //resExpr.delTerm(variables.get(nomVar));
                    XPRBvar variableRes = variables.get(nomVar);
                    // TODO: en la documentacion se agrega asi, pero quizas deberia utilizar el valor devuelto por add()????
                    resExpr.setTerm(variableRes, coef);
                    //resExpr.add(variableRes.mul(coef));
                }
            }
            // TODO: fijarse si esto quedo bien o al revez
            XPRBctr nuevaRestriccion = null;
            if(expresiones.containsKey(dr.getNombre())){
                // TODO: lp.clearDir();????
                XPRBctr ctr = lp.getCtrByName(dr.getNombre());
                ctr.setTerm(dr.getSegundoMiembro());
                for(String termNom: dr.getTerminos().keySet()){
                    double coef = dr.getTermino(termNom);
                    XPRBvar var = variables.get(termNom);
                    ctr.setTerm(var, coef);
                }
                if(dr.getTipo() == Constantes.RESTMENOROIGUAL){
                    ctr.setType(XPRB.L);
                } else if(dr.getTipo() == Constantes.RESTIGUAL){
                    ctr.setType(XPRB.E);
                } else if (dr.getTipo() == Constantes.RESTMAYOROIGUAL){
                    ctr.setType(XPRB.G);
                }
            } else {
                if(dr.getTipo() == Constantes.RESTMENOROIGUAL){
                    nuevaRestriccion = lp.newCtr(dr.getNombre(), resExpr.lEql(dr.getSegundoMiembro()));
                } else if(dr.getTipo() == Constantes.RESTIGUAL){
                    nuevaRestriccion = lp.newCtr(dr.getNombre(), resExpr.eql(dr.getSegundoMiembro()));
                } else if (dr.getTipo() == Constantes.RESTMAYOROIGUAL){
                    nuevaRestriccion = lp.newCtr(dr.getNombre(), resExpr.gEql(dr.getSegundoMiembro()));
                }

                expresiones.put(dr.getNombre(), resExpr);
                restricciones.put(dr.getNombre(), nuevaRestriccion);
            }



        }
    }

    /**
     * Carga las variables del problema lineal
     * Precondicion: Deben de haberse cargado las variables
     * @param dataObj lista con la informacion del objetivo
     * @param vars lista con la informacion de las variables
     */
    private void cargarObjetivo (DatosObjetivo dataObj, ArrayList<DatosVariableControl> vars){
        exprObj = new XPRBexpr();
        for(DatosVariableControl vc: vars){
            Double coef = dataObj.getTerminos().get(vc.getNombre());
            if(coef != null){
                XPRBvar var = variables.get(vc.getNombre());
                // TODO: en la documentacion se agrega asi, pero quizas deberia utilizar el valor devuelto por add()????
                exprObj.add(var.mul(coef));
            }
        }
        // TODO: ver si tiene que ir en positivo o en negativo
        exprObj.add(dataObj.getTerminoIndependiente());
        lp.setObj(exprObj);
    }

    private int resolverProblema(){
        lp.setSense(XPRB.MINIM);
        if(baseVieja != null){
            lp.loadBasis(baseVieja);
        }
        int ret = 0;
        try {
            lp.exportProb("d:/prob");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(tipoProblema == Constantes.TP_LP){
            /*
            * El método de resolución puede ser:
            * "p" use primal simplex algorithm
              "d" use dual simplex algorithm
              "b" use Newton-Barrier algorithm
              "n" use the network solver
              "c" continue previously interrupted optimization run
              "" no parameter: use the default setting
            * */
            lp.lpOptimise("");
            ret = lp.getLPStat();
        } else if(tipoProblema == Constantes.TP_MIP){
            /*
            * "p" use primal simplex algorithm
              "d" use dual simplex algorithm
              "b" use Newton-Barrier algorithm
              "n" use the network solver (for the initial LP)
              "l" stop after solving the initial continuous relaxation
              "c" continue previously interrupted optimization run
              "" no parameter: use the default setting
            * */
            lp.mipOptimise();
            ret = lp.getMIPStat();
        }
        return ret;
    }

    /**
     * En caso de error devuelve un mensaje sobre lo que ocurrio
     * @param ret Código de retorno
     * @return mensaje de error
     */
    private String getMensajeError(int ret){
        String res = "";
        if(tipoProblema == Constantes.TP_LP){
            switch(ret) {
                case XPRB.LP_OPTIMAL:
                    res = "Se encontro la solución óptima";
                    break;
                case XPRB.LP_CUTOFF:
                    res = "El proceso de optimización se detuvo debido a un criterio de parada";
                    break;
                case XPRB.LP_INFEAS:
                    res = "El problema es infactible, presenta restricciones contradictorias";
                    break;
                case XPRB.LP_NONCONVEX:
                    res = "El problema no es convexo, quizas debería de utilizar otro algoritmo de resolución";
                    break;
                case XPRB.LP_CUTOFF_IN_DUAL:
                    res = "El proceso de optimización dual se detuvo debido a un criterio de parada";
                    break;
                case XPRB.LP_UNBOUNDED:
                    res = "El problema es no acotado";
                    break;
                case XPRB.LP_UNFINISHED:
                    res = "El método de resolución se detuvo prematuramente debido a algún criterio (limite iteración/tiempo/etc.)";
                    break;
                case XPRB.LP_UNSOLVED:
                    res = "El solver fue incapaz de encontrar una solución";
                    break;
            }
        }
        if(tipoProblema == Constantes.TP_MIP){
            switch(ret) {
                case XPRB.MIP_OPTIMAL:
                    res = "Se encontro la solución óptima";
                    break;
                case XPRB.MIP_INFEAS:
                    res = "El problema es infactible, presenta restricciones contradictorias";
                    break;
                case XPRB.MIP_SOLUTION:
                    res = "Se encontro una solución factible no optima";
                    break;
                case XPRB.MIP_LP_OPTIMAL:
                    res = "En la relajación del problema se encontro la solución optima, pero no se resolvio el MIP";
                    break;
                case XPRB.MIP_LP_NOT_OPTIMAL:
                    res = "No se eocntro la solución óptima a la relajación del MIP";
                    break;
                case XPRB.MIP_NO_SOL_FOUND:
                    res = "No pudo encontrarse ninguna solución factible";
                    break;
                case XPRB.MIP_NOT_LOADED:
                    res = "No se cargo el problema MIP, puede que exista alguna anomalía al cargar el problema";
                    break;
                case XPRB.MIP_UNBOUNDED:
                    res = "El problema es no acotado";
                    break;
            }
        }
        return res;
    }

    private DatosSalidaProblemaLineal obtenerSolucion(){
        DatosSalidaProblemaLineal res = new DatosSalidaProblemaLineal();
        res.setInfactible(false);
        res.setValorOptimo(lp.getObjVal());
        // Cargo solución
        Hashtable<String, Double> solucion = new Hashtable<>();
        for(String nomVar: variables.keySet()){
            XPRBvar var = variables.get(nomVar);
            solucion.put(nomVar, var.getSol());
        }
        res.setSolucion(solucion);

        if(tipoProblema == Constantes.TP_MIP){
        	lp.fixMIPEntities();
        	lp.lpOptimise();
        }
        
        // Cargo las duales asociadas a las restricciones
        Hashtable<String, Double> duales = new Hashtable<>();
        for(String nomRest: restricciones.keySet()){
            XPRBctr restr = restricciones.get(nomRest);
            try {
                duales.put(nomRest, restr.getDual());
            } catch (Exception e) {

            }


        }
        // Cargo las duales asociadas a las cajas de las variables
        // TODO: ATENCIÓN!!!! POSIBLE PROBLEMA PARA PROBLEMAS MIP, SEGURAMENTE HAY QUE RE RESOLVERLOS PARA OBTENER LA DUAL
        for(String nomVar: variables.keySet()){
            XPRBvar var = variables.get(nomVar);
            duales.put(nomVar + Constantes.prefijoRestriccionCaja, var.getRCost());
        }
        res.setDuales(duales);
        return res;
    }

    /**
     * Libera la memoria
     */
    private void liberarResolvedor(){
        // TODO: quizas hace falta borrar variables, la solución o tal vez la función objetivo


        lp.reset();
        //
        for (String nomRest: restricciones.keySet()){
            XPRBctr rest = restricciones.get(nomRest);
            lp.delCtr(rest);
        }
        for(String nomExp: expresiones.keySet()){
            XPRBexpr expr = expresiones.get(nomExp);
            expr.reset();
            expr.close();
        }
        //lp.close();
        lp.finalize();
        lp = null;

        if(exprObj != null){
            exprObj.reset();
        }
        restricciones.clear();
        variables.clear();
        expresiones.clear();
    }

    @Override
    public DatosSalidaProblemaLineal resolver(DatosEntradaProblemaLineal entrada, DatosEntradaProblemaLineal actualizado,
                                              int escenario, int paso, String dirInfactible, String dirSalidaLP) {
        if(actualizado == null){
            liberarResolvedor();
            cargarProblema(entrada);
        } else {
            cargarVariables(actualizado.getVariables());
            cargarRestricciones(actualizado.getRestricciones(), actualizado.getVariables());
            cargarObjetivo(entrada.getObjetivo(), entrada.getVariables());
        }

        numPaso = paso;
        int ret = resolverProblema();
        DatosSalidaProblemaLineal sol;
        if((tipoProblema == Constantes.TP_LP && ret == XPRB.LP_OPTIMAL) ||
                (tipoProblema == Constantes.TP_MIP && ret == XPRB.MIP_OPTIMAL)){
            // Encontre la solución
            sol = obtenerSolucion();
            if(baseVieja != null){
                //baseVieja.close();
            }
            baseVieja = lp.saveBasis();
        } else {
            sol = new DatosSalidaProblemaLineal();
            sol.setInfactible(true);
            if(msgLevel>=1){
                // Imprimo los errores
                String errMsg = getMensajeError(ret);
                System.out.println("El Solver Xpress tuvo problemas en la resolución: " + errMsg);
                try {
                    lp.exportProb("d:/prob");
                } catch (IOException e) {

                }
            } else {
                System.out.println("PROBLEMA INFACTIBLE EN Xpress EN PASO: " + paso);
                System.out.println("RETORNO: " + ret);
            }
        };
        //liberarResolvedor();
        return sol;
    }

    public int getMsgLevel() {
        return msgLevel;
    }

    public void setMsgLevel(int msgLevel) {
        this.msgLevel = msgLevel;
    }

}
