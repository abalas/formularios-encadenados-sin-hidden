# D1-UD3 - Formularios encadenados sin campos hidden

Quizz de la UD2 modificado para no usar ningún `input type="hidden"`. Hay tres versiones:

| Ruta         | Versión                         | Clase                                  |
|--------------|---------------------------------|----------------------------------------|
| `/sesion`    | HttpSession                     | `QuizzSesionController`                |
| `/atributos` | `@SessionAttributes`            | `QuizzSessionAttributesController`     |
| `/cookies`   | Cookies (sin sesión)            | `QuizzCookiesController`               |

## Ejecutar

```
./gradlew bootRun
```

y abrir http://localhost:8081 (el puerto está cambiado en `application.properties`).

