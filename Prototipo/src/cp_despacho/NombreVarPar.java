package cp_despacho;

public class NombreVarPar {
	
	
	private String var;  // nombre de participante
	private String par;  // nombre de variable según las constantes de ConCP
	
	public NombreVarPar(String var, String par) {
		super();
		this.var = var;
		this.par = par;
	}
	
	public String nombre() {
		return BaseVar.generaNomVarPar(var, par);
	}
	
	

}
