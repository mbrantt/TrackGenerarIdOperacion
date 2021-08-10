package com.nobelti.cl.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.nobelti.cl.model.CommonResponse;
import com.nobelti.cl.model.Operacion;
import com.nobelti.cl.model.OperacionObjectResponse;
import com.nobelti.cl.model.OperacionResponse;
import com.nobelti.cl.model.Proyecto;
import com.nobelti.cl.model.RequestGenerarIdOperacion;
import com.nobelti.cl.model.RequestGenerarIdProyecto;
import com.nobelti.cl.model.RequestValidaIdProyecto;
import com.nobelti.cl.model.ResponseGenerarIdOperacion;
import com.nobelti.cl.model.ValidaProyectoResponse;

@RestController
@RequestMapping("/api/track")
public class GenerarIdOperacion {

	/* Spring Security (Cors) */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "PUT", "POST", "PATCH", "DELETE",
						"OPTIONS");
			}
		};
	}

	@Value("#{'${property.ambientes}'.split('-')}")
	List<List<String>> listOfAmbientes;
	private int idProyecto;
	private int idOperacion;
	private String nombreProyectoTemp;
	private static final String SEPARADOR = "-----------------------";
	Logger logger = LoggerFactory.getLogger(GenerarIdOperacion.class);

	@GetMapping("/project/valida")
	public Boolean validaIntegridadIdProyecto() {
		List<Integer> listOfIdProyectos = new ArrayList<>();
		logger.info(SEPARADOR);
		logger.info("- Ambientes que valida ID Proyecto -");
		logger.info(SEPARADOR);
		for (List<String> ambiente : listOfAmbientes) {

			String nombreAmbiente = ambiente.get(0);
			String protocoloAmbiente = ambiente.get(1);
			String hostAmbiente = ambiente.get(2);

			logger.info("Nombre Ambiente => " + nombreAmbiente);
			logger.info("Protocolo => " + protocoloAmbiente);
			logger.info("Host => " + hostAmbiente);

			// Se arma cuerpo del request (no se envia nada para esta API)
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
					.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);

			// Armado de URI
			UriComponents uri = UriComponentsBuilder.newInstance().scheme(protocoloAmbiente).host(hostAmbiente)
					.path("/tracksystemapi/dal/track/project/last").build();
			logger.info("URL api => " + uri.toUriString());
			logger.info("****");
			// Consumo de API
			ResponseEntity<Proyecto> response = new RestTemplate(requestFactory).exchange(uri.toUriString(),
					HttpMethod.GET, entity, Proyecto.class);
			listOfIdProyectos.add(response.getBody().getIdProject());
		}
		// listOfIdProyectos.stream().forEach(System.out::println);
		if (listOfIdProyectos.stream().distinct().collect(Collectors.toList()).size() == 1) {
			idProyecto = listOfIdProyectos.get(0);
			// logger.info("ID proyecto: " + idProyecto);
			return true;
		} else {
			logger.error("Ultimos Id de proyectos no coinciden, porfavor regularizar ambientes.");
			return false;
		}

	}

	@GetMapping("/project/validate")
	@ResponseBody
	public ValidaProyectoResponse ValidaProyecto(@RequestParam(name = "ID_PROJECT") Integer idProyecto) {

		ValidaProyectoResponse respuestaValidaProyecto = new ValidaProyectoResponse();
		List<Integer> listOfIdProyecto = new ArrayList<>();

		for (List<String> ambiente : listOfAmbientes) {
			String nombreAmbiente = ambiente.get(0);
			String protocoloAmbiente = ambiente.get(1);
			String hostAmbiente = ambiente.get(2);

			// Se arma cuerpo del request (no se envia nada para esta API)
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
					.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);

			// Armado de URI
			UriComponents uri = UriComponentsBuilder.newInstance().scheme(protocoloAmbiente).host(hostAmbiente)
					.path("/tracksystemapi/dal/track/project").queryParam("id_proyecto", idProyecto).build();
			logger.info("URL api => " + uri.toUriString());
			logger.info("****");

			// Consumo de API
			ResponseEntity<Proyecto> response = new RestTemplate(requestFactory)
					.exchange(uri.toUriString(), HttpMethod.GET, entity, Proyecto.class);
			listOfIdProyecto.add(response.getBody().getIdProject());
			respuestaValidaProyecto.setNombreProyecto(response.getBody().getProjectName());
			respuestaValidaProyecto.setCompanyOwner(response.getBody().getCompanyOwner());
			respuestaValidaProyecto.setCreateDate(response.getBody().getCreatedDate()); 

		}

		 if (listOfIdProyecto.stream().distinct().collect(Collectors.toList()).size() == 1)  {

		// Valida que proyecto sea diferente a -1 y que no venga null
			if (listOfIdProyecto.get(0) != -1 && listOfIdProyecto.get(0) != null) {
			// idOperacion = listOfIdProyecto.get(0)
			// logger.info("ID proyecto: " + idProyecto);
			respuestaValidaProyecto.setValidate(true);
			
			}
			else{
				logger.error("Id Proyecto no encontrado.");
				respuestaValidaProyecto.setMensaje("Id Proyecto no encontrado.");
				respuestaValidaProyecto.setValidate(false);
				
				
			}

		} else {
			logger.error("El Id proyecto "+idProyecto+" no ha sido creado en todos los ambientes, porfavor regularizar.");
			respuestaValidaProyecto.setMensaje("El Id proyecto "+idProyecto+" no ha sido creado en los ambientes trabajados, porfavor regularizar.");
			respuestaValidaProyecto.setValidate(false);
			
		}
		return respuestaValidaProyecto;
	}

	public Boolean validaUltimosIdOperacion() {

		List<Integer> listOfIdOperacion = new ArrayList<>();
		logger.info(SEPARADOR);
		logger.info("- Ambientes que valida ID Operacion -");
		logger.info(SEPARADOR);
		for (List<String> ambiente : listOfAmbientes) {

			String nombreAmbiente = ambiente.get(0);
			String protocoloAmbiente = ambiente.get(1);
			String hostAmbiente = ambiente.get(2);

			logger.info("Nombre Ambiente => " + nombreAmbiente);
			logger.info("Protocolo => " + protocoloAmbiente);
			logger.info("Host => " + hostAmbiente);

			// Se arma cuerpo del request (no se envia nada para esta API)
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
					.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);

			// Armado de URI
			UriComponents uri = UriComponentsBuilder.newInstance().scheme(protocoloAmbiente).host(hostAmbiente)
					.path("/tracksystemapi/dal/track/operation/last").build();
			logger.info("URL api => " + uri.toUriString());
			logger.info("****");
			// Consumo de API
			ResponseEntity<OperacionObjectResponse> response = new RestTemplate(requestFactory)
					.exchange(uri.toUriString(), HttpMethod.GET, entity, OperacionObjectResponse.class);
			listOfIdOperacion.add(response.getBody().getIdProject());
		}

		if (listOfIdOperacion.stream().distinct().collect(Collectors.toList()).size() == 1) {
			idOperacion = listOfIdOperacion.get(0);
			// logger.info("ID proyecto: " + idProyecto);
			return true;
		} else {
			logger.error("Ultimos Id de operación no coinciden, porfavor regularizar ambientes.");
			return false;
		}
	}

	// @PostMapping("/generarNuevo")
	/*
	 * public @ResponseBody() ResponseGenerarIdOperacion
	 * nuevoProyectoConIdOperacion(
	 * 
	 * @RequestBody() RequestGenerarIdOperacion request) {
	 * ResponseGenerarIdOperacion response = new ResponseGenerarIdOperacion(); if
	 * (validaIntegridadIdProyecto()) { if (validaUltimosIdOperacion()) {
	 * 
	 * for (List<String> ambiente : listOfAmbientes) {
	 * 
	 * // String nombreAmbiente = ambiente.get(0); String protocoloAmbiente =
	 * ambiente.get(1); String hostAmbiente = ambiente.get(2); int countIdProyecto =
	 * idProyecto + 1; logger.info("IdProyecto: " + idProyecto);
	 * logger.info("count IdProyecto: " + countIdProyecto); // PRIMERO se genera el
	 * IdProyecto // Se arma cuerpo del request (no se envia nada para esta API)
	 * HttpHeaders headersProyecto = new HttpHeaders(); HttpEntity<String>
	 * entityProyecto = new HttpEntity<String>("parameters", headersProyecto);
	 * CloseableHttpClient httpClientProyecto = HttpClients.custom()
	 * .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
	 * HttpComponentsClientHttpRequestFactory requestFactoryProyecto = new
	 * HttpComponentsClientHttpRequestFactory();
	 * requestFactoryProyecto.setHttpClient(httpClientProyecto);
	 * 
	 * // Armado de URI UriComponents uriProyecto =
	 * UriComponentsBuilder.newInstance().scheme(protocoloAmbiente)
	 * .host(hostAmbiente).path("/tracksystemapi/dal/track/project/create")
	 * .queryParam("id_proyecto", countIdProyecto) .queryParam("nombre_proyecto",
	 * request.getNombreProyecto()).build(); //
	 * logger.info(uriProyecto.toUriString()); // Consumo API proyecto
	 * ResponseEntity<CommonResponse> responseProyecto = new
	 * RestTemplate(requestFactoryProyecto) .exchange(uriProyecto.toUriString(),
	 * HttpMethod.POST, entityProyecto, CommonResponse.class); if
	 * (responseProyecto.getBody().getCodigo() != 0) { response.setCodigo(99);
	 * response.setMensaje("Error al insertar proyecto: " +
	 * responseProyecto.getBody().getMensaje()); break; }
	 * response.setIdProyecto(countIdProyecto);
	 * response.setNombreProyecto(request.getNombreProyecto());
	 * 
	 * // SEGUNDO se generan las operaciones int countIdOperacion = idOperacion + 1;
	 * List<Operacion> listOperaciones = new ArrayList<>(); for (String elementos :
	 * request.getNombrePipeline()) { countIdOperacion = countIdOperacion + 1; // Se
	 * arma cuerpo del request (no se envia nada para esta API) HttpHeaders
	 * headersOperacion = new HttpHeaders(); HttpEntity<String> entityOperacion =
	 * new HttpEntity<String>("parameters", headersOperacion); CloseableHttpClient
	 * httpClientOperacion = HttpClients.custom() .setSSLHostnameVerifier(new
	 * NoopHostnameVerifier()).build(); HttpComponentsClientHttpRequestFactory
	 * requestFactoryOperacion = new HttpComponentsClientHttpRequestFactory();
	 * requestFactoryOperacion.setHttpClient(httpClientOperacion);
	 * 
	 * // Armado de URI UriComponents uriOperacion =
	 * UriComponentsBuilder.newInstance().scheme(protocoloAmbiente)
	 * .host(hostAmbiente).path("/tracksystemapi/dal/track/operation/create")
	 * .queryParam("id_operacion", countIdOperacion).queryParam("id_proyecto",
	 * countIdProyecto) .queryParam("nombre_operacion", elementos).build(); //
	 * Consumo API operacion ResponseEntity<OperacionResponse> responseOperacion =
	 * new RestTemplate(requestFactoryOperacion)
	 * .exchange(uriOperacion.toUriString(), HttpMethod.POST, entityOperacion,
	 * OperacionResponse.class); if (responseOperacion.getBody().getCodigo() != 0) {
	 * response.setCodigo(99);
	 * response.setMensaje("Error al generar id operacion para '" + elementos +
	 * "'. Detalle: " + responseOperacion.getBody().getMensaje()); break; }
	 * Operacion operacionTemporal = new Operacion();
	 * operacionTemporal.setIdOperacion(responseOperacion.getBody().getIdOperacion()
	 * ); operacionTemporal.setNombreOperacion(responseOperacion.getBody().
	 * getNombreOperacion()); listOperaciones.add(operacionTemporal); }
	 * response.setOperaciones(listOperaciones);
	 * 
	 * } } else { response.setCodigo(99); response.setMensaje(
	 * "Error validación: Ultimos Id de operación no coinciden, porfavor regularizar ambientes."
	 * ); } } else { response.setCodigo(99); response.setMensaje(
	 * "Error validación: Ultimos Id de proyectos no coinciden, porfavor regularizar ambientes."
	 * ); } return response; }
	 */
	@PostMapping("/project/create")
	public @ResponseBody() ResponseGenerarIdOperacion crearProyecto(@RequestBody() RequestGenerarIdProyecto request) {
		ResponseGenerarIdOperacion response = new ResponseGenerarIdOperacion();
		if (validaIntegridadIdProyecto()) {

			for (List<String> ambiente : listOfAmbientes) {

				// String nombreAmbiente = ambiente.get(0);
				String protocoloAmbiente = ambiente.get(1);
				String hostAmbiente = ambiente.get(2);
				int countIdProyecto = idProyecto + 1;
				logger.info("IdProyecto: " + idProyecto);
				logger.info("count IdProyecto: " + countIdProyecto);
				// PRIMERO se genera el IdProyecto
				// Se arma cuerpo del request (no se envia nada para esta API)
				HttpHeaders headersProyecto = new HttpHeaders();
				HttpEntity<String> entityProyecto = new HttpEntity<String>("parameters", headersProyecto);
				CloseableHttpClient httpClientProyecto = HttpClients.custom()
						.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
				HttpComponentsClientHttpRequestFactory requestFactoryProyecto = new HttpComponentsClientHttpRequestFactory();
				requestFactoryProyecto.setHttpClient(httpClientProyecto);

				// Armado de URI
				UriComponents uriProyecto = UriComponentsBuilder.newInstance().scheme(protocoloAmbiente)
						.host(hostAmbiente).path("/tracksystemapi/dal/track/project/create")
						.queryParam("id_proyecto", countIdProyecto)
						.queryParam("nombre_proyecto", request.getNombreProyecto()).build();
				// logger.info(uriProyecto.toUriString());
				// Consumo API proyecto
				ResponseEntity<CommonResponse> responseProyecto = new RestTemplate(requestFactoryProyecto)
						.exchange(uriProyecto.toUriString(), HttpMethod.POST, entityProyecto, CommonResponse.class);
				if (responseProyecto.getBody().getCodigo() != 0) {
					response.setCodigo(99);
					response.setMensaje("Error al insertar proyecto: " + responseProyecto.getBody().getMensaje());
					break;
				}
				response.setIdProyecto(countIdProyecto);
				response.setNombreProyecto(request.getNombreProyecto());

			}
		} else {
			response.setCodigo(99);
			response.setMensaje(
					"Error validación: Ultimos Id de proyectos no coinciden, porfavor regularizar ambientes.");
		}
		return response;
	}

	@PostMapping("/operation/create")
	public @ResponseBody() ResponseGenerarIdOperacion crearOperacion(
			@RequestParam(name = "idProyecto") Integer idProyecto,
			@RequestParam(name = "nombreOperacion") String nombrePipeline) {
		ResponseGenerarIdOperacion response = new ResponseGenerarIdOperacion();
			ValidaProyectoResponse validaProyecto = ValidaProyecto(idProyecto);
		if (validaProyecto.isValidate()) {

			if (validaUltimosIdOperacion()) {
				// SEGUNDO se generan las operaciones

				Operacion listOperaciones = new Operacion();
				for (List<String> ambiente : listOfAmbientes) {
					// for (String elementos : request.getNombrePipeline()) {

					String protocoloAmbiente = ambiente.get(1);
					String hostAmbiente = ambiente.get(2);
					/*
					 * int countIdOperacion = idOperacion + 1; countIdOperacion = countIdOperacion +
					 * 1; logger.info("count IdOperacion: " + countIdOperacion);
					 * logger.info("count IdOperacion2: " + idOperacion);
					 */

					// Se arma cuerpo del request (no se envia nada para esta API)
					HttpHeaders headersOperacion = new HttpHeaders();
					HttpEntity<String> entityOperacion = new HttpEntity<String>("parameters", headersOperacion);
					CloseableHttpClient httpClientOperacion = HttpClients.custom()
							.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
					HttpComponentsClientHttpRequestFactory requestFactoryOperacion = new HttpComponentsClientHttpRequestFactory();
					requestFactoryOperacion.setHttpClient(httpClientOperacion);

					/* Obtener ultimo id proyecto */
					// Se arma cuerpo del request (no se envia nada para esta API)
					HttpHeaders headers = new HttpHeaders();
					HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
					CloseableHttpClient httpClient = HttpClients.custom()
							.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
					HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
					requestFactory.setHttpClient(httpClient);

					// Armado de URI
					UriComponents uri = UriComponentsBuilder.newInstance().scheme(protocoloAmbiente).host(hostAmbiente)
							.path("/tracksystemapi/dal/track/operation/last").build();
					logger.info("URL api => " + uri.toUriString());
					logger.info("****");

					// Consumo de API
					ResponseEntity<Proyecto> responseDOs = new RestTemplate(requestFactory).exchange(uri.toUriString(),
							HttpMethod.GET, entity, Proyecto.class);
					// listOfIdProyectos.add(response.getBody().getIdProject());
					int countIdOperacion = responseDOs.getBody().getIdOperation()+1;
					logger.info("count IdOperacion DAL +1 = " + countIdOperacion);

					/* Creacion nombre operacion segun idProyecto y co */
					// Armado de URI
					UriComponents uriOperacion = UriComponentsBuilder.newInstance().scheme(protocoloAmbiente)
							.host(hostAmbiente).path("/tracksystemapi/dal/track/operation/create")
							.queryParam("id_proyecto", idProyecto)
							.queryParam("id_operacion", countIdOperacion)
							.queryParam("nombre_operacion", nombrePipeline).build();

					// Consumo API operacion
					ResponseEntity<OperacionResponse> responseOperacion = new RestTemplate(requestFactoryOperacion)
							.exchange(uriOperacion.toUriString(), HttpMethod.POST, entityOperacion,
									OperacionResponse.class);
					/* if (responseOperacion.getBody().getCodigo() != 0) {
						response.setCodigo(99);
						response.setMensaje("Error al generar id operacion para '" + idProyecto + "'. Detalle: "
								+ responseOperacion.getBody().getMensaje());
						break;
					} */

					

					Operacion operacionTemporal = new Operacion();
					operacionTemporal.setIdOperacion(countIdOperacion);
					operacionTemporal.setNombreOperacion(responseOperacion.getBody().getNombreOperacion());
					// listOperaciones.add(operacionTemporal);
					// }
					response.setIdProyecto(idProyecto);
					response.setNombreProyecto(validaProyecto.getNombreProyecto());
					response.setIdOperacion(countIdOperacion);
					response.setMensaje("OK");
					response.setNombreOperacion(nombrePipeline);
					response.setCodigo(0);
				}
			} else {
				response.setCodigo(99);
				response.setMensaje("Error validación: Ultimos Id de operación no coinciden, porfavor regularizar ambientes.");
			}
		} else {
			response.setCodigo(99);
			response.setMensaje(validaProyecto.getMensaje());
		}
		return response;
	}

}
