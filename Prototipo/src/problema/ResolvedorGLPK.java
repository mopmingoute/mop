/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ResolvedorGLPK is part of MOP.
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

package problema;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import datatypesProblema.*;
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.SWIGTYPE_p_size_t;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import lpsolve.LpSolve;
import utilitarios.Constantes;
import utilitarios.ProfilerBasicoTiempo;

public class ResolvedorGLPK implements ResolvedorLineal {	
	private glp_prob lp;
	private glp_prob lpI;
	private glp_smcp parm;
	private glp_iocp parmI;
	private Hashtable<String, Integer> diccionarioVar;
	private Hashtable<String, Integer> diccionarioRes;
	private SWIGTYPE_p_int colind;
	private SWIGTYPE_p_double coefs;
	private int totalCalls;
	private int problemCreated;
	private int problemReused;
	private int averageLPTime;

	private int ret;
	private int escenario;
	private int paso;

	private int tipoProblema = Constantes.TP_LP;
	private int tipoMetodoResolucion = Constantes.TMR_SIMPLEX;
	/**
	 * GLPK siempre intentara converger haciendo el minimo esfuerzo en primer instancia
	 * Si esta falla GLPK realizara un mayor esfuerzo hasta lograr convergencia
	 */
	private int maximoEsfuerzoSolucion = Constantes.EF_ALTO;

	private void crearDimensionesProblema(DatosEntradaProblemaLineal entrada) {
		// System.out.println("ResolvedorGLPK::crearProblema");
		ArrayList<DatosVariableControl> variables = entrada.getVariables();
		ArrayList<DatosRestriccion> restricciones = entrada.getRestricciones();

		// Create problem
		// System.out.println("ResolvedorGLPK::crearDimensionesProblema");
		lp = GLPK.glp_create_prob();
		// System.out.println("Problem created");
		GLPK.glp_add_cols(lp, variables.size());
		GLPK.glp_add_rows(lp, restricciones.size());

		int icol = 1;
		for (DatosVariableControl dvc : variables) {
			diccionarioVar.put(dvc.getNombre(), icol);
			GLPK.glp_set_col_name(lp, icol, dvc.getNombre());
			icol++;
		}
		int irest = 1;
		for (DatosRestriccion dr : restricciones) {
			diccionarioRes.put(dr.getNombre(), irest);
			GLPK.glp_set_row_name(lp, irest, dr.getNombre());
			irest++;
		}

		GLPK.glp_set_obj_name(lp, "z");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);

	}

	private void asignarCoeficientesProblema(DatosEntradaProblemaLineal entrada) {

		// System.out.println("ResolvedorGLPK::asignarCoeficientesProblema");

		ArrayList<DatosVariableControl> variables = entrada.getVariables();
		ArrayList<DatosRestriccion> restricciones = entrada.getRestricciones();

		int icol;
		tipoProblema = Constantes.TP_LP;
		if (!variables.isEmpty()) {
			for (DatosVariableControl dvc : variables) {
				icol = diccionarioVar.get(dvc.getNombre());
				// Tipo de variable: binaria, entera o continua
				if (dvc.getTipo() == Constantes.VCBINARIA) {
					// tipo binario
					tipoProblema = Constantes.TP_MIP;
					GLPK.glp_set_col_kind(lp, icol, GLPKConstants.GLP_BV);
				} else if (dvc.getTipo() == Constantes.VCENTERA) {
					// tipo entero
					tipoProblema = Constantes.TP_MIP;
					GLPK.glp_set_col_kind(lp, icol, GLPKConstants.GLP_IV);
				} else if (dvc.getTipo() == Constantes.VCCONTINUA) {
					GLPK.glp_set_col_kind(lp, icol, GLPKConstants.GLP_CV);
				}

				if (dvc.getCotaInferior() == null && dvc.getCotaSuperior() == null) {
					if (dvc.getDominio() == Constantes.VCPOSITIVA) {
						GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_LO, 0.0, 0.0);
					} else {
						GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_FR, 0.0, 0.0);
					}
				}

				if (dvc.getCotaInferior() == null && dvc.getCotaSuperior() != null) {
					if (dvc.getDominio() == Constantes.VCPOSITIVA) {
						if (dvc.getCotaSuperior() == 0.0) {
							GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_FX, 0.0, 0.0);
						} else {
							GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_DB, 0.0, dvc.getCotaSuperior());
						}
					} else {
						GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_UP, 0.0, dvc.getCotaSuperior());
					}
				}

				if (dvc.getCotaInferior() != null && dvc.getCotaSuperior() == null) {
					if (dvc.getDominio() == Constantes.VCPOSITIVA) {
						if (dvc.getCotaInferior() == 0.0) {
							GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_FX, 0.0, 0.0);
						} else {
							GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_LO, Math.max(0.0, dvc.getCotaInferior()),
									0.0);
						}
					} else {
						GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_LO, dvc.getCotaInferior(), 0.0);
					}
				}

				if (dvc.getCotaInferior() != null && dvc.getCotaSuperior() != null) {
					if (dvc.getCotaInferior().equals(dvc.getCotaSuperior())) {
						GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_FX, dvc.getCotaInferior(),
								dvc.getCotaSuperior());
					} else {
						if (dvc.getDominio() == Constantes.VCPOSITIVA) {
							GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_DB, Math.max(0.0, dvc.getCotaInferior()),
									dvc.getCotaSuperior());
						} else {
							GLPK.glp_set_col_bnds(lp, icol, GLPKConstants.GLP_DB, dvc.getCotaInferior(),
									dvc.getCotaSuperior());
						}
					}

				}

				// icol++;
			}
		}

		/*
		 * SWIGTYPE_p_int colind; SWIGTYPE_p_int rowind; SWIGTYPE_p_double val;
		 * 
		 * int cant_col = diccionarioVar.size(); int cant_row = restricciones.size();
		 * 
		 * int mat_dim = cant_col * cant_row;
		 * 
		 * colind = GLPK.new_intArray(mat_dim + 1); rowind = GLPK.new_intArray(mat_dim +
		 * 1); val = GLPK.new_doubleArray(mat_dim + 1);
		 */

		int irest;
		int iaux;
		if (!restricciones.isEmpty()) {
			for (DatosRestriccion dr : restricciones) {
				irest = diccionarioRes.get(dr.getNombre());
				if (dr.getTipo() == Constantes.RESTMENOROIGUAL) {
					GLPK.glp_set_row_bnds(lp, irest, GLPKConstants.GLP_UP, 0, dr.getSegundoMiembro());
				} else if (dr.getTipo() == Constantes.RESTMAYOROIGUAL) {
					GLPK.glp_set_row_bnds(lp, irest, GLPKConstants.GLP_LO, dr.getSegundoMiembro(), 0);
				} else if (dr.getTipo() == Constantes.RESTIGUAL) {
					GLPK.glp_set_row_bnds(lp, irest, GLPKConstants.GLP_FX, dr.getSegundoMiembro(),
							dr.getSegundoMiembro());
				}
			}

			int rowSize = diccionarioVar.size();
			colind = GLPK.new_intArray(rowSize + 1);
			coefs = GLPK.new_doubleArray(rowSize + 1);
			/*
			 * rowind = GLPK.new_intArray(1); colind = GLPK.new_intArray(1); coefs =
			 * GLPK.new_doubleArray(1);
			 */

			for (DatosRestriccion dr : restricciones) {

				irest = diccionarioRes.get(dr.getNombre());
				ArrayList<Double> restrCoefs = dr.getCoefs();
				double coef;
				iaux = 1;
				// System.out.println("ResolvedorGLPK::asignarCoeficientesProblema::"
				// +
				// dr.getNombre());

				for (String var : dr.getVars()) {
					if (var != null) {
						icol = diccionarioVar.get(var);
						coef = (double) restrCoefs.get(iaux - 1);

						GLPK.intArray_setitem(colind, iaux, icol);
						GLPK.doubleArray_setitem(coefs, iaux, coef);

						iaux++;
					}
				}
				GLPK.glp_set_mat_row(lp, irest, iaux - 1, colind, coefs);
			}

			// System.out.println("ResolvedorGLPK::asignarCoeficientesProblema::glp_load_matrix");

			// GLPK.glp_load_matrix(lp, sizeCoefs, rowind, colind, coefs);
			GLPK.delete_intArray(colind);
			// System.out.println("ResolvedorGLPK::resolver::delete_intArray(colind)");
			GLPK.delete_doubleArray(coefs);
			// System.out.println("ResolvedorGLPK::resolver::delete_doubleArray(coefs)");

		}
		// int indice = 1;

		// SE DEFINE OBJETIVO

		String clave;
		if (entrada.getObjetivo() != null) {
			Hashtable<String, Double> terminos = entrada.getObjetivo().getTerminos();
			Set<String> set;

			set = terminos.keySet();
			Iterator<String> itr = set.iterator();
			iaux = 1;
			double coef;
			while (itr.hasNext()) {
				clave = itr.next();
				coef = terminos.get(clave);
				icol = diccionarioVar.get(clave);
				GLPK.glp_set_obj_coef(lp, icol, coef);

				iaux++;
			}
			GLPK.glp_set_obj_coef(lp, 0, entrada.getObjetivo().getTerminoIndependiente());

		}

	}


	private int resolverLPNuevo(){
		parm.setPresolve(GLPKConstants.GLP_ON);
		int retLP = GLPK.glp_simplex(lp, parm);
		boolean resetBasis = false;
		if (retLP != 0) {
			GLPK.glp_std_basis(lp);
			GLPK.glp_scale_prob(lp, GLPKConstants.GLP_SF_AUTO);
			resetBasis = true;
			if(retLP != 0 && maximoEsfuerzoSolucion >= Constantes.EF_BAJO){
				retLP = GLPK.glp_simplex(lp, parm);
			}
			if(retLP != 0 && maximoEsfuerzoSolucion >=  Constantes.EF_MEDIO){
				parm.setMeth(GLPKConstants.GLP_DUALP);
				retLP = GLPK.glp_simplex(lp, parm);
			}
			if(retLP != 0 && maximoEsfuerzoSolucion >= Constantes.EF_ALTO){
			}


			if (retLP != 0) {
//				if (Constantes.NIVEL_CONSOLA > 1) {
				//	System.out.println("ResolvedorGLPK::llamarSolver - cagamos segundo lp");
//				}
			}
			GLPK.glp_unscale_prob(lp);
		}
		DatosResultadoKKT resKKT = ejecutarKKT();
		if(!resKKT.cumpleKKTRelativo() && maximoEsfuerzoSolucion >= Constantes.EF_ALTO){
			parm.setTm_lim(averageLPTime*30);
			retLP = GLPK.glp_exact(lp, parm);
		}

		return retLP;
	}

	private int resolverLPViejo(){
		int retLP = GLPK.glp_simplex(lp, parm);
		boolean resetBasis = false;
		if (retLP != 0) {
			GLPK.glp_scale_prob(lp, GLPKConstants.GLP_SF_AUTO);
			resetBasis = true;
			GLPK.glp_std_basis(lp);
			if (Constantes.RES_GLP_RESET_EXACTO) {
				retLP = GLPK.glp_exact(lp, parm);
			} else {
				retLP = GLPK.glp_simplex(lp, parm);
			}


			if (retLP != 0) {
//				if (Constantes.NIVEL_CONSOLA > 1) {
				//	System.out.println("ResolvedorGLPK::llamarSolver - cagamos segundo lp");
//				}
			}
			GLPK.glp_unscale_prob(lp);
		}
		return retLP;
	}

	private DatosResultadoKKT ejecutarKKT(){
		DatosResultadoKKT resKKT = new DatosResultadoKKT();
		SWIGTYPE_p_int p_ae_ind, p_re_ind;
		SWIGTYPE_p_double p_ae_max, p_re_max;

		p_ae_ind = GLPK.new_intArray(1);
		p_re_ind = GLPK.new_intArray(1);
		p_ae_max = GLPK.new_doubleArray(1);
		p_re_max = GLPK.new_doubleArray(1);

		GLPK.glp_check_kkt(lp, GLPKConstants.GLP_SOL, GLPKConstants.GLP_KKT_PE, p_ae_max, p_ae_ind, p_re_max,
				p_re_ind);

		int ae_indPE = GLPK.intArray_getitem(p_ae_ind, 0);
		int re_indPE = GLPK.intArray_getitem(p_re_ind, 0);
		double ae_maxPE = GLPK.doubleArray_getitem(p_ae_max, 0);
		double re_maxPE = GLPK.doubleArray_getitem(p_re_max, 0);
		resKKT.setAe_indPE(ae_indPE);
		resKKT.setRe_indPE(re_indPE);
		resKKT.setAe_maxPE(ae_maxPE);
		resKKT.setRe_maxPE(re_maxPE);
		ae_indPE = Math.abs(ae_maxPE) > Constantes.RES_GLP_TOL ? ae_indPE : -1;
		re_indPE = Math.abs(re_maxPE) > Constantes.RES_GLP_TOL ? re_indPE : -1;
		ae_maxPE = Math.abs(ae_maxPE) > Constantes.RES_GLP_TOL ? ae_maxPE : 0;
		re_maxPE = Math.abs(re_maxPE) > Constantes.RES_GLP_TOL ? re_maxPE : 0;

		GLPK.glp_check_kkt(lp, GLPKConstants.GLP_SOL, GLPKConstants.GLP_KKT_PB, p_ae_max, p_ae_ind, p_re_max,
				p_re_ind);

		int ae_indPB = GLPK.intArray_getitem(p_ae_ind, 0);
		int re_indPB = GLPK.intArray_getitem(p_re_ind, 0);
		double ae_maxPB = GLPK.doubleArray_getitem(p_ae_max, 0);
		double re_maxPB = GLPK.doubleArray_getitem(p_re_max, 0);
		resKKT.setAe_indPB(ae_indPB);
		resKKT.setRe_indPB(re_indPB);
		resKKT.setAe_maxPB(ae_maxPB);
		resKKT.setRe_maxPB(re_maxPB);
		ae_indPB = Math.abs(ae_maxPB) > Constantes.RES_GLP_TOL ? ae_indPB : -1;
		re_indPB = Math.abs(re_maxPB) > Constantes.RES_GLP_TOL ? re_indPB : -1;
		ae_maxPB = Math.abs(ae_maxPB) > Constantes.RES_GLP_TOL ? ae_maxPB : 0;
		re_maxPB = Math.abs(re_maxPB) > Constantes.RES_GLP_TOL ? re_maxPB : 0;

		GLPK.glp_check_kkt(lp, GLPKConstants.GLP_SOL, GLPKConstants.GLP_KKT_DE, p_ae_max, p_ae_ind, p_re_max,
				p_re_ind);

		int ae_indDE = GLPK.intArray_getitem(p_ae_ind, 0);
		int re_indDE = GLPK.intArray_getitem(p_re_ind, 0);
		double ae_maxDE = GLPK.doubleArray_getitem(p_ae_max, 0);
		double re_maxDE = GLPK.doubleArray_getitem(p_re_max, 0);
		resKKT.setAe_indDE(ae_indDE);
		resKKT.setRe_indDE(re_indDE);
		resKKT.setAe_maxDE(ae_maxDE);
		resKKT.setRe_maxDE(re_maxDE);
		ae_indDE = Math.abs(ae_maxDE) > Constantes.RES_GLP_TOL ? ae_indDE : -1;
		re_indDE = Math.abs(re_maxDE) > Constantes.RES_GLP_TOL ? re_indDE : -1;
		ae_maxDE = Math.abs(ae_maxDE) > Constantes.RES_GLP_TOL ? ae_maxDE : 0;
		re_maxDE = Math.abs(re_maxDE) > Constantes.RES_GLP_TOL ? re_maxDE : 0;

		GLPK.glp_check_kkt(lp, GLPKConstants.GLP_SOL, GLPKConstants.GLP_KKT_DB, p_ae_max, p_ae_ind, p_re_max,
				p_re_ind);

		int ae_indDB = GLPK.intArray_getitem(p_ae_ind, 0);
		int re_indDB = GLPK.intArray_getitem(p_re_ind, 0);
		double ae_maxDB = GLPK.doubleArray_getitem(p_ae_max, 0);
		double re_maxDB = GLPK.doubleArray_getitem(p_re_max, 0);
		resKKT.setAe_indDB(ae_indDB);
		resKKT.setRe_indDB(re_indDB);
		resKKT.setAe_maxDB(ae_maxDB);
		resKKT.setRe_maxDB(re_maxDB);
		ae_indDB = Math.abs(ae_maxDB) > Constantes.RES_GLP_TOL ? ae_indDB : -1;
		re_indDB = Math.abs(re_maxDB) > Constantes.RES_GLP_TOL ? re_indDB : -1;
		ae_maxDB = Math.abs(ae_maxDB) > Constantes.RES_GLP_TOL ? ae_maxDB : 0;
		re_maxDB = Math.abs(re_maxDB) > Constantes.RES_GLP_TOL ? re_maxDB : 0;

		/*
		 * System.out.println("ResolvedorGLPK::check_kkt\t" + totalCalls + "\tPE\taeM\t"
		 * + ae_maxPE + "\taeI\t" + ae_indPE + "\treM\t" + re_maxPE + "\treI\t" +
		 * re_indPE); System.out.println("ResolvedorGLPK::check_kkt\t" + totalCalls +
		 * "\tPB\taeM\t" + ae_maxPB + "\taeI\t" + ae_indPB + "\treM\t" + re_maxPB +
		 * "\treI\t" + re_indPB); System.out.println("ResolvedorGLPK::check_kkt\t" +
		 * totalCalls + "\tDE\taeM\t" + ae_maxDE + "\taeI\t" + ae_indDE + "\treM\t" +
		 * re_maxDE + "\treI\t" + re_indDE);
		 * System.out.println("ResolvedorGLPK::check_kkt\t" + totalCalls + "\tDB\taeM\t"
		 * + ae_maxDB + "\taeI\t" + ae_indDB + "\treM\t" + re_maxDB + "\treI\t" +
		 * re_indDB);
		 */
		GLPK.delete_intArray(p_ae_ind);
		GLPK.delete_intArray(p_re_ind);

		GLPK.delete_doubleArray(p_ae_max);
		GLPK.delete_doubleArray(p_re_max);
		return resKKT;
	}

	/**
	 * Realiza la configuración del solver, y procede a resolver el problema
	 */
	private void llamarSolver() {
		// System.out.println("ResolvedorGLPK::llamarSolver");
		 
		
		
		parmI = new glp_iocp();
		GLPK.glp_init_iocp(parmI);
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		parmI.setMsg_lev(GLPKConstants.GLP_MSG_OFF);
		/*
		 * parm.setMeth(GLPKConstants.GLP_DUALP);
		 * 
		 * parm.setPresolve(GLPKConstants.GLP_ON);
		 */
		// TODO: ver porque la toleracia puede llevar a problemas de estabilidad numérica
		if(Constantes.UTILIZAR_GLPK_NUEVO){
			parm.setTol_bnd(Constantes.RES_GLP_TOL);
			parm.setTol_dj(Constantes.RES_GLP_TOL);
			parm.setTol_piv(Constantes.RES_GLP_TOL);
		}
		parm.setTm_lim(averageLPTime);

		// Modifica seteos default de precision
		parmI.setTol_int(Constantes.RES_GLP_TOL);
		parmI.setMip_gap(Constantes.RES_GLP_TOL);
		parmI.setTm_lim(averageLPTime);

		parm.setMsg_lev(GLPKConstants.GLP_MSG_OFF);
		int retWarm = 0;
		/*
		 * if (GLPK.glp_bf_exists(lp) == 0) { int retFact = GLPK.glp_factorize(lp); if
		 * (retFact != 0) { System.out.println(
		 * "ResolvedorGLPK::llamarSolver - CAGAMOS LA BASE INICIAL"); } }
		 */


		if (Constantes.RES_GLP_RB) {
			retWarm = GLPK.glp_warm_up(lp);

			ejecutarKKT();

		} else {
			GLPK.glp_std_basis(lp);
		}

		if (retWarm != 0) {
//			if (Constantes.NIVEL_CONSOLA > 1) {
			//	System.out.println("ResolvedorGLPK::llamarSolver - CAGAMOS LA BASE INICIAL");
//			}
		}

		int retLP = 0;

		if(tipoProblema == Constantes.TP_LP){
			tipoMetodoResolucion = Constantes.TMR_SIMPLEX;
			if(Constantes.UTILIZAR_GLPK_NUEVO){
				retLP = resolverLPNuevo();
			} else {
				retLP = resolverLPViejo();
			}


		}
		ret = retLP;
		if(tipoProblema == Constantes.TP_MIP){
			tipoMetodoResolucion = Constantes.TMR_intopt;

			if(Constantes.UTILIZAR_GLPK_NUEVO){
				retLP = resolverLPNuevo();
			} else {
				retLP = resolverLPViejo();
			}

			ret = GLPK.glp_intopt(lp, parmI);
			if (ret != 0) {
//			if (Constantes.NIVEL_CONSOLA > 1) {
				//	System.out.println("ResolvedorGLPK::llamarSolver - cagamos el mip");
				//		}
				GLPK.glp_scale_prob(lp, GLPKConstants.GLP_SF_AUTO);
				//resetBasis = true;
				GLPK.glp_std_basis(lp);
				if (Constantes.RES_GLP_RESET_EXACTO) {
					retLP = GLPK.glp_exact(lp, parm);
				} else {
					retLP = GLPK.glp_simplex(lp, parm);
				}
				if (retLP != 0) {
//				if (Constantes.NIVEL_CONSOLA > 1) {
					//	System.out.println("ResolvedorGLPK::llamarSolver - cagamos segundo lp en mip");
//				}
				}
				ret = GLPK.glp_intopt(lp, parmI);
				if (ret != 0) {
//				if (Constantes.NIVEL_CONSOLA > 1) {
					//	System.out.println("ResolvedorGLPK::llamarSolver - cagamos el segundo mip");
//				}
				}

				GLPK.glp_unscale_prob(lp);
			}

		}


		String retLPString = "";
		if (retLP == 0) {
			retLPString = "Optimo";
		}
		if (retLP == GLPKConstants.GLP_EBADB) {
			retLPString = "GLP_EBADB";
		}
		if (retLP == GLPKConstants.GLP_ESING) {
			retLPString = "GLP_ESING";
		}
		if (retLP == GLPKConstants.GLP_ECOND) {
			retLPString = "GLP_ECOND";
		}
		if (retLP == GLPKConstants.GLP_EBOUND) {
			retLPString = "GLP_EBOUND";
		}
		if (retLP == GLPKConstants.GLP_EFAIL) {
			retLPString = "GLP_EFAIL";
		}
		if (retLP == GLPKConstants.GLP_EOBJLL) {
			retLPString = "GLP_EOBJLL";
		}
		if (retLP == GLPKConstants.GLP_EOBJUL) {
			retLPString = "GLP_EOBJUL";
		}
		if (retLP == GLPKConstants.GLP_EITLIM) {
			retLPString = "GLP_EITLIM";
		}
		if (retLP == GLPKConstants.GLP_ETMLIM) {
			retLPString = "GLP_ETMLIM";
		}
		if (retLP == GLPKConstants.GLP_ENOPFS) {
			retLPString = "GLP_ENOPFS";
		}
		if (retLP == GLPKConstants.GLP_ENODFS) {
			retLPString = "GLP_ENODFS";
		}

		String retMIPString = "";
		if (ret == 0) {
			retMIPString = "Optimo";
		}
		if (ret == GLPKConstants.GLP_EBOUND) {
			retMIPString = "GLP_EBOUND";
		}
		if (ret == GLPKConstants.GLP_EROOT) {
			retMIPString = "GLP_EROOT";
		}
		if (ret == GLPKConstants.GLP_ENOPFS) {
			retMIPString = "GLP_ENOPFS";
		}
		if (ret == GLPKConstants.GLP_ENODFS) {
			retMIPString = "GLP_ENODFS";
		}
		if (ret == GLPKConstants.GLP_EFAIL) {
			retMIPString = "GLP_EFAIL";
		}
		if (ret == GLPKConstants.GLP_EMIPGAP) {
			retMIPString = "GLP_EMIPGAP";
		}
		if (ret == GLPKConstants.GLP_ETMLIM) {
			retMIPString = "GLP_ETMLIM";
		}
		if (ret == GLPKConstants.GLP_ESTOP) {
			retMIPString = "GLP_ESTOP";
		}

		//String msg = +totalCalls + "\tstLP\t" + retLPString;
		//msg = resetBasis ? msg + "_RESET_BASIS" : msg;
		//msg = msg + "\tvalLP\t" + GLPK.glp_get_obj_val(lp);
		//msg = msg + "\tstMIP\t" + retMIPString + "\tvalMIP\t" + GLPK.glp_mip_obj_val(lp);

		// System.out.println("ResolvedorGLPK::llamarSolve\t" + msg);
	//	ret = retLP;
	}

	public DatosSalidaProblemaLineal resolver(DatosEntradaProblemaLineal entrada,
			DatosEntradaProblemaLineal actualizado, int escenario, int paso, String dirInfactible, String dirSalidaLP) {
		DatosSalidaProblemaLineal resultados = null;
		ret = 0;
		
		this.escenario = escenario;
		this.paso=paso;
		//String directorio = "../salidasLP";
		// TODO: agregar alguna condicion if, demasiada impresion
		if(paso==97 && escenario==53001701){
			//GLPK.glp_term_out(GLPKConstants.GLP_ON);
			entrada.imprimir("d:/salidasModeloOp/PRUEBA-DUAL/lp/entrada"+Integer.toString(escenario)+"paso"+paso);
		}
		try {
			totalCalls++;
//			ProfilerBasicoTiempo pbt = ProfilerBasicoTiempo.getInstance();

			// int calcTime = (int)
			// Math.ceil(2*pbt.getMilisegundosAcumulados("ResolverGLPK::llamarSolver")/totalCalls);
			// averageLPTime = Math.max(calcTime, averageLPTime);

//			pbt.continuarContador("ResolverGLPK::crearProblema");

			if (!Constantes.RES_GLP_RP) {

				// ------------------------ 1 -------------------------------

				if (diccionarioVar != null) {
					// GLPK.glp_delete_prob(lp); // Solo elimina el problema
				}
				diccionarioVar = new Hashtable<String, Integer>();
				diccionarioRes = new Hashtable<String, Integer>();
				problemCreated++;

				crearDimensionesProblema(entrada);

				////////////////////////////////////////

				asignarCoeficientesProblema(entrada);

			} else {

				// ------------------------ 2 -------------------------------

				if (actualizado != null) {
					asignarCoeficientesProblema(actualizado);
					problemReused++;
				} else {
					if (diccionarioVar != null) {
						GLPK.glp_delete_prob(lp); // Solo elimina el problema
					}
					problemCreated++;
					diccionarioVar = new Hashtable<String, Integer>();
					diccionarioRes = new Hashtable<String, Integer>();
					crearDimensionesProblema(entrada);
					asignarCoeficientesProblema(entrada);
				}
			}

//			pbt.pausarContador("ResolverGLPK::crearProblema");

			if (Constantes.RES_GLP_IMPRIMIR_LP_SOL) {
				// impresion del problema a resolver

				String nameLP = "glpk_";
				if (Constantes.RES_GLP_RP) {
					nameLP += "rp_";
				} else {
					nameLP += "nrp_";
				}
				if (Constantes.RES_GLP_RB) {
					nameLP += "rb_";
				} else {
					nameLP += "nrb_";
				}

				
				// GLPK.glp_write_lp(lp, null, Constantes.RES_GLP_IMPRIMIR_LP_RUTA + nameLP +
				// totalCalls + ".lp");
			}

//			pbt.continuarContador("ResolverGLPK::llamarSolver");
			if (paso==97 && escenario==53001701) {
				int pp = 0;
				pp++;
			}
			llamarSolver();
//			pbt.pausarContador("ResolverGLPK::llamarSolver");

			// Retrieve solution

//			if (ret == 0 || ret == GLPKConstants.GLP_EMIPGAP || ret == GLPKConstants.GLP_EITLIM
//					|| ret == GLPKConstants.GLP_ETMLIM) {
			if (ret == 0) {

//				pbt.continuarContador("ResolverGLPK::obtenerSolucion");
				// GLPK.glp_write_lp(lp, null, Constantes.RES_GLP_IMPRIMIR_LP_RUTA +
				// "casoOptimo.lp");

				DatosSalidaProblemaLineal sal = obtenerSolucion(lp);
//				pbt.pausarContador("ResolverGLPK::obtenerSolucion");
				/*
				 * // invocacion a chequeos de memoria de glpk
				 * 
				 * SWIGTYPE_p_int count = GLPK.new_intArray(1); SWIGTYPE_p_int cpeak =
				 * GLPK.new_intArray(1); GLPK.glp_mem_usage(count, cpeak, null, null); long co =
				 * GLPK.intArray_getitem(count, 0); long cp = GLPK.intArray_getitem(cpeak, 0);
				 * String line = "Paso\t" + paso + "\tes\t" + escenario + "\tcant\t" + cant +
				 * "\tcount\t" + co + "\tcpeak" + cp + "\n"; try { Files.write(Paths.get(
				 * "d:\\salidasModeloOp\\salidaglpk\\log\\mem_glpk.txt"), line.getBytes(),
				 * StandardOpenOption.APPEND); }catch (IOException e) { //exception handling
				 * left as an exercise for the reader } GLPK.delete_intArray(count);
				 * GLPK.delete_intArray(cpeak);
				 */
				// GLPK.glp_delete_prob(lp); // Solo elimina el problema
				// System.out.println("ResolvedorGLPK::resolver::glp_delete_prob");

				if (Constantes.RES_GLP_IMPRIMIR_LP_SOL) {
					// impresion del problema a resolver
					String nameLP = "glpk_";
					if (Constantes.RES_GLP_RP) {
						nameLP += "rp_";
					} else {
						nameLP += "nrp_";
					}
					if (Constantes.RES_GLP_RB) {
						nameLP += "rb_";
					} else {
						nameLP += "nrb_";
					}

					// sal.printSolucion(Constantes.RES_GLP_IMPRIMIR_LP_RUTA + nameLP + "sol_" +
					// totalCalls + ".txt");
				}
				//GLPK.glp_write_lp(lp, null,	"d:\\CONCONTRATO.lp");
//				if (escenario==0 && paso==4) {
//					int ret = GLPK.glp_write_lp(lp, null,
//						"d:\\esc0paso4.lp");
//				}
//				System.out.println("ESCENARIO " + escenario);
//				System.out.println("PASO " + paso);
//				if (escenario==2 && paso==9) {
//					int ret = GLPK.glp_write_lp(lp, null,
//						"d:\\esc2paso17.lp");
//					sal.printSolucion("d:\\salidaesc1paso4.sal");
//				}
				if (paso==97 && escenario==53001701) {
					//GLPK.glp_term_out(GLPKConstants.GLP_OFF);
					int ret = GLPK.glp_write_lp(lp, null,
							"d:/salidasModeloOp/PRUEBA-DUAL/lp/prob"+Integer.toString(escenario)+"paso"+paso+".lp");
					sal.printSolucion("d:/salidasModeloOp/PRUEBA-DUAL/lp/sal"+Integer.toString(escenario)+"paso"+paso);
				}
				
				return sal;
			} else {
				System.out.println("PROBLEMA INFACTIBLE EN GLPK EN PASO: " + paso);
				System.out.println("RETORNO: " + ret);
				if (ret == GLPKConstants.GLP_EROOT) {
					int ret = GLPK.glp_write_lp(lp, null,
							dirInfactible + "infactGLPKesc" + escenario + "paso" + paso + ".lp");
				}
				DatosSalidaProblemaLineal res = new DatosSalidaProblemaLineal();
				res.setInfactible(true);
				return res;
			}

		} catch (GlpkException ex) {
			ex.printStackTrace();
			ret = 1;
		}
		// GLPK.glp_delete_prob(lp);

		return null;
	}

	/**
	 * write simplex solution
	 * 
	 * @param lp problem
	 */
	static void write_lp_solution(glp_prob lp) {
		int i;
		int n;
		String name;
		double val;

		name = GLPK.glp_get_obj_name(lp);
		val = GLPK.glp_get_obj_val(lp);
		System.out.print(name);
		System.out.print(" = ");
		System.out.println(val);
		n = GLPK.glp_get_num_cols(lp);
		for (i = 1; i <= n; i++) {
			name = GLPK.glp_get_col_name(lp, i);
			val = GLPK.glp_get_col_prim(lp, i);
			System.out.print(name);
			System.out.print(" = ");
			System.out.println(val);
		}
		// TODO Auto-generated method stub

	}

	private DatosSalidaProblemaLineal obtenerSolucion(glp_prob lp) {
		DatosSalidaProblemaLineal sal = new DatosSalidaProblemaLineal();
		Hashtable<String, Double> solucion = new Hashtable<String, Double>();
		Hashtable<String, Double> duales = new Hashtable<String, Double>();
	//	System.out.println(GLPK.glp_version());
		sal.setSolucion(solucion);
		sal.setDuales(duales);

		double val = 0;
		if(tipoMetodoResolucion == Constantes.TMR_SIMPLEX){
			val = GLPK.glp_get_obj_val(lp);
		}
		if(tipoMetodoResolucion == Constantes.TMR_intopt){
			val = GLPK.glp_mip_obj_val(lp);
		}
		sal.setValorOptimo(val);

		int icol;
		for (String nom : diccionarioVar.keySet()) {
			icol = diccionarioVar.get(nom);
			if(tipoMetodoResolucion == Constantes.TMR_SIMPLEX){
				solucion.put(nom, GLPK.glp_get_col_prim(lp, icol));
			}
			if(tipoMetodoResolucion == Constantes.TMR_intopt){
				solucion.put(nom, GLPK.glp_mip_col_val(lp, icol));
			}
		}

		int ir;
		for (String nom : diccionarioRes.keySet()) {
			ir = diccionarioRes.get(nom);
			// duales.put(nom, GLPK.glp_mip_row_val(lp, ir));
			duales.put(nom, GLPK.glp_get_row_dual(lp, ir));
		}
		// TODO: codigo prueba para obtener duales de cotas
		for (String nom : diccionarioVar.keySet()) {
			icol = diccionarioVar.get(nom);
			duales.put(nom+Constantes.prefijoRestriccionCaja, GLPK.glp_get_col_dual(lp, icol));
		}

		return sal;
	}

	public ResolvedorGLPK() {
		totalCalls = 0;
		problemCreated = 0;
		problemReused = 0;
		averageLPTime = Constantes.RES_GLP_TIEMPO_MAX;
		
		int termFlag = GLPKConstants.GLP_ON;
		if (Constantes.NIVEL_CONSOLA == 0) {
			termFlag = GLPKConstants.GLP_OFF;
		}
		GLPK.glp_term_out(termFlag);
		if (Constantes.RES_GLP_RB) {
			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("ResolvedorGLPK::REUTILIZA BASE");
		} else {
			if (Constantes.NIVEL_CONSOLA > 1)
				System.out.println("ResolvedorGLPK::NO REUTLIZA BASE");
		}
	}
}