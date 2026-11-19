package goya.daw2.pruebas_plantillas;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import jakarta.servlet.http.HttpSession;

/**
 * VERSION 2: la otra forma de manejar sesiones en Spring, @SessionAttributes.
 *
 * No toco la HttpSession directamente. Le digo a Spring que el atributo del
 * modelo "quizz" tiene que vivir en la sesion y el se encarga de guardarlo al
 * terminar cada peticion y de volver a meterlo en el modelo en la siguiente.
 */
@Controller
@RequestMapping("/atributos")
@SessionAttributes("quizz")
public class QuizzSessionAttributesController {

	private static final String BASE = "/atributos";

	/**
	 * Solo se ejecuta cuando "quizz" todavia no esta en la sesion (primera
	 * peticion o despues de reiniciar). Si ya esta, Spring usa el de la sesion.
	 */
	@ModelAttribute("quizz")
	public RespuestasQuizz nuevoQuizz() {
		return new RespuestasQuizz();
	}

	@GetMapping
	public String mostrarEtapa(@ModelAttribute(name = "quizz", binding = false) RespuestasQuizz quizz,
			Model modelo, HttpSession sesion) {
		return pintarEtapa(quizz, modelo, sesion);
	}

	/*
	 * binding = false es importante: si no, Spring copiaria los parametros del
	 * formulario directamente al objeto de la sesion (incluido numEtapa si alguien
	 * lo manda a mano) y se podria saltar etapas o guardar datos sin validar.
	 */
	@PostMapping
	public String procesarEtapa(@ModelAttribute(name = "quizz", binding = false) RespuestasQuizz quizz,
			@RequestParam(name = "nombre", required = false) String nombre,
			@RequestParam(name = "signo", required = false) String signo,
			@RequestParam(name = "aficiones", required = false) List<String> aficiones,
			Model modelo, HttpSession sesion) {

		String error = null;

		switch (quizz.getNumEtapa()) {
		case 1 -> {
			error = Quizz.validarNombre(nombre);
			if (error == null) {
				quizz.setNombre(nombre.trim());
			}
		}
		case 2 -> {
			error = Quizz.validarSigno(signo);
			if (error == null) {
				quizz.setSigno(Integer.parseInt(signo));
			}
		}
		case 3 -> {
			error = Quizz.validarAficiones(aficiones);
			if (error == null) {
				quizz.setAficiones(aficiones);
			}
		}
		default -> {
		}
		}

		if (error != null) {
			modelo.addAttribute("errores", error);
			return pintarEtapa(quizz, modelo, sesion);
		}

		quizz.siguienteEtapa();
		return "redirect:" + BASE;
	}

	@GetMapping("/reiniciar")
	public String reiniciar(SessionStatus estado) {
		// borra de la sesion solo los atributos de @SessionAttributes ("quizz")
		estado.setComplete();
		return "redirect:" + BASE;
	}

	private String pintarEtapa(RespuestasQuizz quizz, Model modelo, HttpSession sesion) {
		int etapa = quizz.getNumEtapa();

		modelo.addAttribute("base", BASE);
		modelo.addAttribute("version", "Versión 2 - @SessionAttributes");
		modelo.addAttribute("numEtapa", etapa);
		modelo.addAttribute("signos", Quizz.SIGNOS);
		modelo.addAttribute("listaAficiones", Quizz.AFICIONES);
		modelo.addAttribute("nombre", quizz.getNombre());
		modelo.addAttribute("signo", quizz.getSigno());
		modelo.addAttribute("seleccionadas", quizz.getAficiones());

		if (etapa == Quizz.ETAPA_RESULTADO) {
			modelo.addAttribute("respuestas",
					Quizz.respuestas(quizz.getNombre(), quizz.getSigno(), quizz.getAficiones()));
		}

		Map<String, Object> guardado = new LinkedHashMap<>();
		guardado.put("JSESSIONID", sesion.getId());
		guardado.put("quizz", quizz);
		modelo.addAttribute("almacen", "Objeto \"quizz\" guardado con @SessionAttributes");
		modelo.addAttribute("guardado", guardado);

		return "quizz/etapa" + etapa;
	}
}
