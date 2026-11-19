package goya.daw2.pruebas_plantillas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class QuizzTests {

	@Autowired
	MockMvc mvc;

	// ---------- version 1 y 2 (las dos usan sesion) ----------

	private void quizzCompletoConSesion(String base) throws Exception {
		MockHttpSession sesion = new MockHttpSession();

		mvc.perform(get(base).session(sesion)).andExpect(view().name("quizz/etapa1"))
				.andExpect(content().string(not(containsString("type=\"hidden\""))));

		// nombre demasiado corto -> se queda en la etapa 1 con error
		mvc.perform(post(base).session(sesion).param("nombre", "Al")).andExpect(view().name("quizz/etapa1"))
				.andExpect(model().attribute("errores", "La longitud del nombre debe estar entre 3 y 10"));

		mvc.perform(post(base).session(sesion).param("nombre", "Andrei")).andExpect(redirectedUrl(base));
		mvc.perform(get(base).session(sesion)).andExpect(view().name("quizz/etapa2"));

		mvc.perform(post(base).session(sesion).param("signo", "0")).andExpect(view().name("quizz/etapa2"));
		mvc.perform(post(base).session(sesion).param("signo", "5")).andExpect(redirectedUrl(base));

		mvc.perform(post(base).session(sesion)).andExpect(view().name("quizz/etapa3"));
		mvc.perform(post(base).session(sesion).param("aficiones", "Deportes", "Relaciones sociales"))
				.andExpect(redirectedUrl(base));

		mvc.perform(get(base).session(sesion)).andExpect(view().name("quizz/etapa4"))
				.andExpect(content().string(containsString("Andrei")))
				.andExpect(content().string(containsString("Leo")))
				.andExpect(content().string(containsString("Deportes, Relaciones sociales")));

		mvc.perform(get(base + "/reiniciar").session(sesion)).andExpect(redirectedUrl(base));
	}

	@Test
	void versionHttpSession() throws Exception {
		quizzCompletoConSesion("/sesion");
	}

	@Test
	void versionSessionAttributes() throws Exception {
		quizzCompletoConSesion("/atributos");
	}

	@Test
	void noSePuedeSaltarEtapasMandandoNumEtapa() throws Exception {
		MockHttpSession sesion = new MockHttpSession();
		mvc.perform(post("/atributos").session(sesion).param("numEtapa", "4"))
				.andExpect(view().name("quizz/etapa1"));
		mvc.perform(post("/sesion").session(sesion).param("numEtapa", "4"))
				.andExpect(view().name("quizz/etapa1"));
	}

	// ---------- version 3 (cookies) ----------

	@Test
	void versionCookies() throws Exception {
		mvc.perform(get("/cookies")).andExpect(view().name("quizz/etapa1"));

		mvc.perform(post("/cookies").param("nombre", "José Mª"))
				.andExpect(redirectedUrl("/cookies"))
				.andExpect(cookie().value("quizzNombre", "Jos%C3%A9+M%C2%AA"))
				.andExpect(cookie().path("quizzNombre", "/cookies"))
				.andExpect(cookie().httpOnly("quizzNombre", true));

		Cookie nombre = new Cookie("quizzNombre", "Jos%C3%A9+M%C2%AA");
		Cookie signo = new Cookie("quizzSigno", "12");
		Cookie aficiones = new Cookie("quizzAficiones", "Lectura%7CRelaciones+sociales");

		mvc.perform(get("/cookies").cookie(nombre)).andExpect(view().name("quizz/etapa2"));
		mvc.perform(get("/cookies").cookie(nombre, signo)).andExpect(view().name("quizz/etapa3"));
		mvc.perform(get("/cookies").cookie(nombre, signo, aficiones)).andExpect(view().name("quizz/etapa4"))
				.andExpect(content().string(containsString("Piscis")))
				.andExpect(content().string(containsString("Lectura, Relaciones sociales")));

		mvc.perform(get("/cookies/reiniciar")).andExpect(cookie().maxAge("quizzNombre", 0));
	}

	@Test
	void cookieManipuladaNoSeAcepta() throws Exception {
		// signo 99 no existe: el servidor no se lo cree y vuelve a pedir el signo
		mvc.perform(get("/cookies").cookie(new Cookie("quizzNombre", "Andrei"), new Cookie("quizzSigno", "99")))
				.andExpect(view().name("quizz/etapa2"));
	}
}
