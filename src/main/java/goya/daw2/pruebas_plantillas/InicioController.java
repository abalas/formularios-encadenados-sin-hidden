package goya.daw2.pruebas_plantillas;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Pagina de inicio con un enlace a cada una de las tres versiones. */
@Controller
public class InicioController {

	@GetMapping("/")
	public String inicio() {
		return "index";
	}
}
