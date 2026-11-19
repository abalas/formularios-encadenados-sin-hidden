package goya.daw2.pruebas_plantillas;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * VERSION 3: sin sesion, cada respuesta se guarda en una cookie del navegador.
 *
 * Diferencias importantes con las versiones anteriores:
 *  - Los datos estan en el cliente, asi que el usuario los puede ver y cambiar
 *    (F12 > Application > Cookies). Por eso todo se vuelve a validar al leerlo.
 *  - No guardo la etapa en una cookie: la calculo segun que respuestas validas
 *    hay. Si guardase "etapa=4" cualquiera podria saltarse el quizz.
 *  - Los valores de las cookies no admiten espacios, comas, tildes... asi que
 *    los codifico con URLEncoder.
 */
@Controller
@RequestMapping("/cookies")
public class QuizzCookiesController {

	private static final String BASE = "/cookies";

	private static final String C_NOMBRE = "quizzNombre";
	private static final String C_SIGNO = "quizzSigno";
	private static final String C_AFICIONES = "quizzAficiones";

	private static final int DURACION_SEGUNDOS = 30 * 60; // media hora, como la sesion por defecto
	private static final String SEPARADOR = "|";

	@GetMapping
	public String mostrarEtapa(@CookieValue(name = C_NOMBRE, required = false) String cNombre,
			@CookieValue(name = C_SIGNO, required = false) String cSigno,
			@CookieValue(name = C_AFICIONES, required = false) String cAficiones,
			Model modelo) {

		String nombre = decodificar(cNombre);
		String signo = decodificar(cSigno);
		List<String> aficiones = aLista(decodificar(cAficiones));

		return pintarEtapa(nombre, signo, aficiones, modelo);
	}

	@PostMapping
	public String procesarEtapa(@CookieValue(name = C_NOMBRE, required = false) String cNombre,
			@CookieValue(name = C_SIGNO, required = false) String cSigno,
			@CookieValue(name = C_AFICIONES, required = false) String cAficiones,
			@RequestParam(name = "nombre", required = false) String nombre,
			@RequestParam(name = "signo", required = false) String signo,
			@RequestParam(name = "aficiones", required = false) List<String> aficiones,
			HttpServletResponse respuesta, Model modelo) {

		String nombreGuardado = decodificar(cNombre);
		String signoGuardado = decodificar(cSigno);
		List<String> aficionesGuardadas = aLista(decodificar(cAficiones));

		int etapa = calcularEtapa(nombreGuardado, signoGuardado, aficionesGuardadas);
		String error = null;

		switch (etapa) {
		case 1 -> {
			error = Quizz.validarNombre(nombre);
			if (error == null) {
				respuesta.addCookie(crearCookie(C_NOMBRE, nombre.trim()));
			}
		}
		case 2 -> {
			error = Quizz.validarSigno(signo);
			if (error == null) {
				respuesta.addCookie(crearCookie(C_SIGNO, signo));
			}
		}
		case 3 -> {
			error = Quizz.validarAficiones(aficiones);
			if (error == null) {
				respuesta.addCookie(crearCookie(C_AFICIONES, String.join(SEPARADOR, aficiones)));
			}
		}
		default -> {
		}
		}

		if (error != null) {
			modelo.addAttribute("errores", error);
			return pintarEtapa(nombreGuardado, signoGuardado, aficionesGuardadas, modelo);
		}
		return "redirect:" + BASE;
	}

	@GetMapping("/reiniciar")
	public String reiniciar(HttpServletResponse respuesta) {
		// para borrar una cookie se manda otra igual (mismo nombre y path) con maxAge 0
		for (String nombre : List.of(C_NOMBRE, C_SIGNO, C_AFICIONES)) {
			Cookie cookie = crearCookie(nombre, "");
			cookie.setMaxAge(0);
			respuesta.addCookie(cookie);
		}
		return "redirect:" + BASE;
	}

	/** La etapa es la primera pregunta que no tiene una respuesta valida guardada. */
	private int calcularEtapa(String nombre, String signo, List<String> aficiones) {
		if (Quizz.validarNombre(nombre) != null) {
			return 1;
		}
		if (Quizz.validarSigno(signo) != null) {
			return 2;
		}
		if (Quizz.validarAficiones(aficiones) != null) {
			return 3;
		}
		return Quizz.ETAPA_RESULTADO;
	}

	private String pintarEtapa(String nombre, String signo, List<String> aficiones, Model modelo) {
		int etapa = calcularEtapa(nombre, signo, aficiones);

		modelo.addAttribute("base", BASE);
		modelo.addAttribute("version", "Versión 3 - Cookies");
		modelo.addAttribute("numEtapa", etapa);
		modelo.addAttribute("signos", Quizz.SIGNOS);
		modelo.addAttribute("listaAficiones", Quizz.AFICIONES);
		modelo.addAttribute("nombre", nombre);
		modelo.addAttribute("signo", Quizz.validarSigno(signo) == null ? Integer.valueOf(signo) : null);
		modelo.addAttribute("seleccionadas", aficiones);

		if (etapa == Quizz.ETAPA_RESULTADO) {
			modelo.addAttribute("respuestas", Quizz.respuestas(nombre, Integer.parseInt(signo), aficiones));
		}

		Map<String, Object> guardado = new LinkedHashMap<>();
		guardado.put(C_NOMBRE, nombre);
		guardado.put(C_SIGNO, signo);
		guardado.put(C_AFICIONES, aficiones.isEmpty() ? null : aficiones);
		modelo.addAttribute("almacen", "Cookies recibidas del navegador (ya decodificadas)");
		modelo.addAttribute("guardado", guardado);

		return "quizz/etapa" + etapa;
	}

	private Cookie crearCookie(String nombre, String valor) {
		Cookie cookie = new Cookie(nombre, URLEncoder.encode(valor, StandardCharsets.UTF_8));
		cookie.setPath(BASE); // solo se envian a /cookies/...
		cookie.setHttpOnly(true); // JavaScript no puede leerlas
		cookie.setMaxAge(DURACION_SEGUNDOS);
		return cookie;
	}

	private String decodificar(String valor) {
		if (valor == null) {
			return null;
		}
		try {
			return URLDecoder.decode(valor, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			// cookie manipulada con un % mal puesto
			return null;
		}
	}

	private List<String> aLista(String valor) {
		if (valor == null || valor.isBlank()) {
			return Collections.emptyList();
		}
		return Arrays.asList(valor.split("\\" + SEPARADOR));
	}
}
