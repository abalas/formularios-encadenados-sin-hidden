package goya.daw2.pruebas_plantillas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/**
 * VERSION 1: el quizz sin campos hidden, guardando todo en la HttpSession.
 *
 * Antes la etapa y las respuestas anteriores viajaban en inputs hidden en cada
 * formulario. Ahora el formulario solo envia el campo de la etapa actual y el
 * resto lo guarda el servidor en la sesion del usuario (cookie JSESSIONID).
 */
@Controller
@RequestMapping("/sesion")
public class QuizzSesionController {

	private static final String BASE = "/sesion";

	// nombres de los atributos que guardo en la sesion
	private static final String ETAPA = "numEtapa";
	private static final String NOMBRE = "nombre";
	private static final String SIGNO = "signo";
	private static final String AFICIONES = "aficiones";

	@GetMapping
	public String mostrarEtapa(HttpSession sesion, Model modelo) {
		return pintarEtapa(etapaActual(sesion), sesion, modelo);
	}

	@PostMapping
	public String procesarEtapa(@RequestParam(name = "nombre", required = false) String nombre,
			@RequestParam(name = "signo", required = false) String signo,
			@RequestParam(name = "aficiones", required = false) List<String> aficiones,
			HttpSession sesion, Model modelo) {

		int etapa = etapaActual(sesion);
		String error = null;

		switch (etapa) {
		case 1 -> {
			error = Quizz.validarNombre(nombre);
			if (error == null) {
				sesion.setAttribute(NOMBRE, nombre.trim());
			}
		}
		case 2 -> {
			error = Quizz.validarSigno(signo);
			if (error == null) {
				sesion.setAttribute(SIGNO, Integer.parseInt(signo));
			}
		}
		case 3 -> {
			error = Quizz.validarAficiones(aficiones);
			if (error == null) {
				sesion.setAttribute(AFICIONES, aficiones);
			}
		}
		default -> {
			// ya esta en el resultado, no hay nada que procesar
		}
		}

		if (error != null) {
			modelo.addAttribute("errores", error);
			return pintarEtapa(etapa, sesion, modelo);
		}

		if (etapa < Quizz.ETAPA_RESULTADO) {
			sesion.setAttribute(ETAPA, etapa + 1);
		}
		// Post/Redirect/Get: asi si se pulsa F5 no se reenvia el formulario
		return "redirect:" + BASE;
	}

	@GetMapping("/reiniciar")
	public String reiniciar(HttpSession sesion) {
		sesion.invalidate();
		return "redirect:" + BASE;
	}

	private int etapaActual(HttpSession sesion) {
		Integer etapa = (Integer) sesion.getAttribute(ETAPA);
		if (etapa == null) {
			etapa = 1;
			sesion.setAttribute(ETAPA, etapa);
		}
		return etapa;
	}

	@SuppressWarnings("unchecked")
	private String pintarEtapa(int etapa, HttpSession sesion, Model modelo) {
		String nombre = (String) sesion.getAttribute(NOMBRE);
		Integer signo = (Integer) sesion.getAttribute(SIGNO);
		List<String> aficiones = (List<String>) sesion.getAttribute(AFICIONES);

		modelo.addAttribute("base", BASE);
		modelo.addAttribute("version", "Versión 1 - HttpSession");
		modelo.addAttribute("numEtapa", etapa);
		modelo.addAttribute("signos", Quizz.SIGNOS);
		modelo.addAttribute("listaAficiones", Quizz.AFICIONES);

		// valores para que el formulario salga relleno si ya se habia contestado
		modelo.addAttribute("nombre", nombre);
		modelo.addAttribute("signo", signo);
		modelo.addAttribute("seleccionadas", aficiones == null ? Collections.emptyList() : aficiones);

		if (etapa == Quizz.ETAPA_RESULTADO) {
			modelo.addAttribute("respuestas", Quizz.respuestas(nombre, signo, aficiones));
		}

		// esto es solo para ver en pantalla lo que hay guardado (depuracion)
		Map<String, Object> guardado = new LinkedHashMap<>();
		guardado.put("JSESSIONID", sesion.getId());
		sesion.getAttributeNames().asIterator().forEachRemaining(n -> guardado.put(n, sesion.getAttribute(n)));
		modelo.addAttribute("almacen", "Atributos de la HttpSession");
		modelo.addAttribute("guardado", guardado);

		return "quizz/etapa" + etapa;
	}
}
