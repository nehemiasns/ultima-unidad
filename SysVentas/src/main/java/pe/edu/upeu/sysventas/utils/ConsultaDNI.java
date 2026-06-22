package pe.edu.upeu.sysventas.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import pe.edu.upeu.sysventas.dto.PersonaDto;

import java.io.IOException;

public class ConsultaDNI {

    public PersonaDto consultarDNI(String dni){
        String url = "https://api.apis.net.pe/v1/dni?numero=" + dni;
        try {
            Connection.Response getResponse = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
            
            if (getResponse.statusCode() == 200) {
                String body = getResponse.body();
                String nombres = extractJsonValue(body, "nombres");
                String apellidoP = extractJsonValue(body, "apellidoPaterno");
                String apellidoM = extractJsonValue(body, "apellidoMaterno");
                String numeroDNI = extractJsonValue(body, "numeroDocumento");
                
                if (numeroDNI != null && !numeroDNI.isEmpty()) {
                    PersonaDto personaDto = new PersonaDto();
                    personaDto.setDni(numeroDNI);
                    personaDto.setNombre(nombres);
                    personaDto.setApellidoPaterno(apellidoP);
                    personaDto.setApellidoMaterno(apellidoM);
                    return personaDto;
                }
            }
        }catch (Exception e) {
            System.err.println("Error al consultar DNI: " + e.getMessage());
        }
        return null;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
