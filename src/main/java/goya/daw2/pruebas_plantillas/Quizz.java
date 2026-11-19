package goya.daw2.pruebas_plantillas;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos fijos del quizz y validaciones de cada etapa.
 * Lo saco a una clase aparte porque las tres versiones (sesion, @SessionAttributes
 * y cookies) validan exactamente igual y no queria repetir el codigo tres veces.
 */
public final class Quizz {

	public static final String[] SIGNOS = { "", "Aries", "Tauro", "Géminis", "Cáncer", "Leo", "Virgo", "Libra",
			"Escorpio", "Sagitario", "Capricornio", "Acuario", "Piscis" };

	public static final List<String> AFICIONES = List.of("Deportes", "Juerga", "Lectura", "Relaciones sociales");

	public static final int ETAPA_RESULTADO = 4;

	private Quizz() {
	}

	/** Devuelve el mensaje de error o null si el nombre es valido. */
	public static String validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			return "Debes poner un nombre no vacío";
		}
		int longitud = nombre.trim().length();
		if (longitud < 3 || longitud > 10) {
			return "La longitud del nombre debe estar entre 3 y 10";
		}
		return null;
	}

	/** El signo llega como el indice del array SIGNOS (1..12). */
	public static String validarSigno(String signo) {
		if (signo == null || signo.isBlank()) {
			return "Debes seleccionar un signo";
		}
		try {
			int indice = Integer.parseInt(signo);
			if (indice < 1 || indice >= SIGNOS.length) {
				return "Debes seleccionar un signo";
			}
		} catch (NumberFormatException e) {
			return "Debes seleccionar un signo";
		}
		return null;
	}

	public static String validarAficiones(List<String> aficiones) {
		if (aficiones == null || aficiones.isEmpty()) {
			return "Debes elegir al menos una afición, no seas soso/a";
		}
		// por si alguien manda a mano un valor que no esta en la lista
		if (!AFICIONES.containsAll(aficiones)) {
			return "Has enviado una afición que no existe";
		}
		return null;
	}

	/** Lista que se pinta en la etapa 4. */
	public static List<String> respuestas(String nombre, int signo, List<String> aficiones) {
		List<String> respuestas = new ArrayList<>();
		respuestas.add(nombre);
		respuestas.add(SIGNOS[signo]);
		respuestas.add(String.join(", ", aficiones));
		return respuestas;
	}
}
