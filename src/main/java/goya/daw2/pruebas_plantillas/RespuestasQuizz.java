package goya.daw2.pruebas_plantillas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto que guarda el estado del quizz en la version 2 (@SessionAttributes).
 * Implementa Serializable porque Tomcat puede guardar la sesion en disco al
 * reiniciar y los objetos que van en sesion deberian serlo.
 */
public class RespuestasQuizz implements Serializable {

	private static final long serialVersionUID = 1L;

	private int numEtapa = 1;
	private String nombre;
	private Integer signo;
	private List<String> aficiones = new ArrayList<>();

	public int getNumEtapa() {
		return numEtapa;
	}

	public void setNumEtapa(int numEtapa) {
		this.numEtapa = numEtapa;
	}

	public void siguienteEtapa() {
		if (numEtapa < Quizz.ETAPA_RESULTADO) {
			numEtapa++;
		}
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getSigno() {
		return signo;
	}

	public void setSigno(Integer signo) {
		this.signo = signo;
	}

	public List<String> getAficiones() {
		return aficiones;
	}

	public void setAficiones(List<String> aficiones) {
		this.aficiones = aficiones;
	}

	@Override
	public String toString() {
		return "RespuestasQuizz[numEtapa=" + numEtapa + ", nombre=" + nombre + ", signo=" + signo + ", aficiones="
				+ aficiones + "]";
	}
}
