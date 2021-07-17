package com.nobelti.cl.model;

import java.util.List;

public class RequestGenerarIdOperacion {
	private String nombreProyecto;
	private List<String> nombrePipeline;
	
	public String getNombreProyecto() {
		return nombreProyecto;
	}
	public void setNombreProyecto(String nombreProyecto) {
		this.nombreProyecto = nombreProyecto;
	}
	public List<String> getNombrePipeline() {
		return nombrePipeline;
	}
	public void setNombrePipeline(List<String> nombrePipeline) {
		this.nombrePipeline = nombrePipeline;
	}

}
